package ch.akuhn.fame.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.akuhn.fame.Tower;

public class MetmodelBuilderTest {

    @Test
    public void testBuilder() {
        Tower t = new Tower();
        t.metamodel.builder()
        .beginDocument()
            .beginPackage("X")
                .beginClass("Foo")
                    .with("name", "String")
        .endDocument();
        
        assertEquals(3, t.metamodel.size());
    }
    
}
