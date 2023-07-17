package cn.vanillazi.learn.health.checking.web;

import cn.vanillazi.learn.health.checking.task.LongTimeInitializerTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/hello")
public class HelloController {

    @Autowired
    private LongTimeInitializerTask task;

    @GetMapping
    public String hello(){
        return "Hello at:"+ LocalDateTime.now();
    }
}
