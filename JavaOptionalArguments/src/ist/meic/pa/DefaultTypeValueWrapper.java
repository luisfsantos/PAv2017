package ist.meic.pa;

import ist.meic.pa.utils.DefaultValues;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import java.util.logging.Logger;



/**
 * Value wrapper that automatically sets the type's value to the language default value.
 * This should be used instead of the ValueWrapper when the the "value" in the regex Parser's HashMap is null, which
 * means that no value was provided in the KewordArgs annotation argument, which means that the language default
 * type should be used.
 */
public class DefaultTypeValueWrapper extends ValueWrapper {
    private static final Logger logger = Logger.getLogger(DefaultTypeValueWrapper.class.getName());

    public DefaultTypeValueWrapper(CtClass ctClass, String fieldName) throws NotFoundException, CannotCompileException {
       logger.info("DefaultValue request for class:field_name " + ctClass.toString() + ":" +fieldName);

        CtClass fieldType =  ctClass.getField(fieldName).getType();

       Object defValue = DefaultValues.defaultValueFor(fieldType);
        if (defValue == null) {
            defaultValue = null;
        } else {
            defaultValue = defValue.toString();
        }
    }
}
