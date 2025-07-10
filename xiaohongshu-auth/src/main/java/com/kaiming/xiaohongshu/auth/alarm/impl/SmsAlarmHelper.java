package com.kaiming.xiaohongshu.auth.alarm.impl;

import com.kaiming.xiaohongshu.auth.alarm.AlarmInterface;
import lombok.extern.slf4j.Slf4j;

/**
 * ClassName: MailAlarmHelper
 * Package: com.kaiming.xiaohongshu.auth.alarm.impl
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/11 15:13
 * @Version 1.0
 */
@Slf4j
public class SmsAlarmHelper implements AlarmInterface {
    @Override
    public boolean send(String message) {
        log.info("==> 【短信告警】：{}", message);

        // TODO 业务逻辑
        
        
        return true;
    }
}
