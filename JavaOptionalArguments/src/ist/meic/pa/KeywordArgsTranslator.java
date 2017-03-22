package ist.meic.pa;

import javassist.*;

/**
 * Created by lads on 22/03/2017.
 */
public class KeywordArgsTranslator implements Translator {

    @Override
    public void start(ClassPool classPool) throws NotFoundException, CannotCompileException {

    }

    @Override
    public void onLoad(ClassPool classPool, String s) throws NotFoundException, CannotCompileException {
        CtClass ctClass = classPool.get(s);
        System.out.println("We are translating xD");
    }
}
