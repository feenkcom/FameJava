//  Copyright (c) 2007-2008 University of Bern, Switzerland
//  
//  Written by Adrian Kuhn <akuhn(a)iam.unibe.ch>
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

package ch.akuhn.fame.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.akuhn.fame.MetaRepository;
import ch.akuhn.fame.Repository;
import ch.akuhn.fame.Tower;
import ch.akuhn.fame.internal.MSEPrinter;
import ch.akuhn.fame.parser.InputSource;
import ch.akuhn.fame.parser.Parser;
import ch.akuhn.fame.parser.Scanner;

public class Famix30 {

    @Test
    public void asFM3() {
        InputSource input = InputSource.fromResource("ch/unibe/fame/resources/FAMIX30.fm3.mse");
        Appendable output = new StringBuilder();
        Parser p = new Parser(new Scanner(input));
        p.accept(new MSEPrinter(output));
    }

    @Test
    public void checkConstraints() {
        InputSource input = InputSource.fromResource("ch/unibe/fame/resources//FAMIX30.fm3.mse");
        Tower t = new Tower();
        t.getMetamodel().importMSE(input);
        t.getMetamodel().checkConstraints().printOn(System.out);
    }

    @Test
    public void testImporter() {
        InputSource input = InputSource.fromResource("ch/unibe/fame/resources//FAMIX30.fm3.mse");
        Tower t = new Tower();
        t.getMetamodel().importMSE(input);
        Repository m2 = t.getMetamodel();
        assertEquals(70, m2.getElements().size());
    }

    @Test
    public void testImporterSimple() {
        InputSource input = InputSource.fromString("((FM3.Package (id: 4) (name 'X') (classes "
                + "(FM3.Class (id: 1) (name 'A') (superclass (ref: 3)))"
                + "(FM3.Class (id: 2) (name 'B') (superclass (ref: 3)))"
                + "(FM3.Class (id: 3) (name 'C') (superclass (ref: Object))))))");
        Tower t = new Tower();
        t.getMetamodel().importMSE(input);
        MetaRepository m2 = t.getMetamodel();
        assertEquals(m2.descriptionNamed("X.C"), m2.descriptionNamed("X.A").getSuperclass());
    }
}
