package com.kaiming.xiaohongshu.note.biz.rpc;

import com.kaiming.xiaohongshu.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * ClassName: DistributedIdGeneratorRpcService
 * Package: com.kaiming.xiaohongshu.note.biz.rpc
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/13 20:34
 * @Version 1.0
 */
@Component
public class DistributedIdGeneratorRpcService {
    
    @Resource
    private DistributedIdGeneratorFeignApi distributedIdGeneratorFeignApi;

    /**
     * 生成雪花算法ID
     * @return
     */
    public String getSnowflakeId() {
        return distributedIdGeneratorFeignApi.getSnowflakeId("test");
    }
}
