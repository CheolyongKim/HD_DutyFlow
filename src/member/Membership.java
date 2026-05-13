package member;

public enum Membership {
    PRESTIGE(1),
    BLACK(2),
    GOLD(3),
    SILVER(4);

    private final int priority;

    Membership(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}