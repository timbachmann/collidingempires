package collidingempires.server.ingame;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;


public class MapTest {

    private Map map;
    private String mapString = "00000011010000000000001111110011010000001111101111110011"
            + "0111111111111000111111111111111110111111111111111100011111111"
            + "111110000111111111111110000111111111111111100001000111111111100";

    @Before
    public void setUp() throws Exception {
        //creates an empty map with a given Layout.
        map = new Map(mapString);
    }

    @Test
    public void testAddObjectTower() {
        String id = "40";
        Container tower = new Building("tower");
        //checking level of surrounding Field with id 40
        for (String s : map.existingNeighborsList(id)) {
            Assert.assertEquals(0, map.getField(s).getLevel());
        }
        map.addObject(id, tower);
        //checking level of surrounding Field with id 40
        for (String s : map.existingNeighborsList(id)) {
            Assert.assertEquals(1, map.getField(s).getLevel());
        }
    }

    @Test
    public void testAddObjectInvalidId() {
        String id = "199";
        Container tower = new Building("tower");
        //should return without exception
        map.addObject(id, tower);
    }

    @Test
    public void testRemoveContentTwoTowers() {

        //placing towers on Id 61 and 60
        map.addObject("61", new Building("tower"));
        map.addObject("60", new Building("tower"));

        //checking levels of surrounding fields
        for (String s : map.existingNeighborsList("61")) {
            Assert.assertEquals(1, map.getField(s).getLevel());
        }
        for (String s : map.existingNeighborsList("60")) {
            Assert.assertEquals(1, map.getField(s).getLevel());
        }
        //remove tower on id 61
        map.removeContent("61");

        //tower should have been removed
        Assert.assertNull(map.getField("61").getContent());
        //fields not bordering tower with id 60 should be level 0
        Assert.assertEquals(0, map.getField("62").getLevel());
        Assert.assertEquals(0, map.getField("80").getLevel());
        Assert.assertEquals(0, map.getField("79").getLevel());

        //Fields surrounding Id 60 should still be level 1
        for (String s : map.existingNeighborsList("60")) {
            Assert.assertEquals(1, map.getField(s).getLevel());
        }

    }
    @Test
    public void testRemoveContentTower() {
        //placing tower on Id 61
        map.addObject("61", new Building("tower"));
        //checking levels of surrounding fields
        for (String s : map.existingNeighborsList("61")) {
            Assert.assertEquals(1, map.getField(s).getLevel());
        }
        //removing tower
        map.removeContent("61");
        //tower should have been removed
        Assert.assertNull(map.getField("61").getContent());
        //checking levels of surrounding fields
        for (String s : map.existingNeighborsList("61")) {
            Assert.assertEquals(0, map.getField(s).getLevel());
        }
    }

    @Test
    public void testRemoveContentUnit() {
        map.addObject("61",new Unit());
        //Content not null and level 1
        Assert.assertNotNull(map.getField("61").getContent());
        Assert.assertEquals(1, map.getField("61").getLevel());
        //Gold of field should be 0
        Assert.assertEquals(0,  map.getField("61").getGold());
        //removing Unit
        map.removeContent("61");
        //Content null and level 0
        Assert.assertNull(map.getField("61").getContent());
        Assert.assertEquals(0, map.getField("61").getLevel());
        //Gold of field should be 1
        Assert.assertEquals(1,  map.getField("61").getGold());
    }

    @Test
    public void testRemoveContentTree() {
        map.addTree("99");
        //Content not null
        Assert.assertNotNull(map.getField("99").getContent());
        //gold of field should be 0
        Assert.assertEquals(0, map.getField("99").getGold());
        //remove tree
        map.removeContent("99");
        //Content null
        Assert.assertNull(map.getField("99").getContent());
        //Gold of Field should be 1
        Assert.assertEquals(1,  map.getField("99").getGold());
    }

    @Test
    public void testRemoveContentEmptyField() {
        //Content null
        Assert.assertNull(map.getField("99").getContent());
        //try removing content
        map.removeContent("99");
        //Content null
        Assert.assertNull(map.getField("99").getContent());
    }
    @Test
    public void testRemoveContentInvalidId() {
        //Should throw no exception and just return;
        map.removeContent("200");
    }


    @Test
    public void testExistingNeighborsListMiddleOfMap() {
        List<String> correct = new ArrayList<>();
        //adding existing Neighbors
        correct.add("79");
        correct.add("96");
        correct.add("98");
        correct.add("114");
        correct.add("115");
        correct.add("116");
        List<String> toCheck = map.existingNeighborsList("97");
        Assert.assertTrue(correct.size() == toCheck.size()
                && toCheck.containsAll(correct));
    }


    @Test
    public void testExistingNeighborsListBorderOfMap() {
        List<String> correct = new ArrayList<>();
        correct.add("50");
        correct.add("51");
        //id 33 is on the border of the map
        List<String> toCheck = map.existingNeighborsList("33");
        Assert.assertEquals(correct, toCheck);
    }

    @Test
    public void testExistingNeighborsListNotExisting() {
        //returned List should be empty
        List<String> correct = new ArrayList<>();
        //id 200 is too big
        List<String> toCheck = map.existingNeighborsList("200");
        Assert.assertEquals(correct, toCheck);
    }

    @Test
    public void testBorderFieldValidIds() {
        String initial = "98";
        map.setPlayer(initial, "1");
        map.setPlayer("80", "1");

        //right next to initial field
        Assert.assertTrue(map.borderField(initial, "97"));

        //Distance 2 but next to owned 80
        Assert.assertTrue(map.borderField(initial, "61"));

        //Distance 2 but not next to owned 80
        Assert.assertFalse(map.borderField(initial, "115"));

        //on the other side of the map
        Assert.assertFalse(map.borderField(initial, "31"));
    }

    @Test
    public void testBorderFieldInitialInvalid() {
        //id to low
        String initial_1 = "-1";
        //id too high
        String initial_2 = "200";
        //Initial too low, destination valid
        Assert.assertFalse(map.borderField(initial_1, "1"));
        //Initial too high, destination valid
        Assert.assertFalse(map.borderField(initial_2, "179"));
    }

    @Test
    public void testBorderFieldDestinationInvalid() {
        //id to low
        String destination_1  = "-5";
        //id too high
        String destination_2 = "210";
        //Initial valid, destination too low
        Assert.assertFalse(map.borderField("42", destination_1));
        //Initial valid, destination too high
        Assert.assertFalse(map.borderField("179", destination_2));
    }

    @Test
    public void testIsAttackableTowerBorderLv1ToEnemy() {
        String current = "42";
        String enemy = "27";
        //Level 1 Unit for testing purposes
        map.addObject(current, new Unit());
        map.getField(current).setPlayer("1");

        //Placed enemy tower on Field
        map.getField(enemy).setPlayer("Enemy");
        map.addObject(enemy, new Building("tower"));

        //move level 1 Unit next to enemy Tower
        //where Id 44 borders enemy tower
        Assert.assertFalse(map.isAttackable(current, "44"));


    }

    @Test
    public void testIsAttackableTowerBorderLv1ToOwned() {
        String current = "42";
        String owned = "119";
        //Level 1 Unit for testing purposes
        map.addObject(current, new Unit());
        map.getField(current).setPlayer("1");

        //Placed friendly tower on Field
        map.getField(owned).setPlayer("1");
        map.addObject(owned, new Building("tower"));

        //Level 1 Unit next to friendly Tower
        //where Id 101 borders owned tower
        Assert.assertTrue(map.isAttackable(current, "101" ));

    }

    @Test
    public void testIsAttackableTowerBorderLv2ToEnemy() {
        String current = "42";
        String enemy = "27";
        //Level 2 Unit for testing purposes
        map.addObject(current, new Unit());
        map.addObject(current, new Unit());
        map.getField(current).setPlayer("1");

        //Placed enemy tower on Field
        map.getField(enemy).setPlayer("Enemy");
        map.addObject(enemy, new Building("tower"));

        //move level 2 Unit next to enemy Tower
        //where Id 44 borders enemy tower
        Assert.assertTrue(map.isAttackable(current, "44"));

    }

    @Test
    public void testIsAttackableTowerBorderLv2ToOwned() {
        String current = "42";
        String owned = "119";
        //Level 2 Unit for testing purposes
        map.addObject(current, new Unit());
        map.addObject(current, new Unit());
        map.getField(current).setPlayer("1");

        //Placed friendly tower on Field
        map.getField(owned).setPlayer("1");
        map.addObject(owned, new Building("tower"));

        //Level 2 Unit next to friendly Tower
        //where Id 101 borders owned tower
        Assert.assertTrue(map.isAttackable(current, "101" ));

    }


    @Test
    public void testIsAttackableNoTowerEmptyNeutral() {
        String current = "42";
        String neutral = "99";
        //create Level 1 Unit for testing purposes
        map.getField(current).setPlayer("1");
        map.addObject(current, new Unit());

        //empty neutral field
        Assert.assertTrue(map.isAttackable(current, neutral));

    }

    @Test
    public void testIsAttackableNoTowerUnitOwned() {
        String current = "42";
        String owned = "23";
        //create Level 1 Unit for testing purposes
        map.getField(current).setPlayer("1");
        map.addObject(current, new Unit());

        //owned field with unit
        map.getField(owned).setPlayer("1");
        map.addObject(owned, new Unit());
        Assert.assertFalse(map.isAttackable(current, owned));
    }

    @Test
    public void testIsAttackableNoTowerEmptyEnemy() {
        String current = "42";
        String enemy = "99";
        //create Level 1 Unit for testing purposes
        map.getField(current).setPlayer("1");
        map.addObject(current, new Unit());

        //empty enemy field
        map.getField(enemy).setPlayer("Enemy");
        Assert.assertTrue(map.isAttackable(current, enemy));


    }

    @Test
    public void testIsAttackableNoTowerUnitEnemy() {
        String current = "42";
        String enemy = "99";
        //create Level 1 Unit for testing purposes
        map.getField(current).setPlayer("1");
        map.addObject(current, new Unit());

        //enemy field level with 1 unit
        map.getField(enemy).setPlayer("Enemy");
        map.addObject(enemy, new Unit());
        Assert.assertFalse(map.isAttackable(current, enemy));

    }

    @Test
    public void testIsAttackableNoTowerUnitEnemyVsLv2Unit() {
        String current = "42";
        String enemy = "99";
        //create Level 2 Unit for testing purposes
        map.getField(current).setPlayer("1");
        map.addObject(current, new Unit());
        map.addObject(current, new Unit());

        //create Level 1 enemy Unit
        map.getField(enemy).setPlayer("Enemy");
        map.addObject(enemy, new Unit());


        //enemy field level 1 vs level 2 unit
        Assert.assertTrue(map.isAttackable(current, enemy));
    }

    @Test
    public void testIsAttackableInvalidId() {
        String current = "42";
        String invalid = "9834";

        map.getField(current).setPlayer("1");
        map.addObject(current, new Unit());

        Assert.assertFalse(map.isAttackable(current, invalid));
    }

    @Test
    public void testIsAttackableNotExistingField() {
        String current = "42";
        String notExisting = "1";

        map.getField(current).setPlayer("1");
        map.addObject(current, new Unit());

        Assert.assertFalse(map.isAttackable(current, notExisting));
    }


    @Test
    public void testFieldsInRangeMiddle() {
        //Id in the middle of the map.
        String middleId = "99";
        String[] expectedIds = {"63", "80", "79", "81", "82", "83", "97", "98"
                , "100", "101", "116", "117", "118", "115", "119", "134", "136", "135"};
        //Create expectedList from expected Ids.
        List<String> expectedList = new ArrayList<>(Arrays.asList(expectedIds));
        //List from method fieldsInRange.
        List<String> actualList = map.fieldsInRange(middleId);

        Assert.assertTrue(expectedList.size() == actualList.size() &&
                actualList.containsAll(expectedList));
    }

    @Test
    public void testFieldsInRangeBorder() {
        //Id on the border of the map.
        String borderId = "177";
        String[] expectedIds = {"159", "158", "176", "157", "175"};
        //Create expectedList from expected Ids.
        List<String> expectedList = new ArrayList<>(Arrays.asList(expectedIds));
        //List from method fieldsInRange.
        List<String> actualList = map.fieldsInRange(borderId);

        Assert.assertTrue(expectedList.size() == actualList.size() &&
                actualList.containsAll(expectedList));

    }

    @Test
    public void testFieldsInRangeIdOutOfRange() {
        //Id is > 179 (has to be in Range 0 - 179).
        String outOfRangeId = "200";
        List<String> outOfRange = map.fieldsInRange(outOfRangeId);
        //returned List is expected to be empty.
        boolean check = outOfRange.isEmpty();
        Assert.assertTrue(check);

    }




    @Test
    public void testIsFullDefault() {
        //Map shouldn't be full in the beginning.
        Assert.assertFalse(map.isFull());
        //Place Unit on every field which has no content.
        for (int i = 0; i < 180; i++) {
            Field f = map.getField(Integer.toString(i));
            if (f.getContent() == null && f.exists()) {
                f.addContent(new Unit());
            }
        }
        //Map should be full now
        Assert.assertTrue(map.isFull());
    }

    @Test
    public void testIsFullWhenFull() {
        //Place Unit on every field which has no content.
        for (int i = 0; i < 180; i++) {
            Field f = map.getField(Integer.toString(i));
            if (f.getContent() == null && f.exists()) {
                f.addContent(new Unit());
            }
        }
        //Map should be full now
        Assert.assertTrue(map.isFull());
    }

    @Test
    public void testIsFullNoExistingFields() {
        String emptyMap = "00000000000000000000000000000000000000000000000000000"
                + "000000000000000000000000000000000000000000000000000000000000000000"
                + "0000000000000000000000000000000000000000000000000000000000000";
        //create map where no Fields exist
        Map mapNotExisting = new Map(emptyMap);
        //should be true, because no Fields exist;
        Assert.assertTrue(mapNotExisting.isFull());

    }
}
