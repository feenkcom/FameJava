package ch.akuhn.fame.internal;

import ch.akuhn.fame.FameDescription;
import ch.akuhn.fame.FamePackage;
import ch.akuhn.fame.FameProperty;
import ch.akuhn.fame.MetaRepository;
import ch.akuhn.fame.fm3.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

public class TraitFactory {

    private Class base;
    private Collection<PropertyFactory> childFactories;
    protected FM3Trait instance;
    private MetaRepository repository;

    public TraitFactory(Class base, MetaRepository repository) {
        this.base = base;
        this.repository = repository;
        this.childFactories = new ArrayList<PropertyFactory>();
    }

    public FM3Trait createInstance() {
        assert this.isAnnotationPresent();
        instance = new FM3Trait(this.name());
        return instance;
    }

    protected void createPropertyFactories() {
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

    protected void createPropertyInstances() {
        for (PropertyFactory factory : childFactories) {
            PropertyDescription property = factory.createInstance();
            instance.addOwnedProperty(property);
            property.setOwningMetaDescription(instance);
        }
    }

    private FameDescription getAnnotation() {
        return (FameDescription) base.getAnnotation(FameDescription.class);
    }


    public void initializeInstance() {
        this.initializePackage();
        this.createPropertyFactories();
        this.createPropertyInstances();
        this.initializeProperties();
        this.initializeTraits();
    }


    protected void initializePackage() {
        PackageDescription pack = repository.initializePackageNamed(this.packageName());
        instance.setPackage(pack);
        pack.addElement(instance);
    }

    protected void initializeProperties() {
        for (PropertyFactory factory : childFactories) {
            factory.initializeInstance();
        }
    }

    protected void initializeTraits() {
        for (Class i : base.getInterfaces()){
            repository.with(i);
            FM3Trait trait = (FM3Trait) repository.getDescription(i);
            instance.addOwnedTrait(trait);
        }
    }

    // /////////////////////////////////////////

    public boolean isAnnotationPresent() {
        return base.isAnnotationPresent(FameDescription.class);
    }

    /** Answer either the name given in the annotation, or the class name. */
    protected String name() {
        String name = this.getAnnotation().value();
        if (name.equals("*")) {
            name = base.getSimpleName();
        }
        return name;
    }

    protected String packageName() {
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
