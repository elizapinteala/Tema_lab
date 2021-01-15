package com.example.Lab10Demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = PaymentController.class)
@EnableTransactionManagement
@ComponentScan
public class PaymentControllerIntegrationTests {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PaymentRepository repository;
    @MockBean
    private PlatformTransactionManager transactionManager;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${administrator.email}")
    private String adminEmail;
    @Value("${administrator.password}")
    private String adminPassword;

    private static UserModel sender;
    private static UserModel receiver;
    private static List<AccountModel> senderAccounts;
    private static List<AccountModel> receiverAccounts;

    @BeforeAll
    public static void setup(){
       sender = new UserModel(id(), "John", "Doe", "john.doe@gmail.com", "password");
       receiver = new UserModel(id(), "Ion", "Popescu", "ion.popescu@yahoo.com", "parola");
       senderAccounts = new ArrayList<>();
       senderAccounts.add(new AccountModel(id(), sender.getId(), AccountModel.Currency.EUR, 10000));
       senderAccounts.add(new AccountModel(id(), sender.getId(), AccountModel.Currency.USD, 10000));
       receiverAccounts = new ArrayList<>();
       receiverAccounts.add(new AccountModel(id(), receiver.getId(), AccountModel.Currency.EUR, 10000));
       receiverAccounts.add(new AccountModel(id(), receiver.getId(), AccountModel.Currency.USD, 10000));
    }
    private static String id(){
        return UUID.randomUUID().toString();
    }

    private static String credentials(String email, String password){
        return String.format("Basic %s",
                Base64.getEncoder().encodeToString(String.format("%s:%s", email, password).getBytes()));
    }
    private static String credentials(UserModel user){
        return credentials(user.getEmail(), user.getPassword());
    }
    @Test
    public void getAccounts() throws Exception {
        when(repository.getUser("dummy")).thenReturn(Optional.empty());
        String endpoint = "/api/accounts";
        mockMvc.perform(MockMvcRequestBuilders.get(endpoint)
                .header((Credentials.AUTHORIZATION), credentials("dummy", "dummy")))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$").value(PaymentException.userNotFound().toString()));

        when(repository.getUser(sender.getEmail())).thenReturn(Optional.of(sender));
        when(repository.getUserAccounts(sender.getId())).thenReturn(Collections.emptyList());
        mockMvc.perform(MockMvcRequestBuilders.get(endpoint)
                .header((Credentials.AUTHORIZATION), credentials(sender)))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        when(repository.getUserAccounts(sender.getId())).thenReturn(senderAccounts);
        mockMvc.perform(MockMvcRequestBuilders.get(endpoint)
                .header((Credentials.AUTHORIZATION), credentials(sender)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void sendPayment() throws Exception {
        when(repository.getUser(sender.getEmail())).thenReturn(Optional.of(sender));
        when(repository.getUserAccounts(sender.getId())).thenReturn(senderAccounts);
        when(repository.getUser(receiver.getEmail())).thenReturn(Optional.of(receiver));
        when(repository.getUserAccounts(receiver.getId())).thenReturn(receiverAccounts);
        when(repository.savePayment(any())).thenReturn(true);
        String endpoint = "/api/payments?receiver=%s&currency=%s&amount=%f";
        mockMvc.perform(MockMvcRequestBuilders.post(
                String.format(endpoint, receiver.getEmail(), senderAccounts.get(0).getCurrency(), 10* senderAccounts.get(0).getAmount()))
                .header((Credentials.AUTHORIZATION), credentials(sender)))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$").value(PaymentException.accountHasNotEnoughAmountForPayment().toString()));

        mockMvc.perform(MockMvcRequestBuilders.post(
                String.format(endpoint, receiver.getEmail(), senderAccounts.get(0).getCurrency(), senderAccounts.get(0).getAmount()/10))
                .header((Credentials.AUTHORIZATION), credentials(sender)))
                .andExpect(MockMvcResultMatchers.status().isAccepted());

      }
      @Test
      public void testSaveUser() throws Exception {
        String endpoint ="/api/users";
          mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                  .header((Credentials.AUTHORIZATION), credentials(sender))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(sender)))
                  .andExpect(MockMvcResultMatchers.status().isForbidden());

          when(repository.getUser(sender.getEmail())).thenReturn(Optional.of(receiver));
          mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                  .header((Credentials.AUTHORIZATION), credentials(adminEmail, adminPassword))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(sender)))
                  .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                  .andExpect(MockMvcResultMatchers.jsonPath("$").value(PaymentException.userWithSameEmailAlreadyExists().toString()));

          when(repository.getUser(sender.getEmail())).thenReturn(Optional.of(sender));
          when(repository.saveUser(any())).thenReturn(true);
          mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                  .header((Credentials.AUTHORIZATION), credentials(adminEmail, adminPassword))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(sender)))
                  .andExpect(MockMvcResultMatchers.status().isAccepted());
     }

     @Test
    public void testDeleteUsers() throws Exception{
         when(repository.removeUser(sender.getEmail())).thenReturn(true);
         String endpoint = "/api/users?email=%s";

         mockMvc.perform(MockMvcRequestBuilders.delete(endpoint, sender.getEmail())
                 .header((Credentials.AUTHORIZATION), credentials(sender)))
                 .andExpect(MockMvcResultMatchers.status().isForbidden());

         mockMvc.perform(MockMvcRequestBuilders.delete( String.format(endpoint, sender.getEmail()))
                 .header((Credentials.AUTHORIZATION), credentials(adminEmail, adminPassword)))
                 .andExpect(MockMvcResultMatchers.status().isAccepted());
     }

     @Test
     public void testDeleteAccount() throws Exception{
     
         when(repository.getUserAccounts(sender.getId())).thenReturn(senderAccounts);
         when(repository.removeAccount(sender.getId(),senderAccounts.get(0).getCurrency())).thenReturn(true);
         String endpoint = "/api/accounts?email=%s&currency=%s";

         mockMvc.perform(MockMvcRequestBuilders.delete( String.format(endpoint, sender.getId(), senderAccounts.get(0).getCurrency()))
                 .header((Credentials.AUTHORIZATION), credentials(sender)))
                 .andExpect(MockMvcResultMatchers.status().isForbidden());

         mockMvc.perform(MockMvcRequestBuilders.delete( String.format(endpoint, sender.getId(), senderAccounts.get(0).getCurrency()))
                 .header((Credentials.AUTHORIZATION), credentials(adminEmail, adminPassword)))
                 .andExpect(MockMvcResultMatchers.status().isAccepted());
     }

     @Test
    public void testPutAccount() throws Exception{
         when(repository.saveAccount(any())).thenReturn(true);

         String endpoint ="/api/accounts";

         mockMvc.perform(MockMvcRequestBuilders.put(endpoint)
                 .header((Credentials.AUTHORIZATION), credentials(sender))
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(objectMapper.writeValueAsString(sender)))
                 .andExpect(MockMvcResultMatchers.status().isForbidden());

         mockMvc.perform(MockMvcRequestBuilders.put(
                 String.format(endpoint))
                 .header((Credentials.AUTHORIZATION), credentials(adminEmail, adminPassword))
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(objectMapper.writeValueAsString(sender)))
                 .andExpect(MockMvcResultMatchers.status().isAccepted());

     }


}
