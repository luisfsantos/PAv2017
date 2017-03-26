package ist.meic.pa;

import javassist.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
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
        keyWordArguments = new ParseWrapper((KeywordArgs) ctConstructor.get().getAnnotation(KeywordArgs.class), ctClass).parse();
        injectFieldGetter();
        injectDefaultConstructor(keyWordArguments);
        injectCodeAnnotatedConstructor(keyWordArguments);
    }

    private void injectDefaultConstructor(HashMap<String, ValueWrapper> keyWordArguments) throws CannotCompileException {
        StringBuilder defaultConstructor = new StringBuilder();
        defaultConstructor.append("public " + ctClass.getName() + "() {");
        for (String field : keyWordArguments.keySet()) {
            defaultConstructor.append(field);
            defaultConstructor.append("=");
            defaultConstructor.append(keyWordArguments.get(field).getDefaultValue());
            defaultConstructor.append(";");
        }
        defaultConstructor.append(" }");
        logger.log(Level.INFO, "The default constructor looks like this: " + defaultConstructor.toString());
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
  
    private void injectCodeAnnotatedConstructor(HashMap<String, ValueWrapper> keyWordArguments) throws CannotCompileException {
        if (!ctConstructor.isPresent()) {
            logger.log(Level.SEVERE, "There is no constructor in which to inject code.");
            return;
        }
        CtConstructor constructor = ctConstructor.get();
        StringBuilder template = new StringBuilder();
        template.append("{");
        // assign default values
        for (String field : keyWordArguments.keySet()) {
            template.append(field);
            template.append("=");
            template.append(keyWordArguments.get(field).getDefaultValue());
            template.append(";");
        }

        // overwrite defaults when applicable
        template.append("java.lang.Class my$Class = this.getClass();");
        template.append(
                "for (int i = 0; i < $1.length; i+=2) {" +
                        "java.lang.reflect.Field field = getField$injected((java.lang.String)$1[i], my$Class);" +
                        "if (field == null) {" +
                            "throw new RuntimeException(\"Unrecognized keyword: \" + (java.lang.String)$1[i]);" +
                        "}" +
                        "field.set(this, $1[i+1]);" +
                "}");

        template.append("}");
        constructor.setBody(template.toString());
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
