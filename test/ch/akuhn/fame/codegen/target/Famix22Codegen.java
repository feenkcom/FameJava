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

import java.io.IOException;

import ch.akuhn.fame.MetaRepository;
import ch.akuhn.fame.Repository;
import ch.akuhn.fame.codegen.CodeGeneration;
import ch.akuhn.fame.parser.Importer;
import ch.akuhn.fame.parser.InputSource;

public class Famix22Codegen {

    public static void main(String... args) throws IOException {
        InputSource input = InputSource.fromResource("ch/unibe/mse/resources/FAMIX22.fm3.mse");
        MetaRepository m3 = MetaRepository.createFM3();
        Importer builder = new Importer(m3);
        builder.readFrom(input);
        Repository m2 = builder.getResult();

        // File file = new File("FAMIX22.fm3.mse");
        // ParseClient printer = new MSEPrinter(new OutputStreamWriter(
        // new FileOutputStream(file), "UTF-8"));
        // m2.accept(printer);

        CodeGeneration gen = new CodeGeneration("ch.unibe", "gen", "");
        gen.accept(m2);
        // licenseGeneratedSources();
        puts("done");
    }

}
