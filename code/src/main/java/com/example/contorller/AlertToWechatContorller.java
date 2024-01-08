package com.example.contorller;


import com.alibaba.fastjson.JSON;
import com.example.domain.Alert;
import com.example.domain.AlertRequest;
import com.example.service.AlertToWechatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
public class AlertToWechatContorller {

    @Autowired
    private AlertToWechatService alertToWechatService;

    private final Logger log = LoggerFactory.getLogger(AlertToWechatContorller.class);

    @PostMapping("/alert")
    public ResponseEntity<String> receiveAlerts(@RequestBody String json) {

        // 打印传入的json数据

        log.info("Received Alerts: {}", json);
        AlertRequest alertRequest = JSON.parseObject(json, AlertRequest.class);
        for(Alert alert : alertRequest.getAlerts()) {

            alertToWechatService.formatAlert(alert);
        }
        return ResponseEntity.ok("Received Alerts");
    }

}
