package com.zss.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zss.seckill.pojo.User;
import com.zss.seckill.vo.LoginVo;
import com.zss.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zss
 * @since 2022-12-05
 */
public interface IUserService extends IService<User> {

    /**
     * 登录
     * @param vo
     * @return
     */
    RespBean doLogin(LoginVo vo, HttpServletRequest request, HttpServletResponse response);

    /**
     * get user by cookie
     * @param userTicket
     * @return
     */
    User getUserByCookie(String userTicket,HttpServletRequest request,HttpServletResponse response);

    /**
     * 更新密码
     * @param userTicket
     * @param password
     * @return
     */
    RespBean updatePassword(String userTicket,String password,
                            HttpServletRequest request,HttpServletResponse response);
}
