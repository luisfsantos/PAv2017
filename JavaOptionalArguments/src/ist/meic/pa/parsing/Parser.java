package ist.meic.pa.parsing;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;

/**
 * Class responsible for parsing an expression into a Key-Value HashMap.
 */
public class Parser {
    private final static String PATTERN = "(?<varName>.+?)(=(?<varValue>(\".+?\"|.+?))(,|$)|(,|$))";


    public static HashMap<String, String> parse(String expr) {
        Pattern p = Pattern.compile(PATTERN);
        Matcher m = p.matcher(expr);
        HashMap<String, String> result = new HashMap<>();
        String varName;
        String varValue;

        while(m.find()) {
            varName = m.group("varName");
            varValue = m.group("varValue");
            // System.out.println(varName + ":" + varValue);
            result.put(varName, varValue);
        }

        return result;
    }
}