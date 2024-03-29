package com.my.bank.service;

import com.my.bank.dto.responseBody.TransferResponseBody;
import com.my.bank.dto.BankAccountDto;
import com.my.bank.dto.CustomerDto;
import com.my.bank.dto.TransactionDto;
import com.my.bank.repository.AccountRepository;
import com.my.bank.repository.TransactionRepository;
import com.my.bank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

import static com.my.bank.dto.enums.AccountStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.*;

@Service
public class BankAccountTransferRestService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public ResponseEntity<?> transfer(CustomerDto sender, TransferResponseBody responseBody) {
        Optional<BankAccountDto> optRecipientAccount = accountRepository.findByCardNumberAndStatus(responseBody.getRecipientAccount(), ACCEPTED);
        if (optRecipientAccount.isEmpty()) {
            return new ResponseEntity<>("The recipient's account is specified incorrectly", NOT_FOUND);
        }

        Optional<BankAccountDto> optSenderAccount = accountRepository.findByCardNumberAndStatus(responseBody.getSenderAccount(), ACCEPTED);
        if (Objects.equals(
                responseBody.getRecipientAccount(), responseBody.getSenderAccount())
                || optSenderAccount.isEmpty()) {
            return new ResponseEntity<>("Error", BAD_REQUEST);
        }

        BankAccountDto senderAccount = optSenderAccount.get();
        if (!Objects.equals(senderAccount.getCustomer().getCustomerId(), sender.getCustomerId())) {
            return new ResponseEntity<>("Access error", FORBIDDEN);
        }

        if (senderAccount.getSecurityCode() != responseBody.getSecurityCode()) {
            return new ResponseEntity<>("Security code entered incorrectly", CONFLICT);
        }

        Double transferAmount = responseBody.getTransferAmount();
        Double senderBalance = senderAccount.getBalance();
        if (transferAmount > senderBalance) {
            return new ResponseEntity<>("Insufficient funds", PRECONDITION_FAILED);
        }
        if (transferAmount <= 0) {
            return new ResponseEntity<>("Data entry error", PRECONDITION_FAILED);
        }

        senderAccount.setBalance(senderBalance - transferAmount);
        accountRepository.save(senderAccount);

        BankAccountDto recipientAccount = optRecipientAccount.get();
        Double recipientBalance = recipientAccount.getBalance();

        recipientAccount.setBalance(recipientBalance + transferAmount);
        accountRepository.save(recipientAccount);

        TransactionDto transaction = new TransactionDto(transferAmount, responseBody.getMessage(), recipientAccount, senderAccount);
        transactionRepository.save(transaction);

        return new ResponseEntity<>(OK);
    }
}

