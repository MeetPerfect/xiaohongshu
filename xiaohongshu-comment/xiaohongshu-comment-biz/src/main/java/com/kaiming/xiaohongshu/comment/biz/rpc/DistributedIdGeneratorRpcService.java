package com.kaiming.xiaohongshu.comment.biz.rpc;

import com.kaiming.xiaohongshu.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * ClassName: DistributedIdGeneratorRpcService
 * Package: com.kaiming.xiaohongshu.comment.biz.rpc
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/18 22:41
 * @Version 1.0
 */
@Component
public class DistributedIdGeneratorRpcService {
    
    @Resource
    private DistributedIdGeneratorFeignApi distributedIdGeneratorFeignApi;

    /**
     * 生成评论Id
     * @return
     */
    public String generateCommentId() {
        return distributedIdGeneratorFeignApi.getSegmentId("leaf-segment-comment-id");
    }
}
