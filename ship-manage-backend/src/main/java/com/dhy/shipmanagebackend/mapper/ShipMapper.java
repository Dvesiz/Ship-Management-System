package com.dhy.shipmanagebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dhy.shipmanagebackend.entity.Ship;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShipMapper extends BaseMapper<Ship> {
}