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

package ch.akuhn.fame.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import ch.akuhn.fame.fm3.MetaDescription;
import ch.akuhn.fame.parser.AbstractParserClient;

/**
 * Reads MSE document as {@link RuntimeElement} elements.
 * 
 * @author akuhn
 * 
 */
public class PrototypeBuilder extends AbstractParserClient {

    private class Ref {
        private int serial;

        public Ref(int serial) {
            this.serial = serial;
        }

        public RuntimeElement resolve() {
            RuntimeElement value = index.get(serial);
            assert value != null : serial;
            return value;
        }
    }

    private Stack<RuntimeElement> elemenStack;
    private Stack<Collection> slotStack;
    private Map<Integer, RuntimeElement> index;
    private Collection<RuntimeElement> elements;

    @Override
    public void beginAttribute(String name) {
        Collection slot = elemenStack.peek().slotNamed(name);
        slotStack.push(slot);
    }

    @Override
    public void beginDocument() {
        this.elemenStack = new Stack<RuntimeElement>();
        this.slotStack = new Stack<Collection>();
        this.index = new HashMap<Integer, RuntimeElement>();
        this.elements = new ArrayList();
    }

    @Override
    public void beginElement(String name) {
        RuntimeElement element = new RuntimeElement();
        element.setTypeName(name);
        elemenStack.push(element);
    }

    @Override
    public void endAttribute(String name) {
        slotStack.pop();
    }

    @Override
    public void endDocument() {
        for (RuntimeElement each : elements) {
            for (List values : each.getSlots().values()) {
                for (int n = 0; n < values.size(); n++) {
                    Object value = values.get(n);
                    if (value instanceof Ref) {
                        values.set(n, ((Ref) value).resolve());
                    }
                }
            }
        }
        this.elemenStack = null;
        this.slotStack = null;
        this.index = null;
    }

    @Override
    public void endElement(String name) {
        RuntimeElement element = elemenStack.pop();
        if (!slotStack.isEmpty()) {
            slotStack.peek().add(element);
        }
        elements.add(element);
    }

    public Collection<RuntimeElement> getPrototypes() {
        return elements;
    }

    @Override
    public void primitive(Object value) {
        assert value != null;
        slotStack.peek().add(value);
    }

    @Override
    public void reference(int serial) {
        slotStack.peek().add(new Ref(serial));
    }

    @Override
    public void reference(String name) {
        // TODO find nicer solution (discarded anyway, once moved to FAMIX 3)
        String _name = name;
        if (name.endsWith("Timestamp")) _name = "Date";
        slotStack.peek().add(MetaDescription.primitiveNamed(_name));
    }

    @Override
    public void serial(int serial) {
        index.put(serial, elemenStack.peek());
    }
}
