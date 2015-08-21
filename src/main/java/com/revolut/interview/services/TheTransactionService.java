package com.revolut.interview.services;

import com.revolut.interview.model.Account;
import com.revolut.interview.model.Amount;
import com.revolut.interview.model.Transaction;
import com.revolut.interview.model.Transaction.TransactionStatus;
import com.revolut.interview.repos.*;
import com.revolut.interview.services.CreateTransactionResult.CreationStatus;
import com.revolut.interview.services.ExecuteTransactionResult.ExecutionStatus;

import static com.revolut.interview.utils.Assert.checkNotNull;

class TheTransactionService implements TransactionService {

    private final AccountRepo accountRepo;
    private final TransactionRepo transactionRepo;
    private final ExchangeRateService exchangeRateService;

    public TheTransactionService(AccountRepo accountRepo, TransactionRepo transactionRepo,
                                 ExchangeRateService exchangeRateService) {
        checkNotNull(accountRepo, "accountRepo cannot be null");
        checkNotNull(transactionRepo, "transactionRepo cannot be null");
        checkNotNull(exchangeRateService, "exchangeRateService cannot be null");
        this.accountRepo = accountRepo;
        this.transactionRepo = transactionRepo;
        this.exchangeRateService = exchangeRateService;
    }

    public CreateTransactionResult createTransaction(long srcAccountId, long dstAccountId, Amount amount) {
        try {
            accountRepo.getById(srcAccountId);
            accountRepo.getById(dstAccountId);
            Transaction txn = new Transaction(srcAccountId, dstAccountId, amount);
            txn = transactionRepo.insert(txn);
            return new CreateTransactionResult(txn, CreationStatus.SUCCESS);
        } catch (AccountNotFoundException anfe) {
            return new CreateTransactionResult(null, CreationStatus.ACCOUNT_NOT_FOUND, anfe.getMessage());
        } catch (DataAccessException dae) {
            return new CreateTransactionResult(null, CreationStatus.INTERNAL_ERROR, dae.getMessage());
        }
    }

    public ExecuteTransactionResult executeTransaction(long transactionId) {
        Transaction txn = null;
        Account srcAccount;
        Account dstAccount;
        try {
            transactionRepo.lockById(transactionId);
            txn = transactionRepo.getById(transactionId);

            if (!txn.getStatus().equals(TransactionStatus.PENDING)) {
                return new ExecuteTransactionResult(txn, ExecutionStatus.UNCHANGED,
                        "No changes. Transaction was already " + txn.getStatus());
            }

            accountRepo.lockById(txn.getSourceId());
            accountRepo.lockById(txn.getDestinationId());

            srcAccount = accountRepo.getById(txn.getSourceId());
            dstAccount = accountRepo.getById(txn.getDestinationId());

            Amount toWithdraw = exchangeRateService.convert(txn.getAmount(), srcAccount.getCurrency());
            if (!containsSufficientFunds(srcAccount, toWithdraw)) {
                String message = String.format("Account had %s but needed %s to complete the transaction",
                        srcAccount.getBalance(), toWithdraw);
                return executionFailed(txn, ExecutionStatus.INSUFFICIENT_FUNDS, message);
            }
            Account newSrcAccount = srcAccount.withdraw(toWithdraw);
            Amount toDeposit = exchangeRateService.convert(toWithdraw, dstAccount.getCurrency());
            Account newDstAccount = dstAccount.deposit(toDeposit);

            accountRepo.update(newSrcAccount);
            accountRepo.update(newDstAccount);
            return executionSucceeded(txn);
        } catch (CouldNotLockResourceException cnlre) {
            return executionFailed(txn, ExecutionStatus.COULD_NOT_ACQUIRE_RESOURCES, cnlre.getMessage());
        } catch (TransactionNotFoundException tnfe) {
            return executionFailed(txn, ExecutionStatus.TRANSACTION_NOT_FOUND, tnfe.getMessage());
        } catch (AccountNotFoundException anfe) {
            return executionFailed(txn, ExecutionStatus.ACCOUNT_NOT_FOUND, anfe.getMessage());
        } catch (DataAccessException e) {
            return executionFailed(txn, ExecutionStatus.INTERNAL_ERROR, e.getMessage());
        } finally {
            unlockResources(transactionId, txn);
        }
    }

    private void unlockResources(long transactionId, Transaction txn) {
        try {
            transactionRepo.unlockById(transactionId);
            if (txn != null) {
                accountRepo.unlockById(txn.getSourceId());
                accountRepo.unlockById(txn.getDestinationId());
            }
        } catch (DataAccessException e) {
            throw new RuntimeException("Error unlocking the resources. This should never occur", e);
        }
    }

    private boolean containsSufficientFunds(Account account, Amount neededBalance) {
        return account.getBalance().getValue().compareTo(neededBalance.getValue()) >= 0;
    }

    private ExecuteTransactionResult executionFailed(Transaction txn, ExecutionStatus status, String message) {
        if (txn != null) {
            txn = txn.failed();
            try {
                transactionRepo.update(txn);
            } catch (DataAccessException e) {
                throw new RuntimeException("Could not update transaction");
            }
        }
        return new ExecuteTransactionResult(txn, status, message);
    }

    private ExecuteTransactionResult executionSucceeded(Transaction txn) {
        txn = txn.executed();
        try {
            transactionRepo.update(txn);
        } catch (DataAccessException e) {
            throw new RuntimeException("Could not update transaction");
        }
        return new ExecuteTransactionResult(txn, ExecutionStatus.SUCCESS);
    }
}
