package com.kaiming.xiaohongshu.oss.api;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.oss.config.FeignFormConfig;
import com.kaiming.xiaohongshu.oss.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * ClassName: FileFeignApi
 * Package: com.kaiming.xiaohongshu.oss.api
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/1 10:08
 * @Version 1.0
 */
@FeignClient(name = ApiConstants.SERVICE_NAME, configuration = FeignFormConfig.class)
public interface FileFeignApi {
    String PREFIX = "/file";

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping(value = PREFIX + "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Response<?> uploadFile(@RequestPart(value = "file")MultipartFile file);
}
