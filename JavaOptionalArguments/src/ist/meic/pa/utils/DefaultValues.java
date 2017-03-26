package ist.meic.pa.utils;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtPrimitiveType;

import java.util.HashMap;
import java.util.Map;

public class DefaultValues {
    static final Map<CtClass, String> defaultValues = new HashMap<>();

    static {

        defaultValues.put(CtPrimitiveType.intType, "0");
        defaultValues.put(CtPrimitiveType.shortType, "0");
        defaultValues.put(CtPrimitiveType.doubleType, "0.0");
        defaultValues.put(CtPrimitiveType.longType, "0L");
        defaultValues.put(CtPrimitiveType.booleanType, "false");
        defaultValues.put(CtPrimitiveType.charType, "Character.MIN_VALUE");
        defaultValues.put(CtPrimitiveType.byteType, "0");
        defaultValues.put(CtPrimitiveType.floatType, "0.0F");
    }

    public static final String defaultValueFor(CtClass clazz) {
        // Note: for objects that do not have a corresponding primitive type, the default value will be null (all good)
        return defaultValues.get(clazz);
    }
}
