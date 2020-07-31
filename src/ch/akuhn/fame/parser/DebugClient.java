package ch.akuhn.fame.parser;

import java.util.ArrayList;
import java.util.List;

import ch.akuhn.util.Separator;

public class DebugClient implements ParseClient {

    public final List<Object[]> log = new ArrayList<Object[]>();
    
    public ParseClient client;
    
    public DebugClient(ParseClient client) {
        this.client = client;
    }
    
    public DebugClient() {
        this(null);
    }

    public void beginAttribute(String name) {
        log.add(new Object[] { "beginProperty", name });
        if (client != null) client.beginAttribute(name);
    }

    public void beginDocument() {
        log.add(new Object[] { "beginDocument" });
        if (client != null) client.beginDocument();
    }

    public void beginElement(String name) {
        log.add(new Object[] { "beginElement", name });
        if (client != null) client.beginElement(name);
    }

    public void directive(String name, String... params) {
        throw new UnsupportedOperationException();
    }

    public void endAttribute(String name) {
        log.add(new Object[] { "endProperty", name });
        if (client != null) client.endAttribute(name);
    }

    public void endDocument() {
        log.add(new Object[] { "endDocument" });
        if (client != null) client.endDocument();
    }

    public void endElement(String name) {
        log.add(new Object[] { "endElement", name });
        if (client != null) client.endElement(name);
    }

    public void primitive(Object value) {
        log.add(new Object[] { "primitive", value });
        if (client != null) client.primitive(value);
    }

    public void reference(int index) {
        log.add(new Object[] { "reference(int)", index });
        if (client != null) client.reference(index);
    }

    public void reference(String name) {
        log.add(new Object[] { "reference(String)", name });
        if (client != null) client.reference(name);
    }

    public void reference(String name, int index) {
        throw new UnsupportedOperationException();
    }

    public void serial(int index) {
        log.add(new Object[] { "serial", index });
        if (client != null) client.serial(index);
    }
    
    @Override
    public String toString() {
        StringBuilder $ = new StringBuilder();
        for (Object[] line : log) {
            Separator s = new Separator(", ");
            for (Object each : line) {
                $.append(s).append(each);
            }
            $.append('\n');
        }
        return $.toString();
    }

}
