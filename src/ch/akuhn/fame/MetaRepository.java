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

package ch.akuhn.fame;

import static ch.akuhn.util.Strings.isAlphanumeric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ch.akuhn.fame.dsl.MetamodelBuilder;
import ch.akuhn.fame.dsl.ProtocolChecker;
import ch.akuhn.fame.fm3.*;
import ch.akuhn.fame.internal.MetaDescriptionFactory;
import ch.akuhn.fame.internal.TraitFactory;
import ch.akuhn.fame.internal.Warnings;
import ch.akuhn.fame.parser.DebugClient;
import ch.akuhn.fame.parser.Importer;
import ch.akuhn.fame.parser.ParseClient;
import ch.akuhn.util.Strings;

/**
 * A meta-model (ie elements conforming to FM3).
 * 
 * @author Adrian Kuhn, 2007-2008
 * 
 */
@SuppressWarnings("unchecked")
public class MetaRepository extends Repository {

    public static MetaRepository createFM3() {
        MetaRepository mse = new MetaRepository(null);
        mse.with(ch.akuhn.fame.fm3.MetaDescription.class);
        mse.with(ch.akuhn.fame.fm3.Element.class);
        mse.with(ch.akuhn.fame.fm3.PackageDescription.class);
        mse.with(ch.akuhn.fame.fm3.PropertyDescription.class);
        mse.with(ch.akuhn.fame.fm3.FM3Trait.class);
        mse.with(ch.akuhn.fame.fm3.FM3Type.class);
        mse.addAll(new ArrayList(mse.bindings.values()));
        mse.setImmutable();
        return mse;
    }

    public static boolean isValidElementName(String string) {
        if (string == null || string.length() == 0)
            return false;
        boolean expectStart = true;
        for (char ch: Strings.chars(string)) {
            if (expectStart) {
                if (Character.isLetter(ch)) {
                    expectStart = false;
                } else {
                    return false;
                }
            } else {
                if (ch == '.') {
                    expectStart = true;
                } else {
                    if (!Character.isLetterOrDigit(ch) && ch != '_')
                        return false;
                }
            }
        }
        return !expectStart;
    }

    public static boolean isValidName(String string) {
        return string != null && string.length() > 0 && Character.isLetter(string.charAt(0)) && isAlphanumeric(string);
    }

    private Map<String, Element> bindings;

    private Map<Class, MetaDescription> classes;
    private Map<Class, FM3Trait> traits;


    private boolean immutable = false;

    public MetaRepository() {
        this(createFM3());
    }

    public MetaRepository(MetaRepository metamodel) {
        super(metamodel);
        classes = new HashMap();
        bindings = new HashMap();
        traits = new HashMap();
    }

    @Override
    public void add(Object element) {
        assert !immutable;
        assert element instanceof Element : element.getClass();
        if (element instanceof MetaDescription) {
            MetaDescription meta = (MetaDescription) element;
            if (meta.isPrimitive() || meta.isRoot()) return;
        }
        super.add(element);
        bindings.put(((Element) element).getFullname(), (Element) element);
    }

    public void addClassDescription(Class<? extends Object> cls, MetaDescription desc) {
        classes.put(cls, desc);
    }
    
    public Collection<MetaDescription> allClassDescriptions() {
        return all(MetaDescription.class);
    }
    
    public Collection<PackageDescription> allPackageDescriptions() {
        return all(PackageDescription.class);
    }

    public Collection<PropertyDescription> allPropertyDescriptions() {
        return all(PropertyDescription.class);
    }
    
    public MetamodelBuilder builder() {
        ParseClient client = new Importer(getMetamodel(), this);
        client = new DebugClient(client);
        client = new ProtocolChecker(client);
        return new MetamodelBuilder(client);
    }

    public Warnings checkConstraints() {
        Warnings warnings = new Warnings();
        for (Object each: getElements()) {
            ((Element) each).checkConstraints(warnings);
        }
        return warnings;
    }

    public MetaDescription descriptionNamed(String fullname) {
        Object found = this.get(fullname);
        if (found instanceof MetaDescription) return (MetaDescription) found;
        return null;
    }

    public static class ClassNotMetadescribedException extends AssertionError {
        private static final long serialVersionUID = 894469439304582534L;
        private Class<? extends Object> cls;
        public Class<? extends Object> getTheClass() {
            return cls;
        }
        public ClassNotMetadescribedException(Class<? extends Object> cls) {
            this.cls = cls;
        }
    }
    /** Returns the Fame-class to which the given Java-class is connected in this metammodel.
     * 
     * @throws AssertionError if the Java-class is not connected to any Fame-class in this metammodel.
     * @see #with(Class)
     * @param jclass a causally connected Java-class.
     * @return a causally connected Fame-class.
     */
    public FM3Type getDescription(Class jclass) {
        FM3Type $ = lookupPrimitive(jclass);
        if ($ != null) return $;
        for (Class curr = jclass; curr != null; curr = curr.getSuperclass()) {
            $ = lookupClass(curr);
            if ($ != null) return $;
        }
        throw new AssertionError("Class not metadescribed: " + jclass);
        //throw new ClassNotMetadescribedException(jclass);
    }

    public PackageDescription initializePackageNamed(String name) {
        PackageDescription description = this.get(name);
        if (description == null) {
            description = new PackageDescription(name);
            bindings.put(name, description);
        }
        return description;
    }

    public boolean isSelfDescribed() {
        return getMetamodel() == this;
    }

    private FM3Type lookupClass(Class jclass) {
        if  (jclass.isInterface()){
            return traits.get(jclass);
        } else {
            return classes.get(jclass);
        }
    }

    private FM3Type lookupFull(Class jclass) {
        FM3Type $ = lookupPrimitive(jclass);
        if ($ == null) $ = lookupClass(jclass);
        return $;
    }

    private MetaDescription lookupPrimitive(Class jclass) {
        if (jclass.isPrimitive()) {
            return Boolean.TYPE == jclass 
                ? MetaDescription.BOOLEAN
                : MetaDescription.NUMBER;
        }
        if (Number.class.isAssignableFrom(jclass)) return MetaDescription.NUMBER;
        if (Boolean.class == jclass) return MetaDescription.BOOLEAN;
        if (String.class == jclass) return MetaDescription.STRING;
        if (char[].class == jclass) return MetaDescription.STRING;
        if (Object.class == jclass) return MetaDescription.OBJECT;
        return null;
    }

    public void setImmutable() {
        immutable = true;
    }

    /** Processes the annotations of the given Java-class, creates an according
     * Fame-class and establishes a Scenario-II connection between both. If such
     * a connection is already present, nothing is done. 
     * 
     * @see FameDescription
     * @see FamePackage
     * @see FameProperty
     * @throws AssertionError if the Java-class is not annotated.
     * @param jclass an annotated Java-class
     */
    public void with(Class jclass) {
        FM3Type $ = this.lookupFull(jclass);
        if ($ == null) {
            if(jclass.isInterface()) {
                TraitFactory factory = new TraitFactory(jclass, this);
                if (factory.isAnnotationPresent()) {
                    $ = factory.createInstance();
                    this.traits.put(jclass, (FM3Trait) $);
                    factory.initializeInstance();
                    this.bindings.put($.getFullname(), $);
                }
                if (!this.isSelfDescribed()) { // TODO explain? breaks meta-loop!
                    this.add($);
                }
            } else {
                MetaDescriptionFactory factory = new MetaDescriptionFactory(jclass, this);
                if (factory.isAnnotationPresent()) {
                    $ = factory.createInstance();
                    this.classes.put(jclass, (MetaDescription) $);
                    factory.initializeInstance();
                    this.bindings.put($.getFullname(), $);
                }
                if (!this.isSelfDescribed()) { // TODO explain? breaks meta-loop!
                    this.add($);
                }
            }
        }
        assert $ != null : jclass;
    }

    /** Processes all given Java-classes.
     * 
     * @see #with(Class)
     * @throws AssertionError if any o fthe Java-classes is not annotated.
     * @param jclasses some annotated Java-classes.
     */
    public void withAll(Class... jclasses) {
        for (Class jclass : jclasses) {
            this.with(jclass);
        }
    }

    public <T extends Element> T get(String fullname) {
        return (T) bindings.get(fullname);
    }

}
