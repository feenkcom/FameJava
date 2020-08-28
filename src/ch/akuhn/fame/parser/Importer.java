//  Copyright (c) 2007-2008 Adrian Kuhn <akuhn(a)iam.unibe.ch>
//  
//  This file is part of 'Fame (for Java)'.
//  
//  'Fame (for Java)' is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or (at your
//  option) any later version.
//  
//  'Fame (for Java)' is distributed in the hope that it will be useful, but
//  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
//  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
//  License for more details.
//  
//  You should have received a copy of the GNU Lesser General Public License
//  along with 'Fame (for Java)'. If not, see <http://www.gnu.org/licenses/>.
//  

package ch.akuhn.fame.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import ch.akuhn.fame.MetaRepository;
import ch.akuhn.fame.Repository;
import ch.akuhn.fame.fm3.MetaDescription;
import ch.akuhn.fame.fm3.PropertyDescription;

/**
 * Reads MSE document from input-stream (in one pass).
 * 
 * @author akuhn
 * 
 */
@SuppressWarnings("unchecked")
public class Importer extends AbstractParserClient {

    /** Retains information about parsing an element. */
    private class Elem {

        /** Retains information about parsing an attribute. */
        private class Attr {

            /** Retains information about a dangling reference. */
            private class Rem {

                private int pos;

                public Rem() {
                    this.pos = values.size();
                    openReferences++;
                }

                public void resolve(Object element) {
                    assert element != null;
                    values.set(pos, element);
                    openReferences--;
                    maybeEnd();
                }

            }

            private List<Object> values;
            private String attributeName;

            private int openReferences;

            public Attr(String name) {
                this.attributeName = name;
                this.openReferences = 0;
                this.values = new ArrayList();
            }

            public void add(Object value) {
                values.add(value);
            }

            public void endAttribute(String name) {
                assert this.attributeName == name;
                maybeEnd();
            }

            private void maybeEnd() {
                if (openReferences > 0)
                    return;
                Object parent = Elem.this.getElement();
                MetaDescription meta = (MetaDescription) metamodel.getDescription(parent.getClass());
                PropertyDescription property = meta.attributeNamed(attributeName);
                assert property != null : "'" + attributeName + "' in " + meta;
                property.writeAll(parent, values);
            }

            public void reference(int serial) {
                Object element = index.retrieve(serial);
                values.add(element == null ? index.keepReminder(new Rem(), serial) : element);

            }

        }

        private String elementName;
        private Attr currentAttribute;
        private Object actualElement;

        public Elem(String name) {
            this.elementName = name;
            this.actualElement = null;
            this.currentAttribute = null;
        }

        public void add(Object value) {
            currentAttribute.add(value);
        }

        public void beginAttribute(String name) {
            assert currentAttribute == null;
            currentAttribute = new Attr(name);
        }

        public void endAttribute(String name) {
            currentAttribute.endAttribute(name);
            currentAttribute = null;
        }

        public Object getElement() {
            if (actualElement != null)
                return actualElement;
            MetaDescription meta = metamodel.descriptionNamed(elementName);
            assert meta != null : elementName;
            actualElement = meta.newInstance();
            return actualElement;
        }

        public void reference(int serial) {
            currentAttribute.reference(serial);
        }

        public void serial(int serial) {
            index.assign(serial, this.getElement());
        }
    }

    /**
     * Keeps track of assigned indices and open references.
     */
    private class Index {

        private Map<Integer, Object> serials = new TreeMap<Integer, Object>();
        private Map<Integer, Collection<Elem.Attr.Rem>> reminders = new TreeMap<Integer, Collection<Elem.Attr.Rem>>();
        private int openReferences = 0;

        public void assign(int serial, Object element) {
            assert element != null;
            assert !serials.containsKey(serial);
            serials.put(serial, element);
            resolveReminders(serial, element);
        }

        public boolean hasDanglingReferences() {
            return openReferences > 0;
        }

        public Elem.Attr.Rem keepReminder(Elem.Attr.Rem reminder, int serial) {
            assert !serials.containsKey(serial);
            Collection<Elem.Attr.Rem> todo = reminders.get(serial);
            if (todo == null)
                reminders.put(serial, todo = new LinkedList<Elem.Attr.Rem>());
            todo.add(reminder);
            openReferences++;
            return reminder;
        }

        private void resolveReminders(int serial, Object element) {
            Collection<Elem.Attr.Rem> todo = reminders.remove(serial);
            if (todo == null)
                return;
            for (Elem.Attr.Rem each : todo) {
                each.resolve(element);
                openReferences--;
                assert openReferences >= 0;
            }
        }

        public Object retrieve(int serial) {
            return serials.get(serial);
        }

    }

    private Stack<Elem> elementStack;

    private Collection elements;

    private Index index;

    private MetaRepository metamodel;

    private Repository model;

    public Importer(MetaRepository metamodel) {
        this(metamodel, new Repository(metamodel));
    }

    public Importer(MetaRepository metamodel, Repository model) {
        this.metamodel = metamodel;
        this.model = model;
    }
    
    @Override
    public void beginAttribute(String name) {
        elementStack.peek().beginAttribute(name);
    }

    @Override
    public void beginDocument() {
        this.index = new Index();
        this.elements = new ArrayList();
        this.elementStack = new Stack<Elem>();
    }

    @Override
    public void beginElement(String name) {
        elementStack.push(new Elem(name));
    }

    @Override
    public void endAttribute(String name) {
        elementStack.peek().endAttribute(name);
    }

    @Override
    public void endDocument() {
        assert elementStack.isEmpty();
        this.elementStack = null;
        assert !index.hasDanglingReferences();
        this.index = null;
        for (Object element : elements) {
            model.add(element);
        }
        this.elements = null;
    }

    @Override
    public void endElement(String name) {
        Elem frame = elementStack.pop();
        Object element = frame.getElement();
        elements.add(element);
        if (!elementStack.isEmpty()) {
            elementStack.peek().add(element);
        }
    }

    public Repository getResult() {
        return model;
    }

    @Override
    public void primitive(Object value) {
        elementStack.peek().add(value);
    }

    public void readFrom(InputSource in) {
        Parser parser = new Parser(new Scanner(in));
        parser.accept(this);
    }

    @Override
    public void reference(int serial) {
        elementStack.peek().reference(serial);
    }

    @Override
    public void reference(String name) {
        // TODO find nice solution for this hack
        MetaDescription type = MetaDescription.primitiveNamed(name);
        assert type != null : name;
        elementStack.peek().add(type);
    }

    @Override
    public void serial(int serial) {
        elementStack.peek().serial(serial);
    }

}
