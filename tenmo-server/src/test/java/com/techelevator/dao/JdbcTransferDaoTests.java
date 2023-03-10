package com.techelevator.dao;

import com.techelevator.tenmo.dao.JdbcTransferDao;
import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.model.Transfer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

public class JdbcTransferDaoTests extends BaseDaoTests{

//    INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount)
//    VALUES (1, 1, 2001, 2002, 111.11);
//    INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount)
//    VALUES (2, 2, 2002, 2001, 10);
//    INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount)
//    VALUES (1, 2, 2003, 2002, 500);
//    INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount)
//    VALUES (1, 3, 2003, 2001, 76.01);
    protected static final Transfer TRANSFER_1 = new Transfer(3001, 1, 1, 2001, 2002, BigDecimal.valueOf(111.11), "user1");
    protected static final Transfer TRANSFER_2 = new Transfer(3002, 2, 2, 2002, 2001, BigDecimal.valueOf(10), "user1");
    protected static final Transfer TRANSFER_3 = new Transfer(3003, 1, 2, 2003, 2002, BigDecimal.valueOf(500), "user3");
    protected static final Transfer TRANSFER_4 = new Transfer(3004, 1, 3, 2003, 2001, BigDecimal.valueOf(76.01), "user3");

    private JdbcTransferDao sut;
    private Transfer transfer;

    @Before
    public void setup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        sut = new JdbcTransferDao(jdbcTemplate);
        transfer = new Transfer(3005, 1, 2, 2001, 2002, BigDecimal.valueOf(100), "user1");
    }

    @Test
    public void addTransfer_with_null_value(){
        Assert.assertFalse(sut.addTransfer(transfer.getTypeId(), transfer.getStatusId(), transfer.getAccountFrom(), transfer.getAccountTo(), null));
    }

    @Test
    public void addTransfer_with_negative_value(){
        
    }

}
