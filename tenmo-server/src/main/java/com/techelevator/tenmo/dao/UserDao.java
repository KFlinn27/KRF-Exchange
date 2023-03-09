package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
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

    List<Transfer> listTransfersForUser(int idByUsername);

    Transfer transferByID(int transferId, int userId);

    int getAccountIdWithUserId(int userId);

    List<Transfer> listPendingTransfersForUser(int idByUsername);

    Transfer getTransferById(int id);

    int getUserIdWithAccountId(int accountId);

    void approvePendingTransfer(int id);

    void rejectPendingTransfer(int id);
}
