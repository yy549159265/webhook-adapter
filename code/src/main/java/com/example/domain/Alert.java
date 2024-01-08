package com.example.domain;


import lombok.Data;

import java.util.List;
import java.util.Map;

/***
 *   获取alert数组中每个警告的指标
 */
@Data
public class Alert {

    private Map<String, String> labels;
    private Map<String, String> annotations;
    private String startsAt;
    private String endsAt;
    private String status;

}