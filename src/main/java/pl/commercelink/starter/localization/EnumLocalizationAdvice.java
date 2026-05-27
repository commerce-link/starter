package pl.commercelink.starter.localization;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
class EnumLocalizationAdvice {
    private final EnumLocalizer enumLocalizer;

    EnumLocalizationAdvice(EnumLocalizer enumLocalizer) {
        this.enumLocalizer = enumLocalizer;
    }

    @ModelAttribute("enumI18n")
    EnumLocalizer enumI18n() {
        return enumLocalizer;
    }
}
