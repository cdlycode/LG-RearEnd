package com.group8.service.impl;

import com.group8.dto.CommentLikedCountDTO;
import com.group8.entity.LgUserLike;
import com.group8.service.LikeCommentRedisService;
import com.group8.utils.CommentLikeRedisKeyUtils;
import com.group8.utils.CommentLikedStatusEnum;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class LikeCommentRedisServiceImpl implements LikeCommentRedisService {
    @Resource
    RedisTemplate redisTemplate;
    @Override
    public void saveLiked2Redis(String likedUserId, String likedPostId) {
        String key = CommentLikeRedisKeyUtils.getLikedKey(likedUserId, likedPostId);
        redisTemplate.opsForHash().put(CommentLikeRedisKeyUtils.MAP_KEY_USER_LIKED, key, CommentLikedStatusEnum.LIKE.getCode());

    }

    @Override
    public void unlikeFromRedis(String likedUserId, String likedPostId) {
        String key = CommentLikeRedisKeyUtils.getLikedKey(likedUserId, likedPostId);
        redisTemplate.opsForHash().put(CommentLikeRedisKeyUtils.MAP_KEY_USER_LIKED, key, CommentLikedStatusEnum.UNLIKE.getCode());

    }

    @Override
    public void deleteLikedFromRedis(String likedUserId, String likedPostId) {
        String key = CommentLikeRedisKeyUtils.getLikedKey(likedUserId, likedPostId);
        redisTemplate.opsForHash().delete(CommentLikeRedisKeyUtils.MAP_KEY_USER_LIKED, key);

    }

    @Override
    public void incrementLikedCount(String likedUserId) {
        redisTemplate.opsForHash().increment(CommentLikeRedisKeyUtils.MAP_KEY_USER_LIKED_COUNT, likedUserId, 1);
    }

    @Override
    public void decrementLikedCount(String likedUserId) {
        redisTemplate.opsForHash().increment(CommentLikeRedisKeyUtils.MAP_KEY_USER_LIKED_COUNT, likedUserId, -1);

    }

    @Override
    public List<LgUserLike> getLikedDataFromRedis() {
        Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(CommentLikeRedisKeyUtils.MAP_KEY_USER_LIKED, ScanOptions.NONE);
        List<LgUserLike> list = new ArrayList<>();
        while (cursor.hasNext()){
            Map.Entry<Object, Object> entry = cursor.next();
            String key = (String) entry.getKey();
            //????????? likedUserId???likedPostId
            String[] split = key.split("::");
            String likedUserId = split[0];
            String likedPostId = split[1];
            Integer value = (Integer) entry.getValue();

            //????????? UserLike ??????
            LgUserLike userLike = new LgUserLike(likedUserId, likedPostId, value);
            list.add(userLike);

            //?????? list ?????? Redis ?????????
            redisTemplate.opsForHash().delete(CommentLikeRedisKeyUtils.MAP_KEY_USER_LIKED, key);
        }

        return list;
    }

    @Override
    public List<CommentLikedCountDTO> getLikedCountFromRedis() {
        Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(CommentLikeRedisKeyUtils.MAP_KEY_USER_LIKED_COUNT, ScanOptions.NONE);
        List<CommentLikedCountDTO> list = new ArrayList<>();
        while (cursor.hasNext()){
            Map.Entry<Object, Object> map = cursor.next();
            //???????????????????????? LikedCountDT
            String key = (String)map.getKey();
            CommentLikedCountDTO dto = new CommentLikedCountDTO(key, (Integer) map.getValue());
            list.add(dto);
            //???Redis?????????????????????
            redisTemplate.opsForHash().delete(CommentLikeRedisKeyUtils.MAP_KEY_USER_LIKED_COUNT, key);
        }
        return list;
    }
}
