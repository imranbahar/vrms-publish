package com.vsms.vsms.repositories;

import com.vsms.vsms.models.Withdraw;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WithdrawRepository extends JpaRepository<Withdraw, Integer> {
    List<Withdraw> findByOwnerID(int ownerID);
    List<Withdraw> findByWithdrawStatus(String withdrawStatus);
}
