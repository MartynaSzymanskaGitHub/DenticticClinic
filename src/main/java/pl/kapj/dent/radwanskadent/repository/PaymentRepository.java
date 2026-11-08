package pl.kapj.dent.radwanskadent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.kapj.dent.radwanskadent.model.Payment;

import java.math.BigDecimal;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paid = true AND p.appointment.status = pl.kapj.dent.radwanskadent.model.AppointmentStatus.COMPLETED")
    BigDecimal sumCompletedPaidRevenue();

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paid = true")
    BigDecimal sumPaidRevenue();

}
