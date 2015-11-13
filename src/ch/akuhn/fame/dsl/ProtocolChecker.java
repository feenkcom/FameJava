package ch.akuhn.fame.dsl;

import static ch.akuhn.fame.dsl.ProtocolChecker.State.BEGIN_ATTRIBUTE;
import static ch.akuhn.fame.dsl.ProtocolChecker.State.BEGIN_DOCUMENT;
import static ch.akuhn.fame.dsl.ProtocolChecker.State.BEGIN_ELEMENT;
import static ch.akuhn.fame.dsl.ProtocolChecker.State.DIRECTIVE;
import static ch.akuhn.fame.dsl.ProtocolChecker.State.END_ATTRIBUTE;
import static ch.akuhn.fame.dsl.ProtocolChecker.State.END_DOCUMENT;
import static ch.akuhn.fame.dsl.ProtocolChecker.State.END_ELEMENT;
import static ch.akuhn.fame.dsl.ProtocolChecker.State.PRIMITIVE;
import static ch.akuhn.fame.dsl.ProtocolChecker.State.REFERENCE;
import static ch.akuhn.fame.dsl.ProtocolChecker.State.SERIAL;

import java.util.Arrays;
import java.util.LinkedList;

import ch.akuhn.fame.parser.ParseClient;

public class ProtocolChecker implements ParseClient {

    private LinkedList<String> stack = new LinkedList<String>();
    private State[] expectedState = { State.BEGIN_DOCUMENT, State.DIRECTIVE };
    
    public enum State {
        DIRECTIVE,
        BEGIN_DOCUMENT, END_DOCUMENT,
        BEGIN_ELEMENT, END_ELEMENT,
        BEGIN_ATTRIBUTE, END_ATTRIBUTE,
        REFERENCE, PRIMITIVE,
        SERIAL
    }
    
    public ParseClient client;
    
    public ProtocolChecker(ParseClient client) {
        this.client = client;
    }
    
    public void beginAttribute(String name) {
        stack.addLast(name);
        checkState(BEGIN_ATTRIBUTE);
        expectState(BEGIN_ELEMENT, END_ATTRIBUTE, PRIMITIVE, REFERENCE);
        client.beginAttribute(name);
    }

    private void checkState(State state) {
        for (State each : expectedState) {
            if (each == state) return;
        }
        assert false : "Expected " + Arrays.asList(expectedState) + " but was " + state;
    }

    public void beginDocument() {
        checkState(BEGIN_DOCUMENT);
        expectState(BEGIN_ELEMENT, END_DOCUMENT);
        client.beginDocument();
    }

    private void expectState(State... states) {
        expectedState = states;
    }

    public void beginElement(String name) {
        stack.addLast(name);
        checkState(BEGIN_ELEMENT);
        expectState(BEGIN_ATTRIBUTE, SERIAL, END_ELEMENT);
        client.beginElement(name);
    }

    public void directive(String name, String... params) {
        checkState(DIRECTIVE);
        expectState(BEGIN_DOCUMENT, DIRECTIVE);
        client.directive(name,params);
    }

    public void endAttribute(String name) {
        assert name.equals(stack.removeLast());
        checkState(END_ATTRIBUTE);
        expectState(BEGIN_ATTRIBUTE, END_ELEMENT);
        client.endAttribute(name);
    }

    public void endDocument() {
        checkState(END_DOCUMENT);
        expectState();
        client.endDocument();
    }

    public void endElement(String name) {
        assert name.equals(stack.removeLast());
        checkState(END_ELEMENT);
        if (stack.isEmpty()) {
            expectState(BEGIN_ELEMENT, END_DOCUMENT);
        }
        else {
            expectState(BEGIN_ELEMENT, END_ATTRIBUTE, PRIMITIVE, REFERENCE);
        }
        client.endElement(name);
    }

    public void primitive(Object value) {
        checkState(PRIMITIVE);
        expectState(BEGIN_ELEMENT, END_ATTRIBUTE, PRIMITIVE, REFERENCE);
        client.primitive(value);
    }

    public void reference(int index) {
        checkState(REFERENCE);
        expectState(BEGIN_ELEMENT, END_ATTRIBUTE, PRIMITIVE, REFERENCE);
        client.reference(index);
    }

    public void reference(String name) {
        checkState(REFERENCE);
        expectState(BEGIN_ELEMENT, END_ATTRIBUTE, PRIMITIVE, REFERENCE);
        client.reference(name);
    }

    public void reference(String name, int index) {
        throw new AssertionError("Not yet implemented!");
    }

    public void serial(int index) {
        checkState(SERIAL);
        expectState(BEGIN_ATTRIBUTE, END_ELEMENT);
        client.serial(index);
    }
   
}
