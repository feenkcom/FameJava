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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the FM3.Package of a fame-described element. If this annotation is
 * not present, the declaring class and any enclosing classes are queried for
 * FamePackage annotations. If none is found, the simple name of the enclosing
 * java package is used. Thus, the FM3.Package name of a fame-described element
 * is resolved in the following order:
 * <ol>
 * <li>FamePackage annotation of the element,
 * <li>FamePackage annotation of declaring class (unless element is a class),
 * <li>FamePackage annotation of any enclosing class,
 * <li>FamePackage annotation of containing java package,
 * <li>or else, last part of java package name.
 * </ol>
 * FM3 package names start with a letter, and may contain letters and numbers.
 * It is recommended to use uppercase letters only.
 * 
 * @author Adrian Kuhn, 2008
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE, ElementType.PACKAGE, ElementType.METHOD, ElementType.FIELD })
public @interface FamePackage {

    String value();

}
