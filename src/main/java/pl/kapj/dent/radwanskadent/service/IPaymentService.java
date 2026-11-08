package pl.kapj.dent.radwanskadent.service;

import java.math.BigDecimal;

public interface IPaymentService {

    BigDecimal getTotalRevenue();

    BigDecimal getPaidRevenue();

}
