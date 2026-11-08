package pl.kapj.dent.radwanskadent.service;

import pl.kapj.dent.radwanskadent.model.Recipe;
import pl.kapj.dent.radwanskadent.model.User;

import java.security.Principal;

public interface IRecipeService {

    Recipe createRecipe(Long appointmentId, String description, User requester);

    Recipe getRecipe(Long recipeId);

    byte[] generateRecipePdf(Long recipeId, Principal principal);
}
