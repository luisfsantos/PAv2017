package ist.meic.pa;

import ist.meic.pa.utils.DefaultValues;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;


/**
 * Value wrapper that automatically sets the type's value to the language default value.
 * This should be used instead of the ValueWrapper when the the "value" in the regex Parser's HashMap is null, which
 * means that no value was provided in the KewordArgs annotation argument, which means that the language default
 * type should be used.
 */
public class DefaultTypeValueWrapper extends ValueWrapper {
    public DefaultTypeValueWrapper(CtClass ctClass, String fieldName) throws NotFoundException, CannotCompileException {
       CtClass fieldType =  ctClass.getField(fieldName).getType();
       // TODO IMPORTANT should this really be a string? Or maybe ValueWrapper should be refactored
       // in particular, how will null be distinguished from the string "null" (isSet) attribute?
       defaultValue = DefaultValues.defaultValueFor(fieldType.toClass()).toString();
    }
}
