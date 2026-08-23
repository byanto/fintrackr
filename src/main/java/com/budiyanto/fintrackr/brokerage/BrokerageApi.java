package com.budiyanto.fintrackr.brokerage;

import com.budiyanto.fintrackr.shared.BrokerAccountId;
import com.budiyanto.fintrackr.shared.Money;
import com.budiyanto.fintrackr.shared.Quantity;

public interface BrokerageApi {

    Money computeBuyFee(BrokerAccountId accountId, Quantity quantity, Money price);

    void applyCashFlow(BrokerAccountId accountId, Money delta);

}
