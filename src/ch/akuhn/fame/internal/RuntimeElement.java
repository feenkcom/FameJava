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

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Any element with any slots.
 * 
 * @author akuhn
 * 
 */
public class RuntimeElement {

    public static class Slot extends AbstractCollection {

        private String name;
        private List values;

        public Slot(String name) {
            this.name = name;
            this.values = new ArrayList();
        }

        @Override
        public boolean add(Object element) {
            assert element != null;
            return values.add(element);
        }

        @Override
        public void clear() {
            values.clear();
        }

        @Override
        public boolean contains(Object element) {
            return values.contains(element);
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean isEmpty() {
            return values.isEmpty();
        }

        public boolean isSingleValued() {
            return values.size() <= 1;
        }

        @Override
        public Iterator iterator() {
            return values.iterator();
        }

        @Override
        public boolean remove(Object element) {
            return values.remove(element);
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public int size() {
            return values.size();
        }

    }

    private Map<String, List> slots = new HashMap();

    private String typeName;

    public Map<String, List> getSlots() {
        return slots;
    }

    public String getTypeName() {
        return typeName;
    }

    public Object read(String name) {
        List slot = slots.get(name);
        if (slot == null)
            return null;
        assert slot.size() <= 1;
        return slot.isEmpty() ? null : slot.get(0);
    }

    public Collection readAll(String string) {
        List slot = slots.get(string);
        if (slot == null)
            return Collections.EMPTY_LIST;
        return slot;
    }

    public void setSlots(Map<String, List> slots) {
        this.slots = slots;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public List slotNamed(String name) {
        List slot = slots.get(name);
        if (slot == null)
            slots.put(name, slot = new ArrayList());
        return slot;
    }

    @Override
    public String toString() {
        return "a " + getTypeName();
    }

    public void write(String name, Object value) {
        List slot = slots.get(name);
        if (slot == null) {
            slot = new ArrayList();
            slots.put(name, slot);
        }
        assert slot.size() <= 1;
        if (slot.isEmpty())
            slot.add(value);
        else
            slot.set(0, value);
    }
}
