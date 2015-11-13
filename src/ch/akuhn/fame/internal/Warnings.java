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

package ch.akuhn.fame.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import ch.akuhn.fame.fm3.Element;

public class Warnings {

    private static class Warn {
        public String message;
        public Element element;

        public Warn(Element element, String message) {
            this.message = message;
            this.element = element;
        }

        @Override
        public String toString() {
            return message + ": " + element;
        }
    }

    private Collection<Warn> warnings = new ArrayList();

    public void add(String message, Element element) {
        warnings.add(new Warn(element, message));
    }

    public void printOn(Appendable stream) {
        try {
            for (Warn each : warnings) {
                stream.append(each.toString());
                stream.append('\n');
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
