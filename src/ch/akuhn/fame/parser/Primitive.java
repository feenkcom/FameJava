package ch.akuhn.fame.parser;


public enum Primitive {

    OBJECT(Object.class), STRING(String.class), NUMBER(Number.class), BOOLEAN(Boolean.class);

    public static Primitive valueOf(Object value) {
        if (value instanceof String)
            return STRING;
        if (value instanceof Number)
            return NUMBER;
        if (value instanceof Boolean)
            return BOOLEAN;
        throw new RuntimeException("Unknown type of primitive");
    }

    private Class<?> jclass;

    Primitive(Class<?> jclass) {
        this.jclass = jclass;
    }

    public Class<?> getImplementingClass() {
        return jclass;
    }
}
