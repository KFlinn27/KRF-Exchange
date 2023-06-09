package com.techelevator.tenmo;

import com.techelevator.tenmo.model.Transfer;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserCredentials;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.TenmoService;

import java.math.BigDecimal;
import java.util.List;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private final TenmoService tenmoService = new TenmoService();

    private AuthenticatedUser currentUser;
    private int currentUserAccountID;

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            currentUserAccountID = tenmoService.getAccountID();
            mainMenu();
        }
    }

    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser != null) {
            tenmoService.setAuthToken(currentUser.getToken());
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                transferBucks(true);
            } else if (menuSelection == 5) {
                transferBucks(false);
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private void viewCurrentBalance() {
        BigDecimal balance = tenmoService.getBalance();
        consoleService.printBalance(balance);
    }

    private void viewTransferHistory() {
        List<Transfer> transfers = tenmoService.getTransfers();
        if (transfers.size() > 0) {
            consoleService.listTransfers(transfers, false, currentUserAccountID);
            int targetID = consoleService.promptForInt("Please enter the transfer ID for specific details. Enter 0 to exit display:");
            while (targetID != 0) {
                Transfer targetTransfer = retrieveTransfer(transfers, targetID);
                if (targetTransfer == null) {
                    consoleService.printInvalidId("Not a valid ID, please enter valid ID from list.");
                    consoleService.listTransfers(transfers, false, currentUserAccountID);
                    targetID = consoleService.promptForInt("Please enter transfer ID you would like to approve or reject (enter 0 to exit): ");
                } else {
                    consoleService.printTransfer(targetTransfer, targetTransfer.getUserNotLoggedIn(), targetTransfer.statusType());
                    break;
                }
            }


        } else {
            consoleService.printMessage("You have no transfer history on our platform.");
        }
    }

    private void viewPendingRequests() {
        List<Transfer> pendingTransfers = tenmoService.getPendingTransfers();
        if (pendingTransfers.size() > 0) {

            consoleService.listTransfers(pendingTransfers, true, 0);
            int id = consoleService.promptForInt("Please enter transfer ID you would like to approve or reject (enter 0 to exit): ");
            while (id != 0) {
                Transfer targetedTransfer = retrieveTransfer(pendingTransfers, id);
                if (targetedTransfer == null) {
                    consoleService.printInvalidId("Not a valid ID, please enter valid ID from list.");
                    consoleService.listTransfers(pendingTransfers, true, 0);
                    id = consoleService.promptForInt("Please enter transfer ID you would like to approve or reject (enter 0 to exit): ");
                } else {
                    BigDecimal balance = tenmoService.getBalance();
                    boolean hasEnoughMoney = depositLessThanBalance(targetedTransfer.getAmount(), balance);
                    int approve = consoleService.promptTransferOptions();
                    if (approve == 1 && hasEnoughMoney) {
                        if (tenmoService.acceptRequest(id)) {
                            consoleService.printTransfer(targetedTransfer, currentUsername(), "Approved");
                        } else {
                            consoleService.printFailMessage();
                        }
                        break;
                    } else if (approve == 2) {
                        tenmoService.rejectRequest(id);
                        consoleService.printTransfer(targetedTransfer, currentUsername(), "Rejected");
                        break;
                    } else if (approve == 1) {
                        consoleService.printNotEnoughFunds("Sorry, not enough money in account.");
                    } else if (approve == 0) {
                        break;
                    } else {
                        consoleService.print1Or0Message("Please only enter 0, 1 or 2.");
                    }
                }
            }
        } else {
            consoleService.printMessage("You have no pending transfers.");
        }
    }

    private Transfer retrieveTransfer(List<Transfer> transfers, int id) {
        for (Transfer currentTransfer : transfers) {
            if (currentTransfer.getTransferId() == id) {
                return currentTransfer;
            }
        }
        return null;
    }

    private boolean idValid(List<User> users, int id) {
        for (User current : users) {
            if (current.getId() == id) return true;
        }
        return false;
    }

    private void transferBucks(boolean sending) {
        List<User> users = tenmoService.getUsers();
        consoleService.printUsers(users);
        BigDecimal balance = tenmoService.getBalance();
        int idToSendOrReceive = consoleService.promptForInt("Please enter the user ID: ");
        while (!idValid(users, idToSendOrReceive)) {
            consoleService.printInvalidId("This ID is not valid. Please enter a valid ID.");
            idToSendOrReceive = consoleService.promptForInt("Please enter the user ID: ");
        }

        BigDecimal amount = consoleService.promptForBigDecimal("Please enter amount to transfer(Enter 0 to exit transfer): ");
        boolean amountPositive = validAmount(amount);
        while (!amountPositive) {
            consoleService.printMessage("Your amount to transfer must be greater than 0.");
            amount = consoleService.promptForBigDecimal("Please enter amount to transfer(Enter 0 to exit transfer): ");
            amountPositive = validAmount(amount);
        }

        boolean depositLessThanBalance = depositLessThanBalance(amount, balance);
        if (amount.compareTo(BigDecimal.valueOf(0)) == 0) {
            consoleService.printMessage("Returning to previous menu.");
        } else if (!sending && tenmoService.requestMoney(idToSendOrReceive, amount)) {
            consoleService.printRequestMessage(idToSendOrReceive, amount);
        } else if (sending && depositLessThanBalance && tenmoService.sendMoney(idToSendOrReceive, amount)) {
            consoleService.printSuccessMessage(idToSendOrReceive, amount);
        } else if (sending && !depositLessThanBalance) {
            consoleService.printMessage("There was not enough money in your account to send.");
        } else {
            consoleService.printFailMessage();
        }
    }

    public String currentUsername() {
        return currentUser.getUser().getUsername();
    }

    public boolean validAmount(BigDecimal amount) {
        return amount.compareTo(BigDecimal.valueOf(0)) >= 0;
    }

    public boolean depositLessThanBalance(BigDecimal amount, BigDecimal balance) {
        return balance.compareTo(amount) >= 0;
    }

}
