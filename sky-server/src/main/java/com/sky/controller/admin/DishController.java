package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 新增菜品
     *
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result addDish(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        int rows = dishService.addDish(dishDTO);
        return rows > 0 ? Result.success() : Result.error("新增菜品失败");
    }


    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询:{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 菜品批量删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("菜品批量删除")
    public Result deleteBatch(@RequestParam List<Long> ids) {
        log.info("菜品批量删除：{}", ids);
        boolean success = dishService.deleteBatch(ids);
        return success ? Result.success() : Result.error("删除菜品失败");
    }

    /**
     * 根据id查询菜品
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品：{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return dishVO != null ? Result.success(dishVO) : Result.error("查询菜品失败");
    }

    /**
     * 根据categoryId查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据categoryId查询菜品")
    public Result<List<Dish> > getByCategoryId(Long categoryId) {
        log.info("根据categoryId查询菜品：{}", categoryId);
        List<Dish> dishList = dishService.getByCategoryId(categoryId);
        return (!dishList.isEmpty()) ? Result.success(dishList) : Result.error("查询菜品失败");
    }

    /**
     * 修改菜品
     *
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result updateDish(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);
        int row = dishService.updateWithFlavor(dishDTO);
        return row > 0 ? Result.success() : Result.error("修改菜品失败");
    }

    /**
     * 更新菜品状态
     *
     * @param id 菜品ID，通过路径变量传递
     * @return 操作结果
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    public Result updateDishStatus(@PathVariable Integer status, @RequestParam Long id) {
        boolean success = dishService.startOrStop(status, id);
        return success ? Result.success() : Result.error("菜品起售停售操作失败");
    }
}