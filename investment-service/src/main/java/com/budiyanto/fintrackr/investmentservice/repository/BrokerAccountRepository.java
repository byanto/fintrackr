package com.budiyanto.fintrackr.investmentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.budiyanto.fintrackr.investmentservice.domain.BrokerAccount;

public interface BrokerAccountRepository extends JpaRepository<BrokerAccount, Long>     {

}
