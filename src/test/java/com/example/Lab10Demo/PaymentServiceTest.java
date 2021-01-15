package com.example.Lab10Demo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @Mock
    private PaymentRepository repository;
    @InjectMocks
    private PaymentService service;

    private static Optional<UserModel> sender;
    private static Optional<UserModel> receiver;
    private static AccountModel.Currency currency;
    private static double amount;
    private static List<AccountModel> senderAccounts;
    private static List<AccountModel> receiverAccounts;
    private static List<AccountModel> senderAccountsWrongCurrency;

    @BeforeAll
    public static void setup(){
        sender = Optional.of(new UserModel(UUID.randomUUID().toString(), "John",
                "Doe", "jdoe@gmail.com", "p@ssword"));
        receiver = Optional.of(new UserModel(UUID.randomUUID().toString(), "Ion",
                "Popescu", "popescu@gmail.com", "passw0rd"));
        currency = AccountModel.Currency.USD;
        amount = 10000;
        senderAccounts = new ArrayList<AccountModel>();
        senderAccounts.add(new AccountModel(UUID.randomUUID().toString(), sender.get().getId(), currency, amount));
        senderAccountsWrongCurrency = new ArrayList<AccountModel>();
        senderAccountsWrongCurrency.add(new AccountModel(UUID.randomUUID().toString(), sender.get().getId(),
                AccountModel.Currency.RON, amount));
        senderAccountsWrongCurrency.add(new AccountModel(UUID.randomUUID().toString(), sender.get().getId(),
                AccountModel.Currency.EUR, amount));
        receiverAccounts = new ArrayList<AccountModel>();
        receiverAccounts.add(new AccountModel(UUID.randomUUID().toString(), receiver.get().getId(), currency, amount));
    }

    @Test
    @DisplayName("Test invalid sender")
    public void testInvalidSender(){
        doReturn(Optional.empty()).when(repository).getUser(sender.get().getEmail());
        PaymentException exception = assertThrows(PaymentException.class, () ->
                service.sendPayment(sender.get().getEmail(), sender.get().getPassword(),
                        receiver.get().getEmail(), currency, amount/10));
        assertEquals(PaymentException.PaymentErrors.USER_NOT_FOUND, exception.getError());
    }

    @Test
    @DisplayName("Test invalid password")
    public void testInvalidPassword(){
        doReturn(sender).when(repository).getUser(sender.get().getEmail());
        PaymentException exception = assertThrows(PaymentException.class, () ->
                service.sendPayment(sender.get().getEmail(), "testPassword",
                        receiver.get().getEmail(), currency, amount/10));
        assertEquals(PaymentException.PaymentErrors.BAD_CREDENTIALS, exception.getError());
    }

    @Test
    @DisplayName("Test invalid receiver")
    public void testInvalidReceiver(){
        doReturn(sender).when(repository).getUser(sender.get().getEmail());
        doReturn(Optional.empty()).when(repository).getUser(receiver.get().getEmail());
        PaymentException exception = assertThrows(PaymentException.class, () ->
                service.sendPayment(sender.get().getEmail(), sender.get().getPassword(),
                        receiver.get().getEmail(), currency, amount/10));
        assertEquals(PaymentException.PaymentErrors.USER_NOT_FOUND, exception.getError());
    }

    @Test
    @DisplayName("Test sender has no account")
    public void testSenderNoAccount(){
        doReturn(sender).when(repository).getUser(sender.get().getEmail());
        doReturn(receiver).when(repository).getUser(receiver.get().getEmail());
        doReturn(emptyList()).when(repository).getUserAccounts(sender.get().getId());
        PaymentException exception = assertThrows(PaymentException.class, () ->
                service.sendPayment(sender.get().getEmail(), sender.get().getPassword(),
                        receiver.get().getEmail(), currency, amount/10));
        assertEquals(PaymentException.PaymentErrors.USER_HAS_NO_ACCOUNT_FOR_CURRENCY, exception.getError());
    }

    @Test
    @DisplayName("Test sender has no account for currency")
    public void testSenderNoAccountForCurrency(){
        doReturn(sender).when(repository).getUser(sender.get().getEmail());
        doReturn(receiver).when(repository).getUser(receiver.get().getEmail());
        doReturn(senderAccountsWrongCurrency).when(repository).getUserAccounts(sender.get().getId());
        PaymentException exception = assertThrows(PaymentException.class, () ->
                service.sendPayment(sender.get().getEmail(), sender.get().getPassword(),
                        receiver.get().getEmail(), currency, amount/10));
        assertEquals(PaymentException.PaymentErrors.USER_HAS_NO_ACCOUNT_FOR_CURRENCY, exception.getError());
    }

    @Test
    @DisplayName("Test receiver has no account for currency")
    public void testReceiverNoAccountForCurrency(){
        doReturn(sender).when(repository).getUser(sender.get().getEmail());
        doReturn(receiver).when(repository).getUser(receiver.get().getEmail());
        doReturn(senderAccounts).when(repository).getUserAccounts(sender.get().getId());
        doReturn(emptyList()).when(repository).getUserAccounts(receiver.get().getId());
        PaymentException exception = assertThrows(PaymentException.class, () ->
                service.sendPayment(sender.get().getEmail(), sender.get().getPassword(),
                        receiver.get().getEmail(), currency, amount/10));
        assertEquals(PaymentException.PaymentErrors.USER_HAS_NO_ACCOUNT_FOR_CURRENCY, exception.getError());
    }

    @Test
    @DisplayName("Test sender has not enough amount")
    public void testSenderHasNotEnoughAmount(){
        doReturn(sender).when(repository).getUser(sender.get().getEmail());
        doReturn(receiver).when(repository).getUser(receiver.get().getEmail());
        doReturn(senderAccounts).when(repository).getUserAccounts(sender.get().getId());
        doReturn(receiverAccounts).when(repository).getUserAccounts(receiver.get().getId());
        PaymentException exception = assertThrows(PaymentException.class, () ->
                service.sendPayment(sender.get().getEmail(), sender.get().getPassword(),
                        receiver.get().getEmail(), currency, amount * 2));
        assertEquals(PaymentException.PaymentErrors.ACCOUNT_HAS_NOT_ENOUGH_AMOUNT_FOR_PAYMENT, exception.getError());
    }

    @Test
    @DisplayName("Test payment was not processed")
    public void testPaymentNotProcessed(){
        doReturn(sender).when(repository).getUser(sender.get().getEmail());
        doReturn(receiver).when(repository).getUser(receiver.get().getEmail());
        doReturn(senderAccounts).when(repository).getUserAccounts(sender.get().getId());
        doReturn(receiverAccounts).when(repository).getUserAccounts(receiver.get().getId());
        doReturn(false).when(repository).savePayment(any(PaymentModel.class));
        PaymentException exception = assertThrows(PaymentException.class, () ->
                service.sendPayment(sender.get().getEmail(), sender.get().getPassword(),
                        receiver.get().getEmail(), currency, amount/5));
        assertEquals(PaymentException.PaymentErrors.PAYMENT_COULD_NOT_BE_PROCESSED, exception.getError());
    }
    @Test
    @DisplayName("Test payment sent")
    public void testPaymentSent(){
        doReturn(sender).when(repository).getUser(sender.get().getEmail());
        doReturn(receiver).when(repository).getUser(receiver.get().getEmail());
        doReturn(senderAccounts).when(repository).getUserAccounts(sender.get().getId());
        doReturn(receiverAccounts).when(repository).getUserAccounts(receiver.get().getId());
        doReturn(true).when(repository).savePayment(any(PaymentModel.class));
        service.sendPayment(sender.get().getEmail(), sender.get().getPassword(),
                        receiver.get().getEmail(), currency, amount/5);
    }
}
