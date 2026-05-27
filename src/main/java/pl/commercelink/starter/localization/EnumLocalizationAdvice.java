package pl.commercelink.starter.localization;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
class EnumLocalizationAdvice {
    private final EnumLocalizer enumLocalizer;

    @ModelAttribute("enumI18n")
    EnumLocalizer enumI18n() {
        return enumLocalizer;
    }
}
