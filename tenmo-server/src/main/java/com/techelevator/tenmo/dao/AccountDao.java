package com.techelevator.tenmo.dao;

import java.math.BigDecimal;

public interface AccountDao {

    BigDecimal getBalance(String username);

    boolean withdraw(int id, BigDecimal balance);

    boolean deposit(int id, BigDecimal balance);

    int getAccountIdWithUserId(int userId);

    int getUserIdWithAccountId(int accountId);
}
