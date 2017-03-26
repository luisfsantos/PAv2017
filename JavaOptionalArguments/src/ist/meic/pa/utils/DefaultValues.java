package ist.meic.pa.utils;

import java.util.HashMap;
import java.util.Map;

public class DefaultValues {
    static final Map<Class<?>, Object> defaultValues = new HashMap<>();

    static {

        defaultValues.put(int.class, new Integer(0));
        defaultValues.put(short.class, new Short((short) 0));
        defaultValues.put(double.class, new Double(0.0));
        defaultValues.put(long.class, new Long(0L));
        defaultValues.put(boolean.class, Boolean.FALSE);
        defaultValues.put(char.class, new Character('\0'));
        defaultValues.put(byte.class, new Byte((byte) 0));
        defaultValues.put(float.class, new Float(0.0F));
    }

    public static final <T> T defaultValueFor(Class<T> clazz) {
        // Note: for objects that do not have a corresponding primitive type, the default value will be null (all good)
        return (T) defaultValues.get(clazz);
    }
}
