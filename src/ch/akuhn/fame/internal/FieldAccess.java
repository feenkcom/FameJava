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

import java.lang.reflect.Field;
import java.util.Collection;

import ch.akuhn.fame.FameProperty;
import ch.akuhn.fame.fm3.PropertyDescription.ArrayWrapper;
import ch.akuhn.util.Throw;

public class FieldAccess extends Access {

    private Field field;

    public FieldAccess(Field f) {
        super(f.getGenericType());
        this.field = f;
        this.field.setAccessible(true);
    }

    @Override
    public FameProperty getAnnotation() {
        return field.getAnnotation(FameProperty.class);
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public Object read(Object element) {
        try {
            return field.get(element);
        } catch (IllegalArgumentException ex) {
            throw new AssertionError(ex);
        } catch (IllegalAccessException ex) {
            throw new AssertionError(ex);
        }
    }

    @Override
    public void write(Object element, Object value) {
        try {
            field.set(element, adapt(value));
        } catch (IllegalArgumentException ex) {
            throw Throw.exception(ex);
        } catch (IllegalAccessException ex) {
            throw Throw.exception(ex);
        }
    }

    private Object adapt(Object element) {
        if (isArray() && (element instanceof ArrayWrapper))
            return ((ArrayWrapper<?>) element).array;
        if (isArray() && (element instanceof Collection)) {
            Class<?> kind = field.getType().getComponentType();
            if (kind == Float.TYPE) {
                Collection<?> collection = (Collection<?>) element;
                float[] array = new float[collection.size()];
                int index = 0;
                for (Object each: collection) {
                    array[index++] = ((Number) each).floatValue();
                }
                return array;
            }
            return ((Collection<?>) element).toArray();
        }
        return element;
    }

}
