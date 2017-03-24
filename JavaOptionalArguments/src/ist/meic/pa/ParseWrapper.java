package ist.meic.pa;

import javassist.CtClass;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by lads on 23/03/2017.
 */
public class ParseWrapper {

    KeywordArgs kwAnnotation;
    CtClass ctClass;
    HashMap<String, ValueWrapper> values = new HashMap<>();
    DirectedAcyclicGraph<String, DefaultEdge> dependancies = new DirectedAcyclicGraph<>(DefaultEdge.class);

    public ParseWrapper(KeywordArgs keywordArgs, CtClass ctClass) {
        kwAnnotation = keywordArgs;
        this.ctClass = ctClass;
    }

    public HashMap<String, ValueWrapper> parse() {
        String toParse = kwAnnotation.value();
        String[] arguments = toParse.split(",");
        for (String kwWord : arguments) {
            kwWord.trim();
            String[] leftRight = kwWord.split("=");
            values.putIfAbsent(leftRight[0], new ValueWrapper(leftRight[1]));
        }
        //TODO: regex code which also puts in the values all the inherited values from the super classes
        return values;
    }

    public List<String> getSortedParameters() {
        return new LinkedList<>();
    }


}
