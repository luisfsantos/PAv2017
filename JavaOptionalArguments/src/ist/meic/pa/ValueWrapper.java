package ist.meic.pa;

/**
 * Created by lads on 23/03/2017.
 */
public class ValueWrapper {
    // This is so we can keep extra info about the arguments if we will need it while working
    // right now I could only identify the value
    String defaultValue;

    // if true, it means that a default value has been provided, if not it was not
    boolean isSet = true;
    public ValueWrapper(String defaultValue, boolean isSet) {
        this.defaultValue = defaultValue;
        this.isSet = isSet;
    }

    /**
     * Instantiate ValueWrapper with isSet set to true
     * @param defaultValue default value of the parameter
     */
    public ValueWrapper(String defaultValue) {
        this(defaultValue, true);
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isSet() {
        return isSet;
    }
}
