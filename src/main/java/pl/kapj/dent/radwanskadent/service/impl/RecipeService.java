package pl.kapj.dent.radwanskadent.service.impl;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
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
import pl.kapj.dent.radwanskadent.model.AppointmentStatus;
import pl.kapj.dent.radwanskadent.model.Recipe;
import pl.kapj.dent.radwanskadent.model.Role;
import pl.kapj.dent.radwanskadent.model.User;
import pl.kapj.dent.radwanskadent.repository.AppointmentRepository;
import pl.kapj.dent.radwanskadent.repository.RecipeRepository;
import pl.kapj.dent.radwanskadent.repository.UserRepository;
import pl.kapj.dent.radwanskadent.service.IRecipeService;

import java.io.ByteArrayOutputStream;
import java.security.Principal;

@Service
@Transactional
public class RecipeService implements IRecipeService {

    private static final Logger log = LoggerFactory.getLogger(RecipeService.class);

    private final RecipeRepository recipeRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public RecipeService(RecipeRepository recipeRepository,
                         AppointmentRepository appointmentRepository,
                         UserRepository userRepository) {
        this.recipeRepository = recipeRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Recipe createRecipe(Long appointmentId, String description, User requester) {
        if (requester == null || requester.getRole() != Role.DENTIST) {
            throw new IllegalStateException("label.accessDenied");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("recipe.error.emptyDescription");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("appointment.error.notFound"));

        if (!appointment.getDentist().getId().equals(requester.getId())) {
            throw new IllegalStateException("label.accessDenied");
        }
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED
                && appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("recipe.error.notPaid");
        }
        Recipe recipe = new Recipe();
        recipe.setAppointment(appointment);
        recipe.setDescription(description.trim());
        return recipeRepository.save(recipe);
    }

    @Override
    @Transactional(readOnly = true)
    public Recipe getRecipe(Long recipeId) {
        return recipeRepository.findById(recipeId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateRecipePdf(Long recipeId, Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new AccessDeniedException("label.accessDenied");
        }

        Recipe recipe = getRecipe(recipeId);
        if (recipe == null) {
            throw new EntityNotFoundException("Recipe not found");
        }

        Appointment appointment = recipe.getAppointment();

        User requester = userRepository.findByEmailIgnoreCase(principal.getName().trim()).orElse(null);
        boolean authorized = requester != null && (
                requester.getRole() == Role.ADMIN
                || appointment.getPatient().getId().equals(requester.getId())
                || appointment.getDentist().getId().equals(requester.getId())
        );
        if (!authorized) {
            throw new AccessDeniedException("label.accessDenied");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            BaseFont bfRegular = BaseFont.createFont("fonts/Roboto-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            BaseFont bfBold = BaseFont.createFont("fonts/Roboto-Bold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font titleFont = new Font(bfBold, 16);
            Font textFont = new Font(bfRegular, 12);

            document.add(new Paragraph("RECEPTA LEKARSKA nr " + recipe.getId(), titleFont));
            document.add(new Paragraph("Data wystawienia: " + (recipe.getIssuedAt() != null ? recipe.getIssuedAt().format(DateTimeFormats.DATE_TIME) : ""), textFont));
            document.add(new Paragraph("Lekarz: lek. dent. " + appointment.getDentist().getFirstName() + " " + appointment.getDentist().getLastName(), textFont));
            document.add(new Paragraph("Pacjent: " + appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName(), textFont));
            document.add(new Paragraph("\nPrzepisane leki / dawkowanie:\n" + recipe.getDescription(), textFont));
        } catch (Exception e) {
            log.error("Błąd generowania recepty PDF ID={}: {}", recipeId, e.getMessage(), e);
            throw new IllegalStateException("Błąd generowania recepty PDF", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
        return baos.toByteArray();
    }
}
