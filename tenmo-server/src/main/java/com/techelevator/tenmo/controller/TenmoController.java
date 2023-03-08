package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @RequestMapping(path = "send/{receiverId}?amount=", method = RequestMethod.PUT)
    public boolean sendFunds(@PathVariable int receiverId, @RequestParam BigDecimal amount, Principal principal){
        int senderId = userDao.findIdByUsername(principal.getName());
        boolean hasFunds = userDao.getBalance(principal.getName()).compareTo(amount) >= 0;
        if(hasFunds && userDao.deposit(receiverId, amount) && userDao.withdraw(senderId, amount)){
            userDao.addTransfer(2, 2, senderId, receiverId, amount);
            return true;
        } else {
            return false;
        }
    }
}
