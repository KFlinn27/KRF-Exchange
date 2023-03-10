package com.techelevator.tenmo.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class JdbcAccountDao implements AccountDao{

    private final JdbcTemplate jdbcTemplate;

    public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean withdraw(int id, BigDecimal balance){
        String sql = "UPDATE account SET balance = balance - ? WHERE user_id = ?;";
        try{
            jdbcTemplate.update(sql, balance, id);
            return true;
        } catch (DataAccessException e){
            return false;
        }

    }

    @Override
    public boolean deposit(int id, BigDecimal balance){
        String sql = "UPDATE account SET balance = balance + ? WHERE user_id = ?;";
        try{
            jdbcTemplate.update(sql, balance, id);
            return true;
        } catch (DataAccessException e){
            return false;
        }
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


}
