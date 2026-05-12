package com.springforge.i18n;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class I18nServiceTest {

    @Mock
    private MessageSource messageSource;

    private I18nService i18nService;

    @BeforeEach
    void setUp() {
        i18nService = new I18nService(messageSource);
    }

    @Test
    void shouldReturnSupportedLocales() {
        List<LocaleInfo> locales = i18nService.getSupportedLocales();

        assertThat(locales).hasSize(4);
        assertThat(locales).extracting(LocaleInfo::code)
            .containsExactly("en", "fr", "de", "es");
    }

    @Test
    void shouldGetMessageForLocale() {
        when(messageSource.getMessage("app.name", null, "app.name", Locale.FRENCH))
            .thenReturn("SpringForge");

        String message = i18nService.getMessage("app.name", null, Locale.FRENCH);

        assertThat(message).isEqualTo("SpringForge");
    }

    @Test
    void shouldReturnKeyWhenMessageNotFound() {
        when(messageSource.getMessage("unknown.key", null, "unknown.key", Locale.ENGLISH))
            .thenReturn("unknown.key");

        String message = i18nService.getMessage("unknown.key", null, Locale.ENGLISH);

        assertThat(message).isEqualTo("unknown.key");
    }

    @Test
    void shouldGetMessageWithArgs() {
        when(messageSource.getMessage("validation.javaVersion.unsupported", new Object[]{"8"}, "validation.javaVersion.unsupported", Locale.ENGLISH))
            .thenReturn("Java version 8 is not supported");

        String message = i18nService.getMessage("validation.javaVersion.unsupported", new Object[]{"8"}, Locale.ENGLISH);

        assertThat(message).isEqualTo("Java version 8 is not supported");
    }
}
