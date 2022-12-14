package com.group8.service.impl;

import com.group8.dao.OrderDao;
import com.group8.dto.UserOrders;
import com.group8.entity.LgGroup;
import com.group8.entity.LgSalesPromotionActivity;
import com.group8.entity.LgTourOrder;
import com.group8.entity.LgTourOrderPersonalInformation;
import com.group8.service.OrderService;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    @Autowired
    OrderDao orderDao;

    @Autowired
    RedisTemplate<String,Object> redisTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RedissonClient redissonClient;

    //将数据从数据库存到redis
    @Override
    public void start(){

    }

    //秒杀
    @Override
    public void seckill(int activityId){
        //减redis库存
        ListOperations<String, Object> opsForList = redisTemplate.opsForList();
        Object inventory =  opsForList.rightPop("activity" + activityId);
        if(inventory == null){
            System.out.println("over");
        }else {
            //创建秒杀订单
            LgTourOrder lgTourOrder = new LgTourOrder();
            lgTourOrder.setProductId(activityId);
            lgTourOrder.setOrderChoose("冰火两重天");
            lgTourOrder.setOrderPayoutStatus(0);
            lgTourOrder.setCommentId(0);
            lgTourOrder.setCreatedTime((Timestamp) new Date("yyyy-MM-dd'T'HH-mm-ss"));
            rabbitTemplate.convertAndSend("exchangeadd", "add", lgTourOrder);

        }

    }

    @Override
    public LgSalesPromotionActivity getActivityById(int activityId) {
        return orderDao.getActivityById(activityId);
    }

    @Override
    public void addOrder(LgTourOrder lgTourOrder) {
        orderDao.addOrder(lgTourOrder);
    }


    @Override
    public List<LgSalesPromotionActivity> getAllActivity() {
        return orderDao.getAllActivity();
    }

    @Override
    public void updateInventory() {
        List<LgSalesPromotionActivity> allActivity = orderDao.getAllActivity();
        for(LgSalesPromotionActivity activity : allActivity){
            Long size = redisTemplate.opsForList().size("activity" + activity.getActivityId());
            orderDao.updateInventory(size,activity.getActivityId());
        }

//        ListOperations opsForList = redisTemplate.opsForList();
//        long size1 = opsForList.size("activity"+lgTourOrder.getProductId());
//        for(long i = 0; i < size1; i++){
//            opsForList.rightPop("activity"+lgTourOrder.getProductId());
//        }
//        LgSalesPromotionActivity activity = orderDao.getActivityById((int) lgTourOrder.getProductId());
//        for(int i = 1; i <= activity.getInventory(); i++){
//            opsForList.leftPush("activity"+activity.getActivityId(),i);
//        }
    }

    @Override
    public List<LgTourOrder> getAllOrder(LgTourOrder lgTourOrder) {
        return orderDao.getAllOrder(lgTourOrder);
    }

    @Override
    public List<UserOrders> getNotPayOrder(int userId) {
        return orderDao.getNotPayOrder(userId);
    }

    @Override
    public List<UserOrders> getPayOrder(int userId) {
        return orderDao.getPayOrder(userId);
    }

    @Override
    public List<UserOrders> getAllOrderById(int userId) {
        return orderDao.getAllOrderById(userId);
    }

    @Override
    public List<UserOrders> getNoCommentOrder(int userId) {
        return orderDao.getNoCommentOrder(userId);
    }

    @Override
    public List<LgGroup> findGroup(String groupName) {
        return orderDao.findGroup(groupName);
    }

    @Override
    public List<UserOrders> getNOGoOrder(int userId) {
        return orderDao.getNOGoOrder(userId);
    }

    @Override
    public void deleteOrder(int orderId) {
        orderDao.deleteOrder(orderId);
    }

    @Override
    public List<LgTourOrderPersonalInformation> getAllPersons(int orderId) {
        return orderDao.getAllPersons(orderId);
    }

    @Override
    public List<LgTourOrder> findBookPerson(int orderId) {
        return orderDao.findBookPerson(orderId);
    }

    @Override
    public LgTourOrder findOrderById(int orderId) {
        return orderDao.findOrderById(orderId);
    }

    @Override
    public void updatePayStatus(int orderId) {
        orderDao.updatePayStatus(orderId);
    }

    @Override
    public int buyGroup(LgTourOrder lgTourOrder) {
        return orderDao.buyGroup(lgTourOrder);
    }

    @Override
    public void updateBook(LgTourOrder lgTourOrder) {
        orderDao.updateBook(lgTourOrder);
    }

    @Override
    public void addInfo(LgTourOrderPersonalInformation information) {
        orderDao.addInfo(information);
    }


}
