package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao{

    private final JdbcTemplate jdbcTemplate;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean addTransfer(int type, int status, int senderId, int receiverId, BigDecimal amount) {
        String sql = "INSERT INTO transfer(transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (?, ?, ?, ?, ?);";
        try{
            jdbcTemplate.update(sql, type, status, senderId, receiverId, amount);
            return true;
        } catch (DataAccessException e) {
            return false;
        }
    }

    @Override
    public List<Transfer> listTransfersForUser(int idByUsername) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT * FROM (\n" +
                "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount, username\n" +
                "FROM transfer\n" +
                "JOIN account ON transfer.account_from = account.account_id\n" +
                "JOIN tenmo_user ON account.user_id = tenmo_user.user_id\n" +
                "WHERE account_to = ? AND transfer.transfer_status_id != 1\n" +
                "\n" +
                "UNION\n" +
                "\n" +
                "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount, username\n" +
                "FROM transfer\n" +
                "JOIN account ON transfer.account_to = account.account_id\n" +
                "JOIN tenmo_user ON account.user_id = tenmo_user.user_id\n" +
                "WHERE account_from = ? AND transfer.transfer_status_id != 1\n" +
                ")\n" +
                "as t order by transfer_id;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, idByUsername, idByUsername);
        while(results.next()){
            transfers.add(mapRowToTransfer(results));
        }
        return transfers;
    }

    @Override
    public List<Transfer> listPendingTransfersForUser(int idByUsername) {
        List<Transfer> pendingTransfers = new ArrayList<>();
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount, username " +
                "FROM transfer " +
                "JOIN account ON transfer.account_to = account.account_id " +
                "JOIN tenmo_user ON account.user_id = tenmo_user.user_id " +
                "WHERE account_from = ? AND transfer_type_id = 1 AND transfer_status_id = 1;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, idByUsername);
        while(results.next()){
            pendingTransfers.add(mapRowToTransfer(results));
        }
        return pendingTransfers;
    }

    @Override
    public Transfer getTransferById(int id) {
        Transfer transfer = null;
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount, username " +
                "FROM transfer " +
                "JOIN account ON account.account_id = transfer.account_to " +
                "JOIN tenmo_user ON account.user_id = tenmo_user.user_id " +
                "WHERE transfer_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id);
        if(results.next()){
            transfer = mapRowToTransfer(results);
        }
        return transfer;
    }

    @Override
    public void approvePendingTransfer(int id) {
        String sql = "UPDATE transfer SET transfer_status_id = 2 WHERE transfer_id = ?;";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void rejectPendingTransfer(int id) {
        String sql = "UPDATE transfer SET transfer_status_id = 3 WHERE transfer_id = ?;";
        jdbcTemplate.update(sql, id);
    }

    private Transfer mapRowToTransfer(SqlRowSet row){
        Transfer transfer = new Transfer();
        transfer.setTransferId(row.getInt("transfer_id"));
        transfer.setTypeId(row.getInt("transfer_type_id"));
        transfer.setStatusId(row.getInt("transfer_status_id"));
        transfer.setAccountFrom(row.getInt("account_from"));
        transfer.setAccountTo(row.getInt("account_to"));
        transfer.setAmount(row.getBigDecimal("amount"));
        transfer.setUserNotLoggedIn(row.getString("username"));
        return transfer;
    }

}
