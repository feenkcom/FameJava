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
 * This subclasses NamedElement with attributes
 * </p>
 * <ul>
 * <li>Boolean <code>abstract</code></li>
 * <li>Boolean <code>primitive</code> (derived)</li>
 * <li>Boolean <code>root</code> (derived)</li>
 * <li>Class <code>superclass</code></li>
 * <li>Package <code>package</code> (container, opposite Package.classes)</li>
 * <li>Property <code>allAttributes</code> (derived, multivalued)</li>
 * <li>Property <code>attributes</code> (multivalued, opposite Property.class)</li>
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
 * <li> <code>allAttributes</code> is derived as union of <code>attributes</code>
 * and <code>superclass.allAttributes</code></li>
 * <li>only one of <code>allAttributes</code> may have
 * <code>container = true</code></li>
 * <li> <code>allAttributes</code> must have unique names</li>
 * <li>in particular, none of <code>attributes</code> may have the name of any
 * of <code>superclass.allAttributes</code></li>
 * </ul>
 * 
 * @author Adrian Kuhn
 * 
 */
@FamePackage("FM3")
@FameDescription("Class")
public class MetaDescription extends Element {

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

    private Map<String, PropertyDescription> attributes;

    private Class<?> baseClass;

    private boolean isAbstract;

    private PackageDescription nestingPackage;

    private MetaDescription superclass;

    public MetaDescription() {
        super();
        this.attributes = new HashMap<String, PropertyDescription>();
    }

    public MetaDescription(String name) {
        super(name);
        this.attributes = new HashMap<String, PropertyDescription>();
    }

    public void addOwnedAttribute(PropertyDescription property) {
        attributes.put(property.getName(), property);
        if (property.getOwningMetaDescription() != this) {
            property.setOwningMetaDescription(this);
        }
    }

    @FameProperty(name = "allAttributes", derived = true)
    public Collection<PropertyDescription> allAttributes() {
        Map<String, PropertyDescription> all = new HashMap();
        this.collectAllAttributes(all);
        return all.values();
    }

    public PropertyDescription attributeNamed(String name) {
        PropertyDescription property = attributes.get(name);
        if (property == null && superclass != null) {
            property = superclass.attributeNamed(name);
        }
        return property;
    }

    public void checkConstraints(Warnings warnings) {
        int container = 0;
        for (PropertyDescription property : allAttributes()) {
            if (property.isContainer())
                container++;
        }
        if (container > 1) {
            warnings.add("May not have more than one container", this);
        }
        if (!MetaRepository.isValidName(getName())) {
            warnings.add("Name must be alphanumeric", this);
        }
        if (this != OBJECT && this != STRING && this != BOOLEAN && this != DATE && this != NUMBER) {
            if (nestingPackage == null) {
                warnings.add("Must be owned by a package", this);
            }
            if (superclass == null) {
                warnings.add("Must have a superclass", this);
            }
            if (superclass == STRING && superclass == BOOLEAN && superclass == NUMBER) {
                warnings.add("May not have primitive superclass", this);
            }
        } else {
            assert nestingPackage == null;
            assert superclass == null;
            assert attributes.isEmpty();
        }
        Set<MetaDescription> set = new HashSet<MetaDescription>();
        set.add(this);
        for (MetaDescription each = this; each == null; each = each.superclass) {
            if (!set.add(each)) {
                warnings.add("Superclass chain may not be circular", this);
                break;
            }
        }
    }

    private void collectAllAttributes(Map<String, PropertyDescription> all) {
        // superclass first, to ensure correct shadowing
        if (superclass != null)
            superclass.collectAllAttributes(all);
        all.putAll(attributes);
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
        for (PropertyDescription property : allAttributes()) {
            if (property.isContainer())
                return property;
        }
        return null;
    }

    @FameProperty(opposite = "class")
    public Collection<PropertyDescription> getAttributes() {
        return attributes.values();
    }

    public Class getBaseClass() {
        return baseClass;
    }

    @Override
    public PackageDescription getOwner() {
        return this.getPackage();
    }

    @FameProperty(opposite = "classes", container = true)
    public PackageDescription getPackage() {
        return nestingPackage;
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

    public void setAttributes(Collection<PropertyDescription> attributes) {
        this.attributes = new HashMap<String, PropertyDescription>();
        for (PropertyDescription property : attributes) {
            this.addOwnedAttribute(property);
        }
    }

    public void setBaseClass(Class baseClass) {
        this.baseClass = baseClass;
    }

    public void setPackage(PackageDescription owner) {
        this.nestingPackage = owner;
        owner.addElement(this);
    }

    public void setSuperclass(MetaDescription superclass) {
        this.superclass = superclass;
    }

}
