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
        this.amount = amount;
        this.userNotLoggedIn = userNotLoggedIn;
    }

    public String getTypeString(){
        if(this.typeId == 1){
            return "Request";
        } else if(this.typeId == 2){
            return "Send";
        } else {
            return "Find out how this message even appeared.";
        }
    }

    @Override
    public String toString(){
        return transferId + "      " + userNotLoggedIn + "        " + amount;
    }

    public String getUserNotLoggedIn() {
        return userNotLoggedIn;
    }

    public int getTransferId() {
        return transferId;
    }
    public int getTypeId() {
        return typeId;
    }
    public int getStatusId() {
        return statusId;
    }

    public String statusType(){
        switch(this.statusId){
            case 1: return "Pending";
            case 2: return "Approved";
            case 3: return "Rejected";
            default: {
                return "Unavailable";
            }
        }
    }

    public int getAccountFrom() {
        return accountFrom;
    }

    public int getAccountTo() {
        return accountTo;
    }
    public BigDecimal getAmount() {
        return amount;
    }
}
