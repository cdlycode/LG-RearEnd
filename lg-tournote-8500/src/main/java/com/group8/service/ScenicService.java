package com.group8.service;

import com.group8.dto.DownFile;
import com.group8.dto.UploadImg;
import com.group8.entity.LgScenicspot;

import java.net.MalformedURLException;

/**
 * @author acoffee
 * @create 2022-02-17 17:31
 */
public interface ScenicService {
    String uploadImg(UploadImg uploadImg);

    String downloadStrategy(DownFile file);

    String uploadStrategy(UploadImg uploadImg);


}
