package ist.meic.pa;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by lads on 23/03/2017.
 */
public class ConstructorEditor {

    CtClass ctClass;
    Optional<CtConstructor> ctConstructor = Optional.empty();
    HashMap<String, ValueWrapper> keyWordArguments;

    private final static Logger logger = Logger.getLogger(ConstructorEditor.class.getName());

    public ConstructorEditor(CtClass ctClass) {
        this.ctClass = ctClass;
    }

    public void run() throws ClassNotFoundException {
        logger.log(Level.INFO,"Editing " + ctClass.getName() + " to fix constructor");
        if (!ctConstructor.isPresent()) {
            logger.log(Level.WARNING, "Not in a position to edit a constructor.");
            return;
        }
        keyWordArguments = new ParseWrapper((KeywordArgs) ctConstructor.get().getAnnotation(KeywordArgs.class), ctClass).parse();

    }

    public Optional<KeywordArgs> findAnnotation(CtConstructor ctConstructor) throws ClassNotFoundException {
        Object[] annotations = ctConstructor.getAnnotations();
        for (Object annotation: annotations) {
            if (annotation instanceof KeywordArgs) {
                return Optional.of((KeywordArgs) annotation);
            }
        }
        logger.log(Level.INFO,"The constructor " + ctConstructor.getLongName() + " does not have an annotation");
        return Optional.empty();
    }

    public boolean hasAnnotation(CtConstructor ctConstructor) {
        try {
            return findAnnotation(ctConstructor).isPresent();
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Optional<CtConstructor> findEditableConstructor() throws NotFoundException, ClassNotFoundException {
        Stream<CtConstructor> constructors = Arrays.stream(ctClass.getConstructors());
        return constructors.filter(c -> hasAnnotation(c))
                .peek(c -> logger.log(Level.INFO, "Constructor " + c.getLongName() + " found with correct annotation"))
                .findFirst();
    }

    public boolean isEditable() throws NotFoundException, ClassNotFoundException {
        if (ctConstructor.isPresent()) {
            return true;
        } else {
            ctConstructor = findEditableConstructor();
            return ctConstructor.isPresent();
        }
    }
}
