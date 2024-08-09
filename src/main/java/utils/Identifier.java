package utils;

public enum Identifier {

    INSTANCE;

    private int identifier = 1;

    public int generate() {
        return identifier++;
    }
}
