package com.kaiming.xiaohongshu.distributed.id.generator.api;

import com.kaiming.xiaohongshu.distributed.id.generator.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * ClassName: DistributedIdGeneratorFeignApi
 * Package: com.kaiming.xiaohongshu.distributed.id.generator.api
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/8 15:35
 * @Version 1.0
 */
@FeignClient(name= ApiConstants.SERVICE_NAME)
public interface DistributedIdGeneratorFeignApi {
    
    String PREFIX = "/id";
    
    @GetMapping(value = PREFIX + "/segment/get/{key}")
    String getSegmentId(@PathVariable("key") String key);
    
    @GetMapping(value = PREFIX + "/snowflake/get/{key}")
    String getSnowflakeId(@PathVariable("key") String key);
}
