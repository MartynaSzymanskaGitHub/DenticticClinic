package pl.kapj.dent.radwanskadent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.BindingResult;

import java.util.Locale;
import java.util.stream.Collectors;

public abstract class BaseController {

    private MessageSource messageSource;

    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    protected String msg(String code) {
        if (code == null) {
            return "";
        }
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, null, code, locale);
    }

    protected String extractErrors(BindingResult bindingResult) {
        Locale locale = LocaleContextHolder.getLocale();
        return bindingResult.getAllErrors().stream()
                .map(error -> messageSource.getMessage(error, locale))
                .collect(Collectors.joining(" "));
    }
}
