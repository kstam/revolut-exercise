package com.revolut.interview.controllers;

import com.revolut.interview.model.Account;
import com.revolut.interview.model.Amount;
import com.revolut.interview.repos.AccountNotFoundException;
import com.revolut.interview.repos.AccountRepo;
import com.revolut.interview.repos.DataAccessException;
import com.revolut.interview.utils.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
public class AccountController {

    private final AccountRepo accountRepo;

    @Autowired
    public AccountController(AccountRepo accountRepo) {
        Assert.checkNotNull(accountRepo, "acountRepo cannot be null");
        this.accountRepo = accountRepo;
    }

    @RequestMapping(value = "/accounts", method = RequestMethod.GET)
    public List<Account> getAllAccounts() {
        return accountRepo.getAll();
    }

    @RequestMapping(value = "/accounts/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllAccounts(@PathVariable("id") long id) {
        try {
            Account account = accountRepo.getById(id);
            return ResponseEntity.ok(account);
        } catch (AccountNotFoundException ae) {
            return ResponseEntity.notFound().build();
        } catch (DataAccessException e) {
            return ResponseEntity.status(500).body("An unexpected error occurred. Could not access the account");
        }
    }

    @RequestMapping(value = "/accounts",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Account> getAllAccounts(@RequestBody Account account, UriComponentsBuilder b)
            throws DataAccessException {
        //force validation
        account = new Account(new Amount(account.getBalance().getValue(), account.getBalance().getCurrency()));
        Account newAccount = accountRepo.insert(account);
        return ResponseEntity
                .created(b.path("/accounts/{id}").buildAndExpand(newAccount.getId()).toUri())
                .body(newAccount);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> illegalArgumentExceptionHandler(IllegalArgumentException iae) {
        return ResponseEntity.badRequest().body(iae.getMessage());
    }

}
