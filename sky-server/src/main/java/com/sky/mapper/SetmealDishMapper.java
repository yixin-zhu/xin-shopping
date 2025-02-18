package com.sky.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SetmealDishMapper extends BaseMapper<SetmealDish> {
    default int countByDishId(Long dishId) {
        return this.selectCount(new LambdaQueryWrapper<SetmealDish>()
                .eq(SetmealDish::getDishId, dishId));
    }
}
