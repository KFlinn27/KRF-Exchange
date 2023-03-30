package com.techelevator.tenmo.services;


import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserCredentials;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class ConsoleService {
    private final String LINE_BREAK = "------------------------------------------------------------------------------";

    private final Scanner scanner = new Scanner(System.in);

    public int promptForMenuSelection(String prompt) {
        int menuSelection;
        System.out.print(prompt);
        try {
            menuSelection = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            menuSelection = -1;
        }
        return menuSelection;
    }

    public void printGreeting() {
        System.out.println("*********************");
        System.out.println("* Welcome to TEnmo! *");
        System.out.println("*********************");
    }

    public void printLoginMenu() {
        System.out.println();
        System.out.println("1: Register");
        System.out.println("2: Login");
        System.out.println("0: Exit");
        System.out.println();
    }

    public void printMainMenu() {
        System.out.println();
        System.out.println("1: View your current balance");
        System.out.println("2: View your past transfers");
        System.out.println("3: View your pending requests");
        System.out.println("4: Send TE bucks");
        System.out.println("5: Request TE bucks");
        System.out.println("0: Exit");
        System.out.println();
    }

    public UserCredentials promptForCredentials() {
        String username = promptForString("Username: ");
        String password = promptForString("Password: ");
        return new UserCredentials(username, password);
    }

    public String promptForString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public int promptForInt(String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a number:");
            }
        }
    }

    public BigDecimal promptForBigDecimal(String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                return new BigDecimal(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a decimal number:");
            }
        }
    }

    public void pause() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public void printErrorMessage() {
        System.out.println("An error occurred. Check the log for details.");
    }

    public void listTransfers(List<Transfer> transfers, boolean pending, int userAccId) {
        System.out.println();
        printLineBreak();
        if(pending){
            System.out.println("Pending Transfers");
            System.out.printf("%-15s %-30s %-20s\n", "ID", "To", "Amount");
        } else{
            System.out.println("Transfers");
            System.out.printf("%-15s %-37s %-20s\n", "ID", "From/To", "Amount");
        }
        printLineBreak();
        for(Transfer current : transfers){
            if(pending) {
                System.out.printf("%-15s %-30s $ %-20s\n", current.getTransferId(), current.getUserNotLoggedIn(), current.getAmount());
            } else {
                String type;
                if(userAccId == current.getAccountTo()){
                    type = "From: ";
                } else {
                    type = "To: ";
                }
                String rejected = "";
                if(current.getStatusId() == 3) rejected = " (rejected)";
                System.out.printf("%-15s %-6s %-30s $ %-20s\n", current.getTransferId(), type, current.getUserNotLoggedIn(), current.getAmount() + rejected);
            }
        }
        printLineBreak();
        System.out.println();
    }

    public void printUsers(List<User> users) {
        System.out.println();
        printLineBreak();
        System.out.println("Users");
        System.out.printf("%-10s %-30s\n", "ID", "Name");
        printLineBreak();
        for(User current: users){
            System.out.printf("%-10s %-30s\n", current.getId(), current.getUsername());
        }
        printLineBreak();
        System.out.println();
    }

    public void printSuccessMessage(int sendToId, BigDecimal amount) {
        System.out.println("You sent $" + amount + " to " + sendToId + ".");
    }

    public void printFailMessage() {
        System.out.println("Your send request failed.");
    }

    public void printMessage(String msg){
        System.out.println();
        System.out.println(msg);
    }

    public void printInvalidId(String s) {
        System.out.println(s);
    }

    public void printNotEnoughFunds(String s) {
        System.out.println();
        System.out.println(s);
        System.out.println();
    }

    public void print1Or0Message(String s) {
        System.out.println(s);
    }

    public void printRequestMessage(int requestFromId, BigDecimal amount) {
        System.out.println("You're requesting $" + amount + " from " + requestFromId + ".");
    }

    public void printBalance(BigDecimal balance) {
        System.out.println();
        System.out.println("Your current balance is : $" + balance);
    }

    public void printLineBreak(){
        System.out.println(LINE_BREAK);
    }

    public void printTransfer(Transfer transferToPrint, String username, String status) {
        System.out.println();
        printLineBreak();
        System.out.println("Transfer Details");
        printLineBreak();
        System.out.println("ID: " + transferToPrint.getTransferId());
        System.out.println("From: " + username);
        System.out.println("To: " + transferToPrint.getUserNotLoggedIn());
        System.out.println("Type: " + transferToPrint.getTypeString());
        System.out.println("Status: " + status);
        System.out.println("Amount: $" + transferToPrint.getAmount());
        System.out.println();
    }

    public int promptTransferOptions() {
        return promptForInt("1: Approve\n" +
                "2: Reject\n" +
                "0: Don't approve or reject\n" +
                "---------\n" +
                "Please choose an option: ");
    }

}
