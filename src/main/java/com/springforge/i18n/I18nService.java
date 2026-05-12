package com.springforge.i18n;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class I18nService {

    private final MessageSource messageSource;

    private static final List<LocaleInfo> SUPPORTED_LOCALES = List.of(
        new LocaleInfo("en", "English", "English"),
        new LocaleInfo("fr", "French", "Français"),
        new LocaleInfo("de", "German", "Deutsch"),
        new LocaleInfo("es", "Spanish", "Español")
    );

    public I18nService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String key, Object[] args, Locale locale) {
        return messageSource.getMessage(key, args, key, locale);
    }

    public List<LocaleInfo> getSupportedLocales() {
        return SUPPORTED_LOCALES;
    }

    public Map<String, String> getMessagesForLocale(Locale locale) {
        Map<String, String> messages = new LinkedHashMap<>();
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        for (String key : bundle.keySet()) {
            messages.put(key, bundle.getString(key));
        }
        return messages;
    }
}
