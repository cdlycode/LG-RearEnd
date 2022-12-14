package com.group8.service;

import com.group8.dto.CommentResponse;
import com.group8.entity.LgComment;

import java.util.List;

public interface ProductCommentService {
    /**
     * 商品评论的添加
     * @param uid
     * @param fid
     * @param content
     */
    void addProductComment(int pid, int uid,int fid,int mark,String content,int oid);

    List<CommentResponse> findAll(int id,int userId);

    void update(int cid);

    void delete(int cid);

    void replayProductComment(int id, int uid, int fid, int mark, String content);
}
