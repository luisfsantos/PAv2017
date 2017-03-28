package ist.meic.pa.parsing;


/* Description:
        * As we implemented a topological sort to decide on whether to accept default values
        *  or not we made a small simple parser to find variables and calculate the dependencies of each argument
        */

/* Does NOT support:
         * 1. function call with any parentheses in param expression e.g. Math.sin(2*(1+2))
         * 2. chained function calls e.g. Math.sin(0).toString()
         */

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DependencyEvaluator {
    private static final Logger logger = Logger.getLogger(DependencyEvaluator.class.getName());
    static final String WORD_PATTERN = "^\\w*$";
    static final String FUNCALL_PATTERN = "^(?<obj>\\w+)(?:[.]\\w+\\((?<param>[^()]*)\\))$";
    static final String ARITHMETIC_PATTERN = "^(?<left>\\(.+?\\)|.+?)[/+*-](?<right>\\(.+\\)|.+)$";
    final String PARENTHESIS_PATTERN = "^\\((.*?)\\)$";
    private final HashMap<String, String> argumentsValues;

    private DirectedAcyclicGraph<String, DefaultEdge> dependencies = new DirectedAcyclicGraph<>(DefaultEdge.class);

    public DependencyEvaluator(HashMap<String, String> argumentsValues) {
        this.argumentsValues = argumentsValues;
    }

    public void checkDependencies() {
        DirectedAcyclicGraph<String,DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);
        argumentsValues.keySet().forEach(key -> graph.addVertex(key));
        for (Map.Entry<String, String> entry : argumentsValues.entrySet()) {
            logger.info("Getting dependencies for " + entry.getKey());
            List<String> dep = getDependenciesInValue(new HashSet<>(argumentsValues.keySet()), entry.getValue());
            logger.info("\t-> Dependencies found: "+ Arrays.toString(dep.toArray()));
            dep.forEach(d -> {
                try {
                    graph.addDagEdge(d, entry.getKey());
                } catch (IllegalArgumentException | DirectedAcyclicGraph.CycleFoundException e) {
                    logger.severe("Cannot use argumentsValues as they are because some dependencies cannot be resolved.");
                    throw  new RuntimeException("Cyclic dependencies in KeywordArgs");
                }
            });
        }
        dependencies = graph;
    }

    public List<String> getSortedFields(HashSet<String> myParameters) {
        TopologicalOrderIterator<String, DefaultEdge> topologicalSort = new TopologicalOrderIterator<>(dependencies);
        LinkedList<String> sortedList = new LinkedList<>();
        while (topologicalSort.hasNext()) {
            String vertex = topologicalSort.next();
            if(myParameters.contains(vertex)) {
                sortedList.add(vertex);
            }
        }
        return sortedList;
    }

    private List<String> getDependenciesInValue(HashSet<String> arguments, String expression) {

        List<String> dependencies = new LinkedList<>();
        if (expression == null) {
            return dependencies;
        }

        Pattern p;
        Matcher m;
        expression = trimParenthesis(expression);
        logger.info("value after trimming parenthesis: " + expression);

        /* 1st case: value is a single word */
        p = Pattern.compile(WORD_PATTERN);
        m = p.matcher(expression);
        if (m.find()) {
            String match = m.group(0);
            logger.info("Single word match -> " + match);
            if (arguments.contains(match)) {
                logger.info("Found a dependency: " + match);
                dependencies.add(match);
            }
            return dependencies;
        }

        /* 2nd case: value is a function call */
        /* Does NOT support:
         * 1. function call with any parentheses in param expression e.g. Math.sin(2*(1+2))
         * 2. chained function calls e.g. Math.sin(0).toString()
         */
        p = Pattern.compile(FUNCALL_PATTERN);
        m = p.matcher(expression);
        if (m.find()) {
            String obj = m.group("obj");
            logger.info("Function call match -> Receiver is: " + obj);
            if (arguments.contains(obj)) {
                logger.info("Found a dependency: " + obj);
                dependencies.add(obj);
            }
            String param = m.group("param").trim();
            logger.info("Recursive call on matched param: " + param);
            dependencies.addAll(getDependenciesInValue(arguments, param));
            return dependencies;
        }

        /* 3rd case: value is an arithmetic expression */
        String left;
        String right;
        p = Pattern.compile(ARITHMETIC_PATTERN);
        m = p.matcher(expression);
        if (m.find()) {
            left = m.group("left");
            right = m.group("right");
            logger.info("Arithmetic expression match -> left: " + left + "; right: " + right);
            dependencies.addAll(getDependenciesInValue(arguments, left));
            dependencies.addAll(getDependenciesInValue(arguments, right));
        }
        return dependencies;
    }

    private String trimParenthesis(String s) {
        //language=RegExp
        Pattern p = Pattern.compile(PARENTHESIS_PATTERN);
        Matcher m = p.matcher(s);
        if (m.find()) {
            s = m.group(1);
        }
        return s;
    }

}
