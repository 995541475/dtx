package cn.itcast.dtx.service.impl;

import cn.itcast.dtx.dao.AccountInfoDao;
import cn.itcast.dtx.model.AccountChangeEvent;
import cn.itcast.dtx.service.AccountInfoService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hl
 * @version 1.0
 * @description
 * @date 2023/8/27 16:02
 */
@Service
@Slf4j
public class AccountInfoServiceImpl implements AccountInfoService {
    @Autowired
    private AccountInfoDao accountInfoDao;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    //向mq发送转账消息
    @Override
    public void sendUpdateAccountBalance(AccountChangeEvent accountChangeEvent) {
        //将accountChangeEvent转成json
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("accountChange",accountChangeEvent);
        String jsonString = jsonObject.toJSONString();
        //生成message类型
        Message<String> message = MessageBuilder.withPayload(jsonString).build();
        //发送一条事务消息
        /**
         * String destination topic，
         * Message<?> message, 消息内容
         * Object arg 参数
         */
        rocketMQTemplate.sendMessageInTransaction("topic_txmsg",message,null);
    }

    //更新账户，扣减金额
    @Override
    @Transactional
    public void doUpdateAccountBalance(AccountChangeEvent accountChangeEvent) {
        if (accountInfoDao.isExistTx(accountChangeEvent.getTxNo()) > 0){
            return;
        }
        //扣减金额
        accountInfoDao.doUpdateAccountBalance(accountChangeEvent.getAccountNo(),accountChangeEvent.getAmount());
        //添加事务日志
        accountInfoDao.addTx(accountChangeEvent.getTxNo());
        //手动设置抛出异常用于测试
        if (accountChangeEvent.getAmount() == 3){
            throw new RuntimeException("张三这边--人为制造异常");
        }
    }
}
