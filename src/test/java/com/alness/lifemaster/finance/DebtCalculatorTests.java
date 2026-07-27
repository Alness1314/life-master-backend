package com.alness.lifemaster.finance;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.alness.lifemaster.debts.entity.DebtsEntity;
import com.alness.lifemaster.debts.entity.PaymentsEntity;
import com.alness.lifemaster.debts.service.DebtCalculator;

class DebtCalculatorTests {
    @Test
    void countsOnlyPaidPaymentsAndNeverReturnsNegativeOutstandingAmount() {
        DebtsEntity debt = new DebtsEntity();
        debt.setTotalAmount(new BigDecimal("1000.00"));
        debt.setPayments(List.of(payment("300.00", true), payment("900.00", false)));

        assertThat(DebtCalculator.paidAmount(debt)).isEqualByComparingTo("300.00");
        assertThat(DebtCalculator.outstandingAmount(debt)).isEqualByComparingTo("700.00");
        assertThat(DebtCalculator.paymentsMade(debt)).isEqualTo(1);
        assertThat(DebtCalculator.progressPercentage(debt)).isEqualByComparingTo("30.00");
    }

    private PaymentsEntity payment(String amount, boolean paid) {
        PaymentsEntity payment = new PaymentsEntity();
        payment.setAmountPaid(new BigDecimal(amount));
        payment.setIsPaid(paid);
        return payment;
    }
}
