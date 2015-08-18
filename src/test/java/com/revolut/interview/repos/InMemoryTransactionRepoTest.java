package com.revolut.interview.repos;

import com.revolut.interview.model.Transaction;
import com.revolut.interview.utils.TestConstants;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class InMemoryTransactionRepoTest {

    public static final long SRC_ID = 1L;
    public static final long DST_ID = 2L;

    InMemoryTransactionRepo repo;

    @BeforeMethod
    public void setup() {
        repo = new InMemoryTransactionRepo();
    }

    @Test
    public void testCanInsertATransaction() throws DataAccessException {
        Transaction insertedTransaction = repo.insert(new Transaction(SRC_ID, DST_ID, TestConstants.EUR_5));
        assertNotNull(insertedTransaction.getId());
        assertEquals(insertedTransaction.getStatus(), Transaction.TransactionStatus.PENDING);

        Transaction gotTransaction = repo.getById(insertedTransaction.getId());
        assertEquals(gotTransaction, insertedTransaction);
        assertEquals(gotTransaction.getId(), 1L);
        assertEquals(gotTransaction.getAmount(), TestConstants.EUR_5);
        assertEquals(gotTransaction.getSourceId(), SRC_ID);
        assertEquals(gotTransaction.getDestinationId(), DST_ID);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCannotUpdateANonPersistedTransaction() {
        Transaction transaction = new Transaction(SRC_ID, DST_ID, TestConstants.EUR_5);
        repo.update(transaction);
    }

    @Test
    public void testCanUpdateATransaction() throws DataAccessException {
        Transaction txn = repo.insert(new Transaction(SRC_ID, DST_ID, TestConstants.EUR_5));
        Transaction updatedTxn = repo.update(txn.executed());

        Transaction gotTxn = repo.getById(txn.getId());
        assertEquals(updatedTxn.getStatus(), Transaction.TransactionStatus.EXECUTED);
        assertEquals(gotTxn.getStatus(), Transaction.TransactionStatus.EXECUTED);
    }


    @Test(expectedExceptions = CouldNotLockResourceException.class)
    public void testCanLockATransactionAndIfSomeoneElseTriesToAcquireTheLockTheyFail() throws DataAccessException {
        Transaction txn = repo.insert(new Transaction(SRC_ID, DST_ID, TestConstants.EUR_5));
        repo.lockById(txn.getId(), 50);
        repo.lockById(txn.getId(), 50);
    }

    @Test
    public void testDoesNotFailIfLockIsReleasedWithinGivenTimeout() throws InterruptedException, DataAccessException {
        final Transaction txn = repo.insert(new Transaction(SRC_ID, DST_ID, TestConstants.EUR_5));
        CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> {
            try {
                latch.countDown();
                repo.lockById(txn.getId());
                Thread.sleep(30);
                repo.unlockById(txn.getId());
            } catch (DataAccessException | InterruptedException ignored) {}
        }).start();

        latch.await();
        repo.lockById(txn.getId(), 400); // this has to wait
    }

    @Test
    public void testMultipleUnlocksDoNotIncreaseThePermitsToMoreThanOne() throws DataAccessException {
        final Transaction txn = repo.insert(new Transaction(SRC_ID, DST_ID, TestConstants.EUR_5));
        repo.unlockById(txn.getId());
        assertEquals(repo.getPermitsForLock(txn.getId()), 1);
    }

    @Test
    public void testConcurrentUnlocks() {
        final Transaction txn = repo.insert(new Transaction(SRC_ID, DST_ID, TestConstants.EUR_5));
        int N = 200;
        ExecutorService es = Executors.newFixedThreadPool(N);
        CountDownLatch latch = new CountDownLatch(N);
        List<Future<Integer>> results = new ArrayList<>();

        for (int i = 0 ; i < N; i++) {
            results.add(es.submit(() -> {
                try {
                    latch.countDown();
                    latch.await();
                    repo.lockById(txn.getId());
                    Thread.sleep(5L);
                    int permitsAfterLock = repo.getPermitsForLock(txn.getId());
                    repo.unlockById(txn.getId());
                    return permitsAfterLock;
                } catch (DataAccessException | InterruptedException ignored) {
                    return -1;
                }
            }));
        }

        results.forEach(future -> {
            Integer result = null;
            try {
                result = future.get();
            } catch (InterruptedException | ExecutionException e) {
                result = -1;
            }
            assertEquals(result, new Integer(0));
        });

        assertEquals(repo.getPermitsForLock(txn.getId()), 1);
    }
}
