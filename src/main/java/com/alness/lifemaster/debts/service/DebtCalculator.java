package com.alness.lifemaster.debts.service;

import java.math.*;

import com.alness.lifemaster.debts.entity.DebtsEntity;
import com.alness.lifemaster.debts.entity.PaymentsEntity;

public final class DebtCalculator {
    private DebtCalculator() {
    }

    public static BigDecimal paidAmount(DebtsEntity debt) {
        if (debt.getPayments() == null) {
            return BigDecimal.ZERO;
        }
        return debt.getPayments().stream()
                .filter(payment -> Boolean.TRUE.equals(payment.getIsPaid()))
                .map(PaymentsEntity::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal outstandingAmount(DebtsEntity debt) {
        return debt.getTotalAmount().subtract(paidAmount(debt)).max(BigDecimal.ZERO);
    }

    public static int paymentsMade(DebtsEntity debt) {
        return debt.getPayments() == null ? 0
                : (int) debt.getPayments().stream().filter(p -> Boolean.TRUE.equals(p.getIsPaid())).count();
    }

    public static BigDecimal progressPercentage(DebtsEntity debt) {
        if (debt.getTotalAmount().signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return paidAmount(debt).multiply(BigDecimal.valueOf(100))
                .divide(debt.getTotalAmount(), 2, RoundingMode.HALF_UP)
                .min(BigDecimal.valueOf(100));
    }

    public static void synchronize(DebtsEntity debt) {
        debt.setPaymentsMade(paymentsMade(debt));
        debt.setIsFullyPaid(outstandingAmount(debt).signum() == 0);
    }
}
