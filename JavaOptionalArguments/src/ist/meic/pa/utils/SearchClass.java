package ist.meic.pa.utils;

import ist.meic.pa.ConstructorEditor;
import ist.meic.pa.KeywordArgs;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.Loader;
import javassist.NotFoundException;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by lads on 27/03/2017.
 */
public class SearchClass {

    private final static Logger logger = Logger.getLogger(SearchClass.class.getName());

    public static Optional<CtConstructor> getAnnotatedConstructor(CtClass clazz) {
        CtConstructor[] ctors = clazz.getConstructors();
        logger.info("# Start cycling through ctors of " + clazz.getName());
        for (CtConstructor ctor : ctors) {
            logger.info("\t* current ctor: " + ctor.toString());
            if (ctor.hasAnnotation(KeywordArgs.class)) {
                logger.info("\t* " + ctor.toString() + " has KeywordArgs annotation");
                return Optional.of(ctor);
            }
            logger.info("\t* " + ctor.toString() + " does not have KeywordArgs annotation");
        }
        logger.info("# Stop cycling through ctors of " + clazz.getName());

        return Optional.empty();
    }

    public static Optional<String> getKwargsStringFromCtor(CtClass clazz) throws ClassNotFoundException {
        Optional<CtConstructor> ctor = getAnnotatedConstructor(clazz);
        if (ctor.isPresent()) {
            CtConstructor annotatedCtor = ctor.get();
            KeywordArgs kwargsAnnotation = (KeywordArgs) annotatedCtor.getAnnotation(KeywordArgs.class);
            String kwargsStr = kwargsAnnotation.value();
            logger.info("Annotation for " + clazz.getName() + " " + kwargsStr);
            return Optional.of(kwargsStr);
        }
        return Optional.empty();
    }

}
