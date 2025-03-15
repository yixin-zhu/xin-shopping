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
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

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

    @Transactional
    public boolean deleteBatch(List<Long> ids){
        int rows = 0;
        for(Long id : ids){
            Dish dish = dishMapper.selectById(id);
            if(dish.getStatus() == 1){
                return false;
            }
            if(setmealDishMapper.countByDishId(id) > 0){
                return false;
            }
            rows += dishMapper.deleteById(id);
        }

        return rows == ids.size();
    }

    public DishVO getByIdWithFlavor(Long id){
        //根据id查询菜品数据
        Dish dish = dishMapper.selectById(id);

        //根据菜品id查询口味数据
        List<DishFlavor> dishFlavors = dishFlavorMapper.selectList(new LambdaQueryWrapper<DishFlavor>().eq(DishFlavor::getDishId, id));

        //将查询到的数据封装到VO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    public List<Dish> getByCategoryId(Long categoryId) {
        List<Dish> dishList = dishMapper.selectList(new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, categoryId));
        return dishList;
    }

    @Transactional
    public int updateWithFlavor(DishDTO dishDTO){
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        //修改菜品表基本信息
        int rows = dishMapper.updateById(dish);

        //删除原有的口味数据
        Map<String, Object> columnMap = new HashMap<>();
        columnMap.put("dish_id", dishDTO.getId());
        dishFlavorMapper.deleteByMap(columnMap);


        //重新插入口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
                dishFlavorMapper.insert(dishFlavor);
            });
        }
        return rows;
    }
}