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
import java.util.ArrayList;
import java.util.List;

public class JdbcTransferDaoTests extends BaseDaoTests{

//    INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount)
//    VALUES (1, 1, 2001, 2002, 111.11);
//    INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount)
//    VALUES (2, 2, 2002, 2001, 10);
//    INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount)
//    VALUES (1, 2, 2003, 2002, 500);
//    INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount)
//    VALUES (1, 3, 2003, 2001, 76.01);
    protected static final Transfer TRANSFER_1 = new Transfer(3001, 1, 1, 2001, 2002, BigDecimal.valueOf(111.11), "user2");
    protected static final Transfer TRANSFER_2 = new Transfer(3002, 2, 2, 2002, 2001, BigDecimal.valueOf(10.00), "user1");
    protected static final Transfer TRANSFER_3 = new Transfer(3003, 1, 2, 2003, 2002, BigDecimal.valueOf(500.00), "user3");
    protected static final Transfer TRANSFER_4 = new Transfer(3004, 1, 3, 2003, 2001, BigDecimal.valueOf(76.01), "user3");

    private JdbcTransferDao sut;
    private Transfer transfer;

    @Before
    public void setup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        sut = new JdbcTransferDao(jdbcTemplate);
        transfer = new Transfer(3005, 1, 2, 2001, 2002, BigDecimal.valueOf(100.00), "user1");
    }

    @Test
    public void addTransfer_with_null_value(){
        Assert.assertFalse(sut.addTransfer(transfer.getTypeId(), transfer.getStatusId(), transfer.getAccountFrom(), transfer.getAccountTo(), null));
    }

    @Test
    public void addTransfer_with_negative_value(){
        Assert.assertFalse(sut.addTransfer(transfer.getTypeId(), transfer.getStatusId(), transfer.getAccountFrom(), transfer.getAccountTo(), BigDecimal.valueOf(-100)));
    }

    @Test
    public void addTransfer_with_positive_value(){
        Assert.assertTrue(sut.addTransfer(transfer.getTypeId(), transfer.getStatusId(), transfer.getAccountFrom(), transfer.getAccountTo(), transfer.getAmount()));
    }

    @Test
    public void listTransfersForUser_where_user_valid_expect_2(){
        List<Transfer> transfers = sut.listTransfersForUser(2002);
        Assert.assertEquals(2, transfers.size());
        transferEquals(transfers.get(0), TRANSFER_2);
        transferEquals(transfers.get(1), TRANSFER_3);
    }

    @Test
    public void listTransfersForUser_where_user_invalid_expect_0(){
        List<Transfer> transfers = sut.listTransfersForUser(2006);
        Assert.assertEquals(0, transfers.size());
    }

    @Test
    public void listPendingTransfersForUser_where_user_valid_expect_1(){
        List<Transfer> transfers = sut.listPendingTransfersForUser(2001);
        Assert.assertEquals(1, transfers.size());
        transferEquals(transfers.get(0), TRANSFER_1);
    }

    @Test
    public void listPendingTransfersForUser_where_user_invalid_expect_0(){
        List<Transfer> transfers = sut.listPendingTransfersForUser(2002);
        Assert.assertEquals(0, transfers.size());
    }

    @Test
    public void getTransferById_returns_correct_transfer_with_valid_id(){
        transferEquals(TRANSFER_1, sut.getTransferById(3001));
        transferEquals(TRANSFER_2, sut.getTransferById(3002));
    }

    @Test
    public void getTransferById_returns_null_with_invalid_id(){
        Assert.assertNull(sut.getTransferById(101));
    }

    @Test public void approvePendingTransfer_changes_pending_to_approved(){
        Transfer holder = new Transfer(3001, 1, 2, 2001, 2002, BigDecimal.valueOf(111.11), "user2");
        sut.approvePendingTransfer(3001);
        transferEquals(holder, sut.getTransferById(3001));
    }

    @Test public void rejectPendingTransfer_changes_pending_to_approved(){
        Transfer holder = new Transfer(3001, 1, 3, 2001, 2002, BigDecimal.valueOf(111.11), "user2");
        sut.rejectPendingTransfer(3001);
        transferEquals(holder, sut.getTransferById(3001));
    }




    private void transferEquals(Transfer expected, Transfer actual) {
        Assert.assertEquals(expected.getTransferId(), actual.getTransferId());
        Assert.assertEquals(expected.getUsername(), actual.getUsername());
        double expectedAmount = expected.getAmount().doubleValue();
        double actualAmount = actual.getAmount().doubleValue();
        Assert.assertEquals(expectedAmount, actualAmount, .0000001);
        Assert.assertEquals(expected.getAccountTo(), actual.getAccountTo());
        Assert.assertEquals(expected.getAccountFrom(), actual.getAccountFrom());
        Assert.assertEquals(expected.getStatusId(), actual.getStatusId());
        Assert.assertEquals(expected.getTypeId(), actual.getTypeId());
    }

}
