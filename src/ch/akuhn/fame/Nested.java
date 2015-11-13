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

package ch.akuhn.fame;

/**
 * 
 * Indicates an nested element, used for name resolution.
 * 
 * @see Repository#fullname(Object)
 * @author Adrian Kuhn, 2008
 * 
 */
public interface Nested {

    /**
     * Returns the owner of an element, or <code>null</code>. If a class
     * implements both Owned and {@linkplain Named}, the instances returned by
     * this method must implement {@linkplain Named} as well.
     * 
     * @return may return <code>null</code>
     */
    public Object getOwner();

}
