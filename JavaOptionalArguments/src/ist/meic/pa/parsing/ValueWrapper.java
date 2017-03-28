package ist.meic.pa.parsing;

import java.util.Comparator;

public class ValueWrapper implements Comparator<ValueWrapper> {
    // This is so we can keep extra info about the DefaultValues
    String defaultValue;

    public ValueWrapper(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public ValueWrapper() {
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        return defaultValue;
    }

    @Override
    public int compare(ValueWrapper first, ValueWrapper second) {
        return first.getDefaultValue().compareTo(second.getDefaultValue());
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ValueWrapper) {
            return defaultValue.equals(((ValueWrapper)other).getDefaultValue());
        }
        return false;
    }
}
