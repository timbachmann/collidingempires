package collidingempires.server.ingame;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

public class FieldTest {

    private Field fExist;
    private Field fNoExist;
    private Field fExistFull;

    @Before
    public void setUp() throws Exception {
        //creating a Field, which exists and is empty, Id doesn't matter.
        fExist = new Field(true, "21");
        //creating a Field, which doesn't exist, Id doesn't matter.
        fNoExist = new Field(false, "99");
        //creating a Field, which exists but already has content.
        fExistFull = new Field(true, "55");
        Container testUnit = new Unit();
        fExistFull.addContent(testUnit);
    }

    @Test
    public void testAddContentUnitExists() {
        Container unit = new Unit();
        //Adding Unit to empty Field
        fExist.addContent(unit);
        //Content should be Unit
        Assert.assertTrue(fExist.getContent() instanceof Unit);
        //Level should be adjusted to 1
        Assert.assertEquals(1, fExist.getLevel());
        //Gold of field should be adjusted to 0
        Assert.assertEquals(0, fExist.getGold());
    }

    @Test
    public void testAddContentFarmExists() {
        Container farm = new Building("farm");
        //adding Farm to empty Field
        fExist.addContent(farm);
        //Content should be Building
        Assert.assertTrue(fExist.getContent() instanceof Building);
        //Level should stay at 0
        Assert.assertEquals(0, fExist.getLevel());
        //Gold should be adjusted to 4
        Assert.assertEquals(4, fExist.getGold());
    }

    @Test
    public void testAddContentTreeExists() {
        Container tree = new Building("tree");
        //adding tree to empty Field
        fExist.addContent(tree);
        //Content should be Building
        Assert.assertTrue(fExist.getContent() instanceof Building);
        //Level should stay at 0
        Assert.assertEquals(0, fExist.getLevel());
        //Gold should be adjusted to 1
        Assert.assertEquals(0, fExist.getGold());


    }

    @Test
    public void testAddContentUnitNoExists() {
        Container unit = new Unit();
        //Adding Unit to empty Field
        fExist.addContent(unit);
        //Content shouldn't be added
        Assert.assertNull(fNoExist.getContent());

    }

    @Test
    public void testAddContentFarmNoExists() {
        Container farm = new Building("farm");
        //adding Farm to empty Field
        fExist.addContent(farm);
        //Content shouldn't be added
        Assert.assertNull(fNoExist.getContent());
    }

    @Test
    public void testAddContentTreeNoExists() {
        Container tree = new Building("tree");
        //adding tree to empty Field
        fNoExist.addContent(tree);
        //Content shouldn't be added
        Assert.assertNull(fNoExist.getContent());
    }

    @Test
    public void testAddContentUnitExistFull() {
        Container unit = new Unit();
        //Adding Unit to Field with Unit
        fExistFull.addContent(unit);
        //Content should stay Unit
        Assert.assertTrue(fExistFull.getContent() instanceof Unit);
        //Unit should be level 2 now
        //Gold should be adjusted
        Assert.assertEquals(-4, fExistFull.getGold());
        //Level should be adjusted
        Assert.assertEquals(2, fExistFull.getLevel());

    }

    @Test
    public void testAddContentFarmExistsFull() {
        Container farm = new Building("farm");
        //adding Farm to Field with Unit
        fExistFull.addContent(farm);
        //farm shouldn't be added
        Assert.assertTrue(fExistFull.getContent() instanceof Unit);
    }

    @Test
    public void testAddContentTreeExistsFull() {
        Container tree = new Building("tree");
        //adding tree to Field with Unit
        fExistFull.addContent(tree);
        //tree shouldn't be added
        Assert.assertTrue(fExistFull.getContent() instanceof Unit);
    }



}
