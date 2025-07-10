package com.kaiming.xiaohongshu.user.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: FindUserByIdRespDTO
 * Package: com.kaiming.xiaohongshu.user.dto.resp
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/14 13:56
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindUserByIdRespDTO {
    /**
     * 用户Id
     */
    private Long id;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 简介
     */
    private String introduction;
}
