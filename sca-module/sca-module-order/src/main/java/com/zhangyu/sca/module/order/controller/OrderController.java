package com.zhangyu.sca.module.order.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhangyu
 * @date 2024/10/10 16:28
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @GetMapping("/pageList")
    public String pageList() {
        return "good";
    }
}
