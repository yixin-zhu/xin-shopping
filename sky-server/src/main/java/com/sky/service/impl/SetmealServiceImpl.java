package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;

    @Transactional
    public int addSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        
        setmealMapper.insert(setmeal);

        Long setMealId = setmeal.getId();
        
        // 保存套餐菜品关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setMealId);
                setmealDishMapper.insert(setmealDish);
            });
        }
        return 1;
    }

    public PageResult pageQuery(SetmealPageQueryDTO queryDTO) {
        Page<Setmeal> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        String name = queryDTO.getName();
        Integer categoryId = queryDTO.getCategoryId();
        Integer status = queryDTO.getStatus();
        
        wrapper.like(name !=null, Setmeal::getName, name)
              .eq(categoryId != null, Setmeal::getCategoryId, categoryId)
              .eq(status != null, Setmeal::getStatus, status);

        IPage<Setmeal> result = this.page(page, wrapper);
        return new PageResult(result.getTotal(), result.getRecords());
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Transactional
    public boolean deleteBatch(List<Long> ids) {
        // 检查套餐是否启售
        LambdaQueryWrapper<Setmeal> statusWrapper = new LambdaQueryWrapper<>();
        statusWrapper.in(Setmeal::getId, ids)
                   .eq(Setmeal::getStatus, StatusConstant.ENABLE);
        if (setmealMapper.selectCount(statusWrapper) > 0) {
            return false;
        }
        
        // 删除套餐
        setmealMapper.deleteBatchIds(ids);
        
        // 删除套餐-菜品关联表里的数据
        LambdaQueryWrapper<SetmealDish> dishWrapper = new LambdaQueryWrapper<>();
        dishWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishMapper.delete(dishWrapper);
        
        return true;
    }

    /**
     * 根据id查询套餐和套餐菜品关系
     *
     * @param id
     * @return
     */
    public SetmealVO getByIdWithDish(Long id) {
        Setmeal setmeal = setmealMapper.selectById(id);

        List<SetmealDish> setmealDishes = setmealDishMapper.selectList(new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getSetmealId, id));

        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;
    }

    /**
     * 修改套餐
     *
     * @param setmealDTO
     */
    @Transactional
    public int updateSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        //1、修改套餐表，执行update
        int rows = setmealMapper.updateById(setmeal);

        //套餐id
        Long setmealId = setmealDTO.getId();


        // 2. 删除套餐和菜品的关联关系，使用 MyBatis-Plus 的 delete 方法
        LambdaQueryWrapper<SetmealDish> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SetmealDish::getSetmealId, setmealId);
        setmealDishMapper.delete(deleteWrapper);

        // 3. 重新插入套餐和菜品的关联关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
                setmealDishMapper.insert(setmealDish);
            });
        }

        return rows;
    }

    /**
     * 套餐起售、停售
     * @param status
     * @param id
     */
    public boolean startOrStop(Integer status, Long id) {

        //起售套餐时，判断套餐内是否有停售菜品，有停售菜品提示"套餐内包含未启售菜品，无法启售"
        if(Objects.equals(status, StatusConstant.ENABLE)){
            List<SetmealDish> setmealDishList = setmealDishMapper.selectList(new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getSetmealId, id));
            for (SetmealDish setmealDish : setmealDishList) {
                Dish dish = dishMapper.selectById(setmealDish.getDishId());
                if(Objects.equals(dish.getStatus(), StatusConstant.DISABLE)){
                    return false;
                }
            }
        }

        //更新套餐状态
        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Setmeal::getId, id)
                     .set(Setmeal::getStatus, status);

        return this.update(updateWrapper);
    }

    public List<Setmeal> getSetmealListById(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        // 动态构建查询条件
        Long categoryId = setmeal.getCategoryId();
        Integer status = setmeal.getStatus();
        queryWrapper.eq(categoryId != null, Setmeal::getCategoryId, categoryId)
                    .eq(status != null, Setmeal::getStatus, status);
        return setmealMapper.selectList(queryWrapper); // 执行查询
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
