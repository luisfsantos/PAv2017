package ist.meic.pa;

import javassist.*;

/**
 * Created by lads on 22/03/2017.
 */
public class KeyConstructors {

    public static void main(String[] args) throws Throwable {
        if(args.length < 1) {
            System.err.println("USAGE: java ist.meic.pa.KeyConstructors <class binary file>");
        } else {
            Translator kwargsTranslator = new KeywordArgsTranslator();
            ClassPool pool = ClassPool.getDefault();
            Loader loader = new Loader();
            loader.loadClass(args[0]);
            loader.addTranslator(pool, kwargsTranslator);

            loader.run(args[0], null);
        }
    }
}
