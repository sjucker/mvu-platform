package ch.mvurdorf.platform.common;

public enum Stimme implements LocalizedEnum {
    STIMME_1("1. Stimme"),
    STIMME_2("2. Stimme"),
    STIMME_3("3. Stimme"),
    STIMME_4("4. Stimme");

    private final String description;

    Stimme(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
