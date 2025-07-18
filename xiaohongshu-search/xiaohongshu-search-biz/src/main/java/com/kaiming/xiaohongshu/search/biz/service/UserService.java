package com.kaiming.xiaohongshu.search.biz.service;

import com.kaiming.framework.common.response.PageResponse;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.search.dto.RebuildUserDocumentReqDTO;
import com.kaiming.xiaohongshu.search.biz.model.vo.SearchUserReqVO;
import com.kaiming.xiaohongshu.search.biz.model.vo.SearchUserRespVO;

/**
 * ClassName: SearchService
 * Package: com.kaiming.xiaohongshu.search.service
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/16 12:31
 * @Version 1.0
 */
public interface UserService {

    /**
     * 搜索用户
     * @param searchUserReqVO
     * @return
     */
    PageResponse<SearchUserRespVO> searchUser(SearchUserReqVO searchUserReqVO);

    /**
     * 重建用户文档
     * @param rebuildUserDocumentReqDTO
     * @return
     */
    Response<Long> RebuildDocument(RebuildUserDocumentReqDTO rebuildUserDocumentReqDTO);
}
