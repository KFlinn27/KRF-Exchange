package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDao {

    boolean addTransfer(int type, int status, int senderId, int receiverId, BigDecimal amount);

    List<Transfer> listTransfersForUser(int idByUsername);

    Transfer transferByID(int transferId, int userId);

    List<Transfer> listPendingTransfersForUser(int idByUsername);

    Transfer getTransferById(int id);

    void approvePendingTransfer(int id);

    void rejectPendingTransfer(int id);

}
