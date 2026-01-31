package com.dhy.shipmanagebackend.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class MailService {

    private static final Logger logger =
            Logger.getLogger(MailService.class.getName());

    @Autowired
    private JavaMailSender mailSender;

    @Value("${resend.from-email}")
    private String fromEmail;

    @Async
    public void send(String to, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "船舶管理系统官方");
            helper.setTo(to);
            helper.setSubject("【船舶管理系统】登录验证码");
            helper.setText(html, true);

            helper.addInline(
                    "logo",
                    new ClassPathResource("static/logo.png")
            );

            mailSender.send(message);

            logger.info("异步邮件发送成功：" + to);
            logger.info("当前线程：" + Thread.currentThread().getName());

        } catch (Exception e) {
            logger.warning("邮件发送失败：" + e.getMessage());
        }
    }
}
