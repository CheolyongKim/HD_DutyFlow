package member;

public enum Grade {
    PRESTIGE(1),
    BLACK(2),
    GOLD(3),
    SILVER(4);

    private final int priority;

    Grade(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}