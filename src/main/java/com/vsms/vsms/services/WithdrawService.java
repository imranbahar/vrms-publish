package com.vsms.vsms.services;

import com.vsms.vsms.models.Withdraw;
import com.vsms.vsms.repositories.WithdrawRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WithdrawService {
    @Autowired
    private WithdrawRepository withdrawRepository;

    public void saveWithdraw(Withdraw withdraw) {
        withdrawRepository.save(withdraw);
    }

    public List<Withdraw> findByOwnerID(int ownerID) {
        return withdrawRepository.findByOwnerID(ownerID);
    }

    public List<Withdraw> findByWithdrawStatus(String status) {
        return withdrawRepository.findByWithdrawStatus(status);
    }

    public Withdraw findById(int withdrawID) {
        return withdrawRepository.findById(withdrawID).orElse(null);
    }

    public List<Withdraw> findAllWithdraws() {
    return withdrawRepository.findAll();
}
}