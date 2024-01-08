package com.example.service;


import com.alibaba.fastjson.JSONObject;
import com.example.domain.Alert;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import com.example.tools.*;
@Service
public class AlertToWechatService {

    @Autowired
    private ApplicationArguments applicationArguments;

    @Autowired
    private Configuration freemarkerConfig;

    public String weChatWebhookUrl(){
        return applicationArguments.getOptionValues("wechat.webhook.url").get(0);
    }

    public String weChatWebhookTemplate(){
        return applicationArguments.getOptionValues("wechat.webhook.template").get(0);
    }


    // 把变量放到一个map里
    public void formatAlert(Alert alert) {
        HashMap<String, String> data = new HashMap<>();

        // 常用label信息
        data.put("alertname", alert.getLabels().get("alertname"));
        data.put("namespace", alert.getLabels().get("namespace"));
        data.put("severity", alert.getLabels().get("severity"));
        // 自定义label信息
        if (applicationArguments.containsOption("customLabel")) {
            getCustomFields.getCustomField(applicationArguments,"customLabel", alert.getLabels(), data);
        }
        // 常用annotation信息
        data.put("action", alert.getAnnotations().get("action"));
        data.put("description", alert.getAnnotations().get("description"));
        data.put("message", alert.getAnnotations().get("message"));
        data.put("summary", alert.getAnnotations().get("summary"));
        // 自定义annotation信息
        if (applicationArguments.containsOption("customAnnotations")) {
            getCustomFields.getCustomField(applicationArguments,"customAnnotations", alert.getLabels(), data);
        }

        // 警告信息
        if (alert.getStatus().equals("firing")){
            data.put("status","报警");
        }else {
            data.put("status","恢复");
        }

        // 时间信息
        data.put("startTime",TimeUtils.convertTime(alert.getStartsAt()));
        if("0001-01-01T00:00:00Z".equals(alert.getEndsAt())){
            data.put("endTime","警告尚未恢复，不计算结束时间");
        }else {
            data.put("endTime",TimeUtils.convertTime(alert.getEndsAt()));
        }

        // 添加当前时间
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        data.put("currentTime", TimeUtils.convertTime(now.toString()));
        // 添加持续时间，为end-start
        data.put("duringTime", TimeUtils.calculateDuration(alert.getStartsAt(), alert.getEndsAt()));

        processTemplate(weChatWebhookTemplate(),data);
    }
    // 处理自定义的label和annotation字段
    // 变量赋值到模板文件中
    public void processTemplate(String templateName, Map<String, String> dataMap){
        try {
            Template template = freemarkerConfig.getTemplate(templateName);
            StringWriter result = new StringWriter();
            template.process(dataMap, result);
            sendWeChatAlert(result.toString());
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // 发送到企业微信的逻辑...
    public void sendWeChatAlert(String messageContent) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        try {
            HttpPost httpPost = new HttpPost(weChatWebhookUrl());

            JSONObject alertContent = new JSONObject();
            alertContent.put("content", messageContent);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msgtype", "markdown");
            jsonObject.put("markdown", alertContent);

            StringEntity requestEntity = new StringEntity(jsonObject.toJSONString(),"utf-8");
            requestEntity.setContentType("application/json");

            httpPost.setEntity(requestEntity);
            CloseableHttpResponse response = httpclient.execute(httpPost);

            try {
                System.out.println(response.getStatusLine());
                HttpEntity entity = response.getEntity();
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }
}
