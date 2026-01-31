package com.dhy.shipmanagebackend.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.lang.UUID;
import com.dhy.shipmanagebackend.annotation.OperLog;
import com.dhy.shipmanagebackend.entity.Result;
import com.dhy.shipmanagebackend.entity.User;
import com.dhy.shipmanagebackend.entity.UserVO;
import com.dhy.shipmanagebackend.service.UserService;
import com.dhy.shipmanagebackend.utils.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Email;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Validated
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private TurnstileUtil turnstileUtil;
    @Autowired
    private com.dhy.shipmanagebackend.mapper.UserMapper userMapper;

    @PostMapping("/send-code")
    public Result sendCode(@RequestParam @Email String email) {
        // 这里可以先简单校验一下邮箱是否已被注册

        userService.sendCode(email);
        return Result.success("验证码已发送，请注意查收");
    }

    @PostMapping("/register")
    // 参数中删除了 captcha, captchaUuid，增加了 turnstileToken
    public Result register(String username, String password, String email, String code) {

        User u = userService.findByUsername(username);
        if (u == null) {
            userService.register(username, password, email, code);
            return Result.success();
        } else {
            return Result.error("用户名已被占用");
        }
    }

    @PostMapping("/login")
    // 参数中删除了 captcha, captchaUuid，增加了 turnstileToken
    public Result<String> login(String username, String password, String turnstileToken) {

        // 1. 校验 Cloudflare 人机验证
        if (!turnstileUtil.verify(turnstileToken)) {
            return Result.error("人机验证失败，请刷新重试");
        }

        // 2. 根据用户名查询用户
        User loginUser = userService.findByUsername(username);

        if (loginUser == null) {
            return Result.error("用户名错误");
        }

        // 3. 校验密码
        if (BcryptUtil.match(password, loginUser.getPasswordHash())) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", loginUser.getId());
            claims.put("username", loginUser.getUsername());
            claims.put("role", loginUser.getRole());

            String token = JwtUtil.genToken(claims);
            ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
            ops.set(token, token, 12, TimeUnit.HOURS);
            return Result.success(token);
        }

        return Result.error("密码错误");
    }

    @PostMapping("/loginByEmail")
    public Result<String> loginByEmail(String email, String code) {
        if (code == null || code.length() != 6) {
            return Result.error("验证码格式错误");
        }
        String token = userService.loginByEmail(email, code);
        return Result.success(token);
    }

    /**
     * 邮箱验证码重置密码（无需登录）
     * 
     * @param email       邮箱地址
     * @param code        邮箱验证码
     * @param newPassword 新密码
     * @return 重置结果
     */
    @PostMapping("/reset-password")
    public Result resetPassword(
            @RequestParam @Email String email,
            @RequestParam String code,
            @RequestParam @Pattern(regexp = "^\\S{5,16}$", message = "密码长度为5-16位且不能包含空格") String newPassword) {

        userService.resetPassword(email, code, newPassword);
        return Result.success("密码重置成功，请使用新密码登录");
    }

    @GetMapping("/info")
    public Result<UserVO> userInfo() {
        // 1. 从 ThreadLocal 获取当前登录用户的数据
        // 因为你的工具类是泛型的，这里强转成 Map 即可
        Map<String, Object> map = ThreadLocalUtil.get();

        // 拿到用户名
        String username = (String) map.get("username");

        // 2. 查询数据库
        User user = userService.findByUsername(username);
        UserVO uservo = new UserVO();
        BeanUtils.copyProperties(user, uservo);
        return Result.success(uservo);
    }

    @PutMapping("/update")
    @OperLog(module = "用户管理", operation = "更新个人信息")
    public Result update(@RequestBody User user) {
        userService.update(user);
        return Result.success();
    }

    @PatchMapping("/password")
    @OperLog(module = "用户管理", operation = "修改密码")
    public Result updatePassword(@RequestParam @Pattern(regexp = "^\\S{5,16}$") String oldPassword,
            @RequestParam @Pattern(regexp = "^\\S{5,16}$") String newPassword) {
        // 1. 获取当前登录用户
        Map<String, Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");

        // 2. 根据用户名查询用户
        User loginUser = userService.findByUsername(username);
        if (loginUser == null) {
            return Result.error("用户不存在");
        }
        if (!BcryptUtil.match(oldPassword, loginUser.getPasswordHash())) {
            return Result.error("旧密码错误");
        }
        if (BcryptUtil.match(newPassword, loginUser.getPasswordHash())) {
            return Result.error("新密码不能与旧密码相同");
        }
        loginUser.setPasswordHash(BcryptUtil.encode(newPassword));
        userService.update(loginUser);
        return Result.success();

    }

    @PatchMapping("/avatar")
    public Result updateAvatar(@RequestParam @URL(message = "图片地址格式不正确") String avatarUrl) {
        // 1. 获取当前登录用户
        Map<String, Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");

        // 2. 调用 Service 更新
        userService.updateAvatar(avatarUrl, username);
        return Result.success();
    }

    /**
     * 搜索用户（用于发送消息）
     */
    @GetMapping("/search")
    public Result searchUsers(@RequestParam String query) {
        if (!StringUtils.hasLength(query)) {
            return Result.success(java.util.Collections.emptyList());
        }

        // 限制只搜索前20条，避免大量数据
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.like(User::getUsername, query)
                .or()
                .like(User::getNickname, query)
                .last("LIMIT 20");

        wrapper.select(User::getId, User::getUsername, User::getNickname, User::getAvatarUrl);

        java.util.List<User> list = userMapper.selectList(wrapper);

        // 转换为 VO
        java.util.List<UserVO> voList = list.stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            return vo;
        }).collect(java.util.stream.Collectors.toList());

        return Result.success(voList);
    }
}
