package pl.kapj.dent.radwanskadent.exception;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.kapj.dent.radwanskadent.controller.BaseController;

@ControllerAdvice(assignableTypes = BaseController.class)
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class,
            EntityNotFoundException.class,
            EntityExistsException.class
    })
    public String handleBusinessException(RuntimeException ex,
                                          HttpServletRequest request,
                                          RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", msg(ex.getMessage()));
        return "redirect:" + resolveRedirectPath(request);
    }

    private String resolveRedirectPath(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path == null) {
            return "/home";
        }
        if (path.startsWith("/admin")) {
            return "/admin/dashboard";
        }
        if (path.startsWith("/dentist")) {
            return "/dentist/dashboard";
        }
        if (path.startsWith("/appointment/book") || path.startsWith("/appointment/available-slots")) {
            return "/appointment/book";
        }
        if (path.startsWith("/appointment")) {
            return "/appointment/my";
        }
        if (path.startsWith("/user/profile")) {
            return "/user/profile";
        }
        if (path.startsWith("/user/register")) {
            return "/user/register";
        }
        return "/home";
    }

    private String msg(String code) {
        if (code == null || code.isEmpty()) {
            return "";
        }
        return messageSource.getMessage(code, null, code, LocaleContextHolder.getLocale());
    }
}
