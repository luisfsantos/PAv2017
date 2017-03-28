package ist.meic.pa;

import javassist.*;

import java.util.Date;
import java.util.logging.*;
import java.text.SimpleDateFormat;

final public class KeyConstructors {

    public static void main(String[] args) throws Throwable {

        if(args.length < 1) {
            System.err.println("USAGE: java ist.meic.pa.KeyConstructors <class binary file> [Optional: DEBUG](for logging)");
        } else {
            final String classToRun = args[0];
            final String workDir = System.getProperty("user.dir");

            //LOGGING
            LogManager.getLogManager().reset();
            Logger rootLogger = LogManager.getLogManager().getLogger("");
            rootLogger.setUseParentHandlers(false);
            if (args.length>=2 && args[1].equals("DEBUG")) {
                Handler fileHandler = new FileHandler("KeyConstructors <" + classToRun + ">-" + new SimpleDateFormat("HH.mm.ss").format(new Date()) + ".log");
                fileHandler.setFormatter(new SimpleFormatter());
                rootLogger.addHandler(fileHandler);
            }
            //LOGGING

            Translator kwargsTranslator = new KeywordArgsTranslator();
            ClassPool pool = ClassPool.getDefault();
            Loader loader = new Loader();
            pool.appendClassPath(workDir); // add current dir to classpath
            loader.addTranslator(pool, kwargsTranslator);
            loader.loadClass(classToRun);
            loader.run(classToRun, null);

        }
    }
}
