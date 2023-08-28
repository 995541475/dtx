package cn.itcast.dtx.message;

import cn.itcast.dtx.dao.AccountInfoDao;
import cn.itcast.dtx.model.AccountChangeEvent;
import cn.itcast.dtx.service.AccountInfoService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hl
 * @version 1.0
 * @description
 * @date 2023/8/27 10:33
 */
/**
 * 由于在Springboot rocket mq start 2.1.0版本之后 @RocketMQTransactionListener移出了txProducerGroup属性
 * 所以如果存在多个不同事务，需要从代码层面来区分。（之前是一个Listener对应一个）
 */
@Slf4j
@RocketMQTransactionListener
public class ProducerTxmsgListener implements RocketMQLocalTransactionListener {

    @Autowired
    private AccountInfoService accountInfoService;
    @Autowired
    private AccountInfoDao accountInfoDao;

    //消息发送后的回调方法，当消息发送给mq成功，此方法被回调
    @Override
    @Transactional
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        try {
            String messageString = new String((byte[]) message.getPayload());
            JSONObject jsonObject = JSONObject.parseObject(messageString);
            String accountChangeString = jsonObject.getString("accountChange");
            //将accountChange（json）转成AccountChangeEvent
            AccountChangeEvent accountChangeEvent = JSONObject.parseObject(accountChangeString, AccountChangeEvent.class);
            //执行本地事务，扣减金额
            accountInfoService.doUpdateAccountBalance(accountChangeEvent);
            //当返回RocketMQLocalTransactionState.COMMIT，自动向mq发送commit消息，mq将消息的状态改为可消费
            return RocketMQLocalTransactionState.COMMIT;
        }catch (Exception e){
            e.printStackTrace();
            return RocketMQLocalTransactionState.ROLLBACK;
        }


    }

    //事务状态回查，查询是否扣减金额
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        //解析message，转成AccountChangeEvent
        String messageString = new String((byte[]) message.getPayload());
        JSONObject jsonObject = JSONObject.parseObject(messageString);
        String accountChangeString = jsonObject.getString("accountChange");
        AccountChangeEvent accountChangeEvent = JSONObject.parseObject(accountChangeString, AccountChangeEvent.class);
        //事务id
        String txNo = accountChangeEvent.getTxNo();
        int existTx = accountInfoDao.isExistTx(txNo);
        if (existTx > 0){
            return RocketMQLocalTransactionState.COMMIT;
        }else{
            return RocketMQLocalTransactionState.UNKNOWN;
        }
    }
}
