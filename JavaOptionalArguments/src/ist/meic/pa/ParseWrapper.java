package ist.meic.pa;

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

import java.util.*;

import java.util.logging.Logger;

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
            HashMap<String, String> kwargs = Parser.parse(kwArgsStr);
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
            while(!nullKeysCopy.isEmpty() && !(parent == null)) {
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
            dependencies = checkDependencies(kwargs);
            values = wrapValues(kwargs, ctClass);
            logger.info("Size of wrapped hashmap= " + values.size());
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //TODO: regex code which also puts in the values all the inherited values from the super classes
        return values;
    }

    private DirectedAcyclicGraph<String,DefaultEdge> checkDependencies(HashMap<String, String> kwargs) {
        DirectedAcyclicGraph<String,DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);
        for (Map.Entry<String, String> entry : kwargs.entrySet()) {
            graph.addVertex(entry.getKey());
            getDependenciesInValue(kwargs.keySet(), entry.getValue()).forEach(d -> {
                try {
                    graph.addDagEdge(entry.getKey(), d);
                } catch (DirectedAcyclicGraph.CycleFoundException e) {
                    logger.severe("Cannot use kwrdargs as they are because some dependencies cannot be resolved");
                    e.printStackTrace();
                    throw  new RuntimeException("Cyclic dependencies in KeywordArgs");
                }
            });
        }
        return graph;
    }

    private List<String> getDependenciesInValue(Set<String> keys, String value) {

        LinkedList<String> dependencies = new LinkedList<>();
        String SIMPLE_PATTERN = "[+*/-]";
        List<String> operands = Arrays.asList(value.split(SIMPLE_PATTERN));
        keys.forEach(k -> {
            if (operands.contains(k)) {
                dependencies.add(k);
            }
        });
        return dependencies;
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


    public List<String> getSortedParameters() {
        return new LinkedList<>();
    }


}
