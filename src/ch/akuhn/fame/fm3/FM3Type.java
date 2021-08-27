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

import ch.akuhn.fame.FameDescription;
import ch.akuhn.fame.FamePackage;
import ch.akuhn.fame.FameProperty;
import ch.akuhn.fame.internal.Warnings;

import java.util.*;

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
@FameDescription("Type")
public class FM3Type extends Element {

    private PackageDescription nestingPackage;

    protected Map<String, PropertyDescription> properties;

    private Map<String, FM3Trait> traits;


    public FM3Type(){
        super();
        this.properties = new HashMap<String, PropertyDescription>();
        this.traits = new HashMap<>();
    }

    public FM3Type(String name) {
        super(name);
        this.properties = new HashMap<String, PropertyDescription>();
        this.traits = new HashMap<>();
    }


    @Override
    public PackageDescription getOwner() {
        return this.getPackage();
    }

    @Override
    public void checkConstraints(Warnings warnings) {

    }

    // About package

    @FameProperty(opposite = "classes", container = true)
    public PackageDescription getPackage() {
        return nestingPackage;
    }

    public void setPackage(PackageDescription owner) {
        this.nestingPackage = owner;
        owner.addElement(this);
    }

    // About Property

    @FameProperty(opposite = "class")
    public Collection<PropertyDescription> getProperties() {
        return properties.values();
    }


    public void addOwnedProperty(PropertyDescription property) {
        properties.put(property.getName(), property);
        if (property.getOwningMetaDescription() != this) {
            property.setOwningMetaDescription(this);
        }
    }

    public void setProperties(Collection<PropertyDescription> properties) {
        this.properties = new HashMap<String, PropertyDescription>();
        for (PropertyDescription property : properties) {
            this.addOwnedProperty(property);
        }
    }

    public PropertyDescription propertyNamed(String name) {
        return properties.get(name);
    }

    // About Traits

    public void addOwnedTrait(FM3Trait trait) {
        traits.put(trait.getName(), trait);
    }

    public void setTraits(Collection<FM3Trait> traits) {
        this.traits = new HashMap<String, FM3Trait>();
        for (FM3Trait trait : traits) {
            this.addOwnedTrait(trait);
        }
    }

    @FameProperty(name="traits", opposite = "owner")
    public Collection<FM3Trait> getTraits() {
        return traits.values();
    }

    // Help generation

    @FameProperty(derived = true)
    public boolean isPrimitive() {
        return false;
    }

    @FameProperty(derived = true)
    public boolean isRoot() {
        return true;
    }

    public Set<FM3Trait> computeAllTraits() {
        Set<FM3Trait> traits = new HashSet<>();
        traits.addAll(getTraits());
        for (FM3Trait t : traits) {
            traits.addAll(t.computeAllTraits());
        }
        return traits;
    }
}
