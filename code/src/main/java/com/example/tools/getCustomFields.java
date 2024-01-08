package com.example.tools;

import org.springframework.boot.ApplicationArguments;

import java.util.Map;

public class getCustomFields {
    public static void getCustomField(ApplicationArguments applicationArguments, String optionName,
                                     Map<String, String> sourceMap, Map<String, String> data) {
        // 获取传入参数  --customLabel=key1,key2
        // get(0)就是 key1,key2
        String customLabel = applicationArguments.getOptionValues(optionName).get(0);
        String[] customLabels = customLabel.split(",");
        // 放入data中
        for (String label : customLabels) {
            data.put(label, sourceMap.get(label));
        }
    }
}
