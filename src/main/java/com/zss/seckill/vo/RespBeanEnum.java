package com.zss.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 公共返回对象枚举
 */
@Getter
@ToString
@AllArgsConstructor
public enum RespBeanEnum {
    // 通用模块
    SUCCESS(200,"SUCCESS"),
    ERROR(500,"ERROR"),
    // 登录模块5002
    LOGIN_ERROR(500210,"用户名或密码错误"),
    MOBILE_ERROR(500211,"手机号格式不正确"),
    BIND_ERROR(500212,"数据绑定异常"),
    MOBILE_NOT_EXIST(500213,"用户不存在"),
    PASSWORD_UPDATE_ERROR(500214,"更新密码失败"),
    SESSION_ERROR(500215,"用户不存在"),
    // 秒杀模块5005
    EMPTY_STOCK(500500,"库存不足"),
    REPEATE_ERROR(500501,"该商品每人限购一件"),
    REQUEST_ILLGEAL(500502,"请求非法，重新尝试"),
    CAPTCHA_ERROR(500503,"验证码错误"),
    ACCESS_LIMIT_REACHED(500504,"访问过于频繁，请稍后访问"),
    // 订单模块5003xx
    ORDER_NOT_EXIST(500300,"订单信息不存在")
    ;
    private final Integer code;
    private final String message;
}
