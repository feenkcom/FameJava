package ch.akuhn.fame.test;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Test;

import ch.akuhn.fame.FameDescription;
import ch.akuhn.fame.FamePackage;
import ch.akuhn.fame.FameProperty;
import ch.akuhn.fame.Tower;
import ch.akuhn.fame.fm3.MetaDescription;
import ch.akuhn.fame.fm3.PropertyDescription;

@FamePackage("Test")
public class IterablePropertyTest {

    @FameDescription
    public static class Example {
        
        private Collection<String> values = new HashSet<String>();
        
        public Example() {
            //
        }
        
        public Example(String... values) {
            for (String each: values) this.values.add(each);
        }
        
        @FameProperty
        public Iterable<String> getValues() {
            return values;
        }
        
        public void setValues(Iterable<String> values) {
            for (String each: values) this.values.add(each);
        }
        
    }
    
    @Test
    public void testMetamodel() {
        Tower t = new Tower();
        t.metamodel.with(Example.class);
        PropertyDescription p = t.metamodel.get("Test.Example.values");
        assertEquals("Test.Example.values", p.getFullname());
        assertEquals(MetaDescription.STRING, p.getType());
        assertEquals(true, p.isMultivalued());
    }
    
    @Test
    public void testModelExport() {
        Tower t = new Tower();
        t.metamodel.with(Example.class);
        t.model.add(new Example("foo", "bar", "qux"));
        t.model.exportMSE(System.out);
    }
    
    @Test
    public void testModelExportImport() {
        Tower t = new Tower();
        t.metamodel.with(Example.class);
        t.model.add(new Example("foo", "bar", "qux"));
        String str = t.model.exportMSE();
        t = new Tower();
        t.metamodel.with(Example.class);
        t.model.importMSE(str);
        assertEquals(1, t.model.getElements().size());
    }
}
