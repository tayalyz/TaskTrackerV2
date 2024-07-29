package utils;

public enum Identifier {

    INSTANCE;

    private int identifier = 0;

    public int generate() {
        return identifier++;
    }

}
