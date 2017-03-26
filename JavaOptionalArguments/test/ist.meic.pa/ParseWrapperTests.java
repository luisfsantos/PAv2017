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

class A {
    int a;
    int b;
    int c;

    @KeywordArgs("a=1,b=2,c")
    public A(Object... args) { }
}

class B extends A {
    int d;

    @KeywordArgs("d,a=3,b")
    public B(Object... args) { }
}

class C extends B {
    int e;

    @KeywordArgs("e,c,a,b=1")
    public C(Object... args){ }
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

    @Test
    public void multipleLevelIheritanceTest() throws NoSuchMethodException, NotFoundException {
        KeywordArgs kwargs = C.class.getConstructor(Object[].class).getAnnotation(KeywordArgs.class);
        CtClass ctClass = ClassPool.getDefault().getCtClass(C.class.getName());

        HashMap<String, ValueWrapper> expected = new HashMap<>();
        expected.put("a", new ValueWrapper("3"));
        expected.put("b", new ValueWrapper("1"));
        expected.put("c", new ValueWrapper("0"));
        expected.put("e", new ValueWrapper("0"));

        ParseWrapper pw = new ParseWrapper(kwargs, ctClass);
        HashMap<String, ValueWrapper> result = pw.parse();
        assertEquals("Wrong size of parsed HashMap", expected.size(), result.size());
        assertTrue("Unexpected HashMap content.\nExpected: " + expected + "\nActual: " + result, result.equals(expected));
    }
}
