package pl.kapj.dent.radwanskadent.service.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kapj.dent.radwanskadent.constants.DateTimeFormats;
import pl.kapj.dent.radwanskadent.model.Appointment;
import pl.kapj.dent.radwanskadent.model.Role;
import pl.kapj.dent.radwanskadent.model.Treatment;
import pl.kapj.dent.radwanskadent.model.User;
import pl.kapj.dent.radwanskadent.repository.UserRepository;
import pl.kapj.dent.radwanskadent.service.IAppointmentService;
import pl.kapj.dent.radwanskadent.service.IInvoiceService;

import java.io.ByteArrayOutputStream;
import java.security.Principal;
import java.util.stream.Collectors;

@Service
@Transactional
public class InvoiceService implements IInvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private final IAppointmentService appointmentService;
    private final UserRepository userRepository;

    public InvoiceService(IAppointmentService appointmentService, UserRepository userRepository) {
        this.appointmentService = appointmentService;
        this.userRepository = userRepository;
    }

    @Override
    public byte[] generateInvoicePdf(Long userId, Long appointmentId, Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new AccessDeniedException("label.accessDenied");
        }

        Appointment appointment = appointmentService.getAppointment(appointmentId);
        if(appointment == null) {
            throw new EntityNotFoundException("appointment.error.notFound");
        }

        User requester = userRepository.findByEmailIgnoreCase(principal.getName().trim())
                .orElseThrow(() -> new AccessDeniedException("label.accessDenied"));

        boolean isOwner = appointment.getPatient().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("label.accessDenied");
        }

        if (userId != null && !appointment.getPatient().getId().equals(userId)) {
            throw new IllegalArgumentException("appointment.error.notFound");
        }

        if (appointment.getPayment() == null || !appointment.getPayment().isPaid()) {
            throw new IllegalStateException("appointment.error.notPaid");
        }
        String invoiceNumber = appointment.getPayment().getInvoiceNumber();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, baos);
            document.open();
            document.add(new Paragraph(Chunk.NEWLINE));

            BaseFont bfRegular = BaseFont.createFont("fonts/Roboto-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            BaseFont bfBold = BaseFont.createFont("fonts/Roboto-Bold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font headerFont = new Font(bfBold, 16);
            Font regularFont = new Font(bfRegular, 12);
            Font footerFont = new Font(bfBold, 12);

            document.add(new Paragraph("Faktura nr: " + invoiceNumber, headerFont));
            document.add(new Paragraph(Chunk.NEWLINE));

            String treatmentsNames = appointment.getTreatments() == null ? "" : appointment.getTreatments().stream()
                    .map(Treatment::getName)
                    .collect(Collectors.joining(", "));

            document.add(new Paragraph("Data wizyty: " + appointment.getDateTime().format(DateTimeFormats.DATE_TIME), regularFont));
            document.add(new Paragraph("Pacjent: " + appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName(), regularFont));
            document.add(new Paragraph("Lekarz: " + appointment.getDentist().getFirstName() + " " + appointment.getDentist().getLastName(), regularFont));
            document.add(new Paragraph("Gabinet: " + appointment.getRoom().getName(), regularFont));
            document.add(new Paragraph("Zabiegi: " + treatmentsNames, regularFont));
            document.add(new Paragraph("Kwota: " + appointment.getPayment().getAmount() + " PLN", regularFont));
            document.addTitle("faktura-" + invoiceNumber);
            document.add(new Paragraph(Chunk.NEWLINE));
            document.add(new Paragraph(Chunk.NEWLINE));

            Paragraph footer = new Paragraph("Radwańska-Dent", footerFont);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);
        } catch (Exception e) {
            log.error("Nieoczekiwany błąd podczas generowania faktury: {}", e.getMessage(), e);
            throw new IllegalStateException("Błąd generowania faktury", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
        return baos.toByteArray();
    }
}
