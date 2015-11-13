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
 * Tuple with type and value.
 * 
 * @see Scanner
 * @see Parser
 * @author akuhn
 * 
 */
public class Token {

    public final TokenType type;

    public final Object value;

    public Token(boolean bool) {
        this.type = TokenType.BOOLEAN;
        this.value = bool;
    }

    public Token(double number) {
        this.type = TokenType.NUMBER;
        this.value = number;
    }

    public Token(int number) {
        this.type = TokenType.NUMBER;
        this.value = number;
    }

    public Token(TokenType type, boolean bool) {
        this.type = type;
        this.value = bool;
    }

    public Token(TokenType type, String string) {
        // TODO which is the better strategy?
        // http://www.codeinstructions.com/2008/09/instance-pools-with-weakhashmap.html
        // http://kohlerm.blogspot.com/2009/01/is-javalangstringintern-really-evil.html
        // http://www.codeinstructions.com/2009/01/busting-javalangstringintern-myths.html
        this.type = type;
        this.value = string == null ? null : string.intern();
    }

    public boolean booleanValue() {
        assert type == TokenType.BOOLEAN;
        return (Boolean) value;
    }

    public double doubleValue() {
        assert type == TokenType.NUMBER;
        return ((Number) value).doubleValue();
    }

    public int intValue() {
        assert type == TokenType.NUMBER;
        return ((Number) value).intValue();
    }

    public String stringValue() {
        return value.toString();
    }

    @Override
    public String toString() {
        return type + " " + value;
    }

}
