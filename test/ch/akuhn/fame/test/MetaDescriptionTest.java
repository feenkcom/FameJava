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
import static org.junit.Assert.assertSame;

import org.junit.Test;

import ch.akuhn.fame.MetaRepository;
import ch.akuhn.fame.Tower;
import ch.akuhn.fame.fm3.MetaDescription;

public class MetaDescriptionTest {

    @Test
    public void testAllAttributes() {
        String str = "((FM3.Package (name 'World')"
                + "(classes "
                + "     (FM3.Class (id: 1) (name 'Super') (attributes (FM3.Property (name 'foo')))) "
                + "     (FM3.Class (name 'Sub') (superclass (ref: 1)) (attributes (FM3.Property (name 'foo'))))"
                + ")))";
        Tower t = new Tower();
        t.getMetamodel().importMSE(str);
        MetaRepository repo = t.getMetamodel();
        MetaDescription sub = repo.descriptionNamed("World.Sub");
        assertEquals(1, sub.allProperties().size());
        assertSame(sub.getProperties().iterator().next(), sub.allProperties()
                .iterator().next());
    }

}
