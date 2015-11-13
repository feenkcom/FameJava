package ch.akuhn.fame.test;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.akuhn.fame.codegen.target.DungeonCodegen;
import ch.akuhn.fame.test.DungeonExample.Dragon;
import ch.akuhn.fame.test.DungeonExample.Hero;
import ch.akuhn.fame.test.DungeonExample.Treasure;

public class MultivalueLinkTest {

    @BeforeClass
    public static void runCodegen() {
        DungeonCodegen.main();
        // TODO avoid manual recompile which is requiered here !!!
    }
    
    @Test
    public void testOneOne() {
        Hero h1 = new Hero();
        Treasure t1 = new Treasure();
        Treasure t2 = new Treasure();
        
        // from null to null
        h1.setTalisman(null);
        assertEquals(null, h1.getTalisman());
        assertEquals(null, t1.getOwner());
        
        // from null to value
        h1.setTalisman(t1);
        assertEquals(t1, h1.getTalisman());
        assertEquals(h1, t1.getOwner());
        
        // change value
        h1.setTalisman(t2);
        assertEquals(null, t1.getOwner());
        assertEquals(t2, h1.getTalisman());
        assertEquals(h1, t2.getOwner());
        
        // from value to null
        h1.setTalisman(null);
        assertEquals(null, h1.getTalisman());
        assertEquals(null, t2.getOwner());
    }
    
    @Test
    public void testOneMany() {
        Dragon d1 = new Dragon();
        Dragon d2 = new Dragon();
        Treasure t1 = new Treasure();
        Treasure t2 = new Treasure();
        
        // from null to null
        t1.setKeeper(null);
        assertEquals(0, d1.getHoard().size());
        assertEquals(0, d2.getHoard().size());
        assertEquals(null, t1.getKeeper());
        assertEquals(null, t2.getKeeper());
        
        // from null to value
        t1.setKeeper(d1);
        assertEquals(1, d1.getHoard().size());
        assertEquals(true, d1.getHoard().contains(t1));
        assertEquals(0, d2.getHoard().size());
        assertEquals(d1, t1.getKeeper());
        assertEquals(null, t2.getKeeper());
        
        // change value, case I
        t1.setKeeper(d2);
        assertEquals(0, d1.getHoard().size());
        assertEquals(1, d2.getHoard().size());
        assertEquals(true, d2.getHoard().contains(t1));
        assertEquals(d2, t1.getKeeper());
        assertEquals(null, t2.getKeeper());
        
        // change value, again
        t1.setKeeper(d2);
        assertEquals(0, d1.getHoard().size());
        assertEquals(1, d2.getHoard().size());
        assertEquals(true, d2.getHoard().contains(t1));
        assertEquals(d2, t1.getKeeper());
        assertEquals(null, t2.getKeeper());
        
        // change value, case II
        t2.setKeeper(d2);
        assertEquals(0, d1.getHoard().size());
        assertEquals(2, d2.getHoard().size());
        assertEquals(true, d2.getHoard().contains(t1));
        assertEquals(true, d2.getHoard().contains(t2));
        assertEquals(d2, t1.getKeeper());
        assertEquals(d2, t2.getKeeper());
        
        // from value to null, case I
        t1.setKeeper(null);
        assertEquals(0, d1.getHoard().size());
        assertEquals(1, d2.getHoard().size());
        assertEquals(true, d2.getHoard().contains(t2));
        assertEquals(null, t1.getKeeper());
        assertEquals(d2, t2.getKeeper());

        // from value to null, case II
        t2.setKeeper(null);
        assertEquals(0, d1.getHoard().size());
        assertEquals(0, d2.getHoard().size());
        assertEquals(null, t1.getKeeper());
        assertEquals(null, t2.getKeeper());
    }
    
    @Test
    public void testManyOne() {
        Dragon d1 = new Dragon();
        Dragon d2 = new Dragon();
        Treasure t1 = new Treasure();
        Treasure t2 = new Treasure();
        
        // add value
        d1.addHoard(t1);
        assertEquals(1, d1.getHoard().size());
        assertEquals(true, d1.getHoard().contains(t1));
        assertEquals(0, d2.getHoard().size());
        assertEquals(d1, t1.getKeeper());
        assertEquals(null, t2.getKeeper());
        
        // add another value
        d1.addHoard(t2);
        assertEquals(2, d1.getHoard().size());
        assertEquals(true, d1.getHoard().contains(t1));
        assertEquals(true, d1.getHoard().contains(t2));
        assertEquals(0, d2.getHoard().size());
        assertEquals(d1, t1.getKeeper());
        assertEquals(d1, t2.getKeeper());
        
        // move value
        d2.addHoard(t2);
        assertEquals(1, d1.getHoard().size());
        assertEquals(true, d1.getHoard().contains(t1));
        assertEquals(1, d2.getHoard().size());
        assertEquals(true, d2.getHoard().contains(t2));
        assertEquals(d1, t1.getKeeper());
        assertEquals(d2, t2.getKeeper());
        
        // remove value
        d2.getHoard().remove(t2);
        assertEquals(1, d1.getHoard().size());
        assertEquals(true, d1.getHoard().contains(t1));
        assertEquals(0, d2.getHoard().size());
        assertEquals(d1, t1.getKeeper());
        assertEquals(null, t2.getKeeper());
        
        // remove value, again
        d2.getHoard().remove(t2);
        assertEquals(1, d1.getHoard().size());
        assertEquals(true, d1.getHoard().contains(t1));
        assertEquals(0, d2.getHoard().size());
        assertEquals(d1, t1.getKeeper());
        assertEquals(null, t2.getKeeper());

        // from value to null, case II
        d1.getHoard().remove(t1);
        assertEquals(0, d1.getHoard().size());
        assertEquals(0, d2.getHoard().size());
        assertEquals(null, t1.getKeeper());
        assertEquals(null, t2.getKeeper());
    }
    
}
