package common;

public enum CategoryType {
    ALCOHOL,
    PERFUME,
    ELECTRONICS,
    GENERAL,
    FOOD;

    public static CategoryType from(String name) {
        return CategoryType.valueOf(name.toUpperCase());
    }
}