//  Copyright (c) 2007-2008 University of Bern, Switzerland
//  
//  Written by Adrian Kuhn <akuhn(a)iam.unibe.ch>
//  
//  This file is part of 'Fame Code Generation (for Java)'.
//  
//  'Fame Code Generation (for Java)' is free software: you can redistribute it
//  and/or modify it under the terms of the GNU General Public License as
//  published by the Free Software Foundation, either version 3 of the License,
//  or (at your option) any later version.
//  
//  'Fame Code Generation (for Java)' is distributed in the hope that it will
//  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//  Public License for more details.
//  
//  You should have received a copy of the GNU General Public License along
//  with 'Fame Code Generation (for Java)'. If not, see
//  <http://www.gnu.org/licenses/>.
//  

package ch.akuhn.fame.codegen.target;

import static ch.akuhn.util.Out.puts;
import ch.akuhn.fame.MetaRepository;
import ch.akuhn.fame.Tower;
import ch.akuhn.fame.codegen.CodeGeneration;
import ch.akuhn.fame.parser.InputSource;

public class EMOFCodegen {

    public static void main(String... args) {
        InputSource input = InputSource.fromResource("ch/unibe/mse/resources/EMOF.fm3.mse");
        Tower t = new Tower();
        t.getMetamodel().importMSE(input);
        MetaRepository m2 = t.getMetamodel();
        m2.checkConstraints().printOn(System.out);
        CodeGeneration gen = new CodeGeneration("ch.unibe.mse", "gen", "EMOF");
        gen.accept(m2);
        puts("done");
    }

}
