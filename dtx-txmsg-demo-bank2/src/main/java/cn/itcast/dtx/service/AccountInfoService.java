package cn.itcast.dtx.service;

import cn.itcast.dtx.model.AccountChangeEvent;

/**
 * @author hl
 * @version 1.0
 * @description
 * @date 2023/8/27 16:59
 */
public interface AccountInfoService {
    //更新账户，增加金额
    void addAccountInfoBalance(AccountChangeEvent accountChangeEvent);
}
