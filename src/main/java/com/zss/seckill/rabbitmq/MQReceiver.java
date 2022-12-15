package com.zss.seckill.rabbitmq;

import com.zss.seckill.pojo.SeckillMessage;
import com.zss.seckill.pojo.SeckillOrder;
import com.zss.seckill.pojo.User;
import com.zss.seckill.service.IGoodsService;
import com.zss.seckill.service.IOrderService;
import com.zss.seckill.utils.JsonUtil;
import com.zss.seckill.vo.GoodsVo;
import com.zss.seckill.vo.RespBean;
import com.zss.seckill.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @Auther: zss
 * @Date: 2022/12/14 11:05
 * @Description:
 */
@Slf4j
@Service
public class MQReceiver {
//    @RabbitListener(queues = "queue")
//    public void receive(Object msg){
//        log.info("接受消息：" + msg);
//    }
//
//    @RabbitListener(queues = "queue_fanout01")
//    public void receive01(Object msg){
//        log.info("QUEUE01接受消息：" + msg);
//    }
//    @RabbitListener(queues = "queue_fanout02")
//    public void receive02(Object msg){
//        log.info("QUEUE02接受消息：" + msg);
//    }
//
//    @RabbitListener(queues = "queue_direct01")
//    public void receive03(Object msg){
//        log.info("QUEUE01接受消息：" + msg);
//    }
//
//    @RabbitListener(queues = "queue_direct02")
//    public void receive04(Object msg){
//        log.info("QUEUE02接受消息：" + msg);
//    }
//
//    @RabbitListener(queues = "queue_topic01")
//    public void receive05(Object msg){
//        log.info("QUEUE01接受消息：" + msg);
//    }
//    @RabbitListener(queues = "queue_topic02")
//    public void receive06(Object msg){
//        log.info("QUEUE02接受消息：" + msg);
//    }
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IOrderService orderService;

    @RabbitListener(queues = "seckillQueue")
    public void receive(String message){
        log.info("接受到的消息：" + message);
        SeckillMessage seckillMessage = JsonUtil.jsonStr2Object(message, SeckillMessage.class);
        Long goodId = seckillMessage.getGoodId();
        User user = seckillMessage.getUser();
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodId);
        // 再判断，别嫌麻烦
        // 判断库存是否不足
        if(goodsVo.getStockCount() < 1){
            return;
        }
        // 判断是否重复秒杀
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().
                get("order:" + user.getId() + ":" + goodId);
        if(seckillOrder != null){
            return;
        }
        // 下单操作
        orderService.secKill(user,goodsVo);
    }
}
