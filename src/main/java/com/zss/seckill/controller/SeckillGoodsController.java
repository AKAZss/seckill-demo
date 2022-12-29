package com.zss.seckill.controller;


import com.github.benmanes.caffeine.cache.Cache;
import com.zss.seckill.pojo.SeckillGoods;
import com.zss.seckill.service.ISeckillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zss
 * @since 2022-12-07
 */
@Controller
@RequestMapping("/seckillGoods")
public class SeckillGoodsController {
    @Autowired
    private ISeckillGoodsService seckillGoodsService;
    @Autowired
    private Cache<Long,SeckillGoods> seckillGoodsCache;

    /**
     * Caffeine本地缓存
     * @param id
     * @return
     */
    @RequestMapping("/{goodsId}")
    public SeckillGoods findById(@PathVariable("goodsId")Long id){
        return seckillGoodsCache.get(id,key -> seckillGoodsService.getById(key));
    }
}
