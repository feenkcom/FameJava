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

import java.util.Collection;
import java.util.HashSet;

import ch.akuhn.fame.FameDescription;
import ch.akuhn.fame.FamePackage;
import ch.akuhn.fame.FameProperty;
import ch.akuhn.fame.MetaRepository;
import ch.akuhn.fame.internal.Warnings;

/**
 * Holds meta-information about packaging.
 * <p>
 * subclasses NamedElement with attributes
 * </p>
 * <ul>
 * <li>Class <code>classes</code> (multivalued, opposite Class.package)</li>
 * <li>Property <code>extensions</code> (multivalued, opposite Property.package)
 * </li>
 * </ul>
 * <p>
 * with these constraints
 * </p>
 * <ul>
 * <li> <code>owner</code> is nil</li>
 * <li> <code>classes</code> must have unique names</li>
 * </ul>
 * 
 * @author Adrian Kuhn
 * 
 */
@FamePackage("FM3")
@FameDescription("Package")
public class PackageDescription extends Element {

    public static final String NAME = "FM3.Package";

    private Collection<FM3Type> elements;

    private Collection<PropertyDescription> extensions;

    public PackageDescription() {
        this.elements = new HashSet();
        this.extensions = new HashSet();
    }

    public PackageDescription(String name) {
        super(name);
        this.elements = new HashSet();
        this.extensions = new HashSet();
    }

    public void addElement(FM3Type instance) {
        if (elements.add(instance)) {
            instance.setPackage(this);
        }
    }

    public void checkConstraints(Warnings warnings) {
        if (!MetaRepository.isValidName(getName()))
            warnings.add("Name must be alphanumeric", this);
    }

    @FameProperty(opposite = "package")
    public Collection<FM3Type> getClasses() {
        return elements;
    }

    @FameProperty(opposite = "package")
    public Collection<PropertyDescription> getExtensions() {
        return extensions;
    }

    @Override
    public Element getOwner() {
        return null;
    }

    public void setClasses(Collection<FM3Type> classes) {
        // this.elements = new HashSet(); TODO think about nice handling of
        // multivalued properties with opposite
        for (FM3Type each : classes) {
            this.addElement(each);
        }
    }

    public void setExtensions(Collection<PropertyDescription> extensions) {
        this.extensions = extensions;
    }

}
