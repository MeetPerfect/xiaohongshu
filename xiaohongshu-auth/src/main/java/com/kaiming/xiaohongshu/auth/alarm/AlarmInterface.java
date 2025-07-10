package com.kaiming.xiaohongshu.auth.alarm;

/**
 * ClassName: AlarmInterface
 * Package: com.kaiming.xiaohongshu.auth.alarm
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/11 15:12
 * @Version 1.0
 */
public interface AlarmInterface {

    /**
     * 发送警告信息
     * @param message
     * @return
     */
    boolean send(String message);
}
