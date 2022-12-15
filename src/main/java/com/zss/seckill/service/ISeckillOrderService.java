package com.zss.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zss.seckill.pojo.SeckillOrder;
import com.zss.seckill.pojo.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zss
 * @since 2022-12-07
 */
public interface ISeckillOrderService extends IService<SeckillOrder> {
    /**
     * 获取秒杀结果
     * @param user
     * @param goodsId
     * @return -1 秒杀失败，0 排队中 ，orderId 成功
     */
    Long getResult(User user, Long goodsId);
}
