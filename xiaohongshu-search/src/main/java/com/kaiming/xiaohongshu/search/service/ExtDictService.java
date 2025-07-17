package com.kaiming.xiaohongshu.search.service;

import org.springframework.http.ResponseEntity;

/**
 * ClassName: ExDictService
 * Package: com.kaiming.xiaohongshu.search.service
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/16 19:03
 * @Version 1.0
 */
public interface ExtDictService {

    /**
     * 获取热更新词典
     * @return
     */
    ResponseEntity<String> getHotUpdateExtDict();
}
