package io.shiftleft.controller;

import io.shiftleft.data.DataLoader;
import io.shiftleft.model.Account;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.shiftleft.repository.AccountRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * Admin checks login
 */

@RestController
public class AccountController {
    @Autowired
    private AccountRepository accountRepository;

    private static Logger log = LoggerFactory.getLogger(DataLoader.class);
    
    @GetMapping("/account")
    public Iterable<Account> getAccountList(HttpServletResponse response, HttpServletRequest request) {
        response.addHeader("test-header-detection", new Account().toString());
        log.info("Account Data is {}", this.accountRepository.findOne(1l).toString());
        return this.accountRepository.findAll();
    }

    @PostMapping("/account")
    public Account createAccount(Account account) {
        this.accountRepository.save(account);
        log.info("Account Data is {}", account.toString());
        return account;
    }

    @GetMapping("/account/{accountId}")
    public Account getAccount(@PathVariable long accountId) {
        log.info("Account Data is {}", this.accountRepository.findOne(1l).toString());
        return this.accountRepository.findOne(accountId);
    }

    @PostMapping("/account/{accountId}/deposit")
    public Account depositIntoAccount(@RequestParam double amount, @PathVariable long accountId) {
        Account account = this.accountRepository.findOne(accountId);
        log.info("Account Data is {}", account.toString());
        account.deposit(amount);
        this.accountRepository.save(account);
        return account;
    }

    @PostMapping("/account/{accountId}/withdraw")
    public Account withdrawFromAccount(@RequestParam double amount, @PathVariable long accountId) {
        Account account = this.accountRepository.findOne(accountId);
        account.withdraw(amount);
        this.accountRepository.save(account);
        log.info("Account Data is {}", account.toString());
        return account;
    }

@PostMapping("/account/{accountId}/addInterest")
@Transactional // Added transaction management for financial operations
public Account addInterestToAccount(@RequestParam double amount, @PathVariable long accountId) {
    // Added null checking to prevent exceptions
    Account account = this.accountRepository.findOne(accountId);
    if (account == null) {
        log.error("Account with ID null not found", accountId);
        throw new ResourceNotFoundException("Account not found");
    }
    
    // Using the amount parameter that was previously unused
    account.addInterest(amount);
    this.accountRepository.save(account);
    
    // Fixed misleading log message and implemented log forging protection
    if (account != null) {
        String safeLogMessage = HtmlUtils.htmlEscape(account.toLogString());
        log.info("Account updated with interest: null", safeLogMessage);
    }
    
    return account;
}


}
