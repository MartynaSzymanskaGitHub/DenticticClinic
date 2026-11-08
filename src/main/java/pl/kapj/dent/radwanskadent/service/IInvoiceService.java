package pl.kapj.dent.radwanskadent.service;

import java.security.Principal;

public interface IInvoiceService {

    byte[] generateInvoicePdf(Long userId, Long appointmentId, Principal principal);

}
