package ch.akuhn.fame.test;

import ch.akuhn.fame.Tower;
import ch.akuhn.fame.internal.JSONPrettyPrinter;
import ch.akuhn.fame.parser.InputSource;
import junit.framework.TestCase;

public class JSONPrinterTest extends TestCase {

    private JSONPrettyPrinter printer;
    private Appendable stream;

    public void testExportJSON() {
        InputSource input = InputSource.fromResource("ch/unibe/fame/resources/lib.mse");
        Tower t = new Tower();
        t.getMetamodel().importMSE(input);
        Appendable stream = new StringBuilder();
        t.metamodel.exportJSON(stream);
        System.out.println(stream);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        stream = new StringBuilder();
        printer = new JSONPrettyPrinter(stream);
    }

    public void testBeginAttributeSimple() {
        printer.beginAttribute("hello");
        assertEquals(stream.toString(), "\"hello\":");
    }

    public void testPrimitive() {
        printer.primitive("value");
        assertEquals(stream.toString(), "\"value\"");
    }

    public void testPrimitiveWithSpecialCharacter() {
        printer.primitive("MySuper\"String");
        assertEquals(stream.toString(), "\"MySuper\\\"String\"");
    }

    public void testPrimitiveWithSpecialCharacterAndActualExample() {
        printer.primitive("print(\"Printer \" + name() + \" prints \"+ thePacket.contents(),false)");
        assertEquals(stream.toString(), "\"print(\\\"Printer \\\" + name() + \\\" prints \\\"+ thePacket.contents(),false)\"");
    }

    public void testReference() {
        printer.reference("hello");
        assertEquals(removeWhiteSpaces(stream.toString()), "{\"ref\":\"hello\"}");
    }

    public void testReferenceIndex() {
        printer.reference(2);
        assertEquals(removeWhiteSpaces(stream.toString()), "{\"ref\":2}");
    }

    public void testSerial() {
        printer.serial(2);
        assertEquals(removeWhiteSpaces(stream.toString()), "\"id\":2,");
    }

    public void testBeginElement() {
        printer.beginElement("Java.Class");
        assertEquals(removeWhiteSpaces(stream.toString()), "{\"FM3\":\"Java.Class\",");
    }


    private static String removeWhiteSpaces(String input) {
        return input.replaceAll("\\s+", "");
    }
}