package com.zss.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zss.seckill.pojo.Goods;
import com.zss.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zss
 * @since 2022-12-07
 */
public interface IGoodsService extends IService<Goods> {
    /**
     * 获取商品列表
     * @return
     */
    List<GoodsVo> findGoodsVo();

    /**
     * 通过goodsId查询goodsVO
     * @param goodsId
     */
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
