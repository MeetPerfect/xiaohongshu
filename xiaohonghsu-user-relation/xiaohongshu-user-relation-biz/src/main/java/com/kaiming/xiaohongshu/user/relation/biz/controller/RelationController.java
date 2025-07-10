package com.kaiming.xiaohongshu.user.relation.biz.controller;

import com.kaiming.framework.biz.operationlog.aspect.ApiOperationLog;
import com.kaiming.framework.common.response.PageResponse;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.user.relation.biz.model.vo.*;
import com.kaiming.xiaohongshu.user.relation.biz.service.RelationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: RelationController
 * Package: com.kaiming.xiaohongshu.user.relation.biz.domain.controller
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/28 12:33
 * @Version 1.0
 */
@RestController
@RequestMapping("/relation")
@Slf4j
public class RelationController {
    @Resource
    private RelationService relationService;

    /**
     * 关注用户接口
     * @param followUserReqVO
     * @return
     */
    @PostMapping("/follow")
    @ApiOperationLog(description = "关注用户")
    public Response<?> follow(@RequestBody FollowUserReqVO followUserReqVO) {
         return relationService.follow(followUserReqVO);
    }

    /**
     * 取消关注用户接口
     * @param unfollowUserReqVO
     * @return
     */
    @PostMapping("/unfollow")
    @ApiOperationLog(description = "取消关注用户")
    public Response<?> unfollow(@RequestBody UnfollowUserReqVO unfollowUserReqVO) {
        return relationService.unfollow(unfollowUserReqVO);
    }

    /**
     * 查询关注列表接口
     * @param findFollowingListReqVO
     * @return
     */
    @PostMapping("/following/list")
    @ApiOperationLog(description = "查询关注列表")
    public PageResponse<FindFollowingUserRespVO> findFollowingList(@RequestBody FindFollowingListReqVO findFollowingListReqVO) {
        return relationService.findFollowingList(findFollowingListReqVO);
    }
    
    @PostMapping("/fans/list")
    @ApiOperationLog(description = "查询粉丝列表")
    public PageResponse<FindFansUserRespVO> findFansList(@RequestBody FindFansListReqVO findFansListReqVO) {
        return relationService.findFansList(findFansListReqVO);
    }
}
