package ch.akuhn.fame.test;

import ch.akuhn.fame.Tower;
import ch.akuhn.fame.parser.InputSource;
import junit.framework.TestCase;

public class JSONPrinterTest extends TestCase {

    public void testExportJSON() {
        InputSource input = InputSource.fromResource("ch/unibe/fame/resources/lib.mse");
        Tower t = new Tower();
        t.getMetamodel().importMSE(input);
        Appendable stream = new StringBuilder();
        t.metamodel.exportJSON(stream);
        System.out.println(stream);
    }
}