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
import ch.akuhn.fame.Named;
import ch.akuhn.fame.Nested;
import ch.akuhn.fame.internal.Warnings;

/**
 * Abstract superclass of MSE metamodel.
 * 
 * This is an abstract class with attributes </p>
 * <ul>
 * <li>Element <code>owner</code> (derived)</li>
 * <li>String <code>fullname</code> (derived)</li>
 * <li>String <code>name</code></li>
 * </ul>
 * <p>
 * with these constraints
 * </p>
 * <ul>
 * <li> <code>name</code> must be alphanumeric</li>
 * <li> <code>fullname</code> is derived recursively, concatenating
 * <code>owner.fullname</code> and <code>name</code></li>
 * <li> <code>fullname</code> is separated by dots, eg
 * <code>MSE.Class.attributes</code></li>
 * </ul>
 * 
 * @author Adrian Kuhn
 * 
 */
@FamePackage("FM3")
@FameDescription("Element")
public abstract class Element implements Named, Nested {

    private String name;

    public Element() {
    }

    public Element(String name) {
        this.name = name;
    }

    @FameProperty(derived = true)
    public String getFullname() {
        Element parent = this.getOwner();
        return parent == null ? this.getName() : parent.getFullname() + "." + this.getName();
    }

    @FameProperty
    public String getName() {
        return name;
    }

    @FameProperty(derived = true)
    public abstract Element getOwner();

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.getFullname();
    }
    
    public abstract void checkConstraints(Warnings warnings);

}
