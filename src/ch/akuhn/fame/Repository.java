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

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import ch.akuhn.fame.MetaRepository.ClassNotMetadescribedException;
import ch.akuhn.fame.fm3.MetaDescription;
import ch.akuhn.fame.fm3.PropertyDescription;
import ch.akuhn.fame.internal.JSONPrinter;
import ch.akuhn.fame.internal.MSEPrinter;
import ch.akuhn.fame.internal.RepositoryVisitor;
import ch.akuhn.fame.parser.Importer;
import ch.akuhn.fame.parser.InputSource;
import ch.akuhn.fame.parser.ParseClient;
import ch.akuhn.util.Files;
import ch.akuhn.util.query.Count;
import ch.akuhn.util.query.Select;

/**
 * A group of elements that conform to the same meta-model.
 * 
 * @author Adrian Kuhn, 2007-2008
 * 
 */
@SuppressWarnings("unchecked")
public class Repository {

    /**
     * Resolves the fully qualified name of an element, or <code>null</code> if
     * {@linkplain Named} is not implemented. Nested elements must implement
     * {@linkplain Nested} in order to resolve their full name.
     * 
     * @see Named
     * @see Nested
     * 
     * @param element
     *            should implement Named and maybe also Owned.
     * @return may return <code>null</code>.
     */
    public static final String fullname(Object element) {
        if (!(element instanceof Named))
            return null;
        String name = ((Named) element).getName();
        assert name != null;
        if (!(element instanceof Nested))
            return name;
        Object owner = ((Nested) element).getOwner();
        if (owner == null)
            return name;
        String ownerName = fullname(owner);
        assert ownerName != null;
        return ownerName + "." + name;
    }

    private Collection elements;

    private MetaRepository metamodel;

    /**
     * Creates an empty tower of models. The tower has three layers: both this
     * and the meta-layer are initially empty, whereas the topmost layer is
     * initialized with a new FM3 package.
     * 
     */
    public Repository() {
        this(new MetaRepository(MetaRepository.createFM3()));
    }

    /**
     * Creates a empty layer with the given meta-layer.
     * <p>
     * If the specified parameter is <code>null</code>, creates a
     * self-describing layer (ie an meta-metamodel).
     * 
     * @param metamodel
     */
    public Repository(MetaRepository metamodel) {
        // allow null in order to boot-strap self-described meta-models
        this.metamodel = metamodel == null ? (MetaRepository) this : metamodel;
        this.elements = new HashSet();
    }

    public void accept(ParseClient visitor) {
        Runnable runner = new RepositoryVisitor(this, visitor);
        runner.run();
    }
    
    static public class ElementInPropertyNotMetadescribed extends AssertionError {
        private static final long serialVersionUID = 1661566781761376913L;
        public PropertyDescription property;
        public ElementInPropertyNotMetadescribed(PropertyDescription property) {
            this.property = property;
        }
    }

    public void add(Object element) {
        assert element != null;
        if (elements.add(element)) {
            MetaDescription meta = (MetaDescription) metamodel.getDescription(element.getClass());
            assert meta != null : element.getClass();
            for (PropertyDescription property : meta.allProperties()) {
                if (!property.isPrimitive()) {
                    try {
                        boolean isRoot = property.getType().isRoot();
                        for (Object value : property.readAll(element)) {
                            assert value != null : property.getFullname();
                            if (!(isRoot &&
                                    (value instanceof String ||
                                            value instanceof Boolean ||
                                            value instanceof Number))) {
                                try {
                                    this.add(value);
                                } catch (ClassNotMetadescribedException e) {
                                    throw new ElementInPropertyNotMetadescribed(property);
                                }
                            }
                        }
                    } catch (UnsupportedOperationException ex) {
                        /*
                        property is not implemented.. but it is normal for some derived property we do not support
                        with the parser (for instance, VerveineJ)
                         */
                        // System.err.println(property.getFullname() + " not supported yet.");
                    }
                }
            }
        }
    }

    public void add(Object element, Object... more) {
        this.add(element);
        for (Object o : more) {
            this.add(o);
        }
    }

    public void addAll(Collection all) {
        for (Object each : all) {
            this.add(each);
        }
    }

    /**
     * Collect all elements with the specified class.
     * 
     */
    public <T> Collection<T> all(Class<T> type) {
        Select<T> query = Select.from(elements);
        for (Select<T> each: query) {
            each.yield = type.isAssignableFrom(each.element.getClass());
        }
        return query.result();
    }

    public static class ObjectNotDescribed extends AssertionError {
        private static final long serialVersionUID = -3268614108861432571L;
        public Object object;
        public ObjectNotDescribed(Object o) {
            this.object = o;
        }
    }
    
    public MetaDescription descriptionOf(Object element) {
        try {
            return (MetaDescription) metamodel.getDescription(element.getClass());
        } catch (ClassNotMetadescribedException e) {
            throw new ObjectNotDescribed(element);
        }
    }

    /**
     * Exports all elements as MSE-formatted string.
     * 
     */
    public String exportMSE() {
        StringBuilder stream = new StringBuilder();
        this.exportMSE(stream);
        return stream.toString();
    }
    
    public void importMSE(InputSource input) {
        Importer importer = new Importer(this.getMetamodel());
        importer.readFrom(input);
        this.addAll(importer.getResult().getElements());
    }

    public void importMSEFile(String name) {
        importMSE(InputSource.fromFilename(name));
    }

    
    public void importMSE(CharSequence content) {
        importMSE(InputSource.fromString(content));
    }

    public void importMSE(InputStream stream) {
        importMSE(InputSource.fromInputStream(stream));
    }
    
    public void exportMSEFile(String filename) {
        this.accept(new MSEPrinter(Files.openWrite(filename)));
    }
    
    public void exportMSE(Appendable stream) {
        this.accept(new MSEPrinter(stream));
    }

    public void exportJSONFile(String filename) {
        this.exportJSON(Files.openWrite(filename));
    }

    public void exportJSON(Appendable stream) {
        this.accept(new JSONPrinter(stream));
    }

    public Collection getElements() {
        return elements;
    }

    public MetaRepository getMetamodel() {
        return metamodel;
    }

    public <T> T newInstance(String qname) {
        MetaDescription m = metamodel.descriptionNamed(qname);
        assert m != null;
        T element = (T) m.newInstance();
        this.add(element);
        return element;
    }

    public <T> T read(String propertyName, Object element) {
        MetaDescription m = this.descriptionOf(element);
        PropertyDescription p = m.attributeNamed(propertyName);
        T value = (T) p.read(element);
        return value;
    }

    public void write(String propertyName, Object element, Object... values) {
        MetaDescription m = this.descriptionOf(element);
        PropertyDescription p = m.attributeNamed(propertyName);
        p.writeAll(element, Arrays.asList(values));
        if (p.hasOpposite()) {
            for (Object v : values) {
                p.getOpposite().writeAll(v, Collections.singleton(element));
            }
        }
    }

    public int size() {
        return elements.size();
    }
    
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public <T> int count(Class<T> kind) {
        Count<Object> query = Count.from(elements);
        for (Count<Object> each: query) {
            each.yield = kind.isAssignableFrom(each.element.getClass());
        }
        return query.result();
    }

     
}
