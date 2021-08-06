package ch.akuhn.fame.internal;


/**
 * This class a helps printing model in JSON
 *
 * @author Sabri.BENBRAHIM, Benoit "badetitou" VERHAEGHE
 */
public class JSONPrinter extends AbstractPrintClient {

    /**
     * Constructor
     *
     * @param stream in which we have to write
     */
    public JSONPrinter(Appendable stream) {
        super(stream);
    }

    @Override
    public void beginAttribute(String name) {
        lntabs();
        append("\"");
        append(name);
        append("\"");
        append(":");

    }

    @Override
    public void beginDocument() {
        append("[");
    }

    @Override
    public void beginElement(String name) {
        this.indentation++;
        append("{");
        lntabs();
        append("\"FM3\":\"");
        append(name);
        append("\",");
        lntabs();
    }

    public void beginMultivalue(String name) {
        append("[");
    }

    @Override
    public void directive(String name, String... params) {
    }

    @Override
    public void endAttribute(String name) {
    }

    @Override
    public void endDocument() {
        lntabs();
        append("]");
        close();
    }

    @Override
    public void endElement(String name) {
        lntabs();
        append("}");
        this.indentation--;
    }

    public void endMultivalue(String name) {
        append("]");
    }

    @Override
    public void primitive(Object value) {
        append('"');
        if (value.getClass() == String.class){
            for (char c : ((String) value).toCharArray()) {
                if (c == '"') {
                    append('\\');
                } else if (c == '\\') {
                    append('\\');
                }
                append(c);
            }
        } else {
            append(value.toString());
        }
        append('"');
    }

    @Override
    public void reference(int index) {
        append(" ");
        append("{");
        append("\"ref\":");
        append(" ");
        append(String.valueOf(index));
        append("}");
    }

    @Override
    public void reference(String name) {
        append(" ");
        append("{");
        append("\"ref\":");
        append(" ");
        append("\"");
        append(name);
        append("\"");
        append("}");
    }

    @Override
    public void reference(String name, int index) {
    }

    @Override
    public void serial(int index) {
        append("\"id\":");
        append(String.valueOf(index));
        append(",");
    }

    public void printEntitySeparator() {
        append(",");
    }

    public void printPropertySeparator() {
        append(",");
    }

}