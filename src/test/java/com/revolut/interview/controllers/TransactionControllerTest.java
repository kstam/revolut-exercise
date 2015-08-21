package com.revolut.interview.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.interview.config.SpringRootConfig;
import com.revolut.interview.model.Account;
import com.revolut.interview.model.Transaction;
import com.revolut.interview.model.Transaction.TransactionStatus;
import com.revolut.interview.repos.*;
import com.revolut.interview.services.CreateTransactionResult.CreationStatus;
import com.revolut.interview.services.ExecuteTransactionResult.ExecutionStatus;
import com.revolut.interview.utils.TestConstants;
import org.hamcrest.Matchers;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
@ContextConfiguration(classes = { SpringRootConfig.class })
public class TransactionControllerTest extends AbstractTestNGSpringContextTests {

    private Account account1;
    private Account account2;
    private Transaction transaction1;
    private Transaction transaction2;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeMethod
    public void setup() throws DataAccessException {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        transactionRepo.deleteAll();
        accountRepo.deleteAll();

        account1 = accountRepo.insert(new Account(1, TestConstants.EUR_5));
        account2 = accountRepo.insert(new Account(2, TestConstants.EUR_100));

        transaction1 = transactionRepo
                .insert(new Transaction(account1.getId(), account2.getId(), TestConstants.EUR_5));
        transaction2 = transactionRepo
                .insert(new Transaction(account1.getId(), account2.getId(), TestConstants.EUR_10));
    }

    @Test
    public void testCanGetAnExistingTransaction() throws Exception {
        mockMvc.perform(get("/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$id", is(1)))
                .andExpect(jsonPath("$.sourceId", equalTo((int) account1.getId())))
                .andExpect(jsonPath("$.destinationId", equalTo((int) account2.getId())))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.amount.value", is(5)))
                .andExpect(jsonPath("$.amount.currency", is("EUR")));
    }

    @Test
    public void testGetAnNonExistingTransactionReturnsA404() throws Exception {
        mockMvc.perform(get("/transactions/10"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAllTransactionsReturnsAListWithTheCorrectSize() throws Exception {
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", Matchers.hasSize(2)));
    }

    @Test
    public void testCreateTransactionReturnsCorrectResultOnSuccess() throws Exception {
        ObjectMapper om = new ObjectMapper();
        String txJson = om.writeValueAsString(new Transaction(account1.getId(), account2.getId(), TestConstants.EUR_5));
        mockMvc.perform(post("/transactions")
                .content(txJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.status", is(CreationStatus.SUCCESS.toString())))
                .andExpect(jsonPath("$.transaction.id", is(3)))
                .andExpect(jsonPath("$.detailMessage", is("")));
    }

    @Test
    public void testCreateTransactionReturnsErrorCodeOnFailureDueToNonExistentAccounts() throws Exception {
        ObjectMapper om = new ObjectMapper();
        String txJson = om.writeValueAsString(new Transaction(10, account2.getId(), TestConstants.EUR_5));
        mockMvc.perform(post("/transactions")
                .content(txJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.transaction", isEmptyOrNullString()))
                .andExpect(jsonPath("$.status", is(CreationStatus.ACCOUNT_NOT_FOUND.toString())))
                .andExpect(jsonPath("$.detailMessage", is(new AccountNotFoundException(10L).getMessage())));
    }

    @Test
    public void testExcecutingATransactionReturnsCorrectResultOnSuccess() throws Exception {
        mockMvc.perform(put("/transactions/1/executed")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.transaction.id", is(1)))
                .andExpect(jsonPath("$.transaction.status", is(TransactionStatus.EXECUTED.toString())))
                .andExpect(jsonPath("$.status", is(ExecutionStatus.SUCCESS.toString())))
                .andExpect(jsonPath("$.detailMessage", isEmptyOrNullString()));
    }

    @Test
    public void testExcecutingATransactionReturnsUnprocessableEntityResponseIfAccountIsNotFound() throws Exception {
        accountRepo.deleteAll();
        mockMvc.perform(put("/transactions/1/executed")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.transaction.id", is(1)))
                .andExpect(jsonPath("$.transaction.status", is(TransactionStatus.FAILED.toString())))
                .andExpect(jsonPath("$.status", is(ExecutionStatus.ACCOUNT_NOT_FOUND.toString())))
                .andExpect(jsonPath("$.detailMessage", is(new AccountNotFoundException(1L).getMessage())));
    }

    @Test
    public void testExcecutingATransactionReturnsNotFoundResponseIfTransactionIsNotFound() throws Exception {
        transactionRepo.deleteAll();
        mockMvc.perform(put("/transactions/" + transaction1.getId() + "/executed")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.transaction", isEmptyOrNullString()))
                .andExpect(jsonPath("$.status", is(ExecutionStatus.TRANSACTION_NOT_FOUND.toString())))
                .andExpect(jsonPath("$.detailMessage", is(new TransactionNotFoundException(1L).getMessage())));
    }

    @Test
    public void testExcecutingATransactionReturnsUnprocessableEntityResponseIfAccountHasInsufficientFunds()
            throws Exception {
        mockMvc.perform(put("/transactions/" + transaction2.getId() + "/executed")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.transaction.id", is((int) transaction2.getId())))
                .andExpect(jsonPath("$.transaction.status", is(TransactionStatus.FAILED.toString())))
                .andExpect(jsonPath("$.status", is(ExecutionStatus.INSUFFICIENT_FUNDS.toString())))
                .andExpect(jsonPath("$.detailMessage", Matchers.not(Matchers.isEmptyOrNullString())));
    }

    @Test
    public void testAttemptingToPostWrongDataReturnsA400() throws Exception {
        String txnJson = "{\"sourceId\":null, \"destinationId\": 2,\"amount\": {\"value\": 10, \"currency\": \"EUR\"}}";
        mockMvc.perform(post("/transactions")
                .content(txnJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.message", is("An error occurred")))
                .andExpect(jsonPath("$.exceptionType", is(IllegalArgumentException.class.getSimpleName())));

        txnJson = "{\"sourceId\":1, \"destinationId\": null, \"amount\": {\"value\": 10, \"currency\": \"EUR\"}}";
        mockMvc.perform(post("/transactions")
                .content(txnJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.message", is("An error occurred")))
                .andExpect(jsonPath("$.exceptionType", is(IllegalArgumentException.class.getSimpleName())));

        txnJson = "{\"sourceId\":1, \"destinationId\": 2, \"amount\": null}";
        mockMvc.perform(post("/transactions")
                .content(txnJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.message", is("An error occurred")))
                .andExpect(jsonPath("$.exceptionType", is(IllegalArgumentException.class.getSimpleName())));

        txnJson = "{\"sourceId\":1, \"destinationId\": 2, \"amount\": {}}";
        mockMvc.perform(post("/transactions")
                .content(txnJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.message", is("An error occurred")))
                .andExpect(jsonPath("$.exceptionType", is(IllegalArgumentException.class.getSimpleName())));
    }
}
