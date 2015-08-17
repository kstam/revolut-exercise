package com.revolut.interview.services;

import com.revolut.interview.model.Account;
import com.revolut.interview.model.Transaction;
import com.revolut.interview.repos.*;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;

import static com.revolut.interview.utils.TestConstants.EUR;
import static com.revolut.interview.utils.TestConstants.USD;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.*;

public class TheTransactionServiceTest {

    public static final BigDecimal AMOUNT1 = new BigDecimal(5);
    private Account account1 = new Account(1L, new BigDecimal(10), EUR);
    private Account account2 = new Account(2L, new BigDecimal(20), USD);

    @Mock
    public AccountRepo accountRepo;

    @Mock
    TransactionRepo transactionRepo;

    TheTransactionService transactionService;

    @BeforeMethod
    public void setup() throws AccountNotFoundAccessException {
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
    public void testCreateTransactionGeneratesATransactionAccordingly()
            throws TransactionServiceException, DataAccessException {
        when(transactionRepo.insert(any(Transaction.class))).then(invocation -> {
            Transaction txn = (Transaction) invocation.getArguments()[0];
            return new Transaction(1L, txn.getSource(), txn.getDestination(), txn.getAmount(), txn.getCurrency(),
                    Transaction.TransactionStatus.PENDING);
        });

        Transaction transaction = transactionService
                .createTransaction(account1.getId(), account2.getId(), AMOUNT1, EUR);

        assertEquals(transaction.getId(), new Long(1L));
        assertEquals(transaction.getSource(), account1);
        assertEquals(transaction.getDestination(), account2);
        assertEquals(transaction.getCurrency(), EUR);
        assertEquals(transaction.getStatus(), Transaction.TransactionStatus.PENDING);
        assertTrue(transaction.getAmount().equals(AMOUNT1));
    }

    @Test
    public void testCreateTransactionThrowsExceptionIfAccountDoesNotExist() throws AccountNotFoundAccessException {
        when(accountRepo.getAccountById(3L)).thenThrow(new AccountNotFoundAccessException(3L));
        try {
            transactionService.createTransaction(account1.getId(), 3L, new BigDecimal(5), EUR);
            fail("Exception was not thrown");
        } catch (TransactionServiceException tse) {
            assertTrue(tse.getCause() instanceof AccountNotFoundAccessException);
        }
    }

    @Test
    public void testCreateTransactionThrowsExceptionIfRepoFailedToCreateTransaction() throws DataAccessException {
        when(transactionRepo.insert(any(Transaction.class))).then(invocation -> {
            throw new TransactionInsertAccessException((Transaction) invocation.getArguments()[0]);
        });

        try {
            transactionService.createTransaction(account1.getId(), 3L, new BigDecimal(5), EUR);
            fail("Exception was not thrown");
        } catch (TransactionServiceException tse) {
            assertTrue(tse.getCause() instanceof TransactionInsertAccessException);
        }
    }
}
