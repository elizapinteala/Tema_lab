package com.example.Lab10Demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.Lab10Demo.Credentials.AUTHORIZATION;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private PaymentService service;

    @Value("${administrator.email}")
    private String adminEmail;
    @Value("${administrator.password}")
    private String adminPassword;

    public PaymentController(PaymentService service){
        this.service = service;
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountModel>> getAccounts (@RequestHeader(AUTHORIZATION) String header){
        Credentials credentials = new Credentials(header);
        List<AccountModel> accounts = service.getUserAccounts(credentials.getEmail(), credentials.getPassword());
        return accounts.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(accounts);
    }

    @PostMapping("/payments")
    public ResponseEntity<Void> sendPayment(@RequestHeader(AUTHORIZATION) String header,
                                            @RequestParam String receiver,
                                            @RequestParam AccountModel.Currency currency,
                                            @RequestParam double amount){
        Credentials credentials = new Credentials(header);
        service.sendPayment(credentials.getEmail(), credentials.getPassword(), receiver, currency, amount);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
    @PostMapping("/users")
    public ResponseEntity<Void> saveUser(@RequestHeader(AUTHORIZATION) String header, @RequestBody UserModel userModel){
        Credentials credentials = new Credentials(header);
        if(adminEmail.equals(credentials.getEmail()) && adminPassword.equals(credentials.getPassword())){
            service.saveUser(userModel);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        }
        else {
            throw PaymentException.badCredentials();
        }
    }

    @DeleteMapping("/users")
    public ResponseEntity<Void> removeUser(@RequestHeader(AUTHORIZATION) String header, @RequestParam String email){
        Credentials credentials = new Credentials(header);
        if(adminEmail.equals(credentials.getEmail()) && adminPassword.equals(credentials.getPassword())){
            service.removeUser(email);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        }
        else {
            throw PaymentException.badCredentials();
        }
    }

    @PutMapping("/accounts")
    public ResponseEntity<Void> saveAccount(@RequestHeader(AUTHORIZATION) String header, @RequestBody AccountModel account){
        Credentials credentials = new Credentials(header);
        if(adminEmail.equals(credentials.getEmail()) && adminPassword.equals(credentials.getPassword())){
            service.saveAccount(account);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        }
        else {
            throw PaymentException.badCredentials();
        }
    }
    @DeleteMapping("/accounts")
    public ResponseEntity<Void> removeAccount(@RequestHeader(AUTHORIZATION) String header, @RequestParam String email,
                                              @RequestParam AccountModel.Currency currency){
        Credentials credentials = new Credentials(header);
        if(adminEmail.equals(credentials.getEmail()) && adminPassword.equals(credentials.getPassword())){
            service.removeAccount(email, currency);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        }
        else {
            throw PaymentException.badCredentials();
        }
    }
}
