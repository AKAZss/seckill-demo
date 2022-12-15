package com.zss.seckill.controller;


import com.zss.seckill.pojo.User;
import com.zss.seckill.rabbitmq.MQSender;
import com.zss.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zss
 * @since 2022-12-05
 */
@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private MQSender mqSender;
    /**
     * 用户信息（for test）
     * @param user
     * @return
     */
    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user){
        return RespBean.success(user);
    }

    /**
     * 测试发送rabbitmq消息
     */
    /*@RequestMapping("/mq")
    @ResponseBody
    public void mq(){
        mqSender.sendMsg("Hello");
    }*/

    /**
     * fanout模式发送消息
     */
    /*@RequestMapping("/mq/fanout")
    @ResponseBody
    public void mq01(){
        mqSender.sendMsg("Hello");
    }*/

    /**
     * direct模式发送
     */
    /*@RequestMapping("/mq/direct01")
    @ResponseBody
    public void mq02(){
        mqSender.send01("Hello,Red");
    }
    @RequestMapping("/mq/direct02")
    @ResponseBody
    public void mq03(){
        mqSender.send02("Hello,GREEN");
    }*/

    /**
     * topic模式发消息
     */
    /*@RequestMapping("/mq/topic01")
    @ResponseBody
    public void mq04(){
        mqSender.send03("Hello,Red");
    }
    @RequestMapping("/mq/topic02")
    @ResponseBody
    public void mq05(){
        mqSender.send04("Hello,Green");
    }*/
}
