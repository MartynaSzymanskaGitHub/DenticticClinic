package pl.kapj.dent.radwanskadent.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kapj.dent.radwanskadent.service.IInvoiceService;

import java.security.Principal;

@RestController
@RequestMapping("/invoice")
public class InvoiceRestController {

    private final IInvoiceService invoiceService;

    public InvoiceRestController(IInvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    @GetMapping("/generate/{userId}")
    public ResponseEntity<byte[]> generate(@PathVariable("userId") Long userId,
                                           @RequestParam("appointmentId") Long appointmentId,
                                           Principal principal) {
        byte[] pdfBytes = invoiceService.generateInvoicePdf(userId, appointmentId, principal);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"faktura-" + appointmentId + ".pdf\"")
                .body(pdfBytes);
    }
}
