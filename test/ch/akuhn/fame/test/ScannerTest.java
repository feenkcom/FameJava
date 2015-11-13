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

import static ch.akuhn.fame.parser.TokenType.BOOLEAN;
import static ch.akuhn.fame.parser.TokenType.CLOSE;
import static ch.akuhn.fame.parser.TokenType.EOF;
import static ch.akuhn.fame.parser.TokenType.NAME;
import static ch.akuhn.fame.parser.TokenType.OPEN;
import static ch.akuhn.fame.parser.TokenType.STRING;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.akuhn.fame.parser.ParseError;
import ch.akuhn.fame.parser.Scanner;
import ch.akuhn.fame.parser.Token;

public class ScannerTest {

    private Scanner scan;
    private Token token;

    @Test(expected = ParseError.class)
    public void testDotNameDot() {
        scan = new Scanner("Dot.Name.");
        token = scan.nextOrEOF();
    }

    @Test
    public void testDottedName() {
        scan = new Scanner("Dot.Dot.Name");
        token = scan.nextOrEOF();
        assertEquals(NAME, token.type);
        assertEquals("Dot.Dot.Name", token.value);
        assertEquals(EOF, scan.nextOrEOF().type);
    }

    @Test
    public void testFlase() {
        scan = new Scanner("false");
        token = scan.nextOrEOF();
        assertEquals(BOOLEAN, token.type);
        assertEquals(false, token.value);
        assertEquals(EOF, scan.nextOrEOF().type);
    }

    @Test(expected = ParseError.class)
    public void testIllegalDateMissingCentury() {
        scan = new Scanner("77-12-11");
        token = scan.nextOrEOF();
    }

    @Test(expected = ParseError.class)
    public void testIllegalDateMissingLeadingZero() {
        scan = new Scanner("1980-3-1");
        token = scan.nextOrEOF();
    }

    @Test(expected = ParseError.class)
    public void testIllegalDateTrailingHypen() {
        scan = new Scanner("-2000-00-00");
        token = scan.nextOrEOF();
    }

    @Test(expected = ParseError.class)
    public void testIllegalTimeTrailingHypen() {
        scan = new Scanner("-18:00:00");
        token = scan.nextOrEOF();
    }

    @Test
    public void testLeadingWhitespace() {
        scan = new Scanner("   (");
        assertEquals(OPEN, scan.nextOrEOF().type);
        assertEquals(EOF, scan.nextOrEOF().type);
        scan = new Scanner(" (");
        assertEquals(OPEN, scan.nextOrEOF().type);
        assertEquals(EOF, scan.nextOrEOF().type);
    }

    @Test
    public void testName() {
        scan = new Scanner("Name");
        token = scan.nextOrEOF();
        assertEquals(NAME, token.type);
        assertEquals("Name", token.value);
        assertEquals(EOF, scan.nextOrEOF().type);
    }

    @Test(expected = ParseError.class)
    public void testNameDot() {
        scan = new Scanner("Name.");
        token = scan.nextOrEOF();
    }

    @Test(expected = ParseError.class)
    public void testNameDotColon() {
        scan = new Scanner("Dot.Name:");
        token = scan.nextOrEOF();
    }

    @Test
    public void testParens() {
        scan = new Scanner("()");
        assertEquals(OPEN, scan.nextOrEOF().type);
        assertEquals(CLOSE, scan.nextOrEOF().type);
        assertEquals(EOF, scan.nextOrEOF().type);
    }

    @Test(expected = ParseError.class)
    public void testRunawayString() {
        scan = new Scanner("('runaway");
        assertEquals(OPEN, scan.nextOrEOF().type);
        scan.nextOrEOF();
    }

    @Test(expected = ParseError.class)
    public void testRunawayString2() {
        scan = new Scanner("('runaway''");
        assertEquals(OPEN, scan.nextOrEOF().type);
        scan.nextOrEOF();
    }

    @Test(expected = ParseError.class)
    public void testSelector() {
        scan = new Scanner("idref:");
        token = scan.nextOrEOF();
    }

    @Test
    public void testString() {
        scan = new Scanner("'Abc def!'");
        token = scan.nextOrEOF();
        assertEquals(STRING, token.type);
        assertEquals("Abc def!", token.value);
        assertEquals(EOF, scan.nextOrEOF().type);
    }

    @Test
    public void testString2() {
        scan = new Scanner("('Abc def!')");
        assertEquals(OPEN, scan.nextOrEOF().type);
        token = scan.nextOrEOF();
        assertEquals(STRING, token.type);
        assertEquals("Abc def!", token.value);
        assertEquals(CLOSE, scan.nextOrEOF().type);
        assertEquals(EOF, scan.nextOrEOF().type);
    }

    @Test
    public void testStringWithQuote() {
        scan = new Scanner("('don''t')");
        assertEquals(OPEN, scan.nextOrEOF().type);
        token = scan.nextOrEOF();
        assertEquals(STRING, token.type);
        assertEquals("don't", token.value);
        assertEquals(CLOSE, scan.nextOrEOF().type);
        assertEquals(EOF, scan.nextOrEOF().type);
    }

    @Test
    public void testTrailingWhitespace() {
        scan = new Scanner("(   ");
        assertEquals(OPEN, scan.nextOrEOF().type);
        assertEquals(EOF, scan.nextOrEOF().type);
        scan = new Scanner("( ");
        assertEquals(OPEN, scan.nextOrEOF().type);
        assertEquals(EOF, scan.nextOrEOF().type);
    }

    @Test
    public void testTrue() {
        scan = new Scanner("true");
        token = scan.nextOrEOF();
        assertEquals(BOOLEAN, token.type);
        assertEquals(true, token.value);
        assertEquals(EOF, scan.nextOrEOF().type);
    }

    @Test
    public void testWhitespace() {
        scan = new Scanner("     ");
        assertEquals(EOF, scan.nextOrEOF().type);
        scan = new Scanner(" ");
        assertEquals(EOF, scan.nextOrEOF().type);
    }

}
