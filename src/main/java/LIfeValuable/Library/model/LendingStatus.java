package LifeValuable.Library.model;

public enum LendingStatus {
    ACTIVE("Активно"),
    RETURNED("Возвращено"),
    OVERDUE("Просрочено");

    private final String displayName;

    LendingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return this == ACTIVE || this == OVERDUE;
    }
}
