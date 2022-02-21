package com.group8.service.impl;

import com.group8.dao.NormalUserDao;
import com.group8.dto.UploadImg;
import com.group8.entity.LgGroup;
import com.group8.entity.LgNormalUser;
import com.group8.entity.LgScenicspot;
import com.group8.entity.LgTravelnotes;
import com.group8.service.NormalUserService;
import com.group8.utils.JWTUtils;
import com.group8.utils.MD5Utils;
import com.group8.utils.QiuniuUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import java.util.*;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

/**
 * @author laiyong
 * @date 2022/2/17 11:31 星期四
 * @apiNote
 */
@Service
public class NormalUserServiceImpl implements NormalUserService {

    @Autowired(required = false)
    NormalUserDao normalUserDao;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public LgNormalUser checkUserName(String userName) {
        return normalUserDao.checkUserName(userName);
    }

    @Override
    public int addNormalUser(LgNormalUser lgNormalUser) {
        // 密码使用MD5加密,重新设置回去
        String encryptedPwd = MD5Utils.encrypt(lgNormalUser.getUserPassword(), lgNormalUser.getUserName() + "lg");
        lgNormalUser.setUserPassword(encryptedPwd);
        // 获取当前时间为注册时间
        long currentTimeMillis = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(currentTimeMillis);
        lgNormalUser.setCreatedTime(timestamp);
        // 设置用户默认头像,用户后期自行修改
        String defaultAvatar = "https://gitee.com/cdlycode/oss/raw/master/uPic/2022-02/17-145600.jpeg";
        lgNormalUser.setUserHeadImg(defaultAvatar);
        // 设置账号状态为'0'，用户前往邮件激活
        lgNormalUser.setUserStatus("0");
        // 发送验证邮件
        // 生成随机验证码
        UUID uuid = UUID.randomUUID();
        String activeCode = uuid.toString().replace("-", "");
        lgNormalUser.setActiveCode(activeCode);
        // 将激活码存入redis，设置失效时间为30分钟
        redisTemplate.opsForValue().set(activeCode, activeCode);
        redisTemplate.expire(activeCode, 30, TimeUnit.MINUTES);
        rabbitTemplate.convertAndSend("LG-mail-exchange", "LgMail", lgNormalUser);
        return normalUserDao.addNormalUser(lgNormalUser);
    }

    @Override
    public String login(LgNormalUser lgNormalUser) {
        // 密码加密后查询
        String encryptedPwd = MD5Utils.encrypt(lgNormalUser.getUserPassword(), lgNormalUser.getUserName() + "lg");
        LgNormalUser normalUser = normalUserDao.findByUsernameAndPwd(lgNormalUser.getUserName(), encryptedPwd);
        if (normalUser != null) {
            String token = JWTUtils.sign(normalUser.getUserName(), normalUser.getUserPassword());
            redisTemplate.opsForValue().set(normalUser.getUserName(), token);
            return token;
        } else {
            return null;
        }
    }

    @Override
    public LgNormalUser getInfo(String token) {
        String userName = JWTUtils.getUserName(token);
        String encryptedPwd = JWTUtils.getPassword(token);
        return normalUserDao.findByUsernameAndPwd(userName, encryptedPwd);
    }

    @Override
    public int updateHeadImg(UploadImg uploadImg) {
        String url = QiuniuUtils.uploadPicture(uploadImg);
        System.out.println(url);
        return normalUserDao.updateHeadImg(uploadImg.getId(), url);
    }

    @Override
    public boolean logout(String token) {
        // 删除redis中的token
        String userName = JWTUtils.getUserName(token);
        return redisTemplate.delete(userName);
    }

    @Override
    public List<LgNormalUser> findByCondition(LgNormalUser lgNormalUser) {
        return normalUserDao.findByCondition(lgNormalUser);
    }

    @Override
    public LgNormalUser findById(int id) {
        return normalUserDao.findById(id);
    }

    @Override
    public int update(LgNormalUser lgNormalUser) {
        // 密码使用MD5加密,重新设置回去
        String encryptedPwd = MD5Utils.encrypt(lgNormalUser.getUserPassword(), lgNormalUser.getUserName() + "lg");
        lgNormalUser.setUserPassword(encryptedPwd);
        // 获取当前时间为更新时间
        long currentTimeMillis = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(currentTimeMillis);
        lgNormalUser.setUpdatedTime(timestamp);
        return normalUserDao.update(lgNormalUser);
    }

    @Override
    public boolean checkActiveCode(String code) {
        if (redisTemplate.hasKey(code)) {
            //验证激活码获取此用户信息
            LgNormalUser lgNormalUser = normalUserDao.checkActiveCode(code);
            //如果验证激活码成功修改状态
            if (lgNormalUser != null) {
                normalUserDao.updateUserStatus(lgNormalUser.getUserId());
                redisTemplate.delete(code);
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public int deleteById(int id) {
        return normalUserDao.deleteById(id);
    }

    @Override
    public void browse(long userId, Object browsed) {
        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
        Boolean add = zSetOperations.add(Long.toString(userId), browsed, System.currentTimeMillis());
    }

    @Override
    public List<Object> selectBrowsed(long userId) {
        if (userId <= 0) {
            return Collections.emptyList();
        }
        // 获取用户最近浏览的项目
        Set<Object> set = redisTemplate.opsForZSet().reverseRange(Long.toString(userId), 0, 7);
        List<Object> arrayList = new ArrayList<>();
        set.forEach(item -> {
            arrayList.add(item);
            if (item instanceof LgGroup) {
                System.out.println("LgGroup");
            } else if (item instanceof LgTravelnotes) {
                System.out.println("LgTravelnotes");
            } else if (item instanceof LgScenicspot) {
                System.out.println("LgScenicspot");
            }
        });
        return arrayList;
    }

}