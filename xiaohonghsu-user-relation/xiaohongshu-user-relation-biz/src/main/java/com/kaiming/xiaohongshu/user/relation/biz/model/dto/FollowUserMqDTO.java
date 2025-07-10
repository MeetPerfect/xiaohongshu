package com.kaiming.xiaohongshu.user.relation.biz.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ClassName: FollowUserMqDTO
 * Package: com.kaiming.xiaohongshu.user.relation.biz.model.dto
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/28 17:01
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowUserMqDTO {

    private Long userId;

    private Long followUserId;

    private LocalDateTime createTime;
}
