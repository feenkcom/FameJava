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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.akuhn.fame.MetaRepository;

public class StringsTest {

    @Test
    public void testIsValidElementName() {
        assertTrue(MetaRepository.isValidElementName("a"));
        assertTrue(MetaRepository.isValidElementName("aaa"));
        assertTrue(MetaRepository.isValidElementName("aaa.bbb"));
        assertTrue(MetaRepository.isValidElementName("a123"));
        assertTrue(MetaRepository.isValidElementName("a123.b123"));
        assertTrue(!MetaRepository.isValidElementName(""));
        assertTrue(!MetaRepository.isValidElementName("."));
        assertTrue(!MetaRepository.isValidElementName(".aaa"));
        assertTrue(!MetaRepository.isValidElementName("aaa."));
        assertTrue(!MetaRepository.isValidElementName("123"));
        assertTrue(!MetaRepository.isValidElementName("a.123"));
    }

}
