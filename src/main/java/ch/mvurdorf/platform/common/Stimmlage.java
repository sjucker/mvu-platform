package ch.mvurdorf.platform.common;

public enum Stimmlage implements LocalizedEnum {
    C("C"),
    B("B"),
    ES("ES");

    private final String description;

    Stimmlage(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

}
