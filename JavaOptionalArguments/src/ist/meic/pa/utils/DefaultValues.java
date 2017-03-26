package ist.meic.pa.utils;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtPrimitiveType;

import java.util.HashMap;
import java.util.Map;

public class DefaultValues {
    static final Map<CtClass, Object> defaultValues = new HashMap<>();

    static {

        defaultValues.put(CtPrimitiveType.intType, new Integer(0));
        defaultValues.put(CtPrimitiveType.shortType, new Short((short) 0));
        defaultValues.put(CtPrimitiveType.doubleType, new Double(0.0));
        defaultValues.put(CtPrimitiveType.longType, new Long(0L));
        defaultValues.put(CtPrimitiveType.booleanType, Boolean.FALSE);
        defaultValues.put(CtPrimitiveType.booleanType, new Character('\0'));
        defaultValues.put(CtPrimitiveType.byteType, new Byte((byte) 0));
        defaultValues.put(CtPrimitiveType.floatType, new Float(0.0F));
    }

    public static final <T> T defaultValueFor(CtClass clazz) {
        // Note: for objects that do not have a corresponding primitive type, the default value will be null (all good)
        return (T) defaultValues.get(clazz);
    }
}
