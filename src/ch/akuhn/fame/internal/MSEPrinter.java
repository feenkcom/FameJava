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
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.akuhn.util.Strings;

/**
 * Writes MSE document on output-stream.
 * 
 * 
 * @author akuhn
 * 
 */
public class MSEPrinter extends AbstractPrintClient {

    private static final Format dateFormat = new SimpleDateFormat(
            "yyyy-MM-DD,hh:mm:ss");

    public static final Object UNLIMITED = new Object();

    public MSEPrinter(Appendable stream) {
        super(stream);
    }

    @Override
    public void beginAttribute(String name) {
        indentation++;
        lntabs();
        append('(');
        append(name);
    }

    @Override
    public void beginDocument() {
        // indentation++;
        append('(');
    }

    @Override
    public void beginElement(String name) {
        indentation++;
        lntabs();
        append('(');
        append(name);
    }

    @Override
    public void endAttribute(String name) {
        append(')');
        indentation--;
    }

    @Override
    public void endDocument() {
        append(')');
        close();
    }

    @Override
    public void endElement(String name) {
        append(')');
        indentation--;
    }

    @Override
    public void primitive(Object value) {
        append(' ');
        if (value == UNLIMITED) {
            append('*');
        } else if (value instanceof String) {
            String string = (String) value;
            append('\'');
            for (char ch: Strings.chars(string)) {
                if (ch == '\'') append('\'');
                append(ch);
            }
            append('\'');
        } else if (value instanceof Boolean || value instanceof Number) {
            append(value.toString());
        } else if (value instanceof Date) {
            append(dateFormat.format(value));
        } else if (value instanceof Character) {
            append("'" + value + "'");
        } else if (value instanceof char[]) {
            primitive(new String((char[])value));
        } else {
            assert false : "Unknown primitive: " + value + " of type: " + value.getClass().getCanonicalName();
        }
    }

    @Override
    public void reference(int index) {
        try {
            stream.append(" (ref: "); // must prepend space!
            stream.append(Integer.toString(index));
            stream.append(')');
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void reference(String name) {
        try {
            stream.append(" (ref: "); // must prepend space!
            stream.append(name);
            stream.append(')');
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void serial(int index) {
        try {
            stream.append(" (id: "); // must prepend space!
            stream.append(Integer.toString(index));
            stream.append(')');
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
