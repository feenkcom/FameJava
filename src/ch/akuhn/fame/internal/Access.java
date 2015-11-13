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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collection;

import ch.akuhn.fame.FameProperty;

public abstract class Access {

    private final Class<?> elementType;
    private final Class<?> containerType;
    
    public abstract FameProperty getAnnotation();

    public Access(Type type) {
        if (type instanceof Class) {
            Class<?> thisClass = (Class<?>) type;
            if (Collection.class.isAssignableFrom(thisClass)) {
                elementType = Object.class;
                containerType = thisClass;
            } else if (thisClass.isArray()) {
                elementType = thisClass.getComponentType();
                containerType = thisClass;
            } else {
                elementType = thisClass;
                containerType = null;
            }
        } else if (type instanceof ParameterizedType) {
            ParameterizedType paraType = (ParameterizedType) type;
            Class<?> rawClass = (Class<?>) paraType.getRawType();
            assert (Iterable.class.isAssignableFrom(rawClass));
            assert (paraType.getActualTypeArguments().length == 1);
            containerType = rawClass;
            Type typeArgument = paraType.getActualTypeArguments()[0];
            if (typeArgument instanceof Class) {
                elementType = (Class<?>) typeArgument;
            } else if (typeArgument instanceof WildcardType) {
                WildcardType wildcard = (WildcardType) typeArgument;
                assert wildcard.getLowerBounds().length == 0;
                assert wildcard.getUpperBounds().length == 1;
                assert wildcard.getUpperBounds()[0] instanceof Class;
                elementType = (Class<?>) wildcard.getUpperBounds()[0];
            } else {
                throw new AssertionError();
            }
        } else {
            throw new AssertionError();
        }
    }
    
    public Class<?> getElementType() {
        return elementType;
    }

    public abstract String getName();

    public boolean isAnnotationPresent() {
        return getAnnotation() != null;
    }

    public boolean isMultivalued() {
        return containerType != null;
    }

    public abstract Object read(Object element);

    public abstract void write(Object element, Object value);

    public boolean isArray() {
        return containerType != null && containerType.isArray();
    }
    
    public boolean isIterable() {
        return containerType != null && Iterable.class.isAssignableFrom(containerType);
    }
    
    public boolean isCollection() {
        return containerType != null && Collection.class.isAssignableFrom(containerType);
    }

}
