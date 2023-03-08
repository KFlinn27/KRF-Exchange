package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface UserDao {

    List<User> findAll();

    User getUserById(int id);

    User findByUsername(String username);

    int findIdByUsername(String username);

    boolean create(String username, String password);

    BigDecimal getBalance(String username);

    boolean withdraw(int id, BigDecimal balance);

    boolean deposit(int id, BigDecimal balance);

    boolean addTransfer(int type, int status, int senderId, int receiverId, BigDecimal amount);
}
