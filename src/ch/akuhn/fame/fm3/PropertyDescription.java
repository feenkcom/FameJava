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

package ch.akuhn.fame.fm3;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import ch.akuhn.fame.FameDescription;
import ch.akuhn.fame.FamePackage;
import ch.akuhn.fame.FameProperty;
import ch.akuhn.fame.MetaRepository;
import ch.akuhn.fame.internal.Access;
import ch.akuhn.fame.internal.Warnings;
import ch.akuhn.util.Throw;

/**
 * Holds meta-information of properties.
 * <p>
 * subclasses NamedElement with attributes
 * </p>
 * <ul>
 * <li>Boolean <code>composite</code> (derived)</li>
 * <li>Boolean <code>container</code></li>
 * <li>Boolean <code>derived</code></li>
 * <li>Boolean <code>keyed</code></li>
 * <li>Boolean <code>multivalued</code></li>
 * <li>String <tt>default</tt></li>
 * <li>Class <code>class</code> (container, opposite Class.attributes)</li>
 * <li>Class <code>type</code></li>
 * <li>Property <code>opposite</code> (opposite Property.opposite)</li>
 * <li>Package <code>package</code> (opposite Package.extensions)</li>
 * </ul>
 * 
 * <p>
 * with these constraints
 * </p>
 * <ul>
 * <li> <code>owner</code> is derived from <code>class</code></li>
 * <li> <code>composite</code> is derived from <code>opposite.container</code></li>
 * <li> <code>container</code> implies not <code>multivalued</code></li>
 * <li>if set, <code>opposite.opposite</code> must be self</li>
 * <li>if set, <code>opposite.class</code> must be <code>type</code></li>
 * <li> <tt>default</tt> must be a valid, complete MSE fragment
 * <li> <code>class</code> must not be nil</li>
 * <li> <code>type</code> must not be nil</li>
 * </ul>
 * <p>
 * and these semantics
 * </p>
 * <ul>
 * <li> <code>container</code> property chains may not include cycles</li>
 * <li> <code>opposite</code> properties must refer to each other</li>
 * <li>any multivalued property defaults to empty</li>
 * <li>boolean properties default to <code>false</code></li>
 * <li>non primitive properties default to <code>null</code></li>
 * <li>string and number properties not have a default value // TODO is this
 * wise?</li>
 * </ul>
 * 
 * @author akuhn
 * 
 */
@FamePackage("FM3")
@FameDescription("Property")
public class PropertyDescription extends Element {

    public class ArrayWrapper<E> extends AbstractCollection<E> {

        public final Object array;
        
        public ArrayWrapper(Object array) {
            assert array.getClass().isArray();
            this.array = array;
        }
        
        @Override
        public Iterator<E> iterator() {
            return new Iterator<E>() {
                int index = 0;
                public boolean hasNext() {
                    return index < size();
                }
                @SuppressWarnings("unchecked")
                public E next() {
                    return (E) Array.get(array, index++);
                }
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public int size() {
            return Array.getLength(array);
        }

    }

    private FM3Type declaringClass;

    private PackageDescription extendingPackage;

    // private AccessibleObject getter;

    private boolean isContainer;

    private boolean isDerived;

    private boolean isMultivalued;

    private PropertyDescription opposite;

    // private Method setter;

    private FM3Type type;

    private Access access;

    public PropertyDescription() {
    }

    public PropertyDescription(String name) {
        super(name);
    }

    public void checkConstraints(Warnings warnings) {
        if (isContainer)
            if (isMultivalued) {
                warnings.add("Container must be single-values", this);
            }
        if (opposite != null)
            if (this != opposite.getOpposite()) {
                warnings.add("Opposites must match", this);
            }
        if (!MetaRepository.isValidName(getName())) {
            warnings.add("Name must be alphanumeric", this);
        }
        if (!Character.isLowerCase(getName().charAt(0))) {
            warnings.add("Name should start lowerCase", this);
        }
        if (type == null)
            warnings.add("Missing type", this);
        if (declaringClass == null)
            warnings.add("Must have an owning class", this);
    }

    @FameProperty(name = "package", opposite = "extensions")
    public PackageDescription getExtendingPackage() {
        return extendingPackage;
    }

    protected AccessibleObject getGetter() {
        throw new AssertionError();
    }

    @FameProperty(opposite = "opposite")
    public PropertyDescription getOpposite() {
        return opposite;
    }

    @Override
    public FM3Type getOwner() {
        return this.getOwningMetaDescription();
    }

    @FameProperty(name = "class", opposite = "properties", container = true)
    public FM3Type getOwningMetaDescription() {
        return declaringClass;
    }

    public Method getSetter() {
        throw new AssertionError();
    }

    @FameProperty
    public FM3Type getType() {
        return type;
    }

    public boolean hasOpposite() {
        return opposite != null;
    }

    @FameProperty(derived = true)
    public boolean isComposite() {
        return opposite != null && opposite.isContainer();
    }

    @FameProperty
    public boolean isContainer() {
        return isContainer;
    }

    @FameProperty
    public boolean isDerived() {
        return isDerived;
    }

    @FameProperty
    public boolean isMultivalued() {
        return isMultivalued;
    }

    public boolean isPrimitive() {
        assert type != null : getFullname();
        return type.isPrimitive();
    }

    public static class ReadingPropertyFailed extends AssertionError {
        private static final long serialVersionUID = 6381545746042993261L;
        public PropertyDescription property;
        public Object object;
        public ReadingPropertyFailed(Exception e, PropertyDescription property, Object object) {
            super(e);
            this.property = property;
            this.object = object;
        }
    }
    
    public Object read(Object element) {
        assert access != null;
        return access.read(element);
    }

    public class NoAccessorException extends RuntimeException {
        private static final long serialVersionUID = -2828241533257508153L;
        public PropertyDescription outer() {
            return PropertyDescription.this;
        }
    }
    
    @SuppressWarnings("unchecked")
    public Collection<Object> readAll(Object element) {
        if (access == null) throw new NoAccessorException();
        assert element != null : "Trying to read property (" + this + ") from null";
        try {
            
            if (this.isMultivalued()) {
                return privateReadAllMultivalued(element);
            }
            
            Object result = this.read(element);
            return result == null ? Collections.EMPTY_SET : Collections.singleton(result);
            
        } catch (Exception ex) {
            throw Throw.exception(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<Object> privateReadAllMultivalued(Object element) {
        Collection<Object> all;
        Object read = this.read(element);
        if (read == null) return new ArrayList<Object>();
        if (read.getClass().isArray()) all = new ArrayWrapper(read);
        else all = (Collection<Object>) read;
        assert !all.contains(null) : "Multivalued property contains null: " + this;
        return all;
    }
    
    public void setAccess(Access access) {
        this.access = access;
    }

    public void setComposite(boolean composite) {
        assert opposite != null;
        opposite.setContainer(composite);
    }

    public void setContainer(boolean isContainer) {
        this.isContainer = isContainer;
    }

    public void setDerived(boolean isDerived) {
        this.isDerived = isDerived;
    }

    public void setExtendingPackage(PackageDescription extendingPackage) {
        this.extendingPackage = extendingPackage;
    }

    public void setGetter(AccessibleObject getter) {
        throw new AssertionError();
    }

    public void setMultivalued(boolean isMultivalued) {
        this.isMultivalued = isMultivalued;
    }

    public void setOpposite(PropertyDescription opposite) {
        this.opposite = opposite;
    }

    public void setOwningMetaDescription(FM3Type owner) {
        this.declaringClass = owner;
        owner.addOwnedProperty(this);
    }

    public void setSetter(Method method) {
        throw new AssertionError();
    }

    public void setType(FM3Type type) {
        this.type = type;
    }

    public void writeAll(Object element, Collection<?> values) {
        assert access != null : getFullname();
        try {
            if (this.isMultivalued()) {
                access.write(element, values);
            } else {
                assert values.size() <= 1 : values + " for " + getFullname();
                for (Object first : values) {
                    access.write(element, first);
                    return;
                }
            }
        } catch (Exception ex) {
            //assert false : ex + ": " + values + " for " + getFullname() + ":" + getType() + " in " + element;
            throw Throw.exception(ex);
        }
    }
    
}
