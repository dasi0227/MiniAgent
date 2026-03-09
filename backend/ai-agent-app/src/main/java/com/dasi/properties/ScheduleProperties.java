package com.dasi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "miniagent.schedule",  ignoreInvalidFields = true)
public class ScheduleProperties {

    private String refreshCron;

}
