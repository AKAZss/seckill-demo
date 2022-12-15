package com.zss.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zss.seckill.pojo.Goods;
import com.zss.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zss
 * @since 2022-12-07
 */
public interface GoodsMapper extends BaseMapper<Goods> {

    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVoByGoodsId(Long goodsId);

}
