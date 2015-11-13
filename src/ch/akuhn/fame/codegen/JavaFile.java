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

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

public class JavaFile {

    private StringBuilder body;
    private Collection<String> imports;
    private String myPackage;
    private String name;
    private String superName;
    private String modelPackagename;
    private String modelClassname;

    public JavaFile(String myPackage, String name) {
        this.myPackage = myPackage;
        this.name = name;
        this.body = new StringBuilder();
        this.imports = new HashSet();
    }

    public <T> void addImport(Class<T> tee) {
        this.addImport(tee.getPackage().getName(), tee.getSimpleName());
    }

    public void addImport(String aPackage, String className) {
        if (aPackage.equals(myPackage)) return;
        if (aPackage.equals("java.lang")) return;
        imports.add(aPackage + "." + className);
    }

    public void addSuperclass(String aPackage, String className) {
        if (className.equals("Object") && aPackage.equals("java.lang")) return;
        this.addImport(aPackage, className);
        this.superName = className;
    }

    public void generateCode(Appendable stream) throws IOException {
        Template template = Template.get("Class");
        template.set("PACKAGE", myPackage);
        template.set("AUTOGENCODE", "Automagically generated code");
        template.set("THISTYPE", name);
        template.set("THISPACKAGE", modelPackagename);
        template.set("THISNAME", modelClassname);
        template
                .set("EXTENDS", superName == null ? "" : "extends " + superName);
        template.set("IMPORTS", getImports());
        template.set("FIELDS", "");
        template.set("METHODS", getContentStream().toString());
        stream.append(template.apply());
    }

    public StringBuilder getContentStream() {
        return body;
    }

    public String getImports() {
        StringBuilder stream = new StringBuilder();
        for (String each : imports) {
            stream.append("import ").append(each).append(";\n");
        }
        return stream.toString();
    }

    public String getModelClassname() {
        return modelClassname;
    }

    public String getModelPackagename() {
        return modelPackagename;
    }

    public void setModelClassname(String modelClassname) {
        this.modelClassname = modelClassname;
    }

    public void setModelPackagename(String modelPackagename) {
        this.modelPackagename = modelPackagename;
    }

}
