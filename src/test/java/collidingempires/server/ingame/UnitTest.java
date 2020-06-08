package collidingempires.server.ingame;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

public class UnitTest {
    private Unit unit;

    @Before
    public void setUp() throws Exception {
        unit = new Unit();
    }


    @Test
    public void testLevelUpLv1() {
        //Rent should be -1
        Assert.assertEquals(-1, unit.getRent());
        //Level should be 1
        Assert.assertEquals(1, unit.getLevel());
        //Level up unit
        unit.levelUp();
        //Rent should be -5
        Assert.assertEquals(-5, unit.getRent());
        //Level should be 2
        Assert.assertEquals(2,unit.getLevel());

    }

    @Test
    public void testLevelUpLv2() {
        unit.levelUp();
        //unit now Level 2
        //Level unit up to 3
        unit.levelUp();
        //Rent should be -10
        Assert.assertEquals(-10, unit.getRent());
        //Level should be 3
        Assert.assertEquals(3,unit.getLevel());

    }

    @Test
    public void testLevelUpLv3() {
        unit.levelUp();
        unit.levelUp();
        //unit now Level 3
        //Level unit up to 4
        unit.levelUp();
        //Rent should be -20
        Assert.assertEquals(-20, unit.getRent());
        //Level should be 4
        Assert.assertEquals(4, unit.getLevel());
    }

    @Test
    public void testLevelUpLv4() {
        unit.levelUp();
        unit.levelUp();
        unit.levelUp();
        //Rent should be -20
        Assert.assertEquals(-20, unit.getRent());
        //Level should be 4
        Assert.assertEquals(4, unit.getLevel());
        //Level 4 cant be leveled up further
        unit.levelUp();
        //Rent and Level should stay the same
        Assert.assertEquals(-20, unit.getRent());
        Assert.assertEquals(4, unit.getLevel());
    }

}
