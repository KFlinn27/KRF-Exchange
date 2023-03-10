package com.techelevator.dao;

import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.dao.JdbcTransferDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

public class JdbcAccountDaoTests extends BaseDaoTests{

    private JdbcAccountDao sut;

    @Before
    public void setup(){
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        sut = new JdbcAccountDao(jdbcTemplate);
    }

    @Test
    public void getBalance_on_valid_username_expect_1000(){
        bigDecimalSame(BigDecimal.valueOf(1000), sut.getBalance("user1"));
        bigDecimalSame(BigDecimal.valueOf(1000), sut.getBalance("user3"));
    }

    @Test
    public void getBalance_on_invalid_username_expect_null(){
        Assert.assertNull(sut.getBalance("user6"));
    }

    @Test
    public void withdraw_removes_amount_from_correct_id(){
            sut.withdraw(1001, BigDecimal.valueOf(500));
            bigDecimalSame(BigDecimal.valueOf(500), sut.getBalance("user1"));
    }

    @Test
    public void deposit_adds_amount_to_correct_id(){
        sut.deposit(1001, BigDecimal.valueOf(500));
        bigDecimalSame(BigDecimal.valueOf(1500), sut.getBalance("user1"));
    }

    @Test
    public void getUserIdWithAccountId_returns_correct_id_with_valid(){
        Assert.assertEquals(1001, sut.getUserIdWithAccountId(2001));
        Assert.assertEquals(1002, sut.getUserIdWithAccountId(2002));
    }

    @Test
    public void getUserIdWithAccountId_returns_0_with_invalid(){
        Assert.assertEquals(0, sut.getUserIdWithAccountId(20015));
    }

    @Test
    public void getAccountIdWithUserId_returns_correct_id_with_valid(){
        Assert.assertEquals(2001, sut.getAccountIdWithUserId(1001));
        Assert.assertEquals(2002, sut.getAccountIdWithUserId(1002));
    }
    @Test
    public void getAccountIdWithUserId_returns_0_with_invalid(){
        Assert.assertEquals(0, sut.getAccountIdWithUserId(10501));
    }



    public void bigDecimalSame(BigDecimal expected, BigDecimal actual){
        double expect = expected.doubleValue();
        double actualNumber = actual.doubleValue();
        Assert.assertEquals(expect, actualNumber, .000001);
    }
}
