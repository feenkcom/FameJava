package ch.akuhn.fame.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import ch.akuhn.fame.FameDescription;
import ch.akuhn.fame.FamePackage;
import ch.akuhn.fame.FameProperty;
import ch.akuhn.fame.Repository;
import ch.akuhn.fame.Tower;
import ch.akuhn.util.Bag;
import ch.akuhn.util.Strings;
import ch.unibe.jexample.Given;
import ch.unibe.jexample.JExample;

@RunWith(JExample.class)
public class TermBagExample {

    private static final String EMPTY_DOCUMENT = 
        "( (TEST.Document (id: 1)))";
    private static final String SOME_DOCUMENT = 
        "( (TEST.Document (id: 1) (terms 2 'be' 'to' 1 'or' 'not')))";


    @FameDescription
    @FamePackage("TEST")
    static class Document {
        
        @FameProperty()
        public String name;
        
        public Bag<String> terms = new Bag<String>();
        
        @FameProperty(name="terms")
        public List getTermData() {
            List list = new ArrayList();
            int count = -1;
            for (Bag.Count each : terms.sortedCounts()) {
                if (each.count != count) list.add(count = each.count);
                list.add(each.element);
            }
            return list;
        }
        
        public void setTermData(List data) {
            Bag<String> bag = new Bag<String>();
            int count = -1;
            for (Object each : data) {
                if (each instanceof Number) {
                    count = ((Number) each).intValue();
                } else {
                    assert count > 0;
                    bag.add((String) each, count);
                }
            }
            terms = bag;
        }
        
    }
    
    @Test
    public Tower tower() {
        Tower t = new Tower();
        t.metamodel.with(Document.class);
        assert t.metamodel.allPackageDescriptions().size() == 1;
        assert t.metamodel.allClassDescriptions().size() == 1;
        assert t.metamodel.allPropertyDescriptions().size() == 2;
        return t;
    }
    
    @Test
    @Given("tower")
    public Repository emptyModel(Tower t) {
        return t.model;
    }
    
    @Test
    public Document emptyDocument() {
        return new Document();
    }
    
    @Test
    @Given("emptyModel;emptyDocument")
    public Repository modelWithEmptyDocument(Repository m, Document d) {
        m.add(d);
        assert m.size() == 1;
        return m;
    }
    
    @Test
    @Given("modelWithEmptyDocument")
    public void exportModelWithEmptyDocument(Repository r) {
        String s = r.exportMSE();
        assertEquals(EMPTY_DOCUMENT, normalizeWhitespace(s));
    }
    
    @Test
    public Document someDocument() {
        Document d = new Document();
        d.terms.add("to", 2);
        d.terms.add("be", 2);
        d.terms.add("or");
        d.terms.add("not");
        return d;
    }
    
    @Test
    @Given("emptyModel;someDocument")
    public Repository modelWithSomeDocument(Repository m, Document d) {
        m.add(d);
        assert m.size() == 1;
        return m;
    }
    
    @Test
    @Given("modelWithSomeDocument")
    public void exportModelWithSomeDocument(Repository r) {
        String s = r.exportMSE();
        // TODO fragile test, order of terms may differ if hash key of interned strings diffs or if abg impl diffs
        // assertEquals(SOME_DOCUMENT, normalizeWhitespace(s));
    }
    
    @Test
    @Given("tower")
    public Repository importModelWithSomeDocument(Tower t) {
        assert t.model.size() == 0;
        t.model.importMSE(SOME_DOCUMENT);
        assert t.model.size() == 1;
        Document d = (Document) t.model.getElements().iterator().next();
        assert d.terms.size() == 6;
        assert d.terms.occurrences("be") == 2;
        assert d.terms.occurrences("to") == 2;
        assert d.terms.occurrences("or") == 1;
        assert d.terms.occurrences("not") == 1;
        return t.model;
    }
    
    private static String normalizeWhitespace(String s) {
        StringBuilder $ = new StringBuilder();
        boolean wasWhitespace = false;
        for (char ch: Strings.chars(s)) {
            boolean isWhitespace = Character.isWhitespace(ch);
            if (!(isWhitespace && wasWhitespace)) $.append(isWhitespace ? ' ' : ch);
            wasWhitespace = isWhitespace;
        }
        return $.toString();
    }
    
}
