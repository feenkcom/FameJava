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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

import ch.akuhn.fame.FameDescription;
import ch.akuhn.fame.FamePackage;
import ch.akuhn.fame.FameProperty;
import ch.akuhn.fame.MetaRepository;
import ch.akuhn.fame.fm3.MetaDescription;
import ch.akuhn.fame.fm3.PackageDescription;
import ch.akuhn.fame.fm3.PropertyDescription;

/**
 * Creates metamodel element for {@link FameDescription} annotated class.
 * 
 * @author akuhn
 * 
 */
@SuppressWarnings("unchecked")
public class MetaDescriptionFactory {

    private Class base;
    private Collection<PropertyFactory> childFactories;
    private MetaDescription instance;
    private MetaRepository repository;

    public MetaDescriptionFactory(Class base, MetaRepository repository) {
        this.base = base;
        this.repository = repository;
        this.childFactories = new ArrayList<PropertyFactory>();
    }

    public MetaDescription createInstance() {
        assert this.isAnnotationPresent();
        instance = new MetaDescription(this.name());
        return instance;
    }

    private void createPropertyFactories() {
        for (Method method : base.getDeclaredMethods()) {
            if (method.isAnnotationPresent(FameProperty.class)) {
                PropertyFactory factory = new PropertyFactory(new MethodAccess(method), repository);
                childFactories.add(factory);
            }
        }
        for (Field f : base.getDeclaredFields()) {
            if (f.isAnnotationPresent(FameProperty.class)) {
                PropertyFactory factory = new PropertyFactory(new FieldAccess(f), repository);
                childFactories.add(factory);
            }
        }
    }

    private void createPropertyInstances() {
        for (PropertyFactory factory : childFactories) {
            PropertyDescription property = factory.createInstance();
            instance.addOwnedProperty(property);
            property.setOwningMetaDescription(instance);
        }
    }

    private FameDescription getAnnotation() {
        return (FameDescription) base.getAnnotation(FameDescription.class);
    }

    private void initializeBaseClass() {
        instance.setBaseClass(base);
    }

    public void initializeInstance() {
        this.initializePackage();
        this.createPropertyFactories();
        this.createPropertyInstances();
        this.initializeSuperclass();
        this.initializeProperties();
        this.initializeBaseClass();
        this.initializeIsAbstract();
    }

    private void initializeIsAbstract() {
        boolean isAbstract = Modifier.isAbstract(base.getModifiers());
        instance.setAbstract(isAbstract);
    }

    private void initializePackage() {
        PackageDescription pack = repository.initializePackageNamed(this.packageName());
        instance.setPackage(pack);
        pack.addElement(instance);
    }

    private void initializeProperties() {
        for (PropertyFactory factory : childFactories) {
            factory.initializeInstance();
        }
    }

    private void initializeSuperclass() {
        repository.with(base.getSuperclass());
        MetaDescription superclass = repository.getDescription(base.getSuperclass());
        instance.setSuperclass(superclass);
    }

    // /////////////////////////////////////////

    public boolean isAnnotationPresent() {
        return base.isAnnotationPresent(FameDescription.class);
    }

    /** Answer either the name given in the annotation, or the class name. */
    private String name() {
        String name = this.getAnnotation().value();
        if (name.equals("*")) {
            name = base.getSimpleName();
        }
        return name;
    }

    private String packageName() {
        Class curr = base;
        while (curr != null) {
            FamePackage p = (FamePackage) curr.getAnnotation(FamePackage.class);
            if (p != null)
                return p.value();
            curr = curr.getEnclosingClass();
        }
        Package j = base.getPackage();
        FamePackage p = j.getAnnotation(FamePackage.class);
        if (p != null)
            return p.value();
        String fullName = j.getName();
        return fullName.substring(fullName.lastIndexOf('.') + 1).toUpperCase();
    }

}
