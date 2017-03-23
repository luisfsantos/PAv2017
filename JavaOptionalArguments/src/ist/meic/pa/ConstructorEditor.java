package ist.meic.pa;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by lads on 23/03/2017.
 */
public class ConstructorEditor {

    CtClass ctClass;
    Optional<KeywordArgs> kwAnnotation = Optional.empty();
    Optional<CtConstructor> ctConstructor = Optional.empty();

    private final static Logger logger = Logger.getLogger(ConstructorEditor.class.getName());

    public ConstructorEditor(CtClass ctClass) {
        this.ctClass = ctClass;
    }

    public void run() {
        logger.log(Level.INFO,"Editing " + ctClass.getName() + " to fix constructor");
    }

    public Optional<KeywordArgs> findAnnotation() throws ClassNotFoundException {
        if (!ctConstructor.isPresent()) {
            logger.log(Level.INFO,"There is no constructor that has param Object[] in " + ctClass.getName());
            return kwAnnotation;
        }
        Object[] annotations = ctConstructor.get().getAnnotations();
        for (Object annotation: annotations) {
            if (annotation instanceof KeywordArgs) {
                kwAnnotation = Optional.of((KeywordArgs) annotation);
                return kwAnnotation;
            }
        }
        logger.log(Level.INFO,"There is no constructor that has a KWA in " + ctClass.getName());
        return kwAnnotation;
    }

    public boolean findEditableConstructor() throws NotFoundException, ClassNotFoundException {
        CtConstructor constructors[] = ctClass.getConstructors();
        for (CtConstructor constructor: constructors) {
            if(Arrays.stream(constructor.getParameterTypes())
                    .filter(param -> param.isArray())
                    .count() == 1) { //TODO: this if is wrong and should check if we have a java.lang.Object but that is proving difficult right now
                ctConstructor = Optional.of(constructor);
                return findAnnotation().isPresent();
            }
        }
        return false;
    }
}
