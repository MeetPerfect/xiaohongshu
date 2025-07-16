package com.kaiming.xiaohongshu.search.service;

import com.kaiming.framework.common.response.PageResponse;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.search.model.vo.SearchUserReqVO;
import com.kaiming.xiaohongshu.search.model.vo.SearchUserRespVO;

/**
 * ClassName: SearchService
 * Package: com.kaiming.xiaohongshu.search.service
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/16 12:31
 * @Version 1.0
 */
public interface SearchService {

    /**
     * 搜索用户
     * @param searchUserReqVO
     * @return
     */
    PageResponse<SearchUserRespVO> searchUser(SearchUserReqVO searchUserReqVO);
}
