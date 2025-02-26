package github.kasuminova.mmce.common.util;

public class DataReference<T> {

    private T value;

    public DataReference(final T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(final T value) {
        this.value = value;
    }
}
