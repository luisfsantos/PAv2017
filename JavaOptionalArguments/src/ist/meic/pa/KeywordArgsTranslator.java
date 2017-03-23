package ist.meic.pa;

import javassist.*;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by lads on 22/03/2017.
 */
public class KeywordArgsTranslator implements Translator {

    private final static Logger logger = Logger.getLogger(KeywordArgsTranslator.class.getName());

    @Override
    public void start(ClassPool classPool) throws NotFoundException, CannotCompileException {

    }

    @Override
    public void onLoad(ClassPool classPool, String s) throws NotFoundException, CannotCompileException {
        CtClass toTranslate = classPool.get(s);
        ConstructorEditor editor = new ConstructorEditor(toTranslate);
        try {
            if (editor.isEditable()) {
                editor.run();
            } else {
                logger.log(Level.INFO, "The class " + s + " does not need to be edited.");
            }
        } catch (ClassNotFoundException e) {
            logger.log(Level.WARNING, "onLoad getAnnotations failed for reason: " + e.getLocalizedMessage());
        }
    }
}
