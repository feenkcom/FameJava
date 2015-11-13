package ch.akuhn.fame.internal;

import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import ch.akuhn.fame.fm3.PackageDescription;
import ch.akuhn.fame.parser.AbstractParserClient;
import ch.akuhn.fame.parser.ParseClient;
import ch.akuhn.fame.parser.Primitive;

public class Inferencer extends AbstractParserClient implements Runnable {

    private static class AbstractAttribute {

        private int maxCount;
        final String name;
        public Set<Object> elements;

        public AbstractAttribute(String name) {
            this.name = name;
        }

        public void addCount(int count) {
            maxCount = max(count, maxCount);
        }

        public Object inferElementType() {
            assert elements.size() == 1;
            return elements.iterator().next();
        }

        public boolean inferMultivalued() {
            return maxCount > 1;
        }

        public void resolveReferences(Map<Integer, AbstractElement> indexMap) {
            for (Object each : new ArrayList(elements)) {
                if (each instanceof Integer) {
                    elements.remove(each);
                    elements.add(indexMap.get(each));
                }
            }
        }

    }

    private static class AbstractElement {

        public Map<String, AbstractAttribute> attributes;
        private AbstractAttribute curr;
        private int count;
        final String name;
        final int index;

        public AbstractElement(String name, int index) {
            this.name = name;
            this.index = index;
        }

        public void attributeElementsAdd(Object any) {
            count++;
            curr.elements.add(any);
        }

        public String packageName() {
            int pos = name.lastIndexOf('.');
            assert pos > 0;
            return name.substring(0, pos);
        }

        public void resetCurrentAttribute() {
            assert curr != null;
            curr.addCount(count);
            curr = null;
            count = -1;
        }

        public void setCurrentAttribute(AbstractAttribute attribute) {
            assert curr == null;
            curr = attribute;
            count = 0;
        }

        public String simpleName() {
            int pos = name.lastIndexOf('.');
            assert pos > 0;
            return name.substring(pos + 1);
        }

    }

    private Map<String, AbstractElement> nameMap;
    private Map<Integer, AbstractElement> indexMap;
    private LinkedList<AbstractElement> stack;

    private int serial;
    private ParseClient client;

    @Override
    public void beginAttribute(String name) {
        AbstractElement element = stack.getLast();
        AbstractAttribute attribute = element.attributes.get(name);
        if (attribute == null) {
            attribute = new AbstractAttribute(name);
            element.attributes.put(name, attribute);
        }
        stack.getLast().setCurrentAttribute(attribute);
    }

    @Override
    public void beginDocument() {
        nameMap = new HashMap();
        indexMap = new HashMap();
        stack = new LinkedList();
        serial = 0;
    }

    @Override
    public void beginElement(String name) {
        AbstractElement element = nameMap.get(name);
        if (element == null) {
            element = new AbstractElement(name, nextSerial());
            nameMap.put(name, element);
        }
        if (!stack.isEmpty()) {
            stack.getLast().attributeElementsAdd(element);
        }
        stack.addLast(element);
    }

    @Override
    public void endAttribute(String name) {
        stack.getLast().resetCurrentAttribute();
    }

    @Override
    public void endDocument() {
        assert stack.isEmpty();
        resolveReferences();
    }

    @Override
    public void endElement(String name) {
        stack.removeLast();
    }

    public ParseClient getClient() {
        return client;
    }

    private void inferClass(AbstractElement element) {
        client.beginElement("FM3.Class");
        client.serial(element.index);
        client.beginAttribute("name");
        client.primitive(element.simpleName());
        client.endAttribute("name");
        client.beginAttribute("classes");
        this.inferProperties(element);
        client.endAttribute("classes");
        client.endElement("FM3.Class");
    }

    private void inferClasses(String name) {
        for (AbstractElement each : nameMap.values()) {
            if (each.packageName().equals(name)) {
                this.inferClass(each);
            }
        }
    }

    private void inferPackage(String name) {
        client.beginElement(PackageDescription.NAME);
        client.beginAttribute("name");
        client.primitive(name);
        client.endAttribute("name");
        client.beginAttribute("classes");
        this.inferClasses(name);
        client.endAttribute("classes");
        client.endElement(PackageDescription.NAME);
    }

    private void inferPackages() {
        for (String name : packageNames()) {
            this.inferPackage(name);
        }
    }

    private void inferProperties(AbstractElement element) {
        for (AbstractAttribute each : element.attributes.values()) {
            this.inferProperty(each);
        }
    }

    private void inferProperty(AbstractAttribute attribute) {
        client.beginElement("FM3.Class");
        client.beginAttribute("name");
        client.primitive(attribute.name);
        client.endAttribute("name");
        client.beginAttribute("type");
        Object type = attribute.inferElementType();
        if (type instanceof Primitive) {
            client.reference(((Primitive) type).toString());
        } else {
            client.reference(((AbstractElement) type).index);
        }
        if (attribute.inferMultivalued()) {
            client.beginAttribute("multivalued");
            client.primitive(true);
            client.endAttribute("multivalued");
        }
        client.endAttribute("type");
        client.endElement("FM3.Class");
    }

    private int nextSerial() {
        return ++serial;
    }

    private Iterable<String> packageNames() {
        Set<String> names = new HashSet();
        for (AbstractElement each : nameMap.values()) {
            names.add(each.packageName());
        }
        return names;
    }

    @Override
    public void primitive(Object value) {
        stack.getLast().attributeElementsAdd(Primitive.valueOf(value));
    }

    @Override
    public void reference(int index) {
        stack.getLast().attributeElementsAdd(index);
    }

    @Override
    public void reference(String name) {
        // TODO Auto-generated method stub
        throw new AssertionError("Not yet implemented!");
    }

    private void resolveReferences() {
        for (AbstractElement elem : nameMap.values()) {
            for (AbstractAttribute attr : elem.attributes.values()) {
                attr.resolveReferences(indexMap);
            }
        }
    }

    public void run() {
        client.beginDocument();
        this.inferPackages();
        client.endDocument();
    }

    @Override
    public void serial(int index) {
        AbstractElement element = stack.getLast();
        indexMap.put(index, element);
    }

    public void setClient(ParseClient client) {
        this.client = client;
    }

}
