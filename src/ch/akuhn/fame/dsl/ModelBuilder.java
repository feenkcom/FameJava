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

package ch.akuhn.fame.dsl;

import ch.akuhn.fame.internal.MSEPrinter;
import ch.akuhn.fame.parser.DebugClient;
import ch.akuhn.fame.parser.ParseClient;

public class ModelBuilder {

    interface Endable {
        public void endDocument();
    }
    
    interface Document extends Endable {
        public ElementWithMaybeSerial<Document> element(String string);
    }

    interface Element<T extends Endable> extends Endable {
        public Attribute<Element<T>> with(String attribute);
        public Attribute<Element<T>> with(String attribute, Object... values);
        public T endElement();
    }

    interface ElementWithMaybeSerial<T extends Endable> extends Element<T> {
        public Element<T> index(int index);
    }

    interface Attribute<T extends Element> extends Endable {
        public Attribute<T> value(Object value);
        public Attribute<T> reference(int index);
        public Attribute<T> reference(String name);
        public ElementWithMaybeSerial<Attribute<T>> element(String name);
        public T endAttribute();
        public Attribute<T> with(String attribute);
        public Attribute<T> with(String attribute, Object... b);
    }
    
    private final ParseClient client;
    
    public ModelBuilder(ParseClient client) {
        this.client = client;
    }
    
    public Document beginDocument() {
        return new DocumentImpl();
    }
    
    class DocumentImpl implements Document {

        public DocumentImpl() {
            client.beginDocument();
        }
        
        public ElementWithMaybeSerial<Document> element(String name) {
            return new ElementImpl(name, this);
        }

        public void endDocument() {
            client.endDocument();
        }
        
    }

    class ElementImpl<T extends Endable> implements ElementWithMaybeSerial<T> {
        
        final String name;
        final T outer;
        
        public ElementImpl(String name, T outer) {
            client.beginElement(name);
            this.name = name;
            this.outer = outer;
        }

        public Element<T> index(int index) {
            client.serial(index);
            return this;
        }

        public Attribute<Element<T>> with(String attrName) {
            return new AttributeImpl(attrName, this);
        }

        public T endElement() {
            client.endElement(name);
            return outer;
        }

        public void endDocument() {
            endElement().endDocument();
        }

        public Attribute<Element<T>> with(String attribute, Object... values) {
            Attribute<Element<T>> $ = with(attribute);
            for (Object each : values) {
                $.value(each);
            }
            return $;
        }
        
    }    
    
    class AttributeImpl<T extends Element> implements Attribute<T> {
        
        final String name;
        final T outer;
        
        public AttributeImpl(String name, T outer) {
            client.beginAttribute(name);
            this.name = name;
            this.outer = outer;
        }

        public ElementWithMaybeSerial<Attribute<T>> element(String elemName) {
            return new ElementImpl(elemName, this);
        }

        public T endAttribute() {
            client.endAttribute(name);
            return outer;
        }

        public Attribute<T> reference(int index) {
            client.reference(index);
            return this;
        }

        public Attribute<T> reference(String qname) {
            client.reference(qname);
            return this;
        }

        public Attribute<T> value(Object value) {
            client.primitive(value);
            return this;
        }

        public Attribute<T> with(String string) {
            return endAttribute().with(string);
        }

        public void endDocument() {
            endAttribute().endDocument();
        }

        public Attribute<T> with(String string, Object... values) {
            Attribute<T> $ = with(string);
            for (Object each : values) $.value(each);
            return $;
        }

    }
    
    public static void main(String[] args) {
        
        ParseClient pc = new DebugClient();
        pc = new MSEPrinter(System.out);
        
        new ModelBuilder(pc)
        
        .beginDocument()
            .element("FM3.Package").index(1)
                .with("name", "HAPAX")
                .with("classes")
                    .element("FM3.Class").index(2)
                        .with("name", "Document")
                        .with("attributes")
                            .element("FM3.Property")
                                .with("name", "content")
                                .with("type").reference(3)
                                .with("multivalued", true)
                                .endAttribute()
                            .endElement()
                        .endAttribute()    
                    .endElement()
                    .element("FM3.Class").index(3)
                        .with("name", "Occurrence")
                        .with("attributes")
                            .element("FM3.Property")
                                .with("name", "term")
                                .with("type").reference("String")
                                .endAttribute()
                            .endElement()
                            .element("FM3.Property")
                                .with("name", "frequency")
                                .with("type").reference("Number")
        .endDocument();    
                
        System.out.println(pc);
        
    }

}

