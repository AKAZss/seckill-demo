package com.zss.seckill.config;

import com.zss.seckill.pojo.User;

/**
 * @Auther: zss
 * @Date: 2022/12/15 15:42
 * @Description:
 */
public class UserContext {
    private static ThreadLocal<User> userHolder = new ThreadLocal<>();

    public static void setUser(User user){
        userHolder.set(user);
    }
    public static User getUser(){
        return userHolder.get();
    }
}
