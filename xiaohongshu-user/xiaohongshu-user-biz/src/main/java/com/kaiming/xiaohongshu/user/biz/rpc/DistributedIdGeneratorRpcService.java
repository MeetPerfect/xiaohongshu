package com.kaiming.xiaohongshu.user.biz.rpc;

import com.kaiming.xiaohongshu.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * ClassName: DistributeIdGeneratorRpcService
 * Package: com.kaiming.xiaohongshu.distributed.id.generator.biz.rpc
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/8 15:45
 * @Version 1.0
 */
@Component
public class DistributedIdGeneratorRpcService {
    
    @Resource
    private DistributedIdGeneratorFeignApi distributedIdGeneratorFeignApi;

    /**
     * Leaf号段模式：小红书ID业务标识
     */
    private static final String BIZ_TAG_XIAOHONGSHU_ID = "leaf-segment-xiaohongshu-id";
    /**
     * Leaf 号段模式：用户 ID 业务标识
     */
    private static final String BIZ_TAG_USER_ID = "leaf-segment-user-id";
    
    /**
     * 调用分布式ID生成服务生成小红书ID
     * @return
     */
    public String getXiaohongshuId() {
        return distributedIdGeneratorFeignApi.getSegmentId(BIZ_TAG_XIAOHONGSHU_ID);
    }

    /**
     * 调用分布式 ID 生成服务用户 ID
     *
     * @return
     */
    public String getUserId() {
        return distributedIdGeneratorFeignApi.getSegmentId(BIZ_TAG_USER_ID);
    }
}
