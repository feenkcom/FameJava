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
import ch.akuhn.fame.MetaRepository;
import ch.akuhn.fame.internal.Warnings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
@FameDescription("Trait")
public class FM3Trait extends FM3Type {

    public FM3Trait() {
        super();
    }

    public FM3Trait(String name) {
        super(name);
    }

    FM3Type traitOwner;
    @FameProperty(name = "owner", opposite = "traits")
    public FM3Type getTraitOwner() {
        return traitOwner;
    }
    public void setTraitOwner(FM3Type traitOwner){
        this.traitOwner = traitOwner;
    }

}
