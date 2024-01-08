package com.example.tools;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String convertTime(String input) {
        ZonedDateTime zdt = ZonedDateTime.parse(input);
        ZonedDateTime correctedTime = zdt.plusHours(8);
        String output = correctedTime.format(formatter);
        return output;
    }

    public static String calculateDuration(String start, String end) {
        // 如果警告没有恢复，不进行计算
        if("0001-01-01T00:00:00Z".equals(end)) {
            return "警告尚未恢复，不计算持续时间";
        }

        ZonedDateTime startTime = ZonedDateTime.parse(start);
        ZonedDateTime endTime = ZonedDateTime.parse(end);

        Duration duration = Duration.between(startTime, endTime);
        long totalMinutes = duration.toMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        return hours + "小时" + minutes + "分钟";
    }
}
