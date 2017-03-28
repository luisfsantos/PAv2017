package ist.meic.pa;

import ist.meic.pa.utils.SearchClass;
import javassist.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by lads on 23/03/2017.
 */
public class ConstructorEditor {

    CtClass ctClass;
    Optional<CtConstructor> ctConstructor = Optional.empty();
    HashMap<String, ValueWrapper> keyWordArguments;
    List<String> sortedParameters;
    String name;
    static final String GLOBAL_SETTER = "global$setter";

    private final static Logger logger = Logger.getLogger(ConstructorEditor.class.getName());

    public ConstructorEditor(CtClass ctClass) {
        this.ctClass = ctClass;
    }

    public void run() throws ClassNotFoundException, CannotCompileException, NotFoundException {
        logger.log(Level.INFO,"Editing " + ctClass.getName() + " to fix constructor");
        if (!ctConstructor.isPresent()) {
            logger.log(Level.WARNING, "Not in a position to edit a constructor.");
            return;
        }
        ParseWrapper parser = new ParseWrapper((KeywordArgs) ctConstructor.get().getAnnotation(KeywordArgs.class), ctClass);
        keyWordArguments = parser.parse();
        sortedParameters = parser.getSortedParameters(keyWordArguments);
        injectFieldGetter();
        injectDefaultConstructor(keyWordArguments);
        injectCodeAnnotatedConstructor(keyWordArguments);
    }

    private void injectDefaultConstructor(HashMap<String, ValueWrapper> keyWordArguments) throws CannotCompileException {
        StringBuilder defaultConstructor = new StringBuilder();
        defaultConstructor.append("public " + ctClass.getName() + "() {");
        for (String field : sortedParameters) {
            defaultConstructor.append(field);
            defaultConstructor.append("=");
            defaultConstructor.append(keyWordArguments.get(field).getDefaultValue());
            defaultConstructor.append(";");
        }
        defaultConstructor.append(" }");
        logger.log(Level.INFO, "The default constructor is: " + defaultConstructor.toString());
        CtConstructor newConstructor = new CtNewConstructor().make(defaultConstructor.toString(), ctClass);
        ctClass.addConstructor(newConstructor);
    }

    private void injectFieldGetter() throws CannotCompileException {
        // inject auxiliary method - get field from name, including inherited fields
        String auxMethodTemplate = "private java.lang.reflect.Field getField$injected(java.lang.String name) {" +
                "        java.lang.Class type = this.getClass();" +
                "        do {" +
                "            try {" +
                "                return type.getDeclaredField(name);" +
                "            } catch (java.lang.NoSuchFieldException e) {" +
                "                type = type.getSuperclass();" +
                "            }" +
                "        } while (type != null);" +
                "        return null;" +
                "    }";
        CtMethod method = CtMethod.make(auxMethodTemplate, ctClass);
        ctClass.addMethod(method);
    }
  
    private void injectCodeAnnotatedConstructor(HashMap<String, ValueWrapper> keyWordArguments) throws CannotCompileException {
        if (!ctConstructor.isPresent()) {
            logger.log(Level.SEVERE, "There is no constructor in which to inject code.");
            return;
        }
        CtConstructor constructor = ctConstructor.get();
        StringBuilder template = new StringBuilder();
        template.append("{");
        // assign default values
        for (String field : sortedParameters) {
            template.append(field);
            template.append("=");
            template.append(keyWordArguments.get(field).getDefaultValue());
            template.append(";");
        }

        // overwrite defaults when applicable
        template.append(
                "for (int i = 0; i < $1.length; i+=2) {" +
                        "java.lang.reflect.Field field = getField$injected((java.lang.String)$1[i]);" +
                        "if (field == null) {" +
                            "throw new RuntimeException(\"Unrecognized keyword: \" + (java.lang.String)$1[i]);" +
                        "}" +
                        "field.set(this, $1[i+1]);" +
                "}");

        template.append("}");
        constructor.setBody(template.toString());
    }

    public boolean isEditable() throws NotFoundException, ClassNotFoundException {
        if (ctConstructor.isPresent()) {
            return true;
        } else {
            ctConstructor = SearchClass.getAnnotatedConstructor(ctClass);
            return ctConstructor.isPresent();
        }
    }
}
