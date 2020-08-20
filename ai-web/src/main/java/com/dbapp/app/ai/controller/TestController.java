package com.dbapp.app.ai.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: shawn.xie
 * @Date: 2020/8/18 12:28
 * @Description:
 */
@RestController
public class TestController {


    @RequestMapping("/test")
    public Object test(){
        return "test";
    }
}