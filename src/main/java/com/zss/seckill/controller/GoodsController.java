package com.zss.seckill.controller;

import com.zss.seckill.pojo.User;
import com.zss.seckill.service.IGoodsService;
import com.zss.seckill.service.IUserService;
import com.zss.seckill.vo.DetailVo;
import com.zss.seckill.vo.GoodsVo;
import com.zss.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: zss
 * @Date: 2022/12/7 11:55
 * @Description: 商品控制器
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;
    /**
     * 跳转商品列表页
     * windows优化前qps:1132
     * redis页面缓存后qps:2342
     *
     *
     */
    @RequestMapping(value = "/toList",produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toList(Model model, User user,
                         HttpServletRequest request, HttpServletResponse response){
        // redis页面缓存
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsList");
        // 有缓存
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        model.addAttribute("user",user);
        model.addAttribute("goodsList",goodsService.findGoodsVo());
        // 没有缓存
        // 手动渲染，存入redis并返回
        // 此处用了thymeleafViewResolver渲染
        WebContext context = new WebContext(request,response,request.getServletContext(),
                request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", context);
        if(!StringUtils.isEmpty(html)){
            valueOperations.set("goodsList",html,60, TimeUnit.SECONDS);
        }
        return html;
    }

    /**
     * 跳转到商品详情页
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/toDetail2/{goodsId}",produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toDetail2(Model model,User user,@PathVariable Long goodsId,
                           HttpServletRequest request, HttpServletResponse response){
        // redis的页面缓存
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 如果有缓存
        String html = (String) valueOperations.get("goodsDetail:" + goodsId);
        if(!StringUtils.isEmpty(html)){
            return html;
        }

        model.addAttribute("user",user);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date now = new Date();
        // 秒杀状态
        int secKillStatus = 0;
        // 秒杀倒计时
        int remainSeconds = 0;
        // 未开始
        if(now.before(startDate)){
            remainSeconds = (int)((startDate.getTime() - now.getTime()) / 1000);
        }else if(now.after(endDate)){
            // 已结束
            secKillStatus = 2;
            remainSeconds = -1;
        }else{
            // 进行中
            secKillStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("remainSeconds",remainSeconds);
        model.addAttribute("secKillStatus",secKillStatus);
        model.addAttribute("goods", goodsVo);

        // 没有redis缓存
        // 同样手动渲染通过themyleafResolver
        WebContext context = new WebContext(request,response,request.getServletContext(),
                request.getLocale(),model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", context);
        if(!StringUtils.isEmpty(html)) {
            valueOperations.set("goodsDetail:" + goodsId, html,60,TimeUnit.SECONDS);
        }
        return html;
    }



    /**
     * 跳转到商品详情页
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public RespBean toDetail(Model model,User user, @PathVariable Long goodsId,
                             HttpServletRequest request, HttpServletResponse response){

        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goods.getStartDate();
        Date endDate = goods.getEndDate();
        Date now = new Date();
        // 秒杀状态
        int secKillStatus = 0;
        // 秒杀倒计时
        int remainSeconds = 0;
        // 未开始
        if(now.before(startDate)){
            remainSeconds = (int)((startDate.getTime() - now.getTime()) / 1000);
        }else if(now.after(endDate)){
            // 已结束
            secKillStatus = 2;
            remainSeconds = -1;
        }else{
            // 进行中
            secKillStatus = 1;
            remainSeconds = 0;
        }
        DetailVo detailVo = new DetailVo();
        detailVo.setGoodsVo(goods);
        detailVo.setRemainSeconds(remainSeconds);
        detailVo.setSecKillStatus(secKillStatus);
        detailVo.setUser(user);
        return RespBean.success(detailVo);
    }
}
