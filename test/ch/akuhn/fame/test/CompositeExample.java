package ch.akuhn.fame.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.akuhn.fame.FameDescription;
import ch.akuhn.fame.FamePackage;
import ch.akuhn.fame.FameProperty;
import ch.akuhn.fame.MetaRepository;
import ch.akuhn.fame.Repository;
import ch.akuhn.fame.Tower;
import ch.akuhn.fame.fm3.PropertyDescription;
import ch.unibe.jexample.Given;
import ch.unibe.jexample.Injection;
import ch.unibe.jexample.InjectionPolicy;
import ch.unibe.jexample.JExample;

@RunWith(JExample.class)
@Injection(InjectionPolicy.NONE)
public class CompositeExample {

    @FameDescription
    @FamePackage("TEST")
    static abstract class Composite {

        @FameProperty(opposite = "children")
        public Container parent;

        @FameProperty(derived = true)
        public abstract int getTotalCount();

    }

    @FameDescription
    @FamePackage("TEST")
    static class Container extends Composite {

        @FameProperty(opposite = "parent")
        public Collection<Composite> children = new ArrayList();

        @Override
        public int getTotalCount() {
            int count = 0;
            for (Composite c : children)
                count += c.getTotalCount();
            return count;
        }

    }

    @FameDescription
    @FamePackage("TEST")
    static class Leaf extends Composite {

        @FameProperty
        public int count;

        @Override
        public int getTotalCount() {
            return count;
        }

    }

    @Test
    public Tower createTower() {
        Tower t = new Tower();
        assertNotNull(t.getMetaMetamodel());
        assertNotNull(t.getMetamodel());
        assertNotNull(t.getModel());
        return t;
    }
    
    @Test
    @Given("#createTower")
    public Tower towerHasLayers(Tower t) {
        assertEquals(t.getModel().getMetamodel(), t.getMetamodel());
        assertEquals(t.getMetamodel().getMetamodel(), t.getMetaMetamodel());
        assertEquals(t.getMetaMetamodel().getMetamodel(), t.getMetaMetamodel());
        return t;
    }
    
    @Test
    @Given("#towerHasLayers")
    public Tower tower(Tower t) {
        assertEquals(0, t.getModel().size());
        assertEquals(0, t.getMetamodel().size());
        assertEquals(25, t.getMetaMetamodel().size());
        return t;
    }
    
    @Test
    @Ignore // FIXME
    @Given("model;newParent;newChildA;newChildB")
    public Repository createInstances(Repository $) {
        assertEquals(3, $.getElements().size());
        return $;
    }

    @Test
    @Given("model;parentWithChildren")
    public String exportMSE(Repository m) {
        String mse = m.exportMSE();
        return mse;
    }

    @Test
    @Ignore // FIXME
    @Given("tower;exportMSE;metamodel")
    public Repository importMSE(Tower t, String mse) {
        t.getModel().importMSE(mse);
        Repository m = t.getModel();
        assertEquals(3, m.getElements().size());
        return m;
    }

    @Test
    @Ignore // FIXME
    @Given("model;model")
    public void jexampleKeepWorksFine(Repository m1, Repository m2) {
        assertSame(m1, m2);
    }

    @Test
    @Given("#tower")
    public MetaRepository metamodel(Tower t) {
        t.getMetamodel().withAll(Composite.class, Container.class, Leaf.class);
        MetaRepository $ = t.getMetamodel();
        assertEquals(3, $.allClassDescriptions().size());
        assertEquals(4, $.all(PropertyDescription.class).size());
        return $;
    }

    @Test
    @Given("metamodelNames")
    public Repository model(MetaRepository metamodel) {
        Repository $ = new Repository(metamodel);
        assertEquals(metamodel, $.getMetamodel());
        return $;
    }

    @Test
    @Given("metamodel")
    public MetaRepository metamodelNames(MetaRepository mm) {
        assertNull(mm.descriptionNamed("FAME"));
        assertNotNull(mm.descriptionNamed("TEST.Container"));
        assertNotNull(mm.descriptionNamed("TEST.Leaf"));
        assertNotNull(mm.descriptionNamed("TEST.Composite"));
        return mm;
    }

    @Test
    @Given("model")
    public Leaf newChildA(Repository repo) {
        Object $ = repo.newInstance("TEST.Leaf");
        assertNotNull($);
        assertEquals(Leaf.class, $.getClass());
        return (Leaf) $;
    }

    @Test
    @Given("model")
    public Leaf newChildB(Repository repo) {
        Object $ = repo.newInstance("TEST.Leaf");
        assertNotNull($);
        assertEquals(Leaf.class, $.getClass());
        return (Leaf) $;
    }

    @Test
    @Given("model")
    public Container newParent(Repository repo) {
        Object $ = repo.newInstance("TEST.Container");
        assertNotNull($);
        assertEquals(Container.class, $.getClass());
        return (Container) $;
    }

    @Test
    @Given("model;newChildA")
    public Leaf numberPropertyA(Repository m, Leaf a) {
        assertEquals(0, a.count);
        assertEquals(0, m.read("count", a));
        m.write("count", a, 42);
        assertEquals(42, a.count);
        assertEquals(42, m.read("count", a));
        return a;
    }

    @Test
    @Given("model;newChildB")
    public Leaf numberPropertyB(Repository m, Leaf b) {
        assertEquals(0, b.count);
        assertEquals(0, m.read("count", b));
        m.write("count", b, 23);
        assertEquals(23, b.count);
        assertEquals(23, m.read("count", b));
        return b;
    }

    @Test
    @Given("metamodel;metamodelNames")
    public void parentChildrenAreOpposite(MetaRepository mm) {
        PropertyDescription parent = mm.descriptionNamed("TEST.Composite").attributeNamed("parent");
        PropertyDescription children = mm.descriptionNamed("TEST.Container").attributeNamed("children");
        assertNotNull(parent);
        assertNotNull(children);
        assertTrue(parent.hasOpposite());
        assertTrue(children.hasOpposite());
        assertEquals(parent, children.getOpposite());
        assertEquals(children, parent.getOpposite());
    }

    @Test
    @Given("model;newParent;newChildA;newChildB;parentChildrenAreOpposite")
    public Container parentWithChildren(Repository m, Container p, Leaf a, Leaf b) {
        assertEquals(0, p.children.size());
        assertEquals(null, a.parent);
        assertEquals(null, b.parent);
        m.write("children", p, a, b);
        assertEquals(2, p.children.size());
        assertEquals(p, a.parent);
        assertEquals(p, b.parent);
        return p;
    }
}
