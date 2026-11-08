package pl.kapj.dent.radwanskadent.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kapj.dent.radwanskadent.service.IRecipeService;

import java.security.Principal;

@RestController
@RequestMapping("/recipe")
public class RecipeRestController {

    private final IRecipeService recipeService;

    public RecipeRestController(IRecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('DENTIST') or hasRole('CLIENT')")
    @GetMapping("/pdf/{recipeId}")
    public ResponseEntity<byte[]> generatePdf(@PathVariable("recipeId") Long recipeId,
                                              Principal principal) {
        byte[] pdfBytes = recipeService.generateRecipePdf(recipeId, principal);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"recepta-" + recipeId + ".pdf\"")
                .body(pdfBytes);
    }
}
