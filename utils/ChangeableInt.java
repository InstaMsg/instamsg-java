package utils;

public class ChangeableInt {
    int i;

    public ChangeableInt(int i) {
        this.i = i;
    }

    public void changeTo(int i) {
        this.i = i;
    }

    public int intValue() {
        return i;
    }
}