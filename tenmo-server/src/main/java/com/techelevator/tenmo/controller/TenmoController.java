package com.techelevator.tenmo.controller;

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

    public TenmoController(UserDao userDao){
        this.userDao = userDao;
    }

    @RequestMapping(path = "balance/", method = RequestMethod.GET)
    public BigDecimal getBalance(Principal principal){
        return userDao.getBalance(principal.getName());
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<User> listUsers(Principal principal){
        return userDao.findAll(principal.getName());
    }

    @RequestMapping(path = "accountId/", method = RequestMethod.GET)
    public Integer getAccIdWithUserID(Principal principal){
        return userDao.getAccountIdWithUserId(userDao.findIdByUsername(principal.getName()));
    }

    @RequestMapping(path = "send/{receiverUserId}", method = RequestMethod.PUT)
    public boolean sendFunds(@PathVariable int receiverUserId, @RequestBody AmountDto amountSent, Principal principal){
        BigDecimal amount = amountSent.getAmount();
        int senderUserId = userDao.findIdByUsername(principal.getName());
        int senderId = userDao.getAccountIdWithUserId(senderUserId);
        int receiverId = userDao.getAccountIdWithUserId(receiverUserId);
        boolean hasFunds = userDao.getBalance(principal.getName()).compareTo(amount) >= 0;
        if(hasFunds && userDao.deposit(receiverUserId, amount) && userDao.withdraw(senderUserId, amount)){
            if(userDao.addTransfer(2, 2, senderId, receiverId, amount)){
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
        int receiverId = userDao.getAccountIdWithUserId(receiverUserId);
        int senderId = userDao.getAccountIdWithUserId(senderUserId);
        if(userDao.addTransfer(1, 1, senderId, receiverId, amount)){
            return true;
        }
        return false;
    }

    @RequestMapping(path = "transfers/", method = RequestMethod.GET)
    public List<Transfer> getTransferForUser(Principal principal){
        return userDao.listTransfersForUser(userDao.findIdByUsername(principal.getName()));
    }


    @RequestMapping(path = "transfers/pending", method = RequestMethod.GET)
    public List<Transfer> getPendingTransfersForUser(Principal principal){
        return userDao.listPendingTransfersForUser(userDao.getAccountIdWithUserId(userDao.findIdByUsername(principal.getName())));
    }

    @RequestMapping(path = "transfer/{id}", method = RequestMethod.GET)
    public Transfer getTransferByID(@PathVariable int id, Principal principal){
        return userDao.transferByID(id, userDao.findIdByUsername(principal.getName()));
    }

    //User accepts or denies a pending transfer if denied change transfer status to rejected if accepted need to
    //make sure money can be sent back and forth, if money can be sent we approve or reject it again
    @RequestMapping(path = "transfer/{id}/accept", method = RequestMethod.PUT)
    public boolean acceptPendingTransfers(@PathVariable int id){
        Transfer transfer = userDao.getTransferById(id);
        int senderUserId = userDao.getUserIdWithAccountId(transfer.getAccountFrom());
        int receiverUserId = userDao.getUserIdWithAccountId(transfer.getAccountTo());
        if(senderUserId == 0 || receiverUserId == 0) return false;
        if(userDao.deposit(receiverUserId, transfer.getAmount()) && userDao.withdraw(senderUserId, transfer.getAmount())){
            userDao.approvePendingTransfer(id);
            return true;
            }
        return false;
    }

    @RequestMapping(path = "transfer/{id}/reject", method = RequestMethod.PUT)
    public void rejectPendingTransfers(@PathVariable int id){
        userDao.rejectPendingTransfer(id);
    }


}
