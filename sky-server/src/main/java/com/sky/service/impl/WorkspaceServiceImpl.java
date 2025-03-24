package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.entity.Orders;
import com.sky.entity.Setmeal;
import com.sky.entity.User;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 根据时间段统计营业数据
     * @param begin
     * @param end
     * @return
     */
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        /**
         * 营业额：当日已完成订单的总金额
         * 有效订单：当日已完成订单的数量
         * 订单完成率：有效订单数 / 总订单数
         * 平均客单价：营业额 / 有效订单数
         * 新增用户：当日新增用户的数量
         */

        //查询总订单数
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(Orders::getOrderTime, begin, end);
        Integer totalOrderCount = orderMapper.selectCount(wrapper);

        //查询有效订单数
        wrapper.eq(Orders::getStatus, Orders.COMPLETED);
        Integer validOrderCount = orderMapper.selectCount(wrapper);

        //查询营业额
        List<Orders> validOrders = orderMapper.selectList(wrapper);
        Double turnover = 0.0;
        for (Orders order : validOrders) {
            turnover += order.getAmount().doubleValue();
        }

        // 订单完成率和平均客单价
        Double unitPrice = 0.0;
        Double orderCompletionRate = 0.0;
        if(totalOrderCount != 0 && validOrderCount != 0){
            //订单完成率
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
            //平均客单价
            unitPrice = turnover / validOrderCount;
        }

        //新增用户数
        LambdaQueryWrapper<User> wrapperUser = new LambdaQueryWrapper<>();
        wrapperUser.between(User::getCreateTime, begin, end);
        Integer newUsers = userMapper.selectCount(wrapperUser);


        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }


    /**
     * 查询订单管理数据
     *
     * @return
     */
    public OrderOverViewVO getOrderOverView() {
        //待接单
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getStatus, Orders.TO_BE_CONFIRMED);
        Integer waitingOrders = orderMapper.selectCount(wrapper);

        //待派送
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getStatus, Orders.CONFIRMED);
        Integer deliveredOrders = orderMapper.selectCount(wrapper);

        //已完成
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getStatus, Orders.COMPLETED);
        Integer completedOrders = orderMapper.selectCount(wrapper);

        //已取消
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getStatus, Orders.CANCELLED);
        Integer cancelledOrders = orderMapper.selectCount(wrapper);

        //全部订单
        wrapper = new LambdaQueryWrapper<>();
        Integer allOrders = orderMapper.selectCount(wrapper);

        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }

    /**
     * 查询菜品总览
     *
     * @return
     */
    public DishOverViewVO getDishOverView() {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getStatus, StatusConstant.ENABLE);
        Integer sold = dishMapper.selectCount(wrapper);

        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getStatus, StatusConstant.DISABLE);
        Integer discontinued = dishMapper.selectCount(wrapper);

        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询套餐总览
     *
     * @return
     */
    public SetmealOverViewVO getSetmealOverView() {
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Setmeal::getStatus, StatusConstant.ENABLE);
        Integer sold = setmealMapper.selectCount(wrapper);

        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Setmeal::getStatus, StatusConstant.DISABLE);
        Integer discontinued = setmealMapper.selectCount(wrapper);

        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}
