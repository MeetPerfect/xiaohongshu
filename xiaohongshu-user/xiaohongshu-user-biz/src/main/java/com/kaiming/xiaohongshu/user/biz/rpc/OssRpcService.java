package com.kaiming.xiaohongshu.user.biz.rpc;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.oss.api.FileFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * ClassName: OssRpcService
 * Package: com.kaiming.xiaohongshu.user.biz.rpc
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/1 13:21
 * @Version 1.0
 */
@Component
public class OssRpcService {
    @Resource
    private FileFeignApi fileFeignApi;

    public String uploadFile(MultipartFile file) {
        // 调用对象存储服务上传文件
        Response<?> response = fileFeignApi.uploadFile(file);
        // 检查响应是否成功
        if (!response.isSuccess()) {
            return null;
        }

        return (String) response.getData();
    }
}
