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
    public List<User> listUsers(){
        return userDao.findAll();
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

    @RequestMapping(path = "transfers/", method = RequestMethod.GET)
    public List<Transfer> getTransferForUser(Principal principal){
        return userDao.listTransfersForUser(userDao.findIdByUsername(principal.getName()));
    }

    @RequestMapping(path = "transfer/{id}", method = RequestMethod.GET)
    public Transfer getTransferByID(@PathVariable int id){
        return userDao.transferByID(id);
    }
}
