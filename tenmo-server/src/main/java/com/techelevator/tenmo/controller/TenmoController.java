package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.AmountDto;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.awt.datatransfer.Transferable;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping(path = "user/")
public class TenmoController {

    private UserDao userDao;
    private AccountDao accountDao;
    private TransferDao transferDao;

    public TenmoController(UserDao userDao, AccountDao accountDao, TransferDao transferDao){
        this.userDao = userDao;
        this.accountDao = accountDao;
        this.transferDao = transferDao;
    }

    @RequestMapping(path = "balance/", method = RequestMethod.GET)
    public BigDecimal getBalance(Principal principal){
        return accountDao.getBalance(principal.getName());
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<User> listUsers(Principal principal){
        return userDao.findAll(principal.getName());
    }

    @RequestMapping(path = "accountId/", method = RequestMethod.GET)
    public Integer getAccIdWithUserID(Principal principal){
        return accountDao.getAccountIdWithUserId(userDao.findIdByUsername(principal.getName()));
    }

    @RequestMapping(path = "send/{receiverUserId}", method = RequestMethod.PUT)
    public boolean sendFunds(@PathVariable int receiverUserId, @RequestBody AmountDto amountSent, Principal principal){
        BigDecimal amount = amountSent.getAmount();
        int senderUserId = userDao.findIdByUsername(principal.getName());
        int senderId = accountDao.getAccountIdWithUserId(senderUserId);
        int receiverId = accountDao.getAccountIdWithUserId(receiverUserId);
        boolean hasFunds = accountDao.getBalance(principal.getName()).compareTo(amount) >= 0;
        if(hasFunds && accountDao.deposit(receiverUserId, amount) && accountDao.withdraw(senderUserId, amount)){
            if(transferDao.addTransfer(2, 2, senderId, receiverId, amount)){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @RequestMapping(path = "request/{senderUserId}", method = RequestMethod.POST)
    public boolean requestFunds(@PathVariable int senderUserId, @RequestBody AmountDto amountRequested, Principal principal){
        BigDecimal amount = amountRequested.getAmount();
        int receiverUserId = userDao.findIdByUsername(principal.getName());
        int receiverId = accountDao.getAccountIdWithUserId(receiverUserId);
        int senderId = accountDao.getAccountIdWithUserId(senderUserId);
        if(transferDao.addTransfer(1, 1, senderId, receiverId, amount)){
            return true;
        }
        return false;
    }

    @RequestMapping(path = "transfers/", method = RequestMethod.GET)
    public List<Transfer> getTransferForUser(Principal principal){
        return transferDao.listTransfersForUser(accountDao.getAccountIdWithUserId(userDao.findIdByUsername(principal.getName())));
    }


    @RequestMapping(path = "transfers/pending", method = RequestMethod.GET)
    public List<Transfer> getPendingTransfersForUser(Principal principal){
        return transferDao.listPendingTransfersForUser(accountDao.getAccountIdWithUserId(userDao.findIdByUsername(principal.getName())));
    }

    @RequestMapping(path = "transfer/{id}/accept", method = RequestMethod.PUT)
    public boolean acceptPendingTransfers(@PathVariable int id){
        Transfer transfer = transferDao.getTransferById(id);
        int senderUserId = accountDao.getUserIdWithAccountId(transfer.getAccountFrom());
        int receiverUserId = accountDao.getUserIdWithAccountId(transfer.getAccountTo());
        if(senderUserId == 0 || receiverUserId == 0) return false;
        if(accountDao.deposit(receiverUserId, transfer.getAmount()) && accountDao.withdraw(senderUserId, transfer.getAmount())){
            transferDao.approvePendingTransfer(id);
            return true;
            }
        return false;
    }

    @RequestMapping(path = "transfer/{id}/reject", method = RequestMethod.PUT)
    public void rejectPendingTransfers(@PathVariable int id){
        transferDao.rejectPendingTransfer(id);
    }


}
