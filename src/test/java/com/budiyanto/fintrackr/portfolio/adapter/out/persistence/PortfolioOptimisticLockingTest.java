package com.budiyanto.fintrackr.portfolio.adapter.out.persistence;


import org.junit.jupiter.api.DisplayName;

class PortfolioOptimisticLockingTest {

    @DisplayName("Reject a stale save when a concurrent transaction updated the portfolio first")
    void should_rejectStaleSave_when_concurrentTransactionUpdatedPortfolioFirst() {

    }

}
