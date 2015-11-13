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

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public abstract class MultivalueSet<T> extends AbstractSet<T> {

    private Set<T> values = new HashSet<T>();

    @Override
    public boolean add(T e) {
        if (null == e) throw new IllegalArgumentException("Element must not be null.");
        if (!values.contains(e)) {
            values.add(e);
            this.setOpposite(e);
            return true;
        }
        return false;
    }

    protected abstract void clearOpposite(T e);

    @Override
    public boolean contains(Object o) {
        return values.contains(o);
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        final Iterator<T> it = values.iterator();
        return new Iterator<T>() {
            T current;

            public boolean hasNext() {
                return it.hasNext();
            }

            public T next() {
                return current = it.next();
            }

            public void remove() {
                it.remove();
                clearOpposite(current);
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        if (null == o) throw new IllegalArgumentException("Element must not be null.");
        if (values.contains(o)) {
            values.remove(o);
            this.clearOpposite((T) o);
            return true;
        }
        return false;
    }

    protected abstract void setOpposite(T e);

    @Override
    public int size() {
        return values.size();
    }

}
