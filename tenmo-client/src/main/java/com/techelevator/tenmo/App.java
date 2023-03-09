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
    private TenmoService tenmoService = new TenmoService();

    private AuthenticatedUser currentUser;

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
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
        }else{
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
                //TODO add ability to approve or reject a targeted id
                viewPendingRequests();
            } else if (menuSelection == 4) {
                //TODO add print user method
                sendBucks();
            } else if (menuSelection == 5) {
                //TODO add print user method
                requestBucks();
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
        //TODO create message in consoleservice for balance and get rid of sout
        System.out.println(balance);
	}

	private void viewTransferHistory() {
        consoleService.listTransfers(tenmoService.getTransfers());
	}

	private void viewPendingRequests() {
        List<Transfer> pendingTransfers = tenmoService.getPendingTransfers();
        if(pendingTransfers.size() > 0){
        BigDecimal balance = tenmoService.getBalance();
		consoleService.listTransfers(pendingTransfers);
        int id = consoleService.promptForInt("Please enter transfer ID you would like to approve or reject (enter 0 to exit): ");
        while(id != 0) {
            Transfer transferSearchedFor = transferExists(pendingTransfers, id);
            if (transferSearchedFor == null) {
                consoleService.printInvalidId("Not a valid ID, please enter valid ID from list.");
                consoleService.listTransfers(pendingTransfers);
                id = consoleService.promptForInt("Please enter transfer ID you would like to approve or reject (enter 0 to exit): ");
            } else {
                boolean hasEnoughMoney = balance.compareTo(transferSearchedFor.getAmount()) >= 0;
                int approve = consoleService.promptForInt("Enter 1 to approve, 2 to reject or 0 to exit: ");
                if (approve == 1 && hasEnoughMoney) {
                    //need to move money and change the transfer
                    if (tenmoService.acceptRequest(id)) {
                        consoleService.printSuccessMessage(id, transferSearchedFor.getAmount());
                    } else {
                        consoleService.printFailMessage();
                    }
                    break;
                } else if (approve == 2) {
                    //need to let know rejected and remove from pending list
                    tenmoService.rejectRequest(id);
                    consoleService.promptForString("You rejected the request.");
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
            consoleService.promptForString("You have no pending transfers.");
        }
	}

    private Transfer transferExists(List<Transfer> transfers, int id){
        for(Transfer currentTransfer : transfers){
            if(currentTransfer.getTransferId() == id){
                return currentTransfer;
            }
        }
        return null;
    }

    //TODO print all users and make method
	private void sendBucks() {
        int sendToId = consoleService.promptForInt("Please enter the user ID: ");
        BigDecimal amount = consoleService.promptForBigDecimal("Please enter amount to transfer: ");
        if(tenmoService.sendMoney(sendToId, amount)){
            consoleService.printSuccessMessage(sendToId, amount);
        } else{
            consoleService.printFailMessage();
        }
	}

	private void requestBucks() {
        int requestFromId = consoleService.promptForInt("Please enter the user ID: ");
        BigDecimal amount = consoleService.promptForBigDecimal("Please enter amount to transfer: ");
        if(tenmoService.requestMoney(requestFromId, amount)){
            consoleService.printSuccessMessage(requestFromId, amount);
        } else{
            consoleService.printFailMessage();
        }
	}

}
