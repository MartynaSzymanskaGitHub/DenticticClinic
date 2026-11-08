package pl.kapj.dent.radwanskadent.service.impl;

import org.springframework.stereotype.Service;
import pl.kapj.dent.radwanskadent.repository.PaymentRepository;
import pl.kapj.dent.radwanskadent.service.IPaymentService;

import java.math.BigDecimal;

@Service("paymentService")
public class PaymentService implements IPaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public BigDecimal getTotalRevenue() {
        BigDecimal sum = paymentRepository.sumCompletedPaidRevenue();
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getPaidRevenue() {
        BigDecimal sum = paymentRepository.sumPaidRevenue();
        return sum != null ? sum : BigDecimal.ZERO;
    }

}
