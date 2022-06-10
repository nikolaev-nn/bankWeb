package com.my.bank.service;

import com.my.bank.dto.BankAccountDto;
import com.my.bank.dto.CustomerDto;
import com.my.bank.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

import static com.my.bank.dto.enums.AccountStatus.ACCEPTED;

@Service
public class ProfileService {

    @Autowired
    private AccountRepository accountRepository;

    public ModelAndView getProfile(CustomerDto customer) {
        ModelAndView mav = new ModelAndView();
        List<BankAccountDto> accounts = accountRepository.findAllByCustomerAndStatus(customer, ACCEPTED);
        mav.addObject("accounts", accounts);
        mav.setViewName("profile/profile");
        return mav;
    }
}
