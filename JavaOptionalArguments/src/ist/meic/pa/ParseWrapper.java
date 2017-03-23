package ist.meic.pa;

import javassist.CtClass;

import java.util.HashMap;

/**
 * Created by lads on 23/03/2017.
 */
public class ParseWrapper {

    KeywordArgs kwAnnotation;
    CtClass ctClass;
    HashMap<String, ValueWrapper> values = new HashMap<>();

    public ParseWrapper(KeywordArgs keywordArgs, CtClass ctClass) {
        kwAnnotation = keywordArgs;
        this.ctClass = ctClass;
    }

    public HashMap<String, ValueWrapper> parse() {
        //TODO: regex code which also puts in the values all the inherited values from the super classes
        return values;
    }


}
