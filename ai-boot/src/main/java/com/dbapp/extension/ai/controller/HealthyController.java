package com.dbapp.extension.ai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author steven.zhu
 * @version 1.0.0
 * @date 2024/1/17
 */
@RestController
@RequestMapping("/api/healthy")
public class HealthyController {

    @GetMapping
    public String healthy() {
        return "ok";
    }
}
