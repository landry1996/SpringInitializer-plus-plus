package com.springforge.i18n;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/i18n")
public class I18nController {

    private final I18nService i18nService;

    public I18nController(I18nService i18nService) {
        this.i18nService = i18nService;
    }

    @GetMapping("/locales")
    public List<LocaleInfo> getSupportedLocales() {
        return i18nService.getSupportedLocales();
    }

    @GetMapping("/messages")
    public Map<String, String> getMessages(Locale locale) {
        return i18nService.getMessagesForLocale(locale);
    }

    @GetMapping("/messages/{localeCode}")
    public Map<String, String> getMessagesByLocale(@PathVariable String localeCode) {
        Locale locale = Locale.forLanguageTag(localeCode);
        return i18nService.getMessagesForLocale(locale);
    }
}
