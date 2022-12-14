package com.group8.service.impl;

import com.group8.dao.UserLikeCommentDao;
import com.group8.dto.CommentLikedCountDTO;
import com.group8.entity.LgComment;
import com.group8.entity.LgUserLike;
import com.group8.service.LikeCommentRedisService;
import com.group8.utils.CommentLikedStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

@Service
public class LikeCommentServiceImpl implements com.group8.service.LikeCommentService {

    @Resource
    UserLikeCommentDao userLikeCommentDao;

    @Resource
    LikeCommentRedisService likeCommentRedisService;

    @Override
    public void save(LgUserLike lgUserLike) {
        lgUserLike.setCreateTime(new Timestamp(System.currentTimeMillis()));
        userLikeCommentDao.save(lgUserLike);
    }

    @Override
    public List<LgUserLike> saveAll(List<LgUserLike> list) {
        return userLikeCommentDao.saveAll(list);
    }

    @Override
    public Page<LgUserLike> getLikedListByLikedUserId(String likedUserId, Pageable pageable) {
        return userLikeCommentDao.findByLikedUserIdAndStatus(likedUserId, CommentLikedStatusEnum.LIKE.getCode(), pageable);

    }

    @Override
    public Page<LgUserLike> getLikedListByLikedPostId(String likedPostId, Pageable pageable) {
        return userLikeCommentDao.findByLikedPostIdAndStatus(likedPostId, CommentLikedStatusEnum.LIKE.getCode(), pageable);

    }

    @Override
    public LgUserLike getByLikedUserIdAndLikedPostId(String likedUserId, String likedPostId) {
        return userLikeCommentDao.findByLikedUserIdAndLikedPostId(likedUserId, likedPostId);

    }

    @Override
    public void transLikedFromRedis2DB() {
        List<LgUserLike> list = likeCommentRedisService.getLikedDataFromRedis();
        for (LgUserLike like : list) {
            LgUserLike ul = getByLikedUserIdAndLikedPostId(like.getLikedUserId(), like.getLikedPostId());
            if (ul == null){
                //???????????????????????????
                save(like);
            }else{
                //????????????????????????
                ul.setStatus(like.getStatus());
                update(ul);
            }
        }
    }

    @Override
    public void transLikedCountFromRedis2DB() {
        List<CommentLikedCountDTO> list = likeCommentRedisService.getLikedCountFromRedis();
        for (CommentLikedCountDTO dto : list) {
            LgComment lgComment = userLikeCommentDao.findById(dto.getKey());
            //???????????????????????????????????????????????????????????????
            if (lgComment != null){
                Integer likeNum = Integer.parseInt(lgComment.getPraiseNum()) + dto.getValue();
                lgComment.setPraiseNum(Integer.toString(likeNum));
                //??????????????????
                userLikeCommentDao.numUpdate(lgComment);
            }
        }
    }

    @Override
    public void update(LgUserLike ul) {
        userLikeCommentDao.likeUpdate(ul);
    }
}
