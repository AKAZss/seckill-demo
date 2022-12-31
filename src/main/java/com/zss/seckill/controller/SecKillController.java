package com.zss.seckill.controller;

import com.wf.captcha.ArithmeticCaptcha;
import com.zss.seckill.anno.AccessLimit;
import com.zss.seckill.anno.AccessLimit2;
import com.zss.seckill.exception.GlobalException;
import com.zss.seckill.pojo.SeckillMessage;
import com.zss.seckill.pojo.SeckillOrder;
import com.zss.seckill.pojo.User;
import com.zss.seckill.rabbitmq.MQSender;
import com.zss.seckill.service.IGoodsService;
import com.zss.seckill.service.IOrderService;
import com.zss.seckill.service.ISeckillOrderService;
import com.zss.seckill.utils.JsonUtil;
import com.zss.seckill.utils.SimpleRedisLock;
import com.zss.seckill.vo.RespBean;
import com.zss.seckill.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: zss
 * @Date: 2022/12/12 10:04
 * @Description: 秒杀控制器
 */
@Controller
@RequestMapping("/seckill")
@Slf4j
public class SecKillController {
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MQSender mqSender;
    @Autowired
    private RedisScript<Long> redisScript;
    // 内存标记,减少redis操作
    private Map<Long,Boolean> EmptyStockMap = new HashMap<>();


    /**
     * 秒杀
     * windows 优化前qps:796,同时发现超卖问题！！！
     * redis缓存后qps:1349
     * 通过rabbitmq,内存标记，redis预减库存后qps:2486
     *
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/{path}/doSeckill",method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill(User user, Long goodsId, @PathVariable("path") String path){
        if(user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 校验str地址是否正确
        boolean check = orderService.checkPath(user,goodsId,path);
        if(!check){
            return RespBean.error(RespBeanEnum.REQUEST_ILLGEAL);
        }
        // 一人一单判断通过redis
        // 用分布式锁实现
        SimpleRedisLock lock = new SimpleRedisLock("lock:order:" + user.getId(), redisTemplate);
        // 此处有问题所在：时间不好设置
        boolean tryLock = lock.tryLock(2);
        if(!tryLock){
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        try {
            Object order = redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
            if(order != null){
                return RespBean.error(RespBeanEnum.REPEATE_ERROR);
            }
            // 内存标记，防止库存不足一直操作redis
            if (EmptyStockMap.get(goodsId)) {
                return RespBean.error(RespBeanEnum.EMPTY_STOCK);
            }
            // 预减库存
            //Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
            // 通过lua脚本实现原子操作
            // 此方案有问题，超卖问题坑太多，用redission
            Long stock = (Long) redisTemplate.
                    execute(redisScript, Collections.singletonList("seckillGoods:" + goodsId), Collections.EMPTY_LIST);
            if (stock < 0) {
                EmptyStockMap.put(goodsId, true);
                valueOperations.increment("seckillGoods" + goodsId);
                return RespBean.error(RespBeanEnum.EMPTY_STOCK);
            }
            // 下订单
            // 此处通过rabbitmq异步下单
            // 发送消息
            SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
            mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));
            // 此处返回0告诉前端下单排队中
            // 快速返回，流量削峰
            return RespBean.success(0);
        }finally {
            lock.unlock();
        }


        /*GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
        // 判断库存
        if(goods.getStockCount() <= 0){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // 判断是否已秒杀
        // SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
        // 从redis缓存中判断
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().
                get("order:" + user.getId() + ":" + goodsId);
        if(seckillOrder != null){
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        // 下订单
        Order order = orderService.secKill(user,goods);
        return RespBean.success(order);*/
    }

    /**
     * 原因：rabbitmq异步下单，需要知道是否下单成功
     * 获取秒杀结果
     * @param user
     * @param goodsId
     * @return -1 秒杀失败，0 排队中 ，orderId 成功
     */
    @RequestMapping(value = "/result",method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user,Long goodsId){
        if(user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user,goodsId);
        return RespBean.success(orderId);
    }

    /**
     * 获取秒杀地址
     * @param user
     * @param goodsId
     * @return
     */
    //@AccessLimit(second=5,maxCount=5,needLogin=true) // 通用接口限流
    @AccessLimit2
    @RequestMapping(value = "/path",method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(User user, Long goodsId, String captcha, HttpServletRequest request){
        if(user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        boolean check = orderService.checkCaptcha(user,goodsId,captcha);
        if(!check){
            return RespBean.error(RespBeanEnum.CAPTCHA_ERROR);
        }
        String str = orderService.createPath(user,goodsId);
        return RespBean.success(str);
    }

    /**
     * 设置验证码
     * 点击秒杀开始前，先输入验证码，分散用户的请求
     * @param user
     * @param goodsId
     * @param response
     */
    @RequestMapping(value = "/captcha",method = RequestMethod.GET)
    public void verifyCode(User user, Long goodsId, HttpServletResponse response){
        if(user == null || goodsId < 0){
            throw new GlobalException(RespBeanEnum.REQUEST_ILLGEAL);
        }
        // 设置响应实体
        response.setContentType("image/jpg");
        response.setHeader("Pargam","No-cache");
        response.setHeader("Cache-Control","no-cache");
        response.setDateHeader("Expires",0);
        // 生成验证码，将结果放入redis
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);
        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId,captcha.text(),300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失败：" + e.getMessage());
        }
    }

}
