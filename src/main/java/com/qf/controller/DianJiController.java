package com.qf.controller;

import com.qf.service.IDianJiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
public class DianJiController {

    @Autowired
    private IDianJiService dianJiService;

    private ExecutorService executorService = Executors.newFixedThreadPool(100);

    @RequestMapping("/dianji")
    @ResponseBody
    public String dianji(){

        for (int i = 0; i < 10; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    dianJiService.insertDianJi();
                }
            });
        }
        return null;
    }

}
