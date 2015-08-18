package com.revolut.interview.services;

import com.revolut.interview.model.Account;
import com.revolut.interview.model.Amount;
import com.revolut.interview.model.Transaction;
import com.revolut.interview.model.Transaction.TransactionStatus;
import com.revolut.interview.repos.*;
import com.revolut.interview.services.ExecuteTransactionResult.ExecutionStatus;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.revolut.interview.utils.TestConstants.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.*;

public class TheTransactionServiceTest {

    public static final Amount SMALL_AMOUNT = EUR_5;
    private Account account1 = new Account(1L, EUR_10);
    private Account account2 = new Account(2L, USD_20);

    @Mock
    public AccountRepo accountRepo;

    @Mock
    TransactionRepo transactionRepo;

    TheTransactionService transactionService;

    @BeforeMethod
    public void setup() throws DataAccessException {
        initMocks(this);
        when(accountRepo.getById(account1.getId())).thenReturn(account1);
        when(accountRepo.getById(account2.getId())).thenReturn(account2);
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
            return new Transaction(1L, txn.getSourceId(), txn.getDestinationId(), txn.getAmount(),
                    TransactionStatus.PENDING);
        });

        Transaction transaction = transactionService
                .createTransaction(account1.getId(), account2.getId(), SMALL_AMOUNT);

        assertEquals(transaction.getId(), new Long(1L));
        assertEquals(transaction.getSourceId(), account1.getId());
        assertEquals(transaction.getDestinationId(), account2.getId());
        assertEquals(transaction.getCurrency(), EUR);
        assertEquals(transaction.getStatus(), TransactionStatus.PENDING);
        assertTrue(transaction.getAmount().equals(SMALL_AMOUNT));
    }

    @Test
    public void testCreateTransactionThrowsExceptionIfAccountDoesNotExist() throws DataAccessException {
        when(accountRepo.getById(3L)).thenThrow(new AccountNotFoundException(3L));
        try {
            transactionService.createTransaction(account1.getId(), 3L, SMALL_AMOUNT);
            fail("Exception was not thrown");
        } catch (TransactionServiceException tse) {
            assertTrue(tse.getCause() instanceof AccountNotFoundException);
        }
    }

    @Test
    public void testCreateTransactionThrowsExceptionIfRepoFailedToCreateTransaction() throws DataAccessException {
        when(transactionRepo.insert(any(Transaction.class))).then(invocation -> {
            throw new TransactionInsertException((Transaction) invocation.getArguments()[0]);
        });

        try {
            transactionService.createTransaction(account1.getId(), 3L, SMALL_AMOUNT);
            fail("Exception was not thrown");
        } catch (TransactionServiceException tse) {
            assertTrue(tse.getCause() instanceof TransactionInsertException);
        }
    }
}
