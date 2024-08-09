package model;

public enum Type {
    TASK,
    EPIC,
    SUBTASK;

    public static Type valueOfType(String value) {
        for (Type type : Type.values()) {
            if (type.name().equals(value)) {
                return type;
            }
        }
        return null;
    }
}



