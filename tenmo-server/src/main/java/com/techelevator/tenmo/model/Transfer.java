package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class Transfer {
    private int transferId;
    private int typeId;
    private int statusId;
    private int accountFrom;
    private int accountTo;
    private BigDecimal amount;
    private String userNotLoggedIn;

    public Transfer(){}

    public Transfer(int id, int typeId, int statusId, int accountFrom, int accountTo, BigDecimal amount, String userNotLoggedIn){
        this.transferId = id;
        this.typeId = typeId;
        this.statusId = statusId;
        this.accountFrom = accountFrom;
        this.accountTo = accountTo;
        this.userNotLoggedIn = userNotLoggedIn;
        this.amount = amount;
    }

    public String getUserNotLoggedIn() {
        return userNotLoggedIn;
    }

    public void setUserNotLoggedIn(String userNotLoggedIn) {
        this.userNotLoggedIn = userNotLoggedIn;
    }

    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public int getAccountFrom() {
        return accountFrom;
    }

    public void setAccountFrom(int accountFrom) {
        this.accountFrom = accountFrom;
    }

    public int getAccountTo() {
        return accountTo;
    }

    public void setAccountTo(int accountTo) {
        this.accountTo = accountTo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
