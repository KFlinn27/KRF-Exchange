package com.techelevator.tenmo.services;


import com.techelevator.tenmo.model.Amount;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TenmoService {

    public static final String API_BASE_URL = "http://localhost:8080/user/";
    private RestTemplate restTemplate = new RestTemplate();
    private String authToken = null;

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public BigDecimal getBalance(){
        BigDecimal balance = null;
        try{
            ResponseEntity<BigDecimal> response = restTemplate.exchange(API_BASE_URL + "balance/", HttpMethod.GET, makeAuthEntity(), BigDecimal.class);
            balance = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return balance;
    }

    //TODO getusers


    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }

    public List<Transfer> getTransfers() {
        List<Transfer> transfers = new ArrayList<>();
        try{
            ResponseEntity<Transfer[]> response = restTemplate.exchange(API_BASE_URL + "transfers/", HttpMethod.GET, makeAuthEntity(), Transfer[].class);
            transfers = Arrays.asList(Objects.requireNonNull(response.getBody()));

        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfers;
    }

    public List<Transfer> getPendingTransfers() {
        List<Transfer> transfers = new ArrayList<>();
        try{
            ResponseEntity<Transfer[]> response = restTemplate.exchange(API_BASE_URL + "transfers/pending", HttpMethod.GET, makeAuthEntity(), Transfer[].class);
            transfers = Arrays.asList(Objects.requireNonNull(response.getBody()));

        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfers;
    }

    public boolean sendMoney(int sendToId, BigDecimal amount) {
        Amount amountSending = new Amount(amount);
        HttpEntity<Amount> toSend = makeAmountEntity(amountSending);
        try{
            ResponseEntity<Boolean> response = restTemplate.exchange(API_BASE_URL + "send/" + sendToId, HttpMethod.PUT, toSend, Boolean.class);
            return Boolean.TRUE.equals(response.getBody());
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return false;
    }

    public boolean requestMoney(int requestFromId, BigDecimal amount) {
        Amount amountRequesting = new Amount(amount);
        HttpEntity<Amount> toRequest = makeAmountEntity(amountRequesting);
        try{
            ResponseEntity<Boolean> response = restTemplate.exchange(API_BASE_URL + "request/" + requestFromId, HttpMethod.POST, toRequest, Boolean.class);
            return Boolean.TRUE.equals(response.getBody());
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return false;
    }

    public boolean acceptRequest(int id) {
        try{
            ResponseEntity<Boolean> response = restTemplate.exchange(API_BASE_URL + "transfer/" + id + "/accept", HttpMethod.PUT, makeAuthEntity(), Boolean.class);
            return Boolean.TRUE.equals(response.getBody());
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return false;
    }

    public void rejectRequest(int id) {
        try {
            ResponseEntity<Void> response = restTemplate.exchange(API_BASE_URL + "transfer/" + id + "/reject", HttpMethod.PUT, makeAuthEntity(), Void.class);
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
    }

    private HttpEntity<Amount> makeAmountEntity(Amount amount) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(amount, headers);
    }


}
