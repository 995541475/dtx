package cn.itcast.dtx.dao;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

/**
 * @author hl
 * @version 1.0
 * @description
 * @date 2023/8/27 16:09
 */
@Mapper
@Component
public interface AccountInfoDao {

    @Update("update account_info set account_balance=account_balance-#{amount} where account_no=#{accountNo}")
    int doUpdateAccountBalance(@Param("accountNo") String accountNo, @Param("amount") double amount);


    @Insert("insert into de_duplication values (#{txNo},now())")
    int addTx(String txNo);

    @Select("select count(1) from de_duplication where tx_no = #{txNo}")
    int isExistTx(String txNo);
}
