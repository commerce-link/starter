package pl.commercelink.starter.localization;

public interface LocalizedEnum<T extends Enum<T>> {
    String getLocalizedName();

    static <T extends Enum<T> & LocalizedEnum<T>> T fromLocalizedName(Class<T> enumType, String name) {
        for (T enumConstant : enumType.getEnumConstants()) {
            if (enumConstant.getLocalizedName().equals(name)) {
                return enumConstant;
            }
        }
        throw new IllegalArgumentException("Unknown enum constant for localized name: " + name);
    }
}