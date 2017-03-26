package ist.meic.pa;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

public class ParserTests {
    /** KeywordArgs annotation arguments from the provided tests **/
    private final String EXT_WIDGET = "name=\"Extended\",width=200,margin=10";
    private final String FUNC_WIDGET = "result=40+5,value=Math.sin(result)";
    private final String KEY_PLACES = "first=\"Lordran\",second=\"Drangleic\",third=\"Lothric\"";
    private final String KEY_VISITED = "visited=0,second";
    private final String MIX_KEYS = "l=10,b,v=true";
    private final String UNKNOWN = "";
    private final String VOID_WIDGET = "someNumber,someChar,someDouble";
    private final String WIDGET = "width=100,height=50,margin=5";
    private final String STR_WITH_COMMA_IN_VALUE = "first,second=\"What's up, homie?\",third=\"$tring,\"";


    private ParseWrapper pw;
    @Before
    public void initialize() {
        // empty
    }

    @Test
    public void testExtWidget() {
        HashMap<String, String> expected = new HashMap<>();
        expected.put("name", "\"Extended\"");
        expected.put("width", "200");
        expected.put("margin", "10");

        HashMap<String, String> result = Parser.parse(EXT_WIDGET);

        assertEquals("Wrong size of parsed HashMap",expected.values().size(), result.values().size());
        assertTrue("Unexpected HashMap content", result.equals(expected));
    }

    @Test
    public void testFuncWidget() {
        HashMap<String, String> expected = new HashMap<>();
        expected.put("result", "40+5");
        expected.put("value", "Math.sin(result)");

        HashMap<String, String> result = Parser.parse(FUNC_WIDGET);

        assertEquals("Wrong size of parsed HashMap",expected.values().size(), result.values().size());
        assertTrue("Unexpected HashMap content", result.equals(expected));
    }

    @Test
    public void testKeyPlaces() {
        HashMap<String, String> expected = new HashMap<>();
        expected.put("first", "\"Lordran\"");
        expected.put("second", "\"Drangleic\"");
        expected.put("third", "\"Lothric\"");

        HashMap<String, String> result = Parser.parse(KEY_PLACES);

        assertEquals("Wrong size of parsed HashMap",expected.values().size(), result.values().size());
        assertTrue("Unexpected HashMap content", result.equals(expected));
    }

    @Test
    public void testKeyVisited() {
        HashMap<String, String> expected = new HashMap<>();
        expected.put("visited", "0");
        expected.put("second", null);

        HashMap<String, String> result = Parser.parse(KEY_VISITED);

        assertEquals("Wrong size of parsed HashMap",expected.values().size(), result.values().size());
        assertTrue("Unexpected HashMap content", result.equals(expected));
    }

    @Test
    public void testMixKeys() {
        HashMap<String, String> expected = new HashMap<>();
        expected.put("l", "10");
        expected.put("b", null);
        expected.put("v", "true");

        HashMap<String, String> result = Parser.parse(MIX_KEYS);

        assertEquals("Wrong size of parsed HashMap",expected.values().size(), result.values().size());
        assertTrue("Unexpected HashMap content", result.equals(expected));
    }

    @Test
    public void tesUnknown() {
        HashMap<String, String> expected = new HashMap<>();

        HashMap<String, String> result = Parser.parse(UNKNOWN);

        assertEquals("Wrong size of parsed HashMap", expected.values().size(), result.values().size());
        assertTrue("Unexpected HashMap content", result.equals(expected));
    }

    @Test
    public void testVoidWidget() {
        HashMap<String, String> expected = new HashMap<>();
        expected.put("someNumber", null);
        expected.put("someChar", null);
        expected.put("someDouble", null);

        HashMap<String, String> result = Parser.parse(VOID_WIDGET);

        assertEquals("Wrong size of parsed HashMap",expected.values().size(), result.values().size());
        assertTrue("Unexpected HashMap content", result.equals(expected));
    }

    @Test
    public void testWidget() {
        HashMap<String, String> expected = new HashMap<>();
        expected.put("width", "100");
        expected.put("height", "50");
        expected.put("margin", "5");

        HashMap<String, String> result = Parser.parse(WIDGET);

        assertEquals("Wrong size of parsed HashMap",expected.values().size(), result.values().size());
        assertTrue("Unexpected HashMap content", result.equals(expected));
    }

    @Test
    public void testWithValueWithCommasInIt() {
        HashMap<String, String> expected = new HashMap<>();
        expected.put("first", null);
        expected.put("second", "\"What's up, homie?\"");
        expected.put("third", "\"$tring,\"");

        HashMap<String, String> result = Parser.parse(STR_WITH_COMMA_IN_VALUE);

        assertEquals("Wrong size of parsed HashMap",expected.values().size(), result.values().size());
        assertTrue("Unexpected HashMap content", result.equals(expected));
    }

}