package ist.meic.pa;

import javassist.*;

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
