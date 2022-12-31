package com.zss.seckill.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zss.seckill.exception.GlobalException;
import com.zss.seckill.mapper.OrderMapper;
import com.zss.seckill.pojo.Order;
import com.zss.seckill.pojo.SeckillGoods;
import com.zss.seckill.pojo.SeckillOrder;
import com.zss.seckill.pojo.User;
import com.zss.seckill.service.IGoodsService;
import com.zss.seckill.service.IOrderService;
import com.zss.seckill.service.ISeckillGoodsService;
import com.zss.seckill.service.ISeckillOrderService;
import com.zss.seckill.utils.MD5Util;
import com.zss.seckill.utils.UUIDUtil;
import com.zss.seckill.vo.GoodsVo;
import com.zss.seckill.vo.OrderDetailVo;
import com.zss.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zss
 * @since 2022-12-07
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    @Autowired
    private ISeckillGoodsService seckillGoodsService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * do秒杀
     * @param user
     * @param goods
     * @return
     */
    @Override
    @Transactional
    public Order secKill(User user, GoodsVo goods) {
        // 设置秒杀商品库存减一
        // 超卖问题根源
        // 解决：1.sql语句的优化
        //      2.加了一个 goods_id,user_id 唯一索引
        //      3.从redis中判断是否重复秒杀
        boolean result = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>().
                setSql("stock_count = stock_count - 1").
                eq("goods_id",goods.getId()).gt("stock_count",0));
        if(!result){
            return null;
        }
        // 创建订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goods.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goods.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(goods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        orderMapper.insert(order);
        // 生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(user.getId());
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setGoodsId(goods.getId());
        seckillOrderService.save(seckillOrder);
        redisTemplate.opsForValue().set("order:" + user.getId() + ":" + goods.getId(),1);
        return order;
    }

    @Override
    public OrderDetailVo detail(Long orderId) {
        if(orderId == null){
            throw new GlobalException(RespBeanEnum.ORDER_NOT_EXIST);
        }
        Order order = orderMapper.selectById(orderId);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(order.getGoodsId());
        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setOrder(order);
        orderDetailVo.setGoodsVo(goodsVo);
        return orderDetailVo;
    }

    @Override
    public String createPath(User user, Long goodsId) {
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        // 存在redis中
        redisTemplate.opsForValue().set("seckillPath:" + user.getId() + ":" + goodsId,str,60, TimeUnit.SECONDS);
        return str;
    }

    @Override
    public boolean checkPath(User user, Long goodsId,String path) {
        if(user == null || goodsId < 0 || StringUtils.isEmpty(path)){
            return false;
        }
        // 获取之前存在redis中的paath
        String redisPath = (String) redisTemplate.opsForValue().get("seckillPath:" + user.getId() + ":" + goodsId);
        return path.equals(redisPath);
    }

    @Override
    public boolean checkCaptcha(User user, Long goodsId, String captcha) {
        if(user == null || goodsId < 0 || StringUtils.isEmpty(captcha)){
            return false;
        }
        String redisCaptcha = (String) redisTemplate.opsForValue().get("captcha:" + user.getId() + ":" + goodsId);
        return captcha.equals(redisCaptcha);
    }
}
