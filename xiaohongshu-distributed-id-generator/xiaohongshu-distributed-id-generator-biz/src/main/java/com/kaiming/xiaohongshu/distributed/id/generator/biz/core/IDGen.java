package com.kaiming.xiaohongshu.distributed.id.generator.biz.core;

import com.kaiming.xiaohongshu.distributed.id.generator.biz.core.common.Result;
public interface IDGen {
    Result get(String key);
    boolean init();
}
