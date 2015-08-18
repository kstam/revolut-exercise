package com.revolut.interview.services;

import com.revolut.interview.model.Account;
import com.revolut.interview.model.Amount;
import com.revolut.interview.model.Transaction;
import com.revolut.interview.repos.*;
import com.revolut.interview.services.ExecuteTransactionResult.ExecutionStatus;

import static com.revolut.interview.services.ExecuteTransactionResult.ExecutionStatus.*;
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

    public Transaction createTransaction(long srcAccountId, long dstAccountId, Amount amount)
            throws TransactionServiceException {
        try {
            accountRepo.getById(srcAccountId);
            accountRepo.getById(dstAccountId);
            Transaction txn = new Transaction(srcAccountId, dstAccountId, amount);
            return transactionRepo.insert(txn);
        } catch (DataAccessException dae) {
            throw new TransactionServiceException("Could not create transaction", dae);
        }
    }

    public ExecuteTransactionResult executeTransaction(long transactionId) throws TransactionServiceException {
        Transaction txn = null;
        Account srcAccount;
        Account dstAccount;
        try {
            transactionRepo.lockById(transactionId);
            txn = transactionRepo.getById(transactionId);

            accountRepo.lockById(txn.getSourceId());
            accountRepo.lockById(txn.getDestinationId());

            srcAccount = accountRepo.getById(txn.getSourceId());
            dstAccount = accountRepo.getById(txn.getDestinationId());

            Amount toWithdraw = exchangeRateService.convert(txn.getAmount(), srcAccount.getCurrency());
            if (!sufficientFunds(srcAccount, toWithdraw)) {
                String message = String.format("Account had %s but needed %s to complete the transaction",
                        srcAccount.getBalance(), toWithdraw);
                return transactionFailed(txn, INSUFFICIENT_FUNDS, message);
            }
            Account newSrcAccount = srcAccount.withdraw(toWithdraw);
            Amount toDeposit = exchangeRateService.convert(toWithdraw, dstAccount.getCurrency());
            Account newDstAccount = dstAccount.deposit(toDeposit);

            accountRepo.update(newSrcAccount);
            accountRepo.update(newDstAccount);
            return transactionSucceeded(txn);
        } catch (CouldNotLockResourceException cnlre) {
            return transactionFailed(txn, COULD_NOT_ACQUIRE_RESOURCES, cnlre.getMessage());
        } catch (TransactionNotFoundException tnfe) {
            return transactionFailed(txn, TRANSACTION_NOT_FOUND, tnfe.getMessage());
        } catch (AccountNotFoundException anfe) {
            return transactionFailed(txn, ACCOUNT_NOT_FOUND, anfe.getMessage());
        } catch (DataAccessException e) {
            throw new TransactionServiceException("Error", e);
        }
    }

    private boolean sufficientFunds(Account account, Amount neededBalance) {
        return account.getBalance().getValue().compareTo(neededBalance.getValue()) >= 0;
    }

    private ExecuteTransactionResult transactionFailed(Transaction txn, ExecutionStatus status, String message) {
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

    private ExecuteTransactionResult transactionSucceeded(Transaction txn) {
        txn = txn.executed();
        try {
            transactionRepo.update(txn);
        } catch (DataAccessException e) {
            throw new RuntimeException("Could not update transaction");
        }
        return new ExecuteTransactionResult(txn, SUCCESS);
    }
}
