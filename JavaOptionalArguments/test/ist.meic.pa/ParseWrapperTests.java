package ist.meic.pa;

import static org.junit.Assert.*;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

class Widget {
    int width;
    int height;
    int margin;

    @KeywordArgs("width=100,height=50,margin=5")
    public Widget(Object... args) {}

}

/**
 * Doesn't inherit all attributes (no height).
 */
class ExtendedWidgetDoesNotInheritDefault extends Widget {
    String name;

    @KeywordArgs("name=\"Extended\",width=200,margin=10")
    public ExtendedWidgetDoesNotInheritDefault(Object... args) {}

}

/**
 * Inherits all attributes, plus one should use the inherited value.
 */
class ExtendedWidgetInheritsDefault extends Widget {
    String name;

    @KeywordArgs("name=\"Extended\",width=200,margin=10,height")
    public ExtendedWidgetInheritsDefault(Object... args) {}

}


public class ParseWrapperTests {
    @Before
    public void initialize() {
        // empty
    }

    /**
     * Test when no default parameters are inherited.
     */
    @Test
    public void testNoDefaultInheritance() throws NotFoundException, NoSuchMethodException {
        KeywordArgs kwargs = ExtendedWidgetDoesNotInheritDefault.class.getConstructor(Object[].class).getAnnotation(KeywordArgs.class);
        CtClass ctClass = ClassPool.getDefault().getCtClass(ExtendedWidgetDoesNotInheritDefault.class.getName());

        HashMap<String, ValueWrapper> expected = new HashMap<>();
        expected.put("name", new ValueWrapper("\"Extended\""));
        expected.put("width", new ValueWrapper("200"));
        expected.put("margin", new ValueWrapper("10"));
        ParseWrapper pw = new ParseWrapper(kwargs, ctClass);
        HashMap<String, ValueWrapper> result = pw.parse();
        assertEquals("Wrong size of parsed HashMap", expected.size(), result.size());
        assertTrue("Unexpected HashMap content.\nExpected: " + expected + "\nActual: " + result, result.equals(expected));
    }

    /**
     * Test when default parameters are inherited.
     */
    @Test
    public void testWithDefaultInheritance() throws NoSuchMethodException, NotFoundException {
        // Expected: name="Extended", width=200, margin=10, height=50
        KeywordArgs kwargs = ExtendedWidgetInheritsDefault.class.getConstructor(Object[].class).getAnnotation(KeywordArgs.class);
        CtClass ctClass = ClassPool.getDefault().getCtClass(ExtendedWidgetInheritsDefault.class.getName());

        HashMap<String, ValueWrapper> expected = new HashMap<>();
        expected.put("name", new ValueWrapper("\"Extended\""));
        expected.put("width", new ValueWrapper("200"));
        expected.put("margin", new ValueWrapper("10"));
        expected.put("height", new ValueWrapper("50"));

        ParseWrapper pw = new ParseWrapper(kwargs, ctClass);
        HashMap<String, ValueWrapper> result = pw.parse();
        assertEquals("Wrong size of parsed HashMap", expected.size(), result.size());
        assertTrue("Unexpected HashMap content.\nExpected: " + expected + "\nActual: " + result, result.equals(expected));
    }
}
