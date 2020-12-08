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

import static java.lang.Character.isDigit;
import static java.lang.Character.isLetter;
import static java.lang.Character.isLetterOrDigit;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

import java.util.Iterator;

/**
 * Breaks input-stream into tokens. Accepts the following grammar
 * <p>
 * Tokens
 * 
 * <pre>
 * OPEN       ::= &quot;(&quot;
 * CLOSE      ::= &quot;)&quot;
 * ID         ::= &quot;id:&quot;
 * REF        ::= &quot;ref:&quot;
 * KEYWORD    ::= &quot;@&quot; NamePart
 * NAME       ::= NamePart ( &quot;.&quot; NamePart )*
 * STRING     ::= ( &quot;'&quot; [&circ;']* &quot;'&quot; )+
 * BOOLEAN    ::= &quot;true&quot; | &quot;false&quot;
 * UNDEFINED  ::= &quot;nil&quot;
 * NUMBER     ::= &quot;-&quot;? Digit+ ( &quot;.&quot; Digit+ )? ( &quot;e&quot;  ( &quot;-&quot;? Digit+ ))?
 * </pre>
 * 
 * <p>
 * Terminals and Intermediates
 * 
 * <pre>
 * NamePart   ::= Letter ( Letter | Digit )*
 * Digit      ::= [0-9]
 * Letter     ::= [a-zA-Z_]
 * </pre>
 * 
 * Whitespace
 * 
 * <pre>
 * Whitespace ::= \s+
 * Comment    ::= &quot;\&quot;&quot; [&circ;&quot;]* &quot;\&quot;&quot;
 * </pre>
 * 
 * In order to provide more user friendly error messages, this implementation
 * imposed the following constraint upon the sequence of tokens: <tt>Name</tt>
 * must be followed by whitespace; <tt>String</tt>, <tt>Undefined</tt>,
 * <tt>Boolean</tt>, and <tt>Number</tt> must be followed by <tt>Open</tt>,
 * <tt>Close</tt> or whitespace.
 * 
 * <p>
 * If the input source is in Smalltalk compatibility mode, the grammar is
 * extended as follows
 * 
 * <pre>
 * Unlimited ::= &quot;*&quot; 
 * Keyword ::= ... | Letter+ &quot;:&quot;
 * </pre>
 * 
 */
public class Scanner implements Iterator<Token>, Iterable<Token> {

    private static final Token CLOSE = new Token(TokenType.CLOSE, ")");
    private static final Token EOF = new Token(TokenType.EOF, null);
    private static final Token FALSE = new Token(TokenType.BOOLEAN, false);
    private static final Token OPEN = new Token(TokenType.OPEN, "(");
    private static final Token TRUE = new Token(TokenType.BOOLEAN, true);
    private static final Token REF = new Token(TokenType.REF, "ref:");
    private static final Token ID = new Token(TokenType.ID, "id:");
    private static final Token UNDEFINED = new Token(TokenType.UNDEFINED, "nil");

    protected final InputSource in; // access from parser to query

    public Scanner(CharSequence string) {
        this(InputSource.fromString(string));
    }

    public Scanner(InputSource in) {
        this.in = in;
    }

    private Token closingParenthesis() {
        in.inc();
        return CLOSE;
    }

    private final void expectDelimiterToken() {
        if (!in.hasNext())
            return; // EOF is okay!
        char ch = in.peek();
        if (ch == '\"' || Character.isWhitespace(ch) || ch == '(' || ch == ')')
            return;
        throw new ParseError("Whitespace or delimiter expected", pos());
    }

    private final void expectDigit() {
        if (!isDigit(in.peek()))
            throw new ParseError("Digit expected", pos());
    }

    private final void expectWhitespaceToken() {
        if (!in.hasNext())
            return; // EOF is okay!
        char ch = in.peek();
        if (ch == '\"' || Character.isWhitespace(ch))
            return;
        throw new ParseError("Whitespace expected", pos());
    }

    public boolean hasNext() {
        this.skipWhitespace();
        return in.hasNext();
    }

    public Iterator<Token> iterator() {
        return this;
    }

    private Token keyword() {
        in.mark();
        if ('@' != in.peek())
            throw new ParseError("At sign expected", pos());
        in.inc();
        this.letterExpected();
        in.inc();
        while (isLetterOrDigit(in.peek()))
            in.inc();

        this.expectWhitespaceToken();

        return new Token(TokenType.KEYWORD, in.yank().toString());
    }

    private void letterExpected() {
        if (!isLetter(in.peek()))
            throw new ParseError("Letter expected", pos());
    }

    private Token nameOrSomethingLikeThat() {
        in.mark();
        while (true) {
            this.letterExpected();
            while (isLetterOrDigit(in.peek()) || in.peek() == '-')
                in.inc();
            char ch = in.peek();
            if (ch == ':')
                return reference();
            if (ch != '.')
                break;
            in.inc();
        }

        this.expectDelimiterToken();

        String name = in.yank().toString();
        if (name.equals("nil"))
            return UNDEFINED;
        if (name.equals("true"))
            return TRUE;
        if (name.equals("false"))
            return FALSE;
        return new Token(TokenType.NAME, name);
    }

    public Token next() {
        this.skipWhitespace();
        char ch = in.peek();
        if (ch == '(')
            return openingParenthesis();
        if (ch == ')')
            return closingParenthesis();
        if (ch == '\'')
            return string();
        if (ch == '-' || Character.isDigit(ch))
            return number();
        if (Character.isLetter(ch))
            return nameOrSomethingLikeThat();
        if (ch == '@')
            return keyword();
        throw new ParseError("Illegal character '" + ch + "'", pos());
    }

    public Token nextOrEOF() {
        Token n = hasNext() ? next() : EOF;
        return n;
    }

    /**
     * matches the rule
     * 
     * <pre>
     * &lt;number&gt; ::= -? &lt;digit&gt;+ ( . &lt;digit&gt;+ )? ( e ( -? &lt;digit&gt;+ ))?
     * </pre>
     * 
     * @return a NUMBER token
     */
    private Token number() {
        in.mark();
        boolean isDouble = false;

        // match -? <digit>+
        if (in.peek() == '-')
            in.inc();
        this.expectDigit();
        while (isDigit(in.peek())) {
            in.inc();
        }

        // match ( . <digit>+ )?
        if (in.peek() == '.') {
            isDouble = true;
            in.inc();
            this.expectDigit();
            while (isDigit(in.peek()))
                in.inc();
        }

        // match ( e -? <digit>+ )?
        if (in.peek() == 'e' || in.peek() == 'E') {
            isDouble = true;
            in.inc();
            if (in.peek() == '-')
                in.inc();
            this.expectDigit();
            while (isDigit(in.peek()))
                in.inc();
        }

        this.expectDelimiterToken();

        String str = in.yank().toString();
        return isDouble ? new Token(parseDouble(str)) : new Token(parseInt(str));
    }

    private Token openingParenthesis() {
        in.inc();
        return OPEN;
    }

    public final Position pos() {
        return in.getPosition();
    }

    private Token reference() {
        // called from nextNameOrKeywordOrBoolean
        in.inc(); // consume ':'
        String name = in.yank().toString();

        this.expectWhitespaceToken();

        if (name.equals("id:"))
            return ID;
        if (name.equals("ref:"))
            return REF;
        throw new ParseError("Illegal character ':'", pos());
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    private void skipComment() {
        Position start = this.pos();
        if (in.peek() != '\"')
            return;
        for (in.inc2();; in.inc2()) {
            char ch = in.peek();
            if (ch == InputSource.EOF)
                throw new ParseError("Runaway comment ", start);
            if (ch == '\"')
                break;
        }
        in.inc2();
    }

    private void skipWhitespace() {
        for (;; in.inc2()) {
            Character ch = in.peek();
            if (ch == InputSource.EOF)
                break;
            if (ch == '\"') {
                this.skipComment();
                continue;
            }
            if (!Character.isWhitespace(ch))
                break;
        }
    }

    private Token string() {
        Position start = this.pos();
        StringBuilder buffer = new StringBuilder();
        for (in.inc();;) {
            char ch = in.peek();
            if (ch == InputSource.EOF)
                throw new ParseError("Runaway string", start);
            in.inc();
            if (ch == '\'') {
                if (in.peek() != '\'')
                    break;
                in.inc();
            }
            buffer.append(ch);
        }

        this.expectDelimiterToken();

        return new Token(TokenType.STRING, buffer.toString());
    }

}
