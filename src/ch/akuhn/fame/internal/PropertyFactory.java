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

import ch.akuhn.fame.FameProperty;
import ch.akuhn.fame.MetaRepository;
import ch.akuhn.fame.fm3.FM3Type;
import ch.akuhn.fame.fm3.MetaDescription;
import ch.akuhn.fame.fm3.PropertyDescription;

/**
 * Creates metamodel element for {@link FameProperty} annotated method.
 * 
 * @author akuhn
 * 
 */
public class PropertyFactory {

    private Access base;
    private PropertyDescription instance;
    private MetaRepository repository;

    protected PropertyFactory(Access accessor, MetaRepository repository) {
        this.base = accessor;
        this.repository = repository;
    }

    @SuppressWarnings("unchecked")
    private Class baseType() {
        Class type = this.getAnnotation().type();
        if (type == Void.class) { // ie, no type has been specified
            type = base.getElementType();
        }
        return type;
    }

    protected PropertyDescription createInstance() {
        assert this.isAnnotationPresent();
        instance = new PropertyDescription(this.getName());
        return instance;
    }

    protected FameProperty getAnnotation() {
        return base.getAnnotation();
    }

    private String getName() {
        String name = this.getAnnotation().name();
        if (name.equals("*")) {
            name = base.getName();
            if (name.startsWith("is"))
                name = Character.toLowerCase(name.charAt(2)) + name.substring(3);
            if (name.startsWith("get"))
                name = Character.toLowerCase(name.charAt(3)) + name.substring(4);

        }
        return name;
    }

    private void initializeAccessors() {
        instance.setAccess(base);
    }

    protected void initializeInstance() {
        this.initializeType();
        this.initializeIsMultivalues();
        this.initializeIsDerived();
        this.initializeIsContainer();
        this.initializeAccessors();
        this.initializeOpposite();
    }

    private void initializeIsContainer() {
        boolean isContainer = this.getAnnotation().container();
        instance.setContainer(isContainer);
    }

    private void initializeIsDerived() {
        boolean isDerived = this.getAnnotation().derived();
        instance.setDerived(isDerived);
    }

    private void initializeIsMultivalues() {
        boolean isMultivalued = this.isMultivalued();
        instance.setMultivalued(isMultivalued);
    }

    private void initializeOpposite() {
        String oppositeName = this.oppositeName();
        if (oppositeName == null)
            return;
        PropertyDescription opposite = instance.getType().propertyNamed(oppositeName);
        assert opposite != null : "Opposite not found: " + oppositeName + " in " + instance.getType();
        instance.setOpposite(opposite);
    }

    private void initializeType() {
        repository.with(this.baseType());
        FM3Type type = repository.getDescription(this.baseType());
        instance.setType(type);
    }

    protected boolean isAnnotationPresent() {
        return base.isAnnotationPresent();
    }

    private boolean isMultivalued() {
        return base.isMultivalued();
    }

    private String oppositeName() {
        String name = this.getAnnotation().opposite();
        if (name.length() == 0)
            name = null;
        return name;
    }

}
