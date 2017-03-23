package ist.meic.pa;

import java.util.HashMap;

/**
 * Created by lads on 23/03/2017.
 */
public class ParseWrapper {

    KeywordArgs kwAnnotation;
    HashMap<String, String> values = new HashMap<>();

    public ParseWrapper(KeywordArgs keywordArgs) {
        kwAnnotation = keywordArgs;
    }

    public HashMap<String, String> parse() {
        //TODO: regex code
        return values;
    }


}
