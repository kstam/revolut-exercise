package com.revolut.interview.services;

import com.revolut.interview.model.Account;
import com.revolut.interview.model.Amount;
import com.revolut.interview.model.Transaction;
import com.revolut.interview.model.Transaction.TransactionStatus;
import com.revolut.interview.repos.*;
import com.revolut.interview.services.CreateTransactionResult.CreationStatus;
import com.revolut.interview.services.ExecuteTransactionResult.ExecutionStatus;
import com.revolut.interview.utils.TestUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static com.revolut.interview.utils.TestConstants.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.*;

public class TheTransactionServiceTest {

    public static final Amount SMALL_AMOUNT = EUR_5;
    public static final Amount LARGE_AMOUNT = EUR_100;
    private Account account1 = new Account(1L, EUR_10);
    private Account account2 = new Account(2L, USD_20);

    @Mock
    public AccountRepo accountRepo;

    @Mock
    TransactionRepo transactionRepo;

    @Mock
    ExchangeRateService exchangeRateService;

    TheTransactionService transactionService;

    @BeforeMethod
    public void setup() throws DataAccessException {
        initMocks(this);
        when(accountRepo.getById(account1.getId())).thenReturn(account1);
        when(accountRepo.getById(account2.getId())).thenReturn(account2);

        // mock exchange rate service for 1:1 exchange rates
        when(exchangeRateService.convert(any(Amount.class), any(Currency.class))).then(invocation -> {
            BigDecimal value = ((Amount) invocation.getArguments()[0]).getValue();
            Currency currency = (Currency) invocation.getArguments()[1];
            return new Amount(value, currency);
        });

        transactionService = new TheTransactionService(accountRepo, transactionRepo, exchangeRateService);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCannotBeInitializedWithNullAccountRepo() {
        new TheTransactionService(null, transactionRepo, exchangeRateService);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCannotBeInitializedWithNullTransactionRepo() {
        new TheTransactionService(accountRepo, null, exchangeRateService);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCannotBeInitializedWithNullExchangeRateService() {
        new TheTransactionService(accountRepo, transactionRepo, null);
    }

    @Test
    public void testCanBeInitializedCorrectlyWhenBothReposAreNotNull() {
        new TheTransactionService(accountRepo, transactionRepo, exchangeRateService);
    }

    @Test
    public void testCreateTransactionGeneratesATransactionAccordingly()
            throws TransactionServiceException, DataAccessException {
        when(transactionRepo.insert(any(Transaction.class))).then(invocation -> {
            Transaction txn = (Transaction) invocation.getArguments()[0];
            return new Transaction(1L, txn.getSourceId(), txn.getDestinationId(), txn.getAmount(),
                    TransactionStatus.PENDING);
        });

        CreateTransactionResult result = transactionService
                .createTransaction(account1.getId(), account2.getId(), SMALL_AMOUNT);

        assertEquals(result.getStatus(), CreationStatus.SUCCESS);
        assertEquals(result.getDetailMessage(), "");

        Transaction transaction = result.getTransaction();
        assertEquals(transaction.getId(), 1L);
        assertEquals(transaction.getSourceId(), account1.getId());
        assertEquals(transaction.getDestinationId(), account2.getId());
        assertEquals(transaction.getCurrency(), EUR);
        assertEquals(transaction.getStatus(), TransactionStatus.PENDING);
        assertTrue(transaction.getAmount().equals(SMALL_AMOUNT));
        verify(transactionRepo).insert(any(Transaction.class));
    }

    @Test
    public void testCreateTransactionThrowsExceptionIfAccountDoesNotExist() throws DataAccessException {
        when(accountRepo.getById(3L)).thenThrow(new AccountNotFoundException(3L));
        CreateTransactionResult result = transactionService.createTransaction(account1.getId(), 3L, SMALL_AMOUNT);

        assertNull(result.getTransaction());
        assertEquals(result.getStatus(), CreationStatus.ACCOUNT_NOT_FOUND);
        assertTrue(result.getDetailMessage().contains("[3]"));
    }

    @Test
    public void testCreateTransactionThrowsExceptionIfRepoFailedToCreateTransaction() throws DataAccessException {
        when(transactionRepo.insert(any(Transaction.class))).then(invocation -> {
            throw new TransactionInsertException((Transaction) invocation.getArguments()[0]);
        });

        CreateTransactionResult result = transactionService.createTransaction(account1.getId(), 3L, SMALL_AMOUNT);
        assertNull(result.getTransaction());
        assertEquals(result.getStatus(), CreationStatus.INTERNAL_ERROR);
    }

    @Test
    public void testExecuteReturnsAppropriateErrorIfLockingOfTransactionFails()
            throws DataAccessException, TransactionServiceException {
        doThrow(new CouldNotLockResourceException("")).when(transactionRepo).lockById(1L);

        ExecuteTransactionResult result = transactionService.executeTransaction(1L);

        assertEquals(result.getStatus(), ExecutionStatus.COULD_NOT_ACQUIRE_RESOURCES);
    }

    @Test
    public void testExecuteReturnsAppropriateErrorIfLockingOfSrcAccountsFails()
            throws DataAccessException, TransactionServiceException {
        when(transactionRepo.getById(1L)).thenReturn(
                new Transaction(1L, account1.getId(), account2.getId(), SMALL_AMOUNT, TransactionStatus.PENDING));
        doThrow(new CouldNotLockResourceException("theMessage")).when(accountRepo).lockById(account1.getId());

        ExecuteTransactionResult result = transactionService.executeTransaction(1L);

        assertEquals(result.getStatus(), ExecutionStatus.COULD_NOT_ACQUIRE_RESOURCES);
        assertEquals(result.getDetailMessage(), "theMessage");
    }

    @Test
    public void testExecuteReturnsAppropriateErrorIfLockingOfDestinationAccountsFails()
            throws DataAccessException, TransactionServiceException {
        when(transactionRepo.getById(1L)).thenReturn(
                new Transaction(1L, account1.getId(), account2.getId(), SMALL_AMOUNT, TransactionStatus.PENDING));
        doThrow(new CouldNotLockResourceException("theMessage")).when(accountRepo).lockById(account2.getId());

        ExecuteTransactionResult result = transactionService.executeTransaction(1L);

        assertEquals(result.getTransaction().getStatus(), TransactionStatus.FAILED);
        assertEquals(result.getStatus(), ExecutionStatus.COULD_NOT_ACQUIRE_RESOURCES);
        assertEquals(result.getDetailMessage(), "theMessage");
    }

    @Test
    public void testExecuteReturnsNotFoundErrorIfTransactionIdCouldNotBeLocated()
            throws DataAccessException, TransactionServiceException {
        when(transactionRepo.getById(1L)).thenThrow(new TransactionNotFoundException(1L));

        ExecuteTransactionResult result = transactionService.executeTransaction(1L);

        assertNull(result.getTransaction());
        assertEquals(result.getStatus(), ExecutionStatus.TRANSACTION_NOT_FOUND);
    }

    @Test
    public void testExecuteReportsFailureIfSrcAccountInNotFound()
            throws DataAccessException, TransactionServiceException {
        when(transactionRepo.getById(1L)).thenReturn(
                new Transaction(1L, account1.getId(), account2.getId(), SMALL_AMOUNT, TransactionStatus.PENDING));
        doThrow(new AccountNotFoundException(account1.getId())).when(accountRepo).lockById(account1.getId());

        ExecuteTransactionResult result = transactionService.executeTransaction(1L);

        assertEquals(result.getTransaction().getStatus(), TransactionStatus.FAILED);
        assertEquals(result.getStatus(), ExecutionStatus.ACCOUNT_NOT_FOUND);
    }

    @Test
    public void testExecuteReportsFailureIfDstAccountInNotFound()
            throws DataAccessException, TransactionServiceException {
        when(transactionRepo.getById(1L)).thenReturn(
                new Transaction(1L, account1.getId(), account2.getId(), SMALL_AMOUNT, TransactionStatus.PENDING));
        doThrow(new AccountNotFoundException(account2.getId())).when(accountRepo).lockById(account2.getId());

        ExecuteTransactionResult result = transactionService.executeTransaction(1L);

        assertEquals(result.getTransaction().getStatus(), TransactionStatus.FAILED);
        assertEquals(result.getStatus(), ExecutionStatus.ACCOUNT_NOT_FOUND);
    }

    @Test
    public void testExecuteReportsFailureIfSrcAccountHasInsufficientFunds()
            throws DataAccessException, TransactionServiceException {
        when(transactionRepo.getById(1L)).thenReturn(
                new Transaction(1L, account1.getId(), account2.getId(), LARGE_AMOUNT, TransactionStatus.PENDING));

        ExecuteTransactionResult result = transactionService.executeTransaction(1L);

        assertEquals(result.getStatus(), ExecutionStatus.INSUFFICIENT_FUNDS);
        assertEquals(result.getTransaction().getStatus(), TransactionStatus.FAILED);
    }

    @Test
    public void testExecuteSavesAccountsWithTheirNewBalance() throws DataAccessException, TransactionServiceException {
        when(transactionRepo.getById(1L)).thenReturn(
                new Transaction(1L, account1.getId(), account2.getId(), SMALL_AMOUNT, TransactionStatus.PENDING));

        ExecuteTransactionResult result = transactionService.executeTransaction(1L);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepo, times(2)).update(captor.capture());

        List<Account> savedAccounts = captor.getAllValues();
        TestUtils.assertEquals(savedAccounts.get(0).getBalance().getValue(), new BigDecimal(5));
        TestUtils.assertEquals(savedAccounts.get(1).getBalance().getValue(), new BigDecimal(25));

        assertEquals(result.getStatus(), ExecutionStatus.SUCCESS);
        assertEquals(result.getTransaction().getStatus(), TransactionStatus.EXECUTED);

        ArgumentCaptor<Transaction> txnCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepo).update(txnCaptor.capture());
        assertEquals(txnCaptor.getValue().getStatus(), TransactionStatus.EXECUTED);

    }

    @Test
    public void testExecuteSavesTransactionOnFailure() throws DataAccessException, TransactionServiceException {
        when(transactionRepo.getById(1L)).thenReturn(
                new Transaction(1L, account1.getId(), account2.getId(), SMALL_AMOUNT, TransactionStatus.PENDING));
        doThrow(new CouldNotLockResourceException("theMessage")).when(accountRepo).lockById(account2.getId());
        transactionService.executeTransaction(1L);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepo).update(captor.capture());

        assertEquals(captor.getValue().getStatus(), TransactionStatus.FAILED);
    }


    @Test
    public void testExecuteDoesNothingIfTransactionIsAlreadyExecuted()
            throws DataAccessException, TransactionServiceException {
        when(transactionRepo.getById(1L)).thenReturn(
                new Transaction(1L, account1.getId(), account2.getId(), SMALL_AMOUNT, TransactionStatus.EXECUTED));

        ExecuteTransactionResult result = transactionService.executeTransaction(1L);
        assertEquals(result.getStatus(), ExecutionStatus.UNCHANGED);
    }

    @Test
    public void testExecuteDoesNothingIfTransactionIsAlreadyFailed()
        throws DataAccessException, TransactionServiceException {
            when(transactionRepo.getById(1L)).thenReturn(
                    new Transaction(1L, account1.getId(), account2.getId(), SMALL_AMOUNT, TransactionStatus.FAILED));

            ExecuteTransactionResult result = transactionService.executeTransaction(1L);
            assertEquals(result.getStatus(), ExecutionStatus.UNCHANGED);
    }

    @Test
    public void testExecuteUnlocksResourcesOnFailure() throws DataAccessException, TransactionServiceException {
        when(transactionRepo.getById(1L)).thenReturn(
                new Transaction(1L, account1.getId(), account2.getId(), SMALL_AMOUNT, TransactionStatus.PENDING));
        doThrow(new CouldNotLockResourceException("theMessage")).when(accountRepo).lockById(account2.getId());

        transactionService.executeTransaction(1L);

        verify(transactionRepo).unlockById(1L);
        verify(accountRepo).unlockById(account1.getId());
        verify(accountRepo).unlockById(account2.getId());
    }

    @Test
    public void testExecuteUnlocksResourcesOnSuccess() throws DataAccessException, TransactionServiceException {
        when(transactionRepo.getById(1L)).thenReturn(
                new Transaction(1L, account1.getId(), account2.getId(), SMALL_AMOUNT, TransactionStatus.PENDING));

        transactionService.executeTransaction(1L);

        verify(transactionRepo).unlockById(1L);
        verify(accountRepo).unlockById(account1.getId());
        verify(accountRepo).unlockById(account2.getId());
    }
}
