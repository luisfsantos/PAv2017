package ist.meic.pa;

import java.util.Comparator;

/**
 * Created by lads on 23/03/2017.
 */
public class ValueWrapper implements Comparator<ValueWrapper> {
    // This is so we can keep extra info about the arguments if we will need it while working
    // right now I could only identify the value
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
