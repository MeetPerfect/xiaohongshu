package com.kaiming.xiaohongshu.user.relation.biz.service;

import com.kaiming.framework.common.response.PageResponse;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.user.relation.biz.model.vo.*;

/**
 * ClassName: RelationService
 * Package: com.kaiming.xiaohongshu.user.relation.biz.domain.service
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/28 12:12
 * @Version 1.0
 */
public interface RelationService {

    /**
     * 关注用户接口
     * @param followUserReqVO
     * @return
     */
    Response<?> follow(FollowUserReqVO followUserReqVO);
    
    Response<?> unfollow(UnfollowUserReqVO unfollowUserReqVO);
    
    PageResponse<FindFollowingUserRespVO> findFollowingList(FindFollowingListReqVO findFollowingListReqVO);

    /**
     * 查询粉丝列表
     * @param findFansListReqVO
     * @return
     */
    PageResponse<FindFansUserRespVO> findFansList(FindFansListReqVO findFansListReqVO);
}
