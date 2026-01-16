package com.gabriel.picpayteste.services;

import com.gabriel.picpayteste.domain.transaction.Transaction;
import com.gabriel.picpayteste.domain.user.User;
import com.gabriel.picpayteste.dtos.TransactionDTO;
import com.gabriel.picpayteste.repositories.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class TransactionService {

    private UserService userService;
    private NotificationService notificationService;

    private TransactionRepository transactionRepository;

    private RestTemplate restTemplate;

    public TransactionService(UserService userService, NotificationService notificationService, TransactionRepository transactionRepository, RestTemplate restTemplate) {
        this.userService = userService;
        this.notificationService = notificationService;
        this.transactionRepository = transactionRepository;
        this.restTemplate = restTemplate;
    }

    public Transaction createTransaction(TransactionDTO transaction) throws Exception {
        User sender = this.userService.findUserById(transaction.senderId());
        User receiver = this.userService.findUserById(transaction.receiverId());

        userService.validationTransaction(sender, transaction.value());

        boolean isAuthorized = authorizeTransaction(sender, transaction.value());
        if(!isAuthorized){
            throw new Exception("Transação sem autorização");
        }

        Transaction newTransaction = new Transaction();
        newTransaction.setAmount(transaction.value());
        newTransaction.setSender(sender);
        newTransaction.setReceiver(receiver);
        newTransaction.setTimestamp(LocalDateTime.now());

        sender.setBalance(sender.getBalance().subtract(transaction.value()));
        receiver.setBalance(receiver.getBalance().add(transaction.value()));

        this.transactionRepository.save(newTransaction);
        this.userService.saveUSer(sender);
        this.userService.saveUSer(receiver);
        this.notificationService.sendNotification(sender, "Transação realizada");
        this.notificationService.sendNotification(receiver, "Transação realizada");

        return newTransaction;
    }

    public boolean authorizeTransaction(User user, BigDecimal value){
        ResponseEntity<Map> authorizationResponse = restTemplate.getForEntity("https://util.devi.tools/api/v2/authorize", Map.class);
        if(authorizationResponse.getStatusCode() == HttpStatus.OK){
            return true;
        }else {
            return false;
        }
    }
}
