package com.kaiming.xiaohongshu.user.relation.biz.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ClassName: UnfollowUserMQDTO
 * Package: com.kaiming.xiaohongshu.user.relation.biz.model.dto
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/29 22:03
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnfollowUserMqDTO {
    
    private Long userId;

    private Long unfollowUserId;

    private LocalDateTime createTime;
    
}
