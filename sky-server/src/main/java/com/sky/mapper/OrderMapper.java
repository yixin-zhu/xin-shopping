package com.sky.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {

    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);
}
