package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.DishDTO;
import com.sky.entity.Dish;

public interface DishService extends IService<Dish> {

    /**
     * 新增菜品和对应的口味
     *
     * @param dishDTO
     */
    public int addDish(DishDTO dishDTO);

}