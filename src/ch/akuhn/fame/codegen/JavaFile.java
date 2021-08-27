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

public class JavaFile extends GenFile {

    public JavaFile(String myPackage, String name) {
        super(myPackage, name);
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
        template.set("IMPLEMENTS", getTraits());
        template.set("FIELDS", getFieldsContentStream().toString());
        template.set("METHODS", getBodyContentStream().toString());
        stream.append(template.apply());
    }

}
