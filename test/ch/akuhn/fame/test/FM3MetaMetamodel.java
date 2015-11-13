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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.akuhn.fame.MetaRepository;
import ch.akuhn.fame.Repository;
import ch.akuhn.fame.fm3.MetaDescription;
import ch.akuhn.fame.fm3.PropertyDescription;
import ch.akuhn.fame.internal.MSEPrinter;
import ch.akuhn.fame.parser.Importer;
import ch.akuhn.fame.parser.InputSource;

/**
 * Tests covering setup and use of FM3 meta-metamodel.
 * 
 * @author akuhn
 * 
 */
public class FM3MetaMetamodel {

    @Test
    public void has4Classes() {
        MetaRepository m3 = MetaRepository.createFM3();
        assertEquals(4, m3.allClassDescriptions().size());
    }

    @Test
    public void has1Package() {
        MetaRepository m3 = MetaRepository.createFM3();
        assertEquals(1, m3.allPackageDescriptions().size());
    }
    
    @Test
    public void has20Properties() {
        MetaRepository m3 = MetaRepository.createFM3();
        assertEquals(20, m3.allPropertyDescriptions().size());
    }
    
    @Test
    public void allClassDescriptionsAreElement() {
        MetaRepository m3 = MetaRepository.createFM3();
        assertTrue(m3.getElements().containsAll(m3.allClassDescriptions()));
    }

    @Test
    public void checkConstraints() {
        MetaRepository m3 = MetaRepository.createFM3();
        m3.checkConstraints();
    }

    public void packageClassesIsMultivalued() {
        MetaRepository m3 = MetaRepository.createFM3();
        MetaDescription m = m3.descriptionNamed("MSE.Package");
        PropertyDescription p = m.attributeNamed("classes");
        assertEquals(true, p.isMultivalued());
    }

    @Test
    public void readFile() {
        MetaRepository m3 = MetaRepository.createFM3();
        StringBuilder buffer = new StringBuilder();
        m3.accept(new MSEPrinter(buffer));
        Importer builder = new Importer(m3);
        builder.readFrom(InputSource.fromString(buffer));
        Repository m2 = builder.getResult();
        m2.getElements();
    }

    @Test
    public void selfDescribed() {
        MetaRepository m3 = MetaRepository.createFM3();
        assertEquals(m3, m3.getMetamodel());
    }

    @Test
    public void testDescriptionNamed() {
        MetaRepository m3 = MetaRepository.createFM3();
        assertNotNull(m3.descriptionNamed("FM3.Class"));
        assertNotNull(m3.descriptionNamed("FM3.Package"));
        assertNotNull(m3.descriptionNamed("FM3.Property"));
    }

    @Test
    public void writeFile() {
        MetaRepository m3 = MetaRepository.createFM3();
        Appendable stream = new StringBuilder();
        m3.accept(new MSEPrinter(stream));
        String mse = stream.toString();
        assertTrue(mse.length() != 0);
        // out(mse);
    }
}
