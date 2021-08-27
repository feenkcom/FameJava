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

package ch.akuhn.fame.codegen;

import static ch.akuhn.util.Strings.toUpperFirstChar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import ch.akuhn.fame.FameDescription;
import ch.akuhn.fame.FamePackage;
import ch.akuhn.fame.FameProperty;
import ch.akuhn.fame.MetaRepository;
import ch.akuhn.fame.Repository;
import ch.akuhn.fame.fm3.*;
import ch.akuhn.fame.internal.MultivalueSet;

public class CodeGeneration {

    public static final Collection<String> KEYWORDS = Arrays.asList("abstract", "continue", "for", "new", "switch", "assert",
            "default", "goto", "package", "synchronized", "boolean", "do", "if", "private", "this", "break", "double",
            "implements", "protected", "throw", "byte", "else", "import", "public", "throws", "case", "enum", "instanceof",
            "return", "transient", "catch", "extends", "int", "short", "try", "char", "final", "interface", "static", "void",
            "class", "finally", "long", "strictfp", "volatile", "const", "float", "native", "super", "while");

    public static String asJavaSafeName(String name) {
        if (KEYWORDS.contains(name))
            return "my" + toUpperFirstChar(name);
        return name;
    }

    private String destinationPackage = "com.example";
    private String outputDirectory = "gen";
    private String classNamePrefix = "";
    private GenFile code;
    private File folder;

    public CodeGeneration() {
    }

    public CodeGeneration(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public CodeGeneration(String destinationPackage, String outputDirectory, String classNamePrefix) {
        this.classNamePrefix = classNamePrefix;
        this.destinationPackage = destinationPackage;
        this.outputDirectory = outputDirectory;
    }

    public void accept(MetaRepository metamodel) {
        try {
            for (PackageDescription each : metamodel.allPackageDescriptions()) {
                this.acceptPackage(each);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void accept(Repository repo) {
        try {
            for (PackageDescription each : repo.all(PackageDescription.class)) {
                this.acceptPackage(each);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    // Properties for classes

    private Void acceptAccessorProperty(PropertyDescription m, MetaDescription owner) {
        code.addImport(FameProperty.class);
        String typeName = "Object";
        if (m.getType() != null) { // TODO should not have null type
            typeName = className(m.getType());
            code.addImport(this.packageName(m.getType().getPackage()), typeName);
        }
        if (m.isMultivalued()) {
            code.addImport("java.util", "*");
        }
        String myName = CodeGeneration.asJavaSafeName(m.getName());

        String base = m.isMultivalued() ? "Many" : "One";
        Template field = Template.get(base + ".Field");
        if (m.getOpposite() != null) {
            base = base + (m.getOpposite().isMultivalued() ? "Many" : "One");
            if (base.equals("ManyOne") || base.equals("ManyMany")) {
                code.addImport(MultivalueSet.class);
            }
        }
        Template getter = Template.get(base + ".Getter");
        Template setter = Template.get(base + ".Setter");

        field.set("TYPE", typeName);
        field.set("THISTYPE", CodeGeneration.asJavaSafeName(className(owner)));
        field.set("FIELD", myName);
        field.set("NAME", m.getName());
        field.set("GETTER", "get" + Character.toUpperCase(myName.charAt(0)) + myName.substring(1));
        field.set("SETTER", "set" + Character.toUpperCase(myName.charAt(0)) + myName.substring(1));
        if (m.getOpposite() != null) {
            String oppositeName = m.getOpposite().getName();
            field.set("OPPOSITENAME", oppositeName);
            oppositeName = CodeGeneration.asJavaSafeName(oppositeName);
            field.set("OPPOSITESETTER", "set" + Character.toUpperCase(oppositeName.charAt(0)) + oppositeName.substring(1));
            field.set("OPPOSITEGETTER", "get" + Character.toUpperCase(oppositeName.charAt(0)) + oppositeName.substring(1));
        }
        getter.setAll(field);
        setter.setAll(field);

        String props = "";
        if (m.isDerived()) {
            props += ", derived = true";
        }
        if (m.isContainer()) {
            props += ", container = true";
        }
        getter.set("PROPS", props);

        StringBuilder fieldsStream = code.getFieldsContentStream();
        StringBuilder bodyStream = code.getBodyContentStream();
        fieldsStream.append(field.apply());
        bodyStream.append(getter.apply());
        bodyStream.append(setter.apply());

        // adder for multivalued properties

        if (!m.isMultivalued())
            return null;

        Template adder = Template.get("Many.Sugar");
        adder.set("TYPE", typeName);
        adder.set("FIELD", myName);
        adder.set("GETTER", "get" + Character.toUpperCase(myName.charAt(0)) + myName.substring(1));
        adder.set("ADDER", "add" + Character.toUpperCase(myName.charAt(0)) + myName.substring(1));
        adder.set("NUMOF", "numberOf" + Character.toUpperCase(myName.charAt(0)) + myName.substring(1));
        adder.set("HAS", "has" + Character.toUpperCase(myName.charAt(0)) + myName.substring(1));
        bodyStream.append(adder.apply());
        return null;

    }

    private Void acceptDerivedProperty(PropertyDescription m, MetaDescription owner) {
        assert m.isDerived() && !m.hasOpposite();
        code.addImport(FameProperty.class);
        String typeName = "Object";
        if (m.getType() != null) { // TODO should not have null type
            typeName = className(m.getType());
            code.addImport(this.packageName(m.getType().getPackage()), typeName);
        }
        if (m.isMultivalued()) {
            code.addImport("java.util", "*");
        }
        String myName = CodeGeneration.asJavaSafeName(m.getName());

        String base = m.isMultivalued() ? "Many" : "One";
        Template getter = Template.get(base + ".Derived.Getter");

        getter.set("TYPE", typeName);
        getter.set("NAME", m.getName());
        getter.set("GETTER", "get" + Character.toUpperCase(myName.charAt(0)) + myName.substring(1));

        String props = "";
        if (m.isDerived()) {
            props += ", derived = true";
        }
        if (m.isContainer()) {
            props += ", container = true";
        }
        getter.set("PROPS", props);

        StringBuilder stream = code.getBodyContentStream();
        stream.append(getter.apply());
        return null;
    }

    // Properties for Traits

    private Void acceptDerivedPropertyTrait(PropertyDescription m) {
        assert m.isDerived() && !m.hasOpposite();
        code.addImport(FameProperty.class);
        String typeName = "Object";
        if (m.getType() != null) { // TODO should not have null type
            typeName = className(m.getType());
            code.addImport(this.packageName(m.getType().getPackage()), typeName);
        }
        if (m.isMultivalued()) {
            code.addImport("java.util", "*");
        }
        String myName = CodeGeneration.asJavaSafeName(m.getName());

        String base = "Trait." + (m.isMultivalued() ? "Many" : "One");
        Template getter = Template.get(base + ".Derived.Getter");

        getter.set("TYPE", typeName);
        getter.set("NAME", m.getName());
        getter.set("GETTER", "get" + Character.toUpperCase(myName.charAt(0)) + myName.substring(1));

        String props = "";
        if (m.isDerived()) {
            props += ", derived = true";
        }
        if (m.isContainer()) {
            props += ", container = true";
        }
        getter.set("PROPS", props);

        StringBuilder stream = code.getBodyContentStream();
        stream.append(getter.apply());
        return null;
    }

    private Void acceptAccessorPropertyTrait(PropertyDescription m) {
        code.addImport(FameProperty.class);
        String typeName = "Object";
        if (m.getType() != null) { // TODO should not have null type
            typeName = className(m.getType());
            code.addImport(this.packageName(m.getType().getPackage()), typeName);
        }
        if (m.isMultivalued()) {
            code.addImport("java.util", "*");
        }
        String myName = CodeGeneration.asJavaSafeName(m.getName());

        String base = "Trait." + (m.isMultivalued() ? "Many" : "One");
        Template field = Template.get(base + ".Field");
        if (m.getOpposite() != null) {
            base = base + (m.getOpposite().isMultivalued() ? "Many" : "One");
            if (base.equals("ManyOne") || base.equals("ManyMany")) {
                code.addImport(MultivalueSet.class);
            }
        }
        Template getter = Template.get(base + ".Getter");
        Template setter = Template.get(base + ".Setter");

        field.set("TYPE", typeName);
        field.set("THISTYPE", CodeGeneration.asJavaSafeName(className(m.getOwningMetaDescription())));
        field.set("FIELD", myName);
        field.set("NAME", m.getName());
        field.set("GETTER", "get" + Character.toUpperCase(myName.charAt(0)) + myName.substring(1));
        field.set("SETTER", "set" + Character.toUpperCase(myName.charAt(0)) + myName.substring(1));
        if (m.getOpposite() != null) {
            String oppositeName = m.getOpposite().getName();
            field.set("OPPOSITENAME", oppositeName);
            oppositeName = CodeGeneration.asJavaSafeName(oppositeName);
            field.set("OPPOSITESETTER", "set" + Character.toUpperCase(oppositeName.charAt(0)) + oppositeName.substring(1));
            field.set("OPPOSITEGETTER", "get" + Character.toUpperCase(oppositeName.charAt(0)) + oppositeName.substring(1));
        }
        getter.setAll(field);
        setter.setAll(field);

        String props = "";
        if (m.isDerived()) {
            props += ", derived = true";
        }
        if (m.isContainer()) {
            props += ", container = true";
        }
        getter.set("PROPS", props);

        StringBuilder fieldsStream = code.getFieldsContentStream();
        StringBuilder bodyStream = code.getBodyContentStream();
        fieldsStream.append(field.apply());
        bodyStream.append(getter.apply());
        bodyStream.append(setter.apply());

        // adder for multivalued properties

        if (!m.isMultivalued())
            return null;

        Template adder = Template.get("Trait.Many.Sugar");
        adder.set("TYPE", typeName);
        adder.set("FIELD", myName);
        adder.set("GETTER", "get" + Character.toUpperCase(myName.charAt(0)) + myName.substring(1));
        adder.set("ADDER", "add" + Character.toUpperCase(myName.charAt(0)) + myName.substring(1));
        adder.set("NUMOF", "numberOf" + Character.toUpperCase(myName.charAt(0)) + myName.substring(1));
        adder.set("HAS", "has" + Character.toUpperCase(myName.charAt(0)) + myName.substring(1));
        bodyStream.append(adder.apply());
        return null;

    }
    
    private void acceptClass(MetaDescription metaDescription) throws IOException {
        if (metaDescription.isPrimitive())
            return;
        code = new JavaFile(this.packageName(metaDescription.getPackage()), className(metaDescription));
        code.setModelPackagename(metaDescription.getPackage().getFullname());
        code.setModelClassname(metaDescription.getName());
        Map<String, List<FM3Trait>> groupedTraits = metaDescription.getTraits().stream().collect(Collectors.groupingBy(FM3Type::getName));

        groupedTraits.forEach((name, fm3Traits) -> {

            if (fm3Traits.size()>1) {
                // There are multiple traits with the same name
                for (FM3Trait trait : fm3Traits)
                    code.addTrait(packageName(trait.getPackage()) + "." + trait.getName());

            } else {
                // There is only one trait with that name
                for (FM3Trait trait : fm3Traits)
                    code.addTrait(trait.getName());
            }
        });

        code.addImport(FameDescription.class);
        code.addImport(FamePackage.class);
        for (FM3Trait t : metaDescription.computeAllTraits()) {
            code.addImport(packageName(t.getPackage()), t.getName());
        }

        if (metaDescription.getSuperclass() != null) {
            code.addSuperclass(this.packageName(metaDescription.getSuperclass().getPackage()), className(metaDescription.getSuperclass()));
        }
        // My own properties
        List<PropertyDescription> propertyDescriptions = metaDescription.getProperties().stream().collect(Collectors.toList());
        propertyDescriptions.sort(Comparator.comparing(Element::getName));
        for (PropertyDescription property : propertyDescriptions) {
            this.acceptProperty(property, metaDescription);
        }
        // Properties from my traits that are not in my own properties
        Set<PropertyDescription> propertyDescriptionSet = new HashSet<>();
        metaDescription.computeAllTraits().stream()
                .map(c -> c.getProperties().stream()
                        .filter(traitProperty -> propertyDescriptions.stream().noneMatch(myProperty -> myProperty.getName().equals(traitProperty.getName())))
                        .collect(Collectors.toList()))
                .forEach(propertyDescriptionSet::addAll);
        propertyDescriptions.clear();
        propertyDescriptions.addAll(propertyDescriptionSet);
        propertyDescriptions.sort(Comparator.comparing(Element::getName));
        for (PropertyDescription propertyDescription : propertyDescriptions) {
            this.acceptProperty(propertyDescription, metaDescription);
        }

        File file = new File(folder, className(metaDescription) + ".java");
        FileWriter stream = new FileWriter(file);
        code.generateCode(stream);
        stream.close();
    }

    private void acceptTrait(FM3Trait m) throws IOException {
        code = new InterfaceFile(this.packageName(m.getPackage()), className(m));
        code.setModelPackagename(m.getPackage().getFullname());
        code.setModelClassname(m.getName());
        code.setTraits(m.getTraits().stream().map(FM3Type::getName).collect(Collectors.toList()));
        code.addImport(FameDescription.class);
        code.addImport(FamePackage.class);
        for (FM3Trait t : m.computeAllTraits()) {
            code.addImport(packageName(t.getPackage()), t.getName());
        }

        for (PropertyDescription property : m.getProperties()) {
            this.acceptPropertyTrait(property);
        }

        File file = new File(folder, className(m) + ".java");
        FileWriter stream = new FileWriter(file);
        code.generateCode(stream);
        stream.close();
    }

    private void acceptPackage(PackageDescription m) throws IOException {
        folder = new File(new File(outputDirectory()), this.packageName(m).replace('.', '/'));
        if (!folder.exists())
            folder.mkdirs();

        for (FM3Type meta : m.getClasses()) {
            if (meta instanceof MetaDescription) {
                this.acceptClass((MetaDescription)meta);
            } else if (meta instanceof FM3Trait) {
                this.acceptTrait((FM3Trait)meta);
            }
        }

        String name = toUpperFirstChar(m.getName().replaceAll("-", "") + "Model");
        Template template = Template.get("Package");
        String packageName = this.packageName(m);
        template.set("PACKAGE", packageName);
        template.set("MODEL", name);
        template.set("AUTOGENCODE", "Automagically generated code");

        StringBuilder builder = new StringBuilder();
        for (FM3Type meta : m.getClasses()) {
            if (!meta.getName().equals(name)) {
                builder.append("\t\tmetamodel.with(");
                builder.append(packageName);
                builder.append('.');
                builder.append(className(meta));
                builder.append(".class);\n");
            }
        }
        template.set("ADDCLASSES", builder.toString());

        File file = new File(folder, name + ".java");
        FileWriter stream = new FileWriter(file);
        stream.append(template.apply());
        stream.close();

        folder = null;
    }

    private Void acceptProperty(PropertyDescription m, MetaDescription owner) throws IOException {
        if (m.isDerived() && !m.hasOpposite()) return acceptDerivedProperty(m, owner);
        return acceptAccessorProperty(m, owner);
    }

    private Void acceptPropertyTrait(PropertyDescription m) throws IOException {
        if (m.isDerived() && !m.hasOpposite()) return acceptDerivedPropertyTrait(m);
        return acceptAccessorPropertyTrait(m);
    }

    private String className(FM3Type meta) {
        if (meta.isPrimitive() || meta.isRoot())
            return meta.getName();
        return mapClassName(meta.getName());
    }

    public String destinationPackage() {
        return destinationPackage;
    }

    public String mapClassName(String name) {
        return classNamePrefix + name;
    }

    public String mapPackageName(String name) {
        return name.toLowerCase().replaceAll("-","");
    }

    public String outputDirectory() {
        return outputDirectory;
    }

    private String packageName(PackageDescription m) {
        if (m == null)
            return "java.lang";
        return destinationPackage() + "." + mapPackageName(m.getName());
    }

}
