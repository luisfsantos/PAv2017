package ist.meic.pa;

import ist.meic.pa.utils.DefaultValues;
import ist.meic.pa.utils.SearchClass;
import javafx.collections.transformation.SortedList;
import javassist.CannotCompileException;
import javassist.CtClass;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javassist.NotFoundException;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.*;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lads on 23/03/2017.
 */
public final class ParseWrapper {

    private KeywordArgs kwAnnotation;
    private final String kwArgsStr;
    private CtClass ctClass;
    private HashMap<String, ValueWrapper> values;
    private DirectedAcyclicGraph<String, DefaultEdge> dependencies = new DirectedAcyclicGraph<>(DefaultEdge.class);
    private static final Logger logger = Logger.getLogger(ParseWrapper.class.getName());

    public ParseWrapper(KeywordArgs keywordArgs, CtClass ctClass) {
        kwAnnotation = keywordArgs;
        kwArgsStr = keywordArgs.value();
        this.ctClass = ctClass;
    }

    public HashMap<String, ValueWrapper> parse() {
        /*
         * Parse all values from the current annotation, check which ones are null and go up each parent class, parse it
         * for each value that has a null value in the current class's hash-value map, check if parent has a
         * non-null value, if it does, set it to such.
         *
         * Optimization: repeat this only until all of the values that are null have been assigned a value.
         */
        try {
            HashMap<String, String> allKwargs = new HashMap<>();

            HashMap<String, String> kwargs = Parser.parse(kwArgsStr);
            allKwargs.putAll(kwargs);

            logger.info("Current class kwargs size = " + kwargs.size());
            logger.info("kwargs = " + Arrays.toString(kwargs.keySet().toArray()));
            // TODO: optimize if there is time
            // Gets all of the keys that have "null" as the value
            Collection<String> nullKeys = filterOutKeysWithNullValue(kwargs);
            Collection<String> nullKeysCopy = new ArrayList<>(nullKeys);
            logger.info("Number of values with null keys = " + nullKeys.size());

            HashMap<String, String> parentKwargs; // used in loop below
            CtClass parent = ctClass.getSuperclass();
            Optional<String> ctorStrOpt;
            String kwargsStr;

            logger.info("# Start looking for inherited kwargs");
            while(!(parent == null)) {
                logger.info("\tParent class: " + parent.getName());
                ctorStrOpt  = SearchClass.getKwargsStringFromCtor(parent);
                if (!ctorStrOpt.isPresent()) {
                    // skip loop iteration, KeywordArgs annotation not found in any of the class's constructors
                    logger.info("\t\t-> this class doesn't have an annotated ctor, skipping...");
                    parent = parent.getSuperclass();
                    continue;
                }

                kwargsStr = ctorStrOpt.get();
                logger.info("\t\t-> value= " + kwargsStr);
                parentKwargs = Parser.parse(kwargsStr);
                parentKwargs.forEach((key, value)  -> allKwargs.putIfAbsent(key, value));
                logger.info("\t\t-> kwargs size= " + parentKwargs.size());

                for (String key : nullKeys) {
                    if (!nullKeysCopy.contains(key)) {
                        // this value has already been inherited
                        continue;
                    }

                    String parentVal = parentKwargs.get(key);
                    if (parentKwargs.containsKey(key) && parentVal != null) {
                        logger.info("\t\t-> [!!] inhereting " + key + ":" + parentVal);
                        kwargs.put(key, parentVal);
                        nullKeysCopy.remove(key);
                    }
                    if (nullKeysCopy.isEmpty()) {
                        // optimization: all values have a value
                        logger.info("\t\t-> all kwargs have a value, ending for loop...");
                        break;
                    }
                }
                parent = parent.getSuperclass();
            }
            logger.info("# Start looking for inherited kwargs");

            // convert HashMap<String, String> to HashMap<String, ValueWrapper>
            dependencies = checkDependencies(allKwargs);
            values = wrapValues(kwargs, ctClass);
            logger.info("Size of wrapped hashmap= " + values.size());
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return values;
    }

    private DirectedAcyclicGraph<String,DefaultEdge> checkDependencies(HashMap<String, String> allKwargs) {
        DirectedAcyclicGraph<String,DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);
        allKwargs.keySet().forEach(key -> graph.addVertex(key));
        for (Map.Entry<String, String> entry : allKwargs.entrySet()) {
            logger.info("Getting dependencies for " + entry.getKey());
            List<String> dep = getDependenciesInValue(allKwargs.keySet(), entry.getValue());
            logger.info("\t-> Dependencies found: "+ Arrays.toString(dep.toArray()));
            dep.forEach(d -> {
                try {
                    graph.addDagEdge(d, entry.getKey());
                } catch (IllegalArgumentException | DirectedAcyclicGraph.CycleFoundException e) {
                    logger.severe("Cannot use kwrdargs as they are because some dependencies cannot be resolved");
                    throw  new RuntimeException("Cyclic dependencies in KeywordArgs");
                }
            });
        }
        return graph;
    }

    private List<String> getDependenciesInValue(Set<String> keys, String value) {

        List<String> dependencies = new LinkedList<>();
        if (value == null) {
            return dependencies;
        }

        final String WORD_PATTERN = "^\\w*$";
        final String FUNCALL_PATTERN = "^(?<obj>\\w+)(?:[.]\\w+\\((?<param>[^()]*)\\))$";
        final String ARITHMETIC_PATTERN = "^(?<left>\\(.+?\\)|.+?)[/+*-](?<right>\\(.+\\)|.+)$";
        Pattern p;
        Matcher m;
        value = trimParenthesis(value);
        logger.info("value after trimming parenthesis: " + value);

        /* 1st case: value is a single word */
        p = Pattern.compile(WORD_PATTERN);
        m = p.matcher(value);
        if (m.find()) {
            String match = m.group(0);
            logger.info("Single word match -> " + match);
            if (keys.contains(match)) {
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
        m = p.matcher(value);
        if (m.find()) {
            String obj = m.group("obj");
            logger.info("Function call match -> Receiver is: " + obj);
            if (keys.contains(obj)) {
                logger.info("Found a dependency: " + obj);
                dependencies.add(obj);
            }
            String param = m.group("param").trim();
            logger.info("Recursive call on matched param: " + param);
            dependencies.addAll(getDependenciesInValue(keys, param));
            return dependencies;
        }

        /* 3rd case: value is an arithmetic expression */
        String left;
        String right;
        p = Pattern.compile(ARITHMETIC_PATTERN);
        m = p.matcher(value);
        if (m.find()) {
            left = m.group("left");
            right = m.group("right");
            logger.info("Arithmetic expression match -> left: " + left + "; right: " + right);
            dependencies.addAll(getDependenciesInValue(keys, left));
            dependencies.addAll(getDependenciesInValue(keys, right));
        }
        return dependencies;
    }

    private static String trimParenthesis(String s) {
        //language=RegExp
        final String PATTERN = "^\\((.*?)\\)$";
        Pattern p = Pattern.compile(PATTERN);
        Matcher m = p.matcher(s);
        if (m.find()) {
            s = m.group(1);
        }
        return s;
    }

    private HashMap<String, ValueWrapper> wrapValues(HashMap<String, String> kwargs, CtClass clazz) {
        HashMap<String, ValueWrapper> wrapperKwargs = new HashMap<>();
        ValueWrapper vw;

        for (Map.Entry<String, String> entry : kwargs.entrySet()) {
            try {

                if (entry.getValue() == null) {
                    vw = new DefaultTypeValueWrapper(clazz, entry.getKey());
                } else {
                    vw = new ValueWrapper(entry.getValue());
                }
                wrapperKwargs.put(entry.getKey(), vw);
            } catch (NotFoundException e) {
                e.printStackTrace();
            } catch (CannotCompileException e) {
                e.printStackTrace();
            }
        }
        return wrapperKwargs;
    }

    private Collection<String> filterOutKeysWithNullValue(HashMap<String, String> kwargs) {
        ArrayList<String> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : kwargs.entrySet()) {
            if (entry.getValue() == null) {
                result.add(entry.getKey());
            }
        }
        return result;
    }


    public List<String> getSortedParameters(HashMap<String, ValueWrapper> myParameters) {
        TopologicalOrderIterator<String, DefaultEdge> topologicalSort = new TopologicalOrderIterator<>(dependencies);
        LinkedList<String> sortedList = new LinkedList<>();
        while (topologicalSort.hasNext()) {
            String vertex = topologicalSort.next();
            if(myParameters.containsKey(vertex)) {
                sortedList.add(vertex);
            }

        }
        return sortedList;
    }


}
