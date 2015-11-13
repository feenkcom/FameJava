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

import static ch.akuhn.fame.parser.TokenType.BOOLEAN;
import static ch.akuhn.fame.parser.TokenType.CLOSE;
import static ch.akuhn.fame.parser.TokenType.EOF;
import static ch.akuhn.fame.parser.TokenType.ID;
import static ch.akuhn.fame.parser.TokenType.KEYWORD;
import static ch.akuhn.fame.parser.TokenType.NAME;
import static ch.akuhn.fame.parser.TokenType.NUMBER;
import static ch.akuhn.fame.parser.TokenType.OPEN;
import static ch.akuhn.fame.parser.TokenType.REF;
import static ch.akuhn.fame.parser.TokenType.STRING;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Simple MSE parser with an event-driven client. The interface
 * {@link ParseClient} defines a set of callback methods that will be called
 * when events occur during parsing.
 * <p>
 * The parser accepts the following grammar
 * 
 * <pre>
 *  root := document | EOF
 *  document := directive* OPEN elementNode* CLOSE
 *  directive := OPEN directiveName parameter* CLOSE
 *  directiveName := KEYWORD
 *  parameter := STRING | NUMBER | BOOLEAN | NAME | KEYWORD
 *  elementNode := OPEN elementName serial? attributeNode* CLOSE
 *  elementName := NAME
 *  serial := OPEN ID identifier CLOSE
 *  attributeNode := OPEN attributeName valueNode* CLOSE
 *  attributeName := NAME | KEYWORD
 *  valueNode := primitive | reference | elementNode
 *  primitive := STRING | NUMBER | BOOLEAN | UNDEFINED
 *  reference := id-reference | named-reference | external-reference
 *  id-reference := OPEN REF identifier CLOSE
 *  named-reference := OPEN REF NAME CLOSE
 *  identifier := NUMBER
 * </pre>
 * 
 * Constraint:
 * <ul>
 * <li>element nodes whose name starts with '<tt>@</tt>' must not have a serial
 * node.
 * <li>the identifier of serial nodes must be unique within the scope of an mse
 * file.
 * <li>id-references must refer to a valid identifier, ie, a matching serial.
 * <li>named references are currently restricted to <tt>Object</tt> and
 * primitive names.
 * <li>the semantics of external references are not covered by this
 * specification.
 * </ul>
 * 
 * Events are created for begin and end of document, elements and attributes,
 * for primitive values, for serial numbers, and for both, references by
 * identifier, by name and external references.
 * 
 * @author akuhn
 * 
 */
public class Parser {

    private ParseClient client;

    private Position pos, pos2;
    private Token peek, peek2;
    private Scanner stream;

    public Parser(Scanner stream) {
        this.stream = stream;
        this.consume();
        this.consume();
    }

    public void accept(ParseClient newClient) {
        this.client = newClient;
        if (peek.type == EOF) {
            client.beginDocument();
            client.endDocument();
        } else if (peek.type == OPEN) {
            while (this.directive())
                ;
            client.beginDocument();
            this.consume(OPEN);
            while (this.elementNode())
                ;
            this.consume(CLOSE);
            client.endDocument();
        }
        this.consume(EOF);
    }

    private boolean attributeNode() {
        if (peek.type == OPEN && (peek2.type == NAME || peek2.type == KEYWORD)) {
            this.consume();
            String name = this.consume().stringValue();
            client.beginAttribute(name);
            while (this.valueNode())
                ;
            if (peek.type == NAME) // nice error message if quotes are missing
                throw new ParseError(STRING, peek, pos);
            this.consume(CLOSE);
            client.endAttribute(name);
            return true;
        }
        return false;
    }

    private Token consume() {
        pos = pos2;
        pos2 = stream.pos();
        Token current = peek;
        peek = peek2;
        peek2 = stream.nextOrEOF();
        return current;
    }

    private Token consume(TokenType type) {
        if (peek.type != type)
            throw new ParseError(type, peek, pos);
        return this.consume();
    }

    private boolean directive() {
        if (peek.type == OPEN && peek2.type == KEYWORD) {
            this.consume();
            String name = this.consume(KEYWORD).stringValue();
            Collection<String> parameters = new LinkedList<String>();
            while (peek.type == STRING || peek.type == BOOLEAN || peek.type == NUMBER || peek.type == KEYWORD
                    || peek.type == NAME) {
                parameters.add(this.consume().stringValue());
            }
            this.consume(CLOSE);
            client.directive(name, parameters.toArray(new String[parameters.size()]));
            return true;
        }
        return false;
    }

    private boolean elementNode() {
        if (peek.type == OPEN && peek2.type == NAME) {
            this.consume();
            String name = this.consume(NAME).stringValue();
            client.beginElement(name);
            this.idColon();
            while (this.attributeNode())
                ;
            this.consume(CLOSE);
            client.endElement(name);
            return true;
        }
        return false;
    }

    private boolean idColon() {
        if (peek.type == OPEN && peek2.type == ID) {
            this.consume();
            this.consume();
            int index = this.consume(NUMBER).intValue();
            client.serial(index);
            this.consume(CLOSE);
            return true;
        }
        return false;
    }

    private boolean primitive() {
        if (peek.type == STRING || peek.type == NUMBER || peek.type == BOOLEAN) {
            Object value = this.consume().value;
            client.primitive(value);
            return true;
        }
        return false;
    }

    private boolean refColon() {
        if (peek.type == OPEN && peek2.type == REF) {
            this.consume();
            this.consume();
            if (peek.type == NAME) {
                String name = this.consume(NAME).stringValue();
                client.reference(name);
            } else if (peek.type == NUMBER) {
                int serial = this.consume(NUMBER).intValue();
                client.reference(serial);
            } else {
                throw new ParseError(NUMBER, peek, pos);
            }
            this.consume(CLOSE);
            return true;
        }
        return false;
    }

    private boolean unknownColon() {
        if (peek.type == OPEN && peek2.type == KEYWORD) {
            throw new ParseError("Unknown selector #" + peek2.value, pos2);
        }
        return false;
    }

    private boolean valueNode() {
        return elementNode() || primitive() || refColon() || unknownColon();
    }

}
