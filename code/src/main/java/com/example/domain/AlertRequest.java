package com.example.domain;

import lombok.Data;
import java.util.List;


/***
 *  获取json中的alert结构体
 */
@Data
public class AlertRequest {
    private List<Alert> alerts;
}