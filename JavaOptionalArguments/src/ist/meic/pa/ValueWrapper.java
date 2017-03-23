package ist.meic.pa;

/**
 * Created by lads on 23/03/2017.
 */
public class ValueWrapper {
    // This is so we can keep extra info about the arguments if we will need it while working
    // right now I could only identify the value
    String defaultValue;

    public ValueWrapper(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
