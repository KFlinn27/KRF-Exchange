package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcUserDao implements UserDao {

    private static final BigDecimal STARTING_BALANCE = new BigDecimal("1000.00");
    private final JdbcTemplate jdbcTemplate;

    public JdbcUserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int findIdByUsername(String username) {
        if (username == null) throw new IllegalArgumentException("Username cannot be null");

        int userId;
        try {
            userId = jdbcTemplate.queryForObject("SELECT user_id FROM tenmo_user WHERE username = ?", int.class, username);
        } catch (NullPointerException | EmptyResultDataAccessException e) {
            throw new UsernameNotFoundException("User " + username + " was not found.");
        }
        return userId;
    }

    @Override
    public User getUserById(int userId) {
        String sql = "SELECT user_id, username, password_hash FROM tenmo_user WHERE user_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
        if (results.next()) {
            return mapRowToUser(results);
        } else {
            return null;
        }
    }

    @Override
    public List<User> findAll(String username) {
        List<User> users = new ArrayList<>();
        //dont select our user
        String sql = "SELECT user_id, username, password_hash FROM tenmo_user";
//        String sql = "SELECT user_id, username, password_hash FROM tenmo_user WHERE username != ?";

        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
        while (results.next()) {
            User user = mapRowToUser(results);
            //find our user and dont add
            if(!user.getUsername().equals(username)){
                users.add(user);
            }
        }

        return users;
    }

    @Override
    public User findByUsername(String username) {
        if (username == null) throw new IllegalArgumentException("Username cannot be null");

        String sql = "SELECT user_id, username, password_hash FROM tenmo_user WHERE username = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, username);
        if (rowSet.next()) {
            return mapRowToUser(rowSet);
        }
        throw new UsernameNotFoundException("User " + username + " was not found.");
    }

    @Override
    public boolean create(String username, String password) {

        // create user
        String sql = "INSERT INTO tenmo_user (username, password_hash) VALUES (?, ?) RETURNING user_id";
        String password_hash = new BCryptPasswordEncoder().encode(password);
        Integer newUserId;
        newUserId = jdbcTemplate.queryForObject(sql, Integer.class, username, password_hash);

        if (newUserId == null) return false;

        // create account
        sql = "INSERT INTO account (user_id, balance) values(?, ?)";
        try {
            jdbcTemplate.update(sql, newUserId, STARTING_BALANCE);
        } catch (DataAccessException e) {
            return false;
        }

        return true;
    }

    @Override
    public boolean withdraw(int id, BigDecimal balance){
        String sql = "UPDATE account SET balance = balance - ? WHERE user_id = ?;";
        try{
            jdbcTemplate.update(sql, balance, id);
        } catch (DataAccessException e){
            return false;
        }
        return true;
    }

    @Override
    public boolean deposit(int id, BigDecimal balance){
        String sql = "UPDATE account SET balance = balance + ? WHERE user_id = ?;";
        try{
            jdbcTemplate.update(sql, balance, id);
        } catch (DataAccessException e){
            return false;
        }
        return true;
    }

    @Override
    public int getUserIdWithAccountId(int accountId) {
        String sql = "SELECT user_id FROM account WHERE account_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId);
        if(results.next()){
            return results.getInt("user_id");
        }
        return 0;
    }

    @Override
    public int getAccountIdWithUserId(int userId) {
        String sql = "SELECT account_id FROM account WHERE account.user_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
        if(results.next()){
            return results.getInt("account_id");
        }
        return 0;
    }

    @Override
    public BigDecimal getBalance(String username){
        String sql = "SELECT balance FROM account JOIN tenmo_user ON account.user_id = tenmo_user.user_id WHERE tenmo_user.username = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, username);
        if(results.next()){
            return results.getBigDecimal("balance");
        }
        return null;
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
        int accIdToSearch = getAccountIdWithUserId(idByUsername);
        String sql = "SELECT * FROM (\n" +
                "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount, username\n" +
                "FROM transfer\n" +
                "JOIN account ON transfer.account_from = account.account_id\n" +
                "JOIN tenmo_user ON account.user_id = tenmo_user.user_id\n" +
                "WHERE account_to = ?\n" +
                "\n" +
                "UNION\n" +
                "\n" +
                "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount, username\n" +
                "FROM transfer\n" +
                "JOIN account ON transfer.account_to = account.account_id\n" +
                "JOIN tenmo_user ON account.user_id = tenmo_user.user_id\n" +
                "WHERE account_from = ?\n" +
                ")\n" +
                "as t order by transfer_id;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accIdToSearch, accIdToSearch);
        while(results.next()){
            transfers.add(mapRowToTransfer(results));
        }
        return transfers;
    }


    @Override
    public Transfer transferByID(int transferId, int userId) {
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount, username\n" +
                "FROM transfer \n" +
                "JOIN account ON transfer.account_from = account.account_id\n" +
                "JOIN tenmo_user ON account.user_id = tenmo_user.user_id\n" +
                "WHERE account_to = ? AND transfer_id = ?\n" +
                "\n" +
                "UNION\n" +
                "\n" +
                "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount, username\n" +
                "FROM transfer \n" +
                "JOIN account ON transfer.account_to = account.account_id\n" +
                "JOIN tenmo_user ON account.user_id = tenmo_user.user_id\n" +
                "WHERE account_from = ? AND transfer_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId, transferId, userId, transferId);
        if(results.next()){
            return mapRowToTransfer(results);
        }
        return null;
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
        transfer.setUsername(row.getString("username"));
        return transfer;
    }

    private User mapRowToUser(SqlRowSet rs) {
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password_hash"));
        user.setActivated(true);
        user.setAuthorities("USER");
        return user;
    }
}
