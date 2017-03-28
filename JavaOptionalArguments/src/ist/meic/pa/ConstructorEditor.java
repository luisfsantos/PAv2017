package ist.meic.pa;

import ist.meic.pa.parsing.ParseWrapper;
import ist.meic.pa.parsing.ValueWrapper;
import ist.meic.pa.utils.SearchClass;
import javassist.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConstructorEditor {

    CtClass ctClass;
    Optional<CtConstructor> ctConstructor = Optional.empty();
    HashMap<String, ValueWrapper> fieldValueMap;
    List<String> sortedFields;
    String name;
    static final String DEFAULT = "$default";
    static final String GET_FIELD = "get$Field";

    private final static Logger logger = Logger.getLogger(ConstructorEditor.class.getName());

    public ConstructorEditor(CtClass ctClass) {
        this.ctClass = ctClass;
    }

    public void run() throws ClassNotFoundException, CannotCompileException, NotFoundException {
        logger.log(Level.INFO,"Editing " + ctClass.getName() + " to fix constructor");
        if (!isEditable() || ctClass.isFrozen()) {
            logger.warning( "Not in a position to edit " + ctClass.getName());
            return;
        }
        String kwArgsString = ((KeywordArgs) ctConstructor.get().getAnnotation(KeywordArgs.class)).value();
        ParseWrapper keywordArgumentsParser = new ParseWrapper(kwArgsString, ctClass);
        fieldValueMap = keywordArgumentsParser.parse();
        sortedFields = keywordArgumentsParser.getSortedFields();
        injectFieldGetter();
        injectDefaultFields();
        injectUpdater();
        injectDefaultConstructor();
        injectCodeAnnotatedConstructor();
        ctClass.freeze();
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
        CtConstructor defaultConstructor;
        try {
            defaultConstructor = ctClass.getDeclaredConstructor(new CtClass[0]);
            defaultConstructor.insertBeforeBody("update$all();");
        } catch (NotFoundException e) {

            logger.info(e.getMessage());

            StringBuilder constructorBuilder = new StringBuilder();

            constructorBuilder.append("public " + ctClass.getName() + "() {");
            constructorBuilder.append("update$all();");
            constructorBuilder.append(" }");

            logger.log(Level.INFO, "The default constructor is: " + constructorBuilder.toString());

            defaultConstructor = new CtNewConstructor().make(constructorBuilder.toString(), ctClass);
            ctClass.addConstructor(defaultConstructor);
        }

    }

    private void injectFieldGetter() throws CannotCompileException {
        // inject auxiliary method - get field from name, including inherited fields
        String auxMethodTemplate = "private java.lang.reflect.Field "+ GET_FIELD +"(java.lang.String name) {" +
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
  
    private void injectCodeAnnotatedConstructor() throws CannotCompileException {
        if (!ctConstructor.isPresent()) {
            logger.log(Level.SEVERE, "There is no constructor in which to inject code.");
            return;
        }
        CtConstructor constructor = ctConstructor.get();
        StringBuilder constructorBuilder = new StringBuilder();
        constructorBuilder.append("{");
        Object[] ola;
        constructorBuilder.append("if (($1.length%2) != 0) {" +
                "throw new RuntimeException(\"Odd number of parameters (\" + $1.length + \") in constructor,  expected even.\");" +
                "}");

        constructorBuilder.append(
                "for (int i = 0; i < $1.length; i+=2) {" +
                        "java.lang.reflect.Field field = "+ GET_FIELD + "((java.lang.String)$1[i]);" +
                        "if (field == null) {" +
                            "throw new RuntimeException(\"Unrecognized keyword: \" + (java.lang.String)$1[i]);" +
                        "}" +
                        "field.set(this, $1[i+1]);" +
                        GET_FIELD + "((java.lang.String)$1[i] + \""+ DEFAULT + "\").setBoolean(this, false);" +
                        "}");

        constructorBuilder.append("update$all();");
        constructorBuilder.append("}");
        constructor.setBody(constructorBuilder.toString());
    }

    private void injectUpdater() throws CannotCompileException, ClassNotFoundException {
        StringBuilder updater = new StringBuilder();

        updater.append("public void update$all() { ");
        CtClass superClass;
        try {
            superClass = ctClass.getSuperclass();
            if(SearchClass.getAnnotatedConstructor(superClass).isPresent()) {
                logger.info("Found SuperClass with KeywordArgs annotation.");
                logger.info("# Start loading " + superClass.getName());
                Loader loader = new Loader(ClassPool.getDefault());
                loader.addTranslator(ClassPool.getDefault(), new KeywordArgsTranslator());
                loader.loadClass(superClass.getName());
                logger.info("# Stop loading " + superClass.getName());
                updater.append("super.update$all();");
            }
        } catch (NotFoundException e) {
            logger.info("There is no SuperClass to load!");
        } finally {
            sortedFields.stream().forEach(s -> updater.append("if (" + s + DEFAULT + ") {" +
                    s + " = " + fieldValueMap.get(s) + ";" +
                    "}"));
            updater.append("}");
        }
        logger.info("Updater method is: " + updater.toString());
        ctClass.addMethod(CtNewMethod.make(updater.toString(), ctClass));
    }


    private void injectDefaultFields() throws CannotCompileException {
        for (String s: sortedFields) {
            try {
                if(ctClass.getDeclaredField(s) != null) {
                    logger.info("Injecting field: " + s + DEFAULT);
                    ctClass.addField(CtField.make("protected boolean " + s + DEFAULT + " = true;", ctClass));
                }
            } catch (NotFoundException e) {
                logger.info(s + " is no a field of " + ctClass.getName() + " must belong to a super.");
            }
        }
    }
}
