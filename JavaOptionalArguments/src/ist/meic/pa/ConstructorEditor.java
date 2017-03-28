package ist.meic.pa;

import ist.meic.pa.utils.SearchClass;
import javassist.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by lads on 23/03/2017.
 */
public class ConstructorEditor {

    CtClass ctClass;
    Optional<CtConstructor> ctConstructor = Optional.empty();
    HashMap<String, ValueWrapper> keyWordArguments;
    List<String> sortedParameters;
    String name;
    static final String DEFAULT = "$default";

    private final static Logger logger = Logger.getLogger(ConstructorEditor.class.getName());

    public ConstructorEditor(CtClass ctClass) {
        this.ctClass = ctClass;
    }

    public void run() throws ClassNotFoundException, CannotCompileException, NotFoundException {
        logger.log(Level.INFO,"Editing " + ctClass.getName() + " to fix constructor");
        if (!isEditable()) {
            logger.warning( "Not in a position to edit constructor of " + ctClass.getName());
            return;
        }
        ParseWrapper keywordArgumentsParser = new ParseWrapper((KeywordArgs) ctConstructor.get().getAnnotation(KeywordArgs.class), ctClass);
        keyWordArguments = keywordArgumentsParser.parse();
        sortedParameters = keywordArgumentsParser.getSortedParameters();
        injectFieldGetter();
        injectDefaultParameters();
        injectUpdater();
        injectDefaultConstructor();
        injectCodeAnnotatedConstructor();
    }


    public boolean isEditable() throws NotFoundException, ClassNotFoundException {
        if (ctConstructor.isPresent()) {
            return true;
        } else {
            ctConstructor = SearchClass.getAnnotatedConstructor(ctClass);
            return ctConstructor.isPresent();
        }
    }

    private void injectDefaultConstructor() throws CannotCompileException {
        StringBuilder defaultConstructor = new StringBuilder();
        defaultConstructor.append("public " + ctClass.getName() + "() {");
        defaultConstructor.append("update$all();");
        defaultConstructor.append(" }");
        logger.log(Level.INFO, "The default constructor is: " + defaultConstructor.toString());
        CtConstructor newConstructor = new CtNewConstructor().make(defaultConstructor.toString(), ctClass);
        ctClass.addConstructor(newConstructor);
    }

    private void injectFieldGetter() throws CannotCompileException {
        // inject auxiliary method - get field from name, including inherited fields
        String auxMethodTemplate = "private java.lang.reflect.Field getField$injected(java.lang.String name, java.lang.Class type) {" +
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
  
    private void injectCodeAnnotatedConstructor() throws CannotCompileException {
        if (!ctConstructor.isPresent()) {
            logger.log(Level.SEVERE, "There is no constructor in which to inject code.");
            return;
        }
        CtConstructor constructor = ctConstructor.get();
        StringBuilder template = new StringBuilder();
        template.append("{");

        // overwrite defaults when applicable
        template.append("java.lang.Class my$Class = this.getClass();");
        template.append(
                "for (int i = 0; i < $1.length; i+=2) {" +
                        "java.lang.reflect.Field field = getField$injected((java.lang.String)$1[i], my$Class);" +
                        "if (field == null) {" +
                            "throw new RuntimeException(\"Unrecognized keyword: \" + (java.lang.String)$1[i]);" +
                        "}" +
                        "field.set(this, $1[i+1]);" +
                        "getField$injected((java.lang.String)$1[i] + \""+ DEFAULT + "\", my$Class).setBoolean(this, false);" +
                        "}");
        template.append("update$all();");
        template.append("}");
        constructor.setBody(template.toString());
    }

    private void injectUpdater() throws CannotCompileException {
        StringBuilder updater = new StringBuilder();

        updater.append("public void update$all() { ");
        CtClass superClass;
        try {
            superClass = ctClass.getSuperclass();
            if(SearchClass.getAnnotatedConstructor(superClass).isPresent()) {
                updater.append("super.update$all();");
            }
        } catch (NotFoundException e) {

        } finally {
            sortedParameters.stream().forEach(s -> updater.append("if (" + s + DEFAULT + ") {" +
                    s + " = " + keyWordArguments.get(s) + ";" +
                    "} "));
            updater.append("}");
        }
        logger.info("Updater method is: " + updater.toString());
        ctClass.addMethod(CtNewMethod.make(updater.toString(), ctClass));
    }


    private void injectDefaultParameters() throws CannotCompileException {
        for (String s: sortedParameters) {
            try {
                if(ctClass.getDeclaredField(s) != null) {
                    ctClass.addField(CtField.make("protected boolean " + s + DEFAULT + "=true;", ctClass));
                }
            } catch (NotFoundException e) {
            }
        }
    }
}
