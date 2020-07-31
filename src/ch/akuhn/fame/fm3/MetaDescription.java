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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.akuhn.fame.FameDescription;
import ch.akuhn.fame.FamePackage;
import ch.akuhn.fame.FameProperty;
import ch.akuhn.fame.MetaRepository;
import ch.akuhn.fame.internal.Warnings;

/**
 * Holds meta-information of classes.
 * <p>
 * This subclasses NamedElement with properties
 * </p>
 * <ul>
 * <li>Boolean <code>abstract</code></li>
 * <li>Boolean <code>primitive</code> (derived)</li>
 * <li>Boolean <code>root</code> (derived)</li>
 * <li>Class <code>superclass</code></li>
 * <li>Package <code>package</code> (container, opposite Package.classes)</li>
 * <li>Property <code>allProperties</code> (derived, multivalued)</li>
 * <li>Property <code>properties</code> (multivalued, opposite Property.class)</li>
 * </ul>
 * <p>
 * with these reserved instances
 * </p>
 * 
 *<pre>
 *  OBJECT = (MSE.Class (name 'Object') (root true))
 *  BOOLEAN = (MSE.Class (name 'Boolean') (primitive true)) 
 *  NUMBER = (MSE.Class (name 'Number') (primitive true)) 
 *  STRING = (MSE.Class (name 'String') (primitive true))
 *</pre>
 * 
 * <p>
 * for any other instance, these constraints apply
 * </p>
 * <ul>
 * <li> <code>owner</code> is derived from <code>package</code></li>
 * <li> <code>superclass</code> is not nil</li>
 * <li> <code>superclass</code> must not be a primitive</li>
 * <li> <code>superclass</code> chain may not include cycles</li>
 * <li> <code>package</code> must not be nil</li>
 * <li> <code>allProperties</code> is derived as union of <code>properties</code>
 * and <code>superclass.allProperties</code></li>
 * <li>only one of <code>allProperties</code> may have
 * <code>container = true</code></li>
 * <li> <code>allProperties</code> must have unique names</li>
 * <li>in particular, none of <code>properties</code> may have the name of any
 * of <code>superclass.allProperties</code></li>
 * </ul>
 * 
 * @author Adrian Kuhn
 * 
 */
@FamePackage("FM3")
@FameDescription("Class")
public class MetaDescription extends FM3Type {

    public static final MetaDescription BOOLEAN = new MetaDescription("Boolean");
    public static final MetaDescription NUMBER = new MetaDescription("Number");
    public static final MetaDescription OBJECT = new MetaDescription("Object");
    public static final MetaDescription STRING = new MetaDescription("String");
    public static final MetaDescription DATE = new MetaDescription("String");

    public static boolean hasPrimitiveNamed(String name) {
        return primitiveNamed(name) != null;
    }
    
    public static MetaDescription primitiveNamed(String name) {
        if ("Boolean".equals(name))
            return BOOLEAN;
        if ("Number".equals(name))
            return NUMBER;
        if ("String".equals(name))
            return STRING;
        if ("Date".equals(name))
            return DATE;
        if ("Object".equals(name))
            return OBJECT;
        return null;
    }

    private Class<?> baseClass;

    private boolean isAbstract;

    private MetaDescription superclass;

    public MetaDescription() {
        super();
    }

    public MetaDescription(String name) {
        super(name);
    }

    @FameProperty(name = "allProperties", derived = true)
    public Collection<PropertyDescription> allProperties() {
        Map<String, PropertyDescription> all = new HashMap();
        this.collectAllProperties(all);
        return all.values();
    }

    public PropertyDescription attributeNamed(String name) {
        PropertyDescription property = properties.get(name);
        if (property == null && superclass != null) {
            property = superclass.attributeNamed(name);
        }
        return property;
    }

    private void collectAllProperties(Map<String, PropertyDescription> all) {
        // superclass first, to ensure correct shadowing
        if (superclass != null)
            superclass.collectAllProperties(all);
        all.putAll(properties);
    }

    /**
     * Answer if this is a subclass of type. This is, if type is a superclass of
     * this: <code>A.conformsTo(B) -> A is B or A extends B</code>.
     * 
     * @param type
     * @return
     */
    public boolean conformsTo(MetaDescription type) {
        return this == type || (superclass != null && superclass.conformsTo(type));
    }

    public PropertyDescription containerPropertyOrNull() {
        for (PropertyDescription property : allProperties()) {
            if (property.isContainer())
                return property;
        }
        return null;
    }

    public Class getBaseClass() {
        return baseClass;
    }

    @FameProperty
    public MetaDescription getSuperclass() {
        return superclass;
    }

    public boolean hasSuperclass() {
        return superclass != null;
    }

    @FameProperty
    // TODO think if I really need isAbstract, and about constraints regarding
    // abstract stuff inheriting from non-abstract stuff, etcetera
    public boolean isAbstract() {
        return isAbstract;
    }

    @FameProperty(derived = true)
    public boolean isPrimitive() {
        return this == STRING || this == BOOLEAN || this == NUMBER || this == DATE;
    }

    @FameProperty(derived = true)
    public boolean isRoot() {
        return this == OBJECT;
    }

    public Object newInstance() {
        try {
            Constructor c = this.getBaseClass().getDeclaredConstructor();
            c.setAccessible(true);
            return c.newInstance();
        } catch (SecurityException ex) {
            throw new AssertionError(ex);
        } catch (IllegalAccessException ex) {
            throw new AssertionError(ex);
        } catch (InstantiationException ex) {
            throw new AssertionError(ex);
        } catch (IllegalArgumentException ex) {
            throw new AssertionError(ex);
        } catch (NoSuchMethodException ex) {
            throw new AssertionError(ex);
        } catch (InvocationTargetException ex) {
            throw new AssertionError(ex);
        }
    }

    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }



    public void setBaseClass(Class baseClass) {
        this.baseClass = baseClass;
    }

    public void setSuperclass(MetaDescription superclass) {
        this.superclass = superclass;
    }

}
