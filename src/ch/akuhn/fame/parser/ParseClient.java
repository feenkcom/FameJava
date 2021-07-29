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

package ch.akuhn.fame.parser;

/**
 * Interface for reading MSE documents using callbacks. This interface allows an
 * application to register for MSE document parsing. The sequence of callbacks
 * is limited to the following protocol
 * <ul>
 * <li><code>MAIN := directive* beginDocument ELEM* endDocument</code>
 * <li><code>ELEM := beginElement serial? (beginAttribute ( primitive | reference | ELEM )* endAttribute) endElement</code>
 * </ul>
 * 
 * @see Parser
 * @author akuhn
 * 
 */
public interface ParseClient {

    public void beginAttribute(String name);

    public void beginDocument();

    public void beginElement(String name);

    public void directive(String name, String... params);

    public void endAttribute(String name);

    public void endDocument();

    public void endElement(String name);

    public void primitive(Object value);

    public void reference(int index);

    public void reference(String name);

    public void reference(String name, int index);

    public void serial(int index);

    default public void printEntitySeparator() {}

    default public void printPropertySeparator() {}

    default public void beginMultivalue(String name) {}

    default public void endMultivalue(String name) {}

}
