package com.budiyanto.fintrackr.portfolio.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PortfolioJpaRepository extends JpaRepository<PortfolioJpaEntity, UUID> {

}
