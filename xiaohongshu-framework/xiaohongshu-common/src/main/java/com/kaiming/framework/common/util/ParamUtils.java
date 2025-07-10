package com.kaiming.framework.common.util;

import java.util.regex.Pattern;

/**
 * ClassName: ParamUtils
 * Package: com.kaiming.framework.common.util
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/31 21:58
 * @Version 1.0
 */
public class ParamUtils {
    
    private ParamUtils() {}

    // 定义昵称长度范围
    private static final int NICK_NAME_MIN_LENGTH = 2;
    private static final int NICK_NAME_MAX_LENGTH = 24;

    // 定义特殊字符的正则表达式
    private static final String NICK_NAME_REGEX = "[!@#$%^&*(),.?\":{}|<>]";
    
    
    public static boolean checkNickName(String nickName) {
        if (nickName.length() < NICK_NAME_MIN_LENGTH || nickName.length() > NICK_NAME_MAX_LENGTH) {
            return false;
        }

        // 检查是否含有特殊字符
        Pattern pattern = Pattern.compile(NICK_NAME_REGEX);
        return !pattern.matcher(nickName).find();
    }

    // ============================== 校验小哈书号 ==============================
    // 定义 ID 长度范围
    private static final int ID_MIN_LENGTH = 6;
    private static final int ID_MAX_LENGTH = 15;

    // 定义正则表达式
    private static final String ID_REGEX = "^[a-zA-Z0-9_]+$";

    /**
     * 小红书Id校验
     * @param xiaohongshuId
     * @return
     */
    public static boolean checkXiaohongshuId(String xiaohongshuId) {
        // 检查长度
        if (xiaohongshuId.length() < ID_MIN_LENGTH || xiaohongshuId.length() > ID_MAX_LENGTH) {
            return false;
        }
        // 检查格式
        Pattern pattern = Pattern.compile(ID_REGEX);
        return pattern.matcher(xiaohongshuId).matches();
    }

    /**
     * 校验字符串长度
     * @param str
     * @param length
     * @return
     */
    public static boolean checkLength(String str, int length) {
        // 检查长度
        if (str.isEmpty() || str.length() > length) {
            return false;
        }
        return true;
    }
}
