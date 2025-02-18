package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Employee;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品和对应的口味
     *
     * @param dishDTO
     */
    @Transactional
    public int addDish(DishDTO dishDTO) {


        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        int rows = dishMapper.insert(dish);

        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
                dishFlavorMapper.insert(dishFlavor);
            });

        }
        return rows;
    }

    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        //开始分页查询
        Page<Dish> page = new Page<>(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        String name = dishPageQueryDTO.getName();
        Integer categoryId = dishPageQueryDTO.getCategoryId();
        Integer status = dishPageQueryDTO.getStatus();
        queryWrapper.like(name != null, Dish::getName, name)
                .eq(categoryId != null, Dish::getCategoryId, categoryId)
                .eq(status!= null,Dish::getStatus, status);
        IPage<Dish> resultPage = this.page(page, queryWrapper);

        return new PageResult(resultPage.getTotal(), resultPage.getRecords());
    }



}