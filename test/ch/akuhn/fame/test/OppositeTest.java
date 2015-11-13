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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Collection;

import org.junit.Test;

import ch.akuhn.fame.internal.MultivalueSet;

public class OppositeTest {

    private static class Many {

        private Collection<One> ones;

        private Collection<Three> threes;

        public Collection<One> getOnes() {
            if (ones == null) {
                ones = new MultivalueSet<One>() {
                    @Override
                    protected void clearOpposite(One e) {
                        e.setMany(null);
                    }

                    @Override
                    protected void setOpposite(One e) {
                        e.setMany(Many.this);
                    }
                };
            }
            return ones;
        }

        public Collection<Three> getThrees() {
            if (threes == null) {
                threes = new MultivalueSet<Three>() {
                    @Override
                    protected void clearOpposite(Three e) {
                        e.getManies().remove(Many.this);
                    }

                    @Override
                    protected void setOpposite(Three e) {
                        e.getManies().add(Many.this);
                    }
                };
            }
            return threes;
        }

    }

    private static class One {

        private Many many = null;

        public Many getMany() {
            return many;
        }

        public void setMany(Many many) {
            if (this.many != null) {
                if (this.many.equals(many)) return;
                this.many.getOnes().remove(this);
            }
            this.many = many;
            if (many == null) return;
            many.getOnes().add(this);
        }
    }

    private static class Three {

        private Collection<Many> manies;

        public Collection<Many> getManies() {
            if (manies == null) {
                manies = new MultivalueSet<Many>() {
                    @Override
                    protected void clearOpposite(Many e) {
                        e.getThrees().remove(Three.this);
                    }

                    @Override
                    protected void setOpposite(Many e) {
                        e.getThrees().add(Three.this);
                    }
                };
            }
            return manies;
        }

    }

    @Test
    public void testGetOnesAddRemove() {
        Many many = new Many();
        One one = new One();
        One two = new One();

        many.getOnes().add(one);

        assertEquals(1, many.getOnes().size());
        assertSame(one, many.getOnes().iterator().next());
        assertSame(many, one.getMany());

        many.getOnes().add(one);

        assertEquals(1, many.getOnes().size());
        assertSame(one, many.getOnes().iterator().next());
        assertSame(many, one.getMany());

        many.getOnes().add(two);

        assertEquals(2, many.getOnes().size());
        assertSame(many, one.getMany());

        many.getOnes().remove(one);

        assertEquals(1, many.getOnes().size());
        assertSame(two, many.getOnes().iterator().next());
        assertSame(null, one.getMany());

        many.getOnes().remove(one);

        assertEquals(1, many.getOnes().size());
        assertSame(two, many.getOnes().iterator().next());
        assertSame(null, one.getMany());
    }

    @Test
    public void testManyToMany() {
        Many a = new Many();
        Many b = new Many();
        Many c = new Many();
        Three x = new Three();
        Three y = new Three();
        Three z = new Three();

        a.getThrees();

        assertEquals(0, a.getThrees().size());
        assertEquals(0, x.getManies().size());

        a.getThrees().add(x);

        assertEquals(1, a.getThrees().size());
        assertEquals(1, x.getManies().size());
        assertSame(a, x.getManies().iterator().next());
        assertSame(x, a.getThrees().iterator().next());

        a.getThrees().add(y);

        assertEquals(2, a.getThrees().size());
        assertEquals(1, y.getManies().size());
        assertSame(a, y.getManies().iterator().next());

        a.getThrees().add(z);

        assertEquals(3, a.getThrees().size());
        assertEquals(1, y.getManies().size());
        assertSame(a, z.getManies().iterator().next());

        a.getThrees().add(z); // add twice

        assertEquals(3, a.getThrees().size());
        assertEquals(1, y.getManies().size());
        assertSame(a, z.getManies().iterator().next());

        a.getThrees().remove(y);

        assertEquals(2, a.getThrees().size());
        assertEquals(0, y.getManies().size());

        a.getThrees().remove(y); // remove twice

        assertEquals(2, a.getThrees().size());
        assertEquals(0, y.getManies().size());

        z.getManies().remove(a); // remove twice

        assertEquals(1, a.getThrees().size());
        assertEquals(0, z.getManies().size());

        z.getManies().add(b);
        b.getThrees().add(y);

        assertEquals(2, b.getThrees().size());

        z.getManies().add(c);
        c.getThrees().remove(z);

        assertEquals(0, c.getThrees().size());

    }

}
