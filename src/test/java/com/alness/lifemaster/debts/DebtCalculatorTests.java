package com.alness.lifemaster.debts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.alness.lifemaster.debts.entity.DebtsEntity;
import com.alness.lifemaster.debts.entity.PaymentsEntity;
import com.alness.lifemaster.debts.service.DebtCalculator;

class DebtCalculatorTests {
    @Test
    void interestDoesNotReduceOutstandingPrincipal() {
        DebtsEntity debt = new DebtsEntity();
        debt.setTotalAmount(new BigDecimal("1000.00"));

        PaymentsEntity payment = new PaymentsEntity();
        payment.setIsPaid(true);
        payment.setPrincipalAmount(new BigDecimal("200.00"));
        payment.setInterestAmount(new BigDecimal("50.00"));
        payment.setAmountPaid(new BigDecimal("250.00"));
        debt.setPayments(List.of(payment));

        assertEquals(new BigDecimal("200.00"), DebtCalculator.paidAmount(debt));
        assertEquals(new BigDecimal("800.00"), DebtCalculator.outstandingAmount(debt));
    }
}
