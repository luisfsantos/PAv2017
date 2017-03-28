package ist.meic.pa;

import javassist.*;

import java.util.Date;
import java.util.logging.*;
import java.text.SimpleDateFormat;

/**
 * Created by lads on 22/03/2017.
 */
final public class KeyConstructors {

    public static void main(String[] args) throws Throwable {

        if(args.length < 1) {
            System.err.println("USAGE: java ist.meic.pa.KeyConstructors <class binary file>");
        } else {
            final String classToRun = args[0];
            final String workDir = System.getProperty("user.dir");

            //LOGGING
            LogManager.getLogManager().reset();
            Logger rootLogger = LogManager.getLogManager().getLogger("");
            Handler fileHandler = new FileHandler("KeyConstructors <"+ classToRun +">-"+ new SimpleDateFormat("HH.mm.ss").format(new Date()) + ".log");
            fileHandler.setFormatter(new SimpleFormatter());
            rootLogger.setUseParentHandlers(false);
            rootLogger.addHandler(fileHandler);
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
