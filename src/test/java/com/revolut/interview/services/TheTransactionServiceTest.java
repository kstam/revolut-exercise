package com.revolut.interview.services;

import com.revolut.interview.model.Account;
import com.revolut.interview.model.Transaction;
import com.revolut.interview.repos.AccountRepo;
import com.revolut.interview.repos.TransactionRepo;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;

public class TheTransactionServiceTest {

    private Account account1 = new Account(1L, new BigDecimal(10), Currency.getInstance("EUR"));
    private Account account2 = new Account(2L, new BigDecimal(20), Currency.getInstance("USD"));

    @Mock
    public AccountRepo accountRepo;

    @Mock
    TransactionRepo transactionRepo;

    TheTransactionService transactionService;

    @BeforeMethod
    public void setup() {
        initMocks(this);
        when(accountRepo.getAccountById(account1.getId())).thenReturn(account1);
        when(accountRepo.getAccountById(account2.getId())).thenReturn(account2);
        transactionService = new TheTransactionService(accountRepo, transactionRepo);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCannotBeInitializedWithNullAccountRepo() {
        new TheTransactionService(null, transactionRepo);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCannotBeInitializedWithNullTransactionRepo() {
        new TheTransactionService(accountRepo, null);
    }

    @Test
    public void testCanBeInitializedCorrectlyWhenBothReposAreNotNull() {
        new TheTransactionService(accountRepo, transactionRepo);
    }

    @Test
    public void testCreateTransactionGeneratesATransactionAccordingly() {
        when(transactionRepo.insert(any(Transaction.class))).then(new Answer<Transaction>() {
            public Transaction answer(InvocationOnMock invocation) throws Throwable {
                Transaction txn = (Transaction) invocation.getArguments()[0];
                return new Transaction(1L, txn.getSource(), txn.getDestination(), txn.getAmount(), txn.getCurrency(),
                        Transaction.TransactionStatus.PENDING);
            }
        });

        Transaction transaction = transactionService
                .createTransaction(account1.getId(), account2.getId(), new BigDecimal(5),
                        Currency.getInstance("EUR"));

        assertEquals(transaction.getId(), new Long(1L));
        assertEquals(transaction.getSource(), account1);
        assertEquals(transaction.getDestination(), account2);
        assertEquals(transaction.getCurrency(), Currency.getInstance("EUR"));
        assertEquals(transaction.getStatus(), Transaction.TransactionStatus.PENDING);
    }

}
