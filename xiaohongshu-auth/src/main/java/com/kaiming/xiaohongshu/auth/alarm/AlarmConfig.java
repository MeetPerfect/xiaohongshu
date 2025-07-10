package com.kaiming.xiaohongshu.auth.alarm;

import com.kaiming.xiaohongshu.auth.alarm.impl.MailAlarmHelper;
import com.kaiming.xiaohongshu.auth.alarm.impl.SmsAlarmHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.commons.lang3.StringUtils;

/**
 * ClassName: AlarmConfig
 * Package: com.kaiming.xiaohongshu.auth.alarm
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/11 15:12
 * @Version 1.0
 */
@Configuration
@RefreshScope
public class AlarmConfig {
    
    @Value("${alarm.type}")
    private String alarmType;
    
    @Bean
    @RefreshScope
    public AlarmInterface alarmHelper() {
        // 根据配置文件中的告警类型，初始化选择不同的告警实现类
        if (StringUtils.equals("sms", alarmType)) {
            return new SmsAlarmHelper();
        } else if (StringUtils.equals("mail", alarmType)) {
            return new MailAlarmHelper();
        } else {
            throw new IllegalArgumentException("不支持的告警类型");
        }
    }
}
