package cn.itcast.dtx.controller;

import cn.itcast.dtx.model.AccountChangeEvent;
import cn.itcast.dtx.service.AccountInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author hl
 * @version 1.0
 * @description
 * @date 2023/8/27 17:05
 */
@RestController
@Slf4j
public class AccountInfoController {
    @Autowired
    private AccountInfoService accountInfoService;

    @GetMapping("/transfer")
    public String transfer(@RequestParam("accountNo") String accountNo , @RequestParam("amount") Double amount){
        //创建一个事务id，作为消息内容发到mq
        String tx_no = UUID.randomUUID().toString();
        AccountChangeEvent accountChangeEvent = new AccountChangeEvent(accountNo,amount,tx_no);
        //发送消息
        accountInfoService.sendUpdateAccountBalance(accountChangeEvent);
        return "转账成功";

    }


}
