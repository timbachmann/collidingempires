package collidingempires.server.ingame;

import collidingempires.server.ClientManager;
import collidingempires.server.LobbyManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import java.util.ArrayList;
import java.util.List;

public class GameTest {

    private static String[] players2 = {"1", "2"};
    private Game game;
    private String mapString = "00000011010000000000001111110011010000001111101111110011"
            + "0111111111111000111111111111111110111111111111111100011111111"
            + "111110000111111111111110000111111111111111100001000111111111100";


    @Before
    public void setUp() throws Exception {
        //creates a new Game with 2 Players and a given Map Layout.
        game = new Game(players2, mapString);
    }

    @Test
    public void testGetTurnCounter() {
        //Turn counter should be initialized with 0;
        Assert.assertEquals(0, game.getTurnCounter());
        for (int i = 0; i < 20; i++) {
            //turn counter should increase by 1 every nextTurn.
            Assert.assertEquals(i, game.getTurnCounter());
            game.nextTurn();
        }
    }

    @Test
    public void testGetWorld() {
        //checks if the world String is generated correctly.
        Assert.assertEquals(mapString, game.getWorld());
    }

    @Test
    public void testNextTurn() {
        String[] players = {"1", "2", "3", "4"};
        Game game1 = new Game(players);
        game.playerLeft("2",new LobbyManager(new ClientManager()),"lobby");

        game1.nextTurn();
        Assert.assertEquals("2",game1.getPlayerActive().getNick());
        game.nextTurn();
        Assert.assertEquals("1",game.getPlayerActive().getNick());
    }


    @Test
    public void testGetPlayers() {
        List<Player> expectedPlayers = new ArrayList<>();
        expectedPlayers.add(game.getPlayer("1"));
        expectedPlayers.add(game.getPlayer("2"));
        Assert.assertEquals(expectedPlayers, game.getPlayers());
    }

    @Test
    public void testGetNextPlayerTwoPlayers() {
        Assert.assertEquals(game.getPlayer("2"), game.getNextPlayer());
        game.nextTurn();
        Assert.assertEquals(game.getPlayer("1"), game.getNextPlayer());
    }

    @Test
    public void testGetNextPlayerOnePlayer() {
        //removing one Player with nick 2
        game.playerLeft("2",new LobbyManager(new ClientManager()),"lobby");
        //ActivePlayer is still "1"
        Assert.assertEquals(game.getPlayer("1"), game.getPlayerActive());
        //NextPlayer should still be "1"
        Assert.assertEquals(game.getPlayer("1"), game.getNextPlayer());
    }

    @Test
    public void testGetStartTower() {
        Assert.assertEquals(game.getStartTower("1"), "41");
    }

    @Test
    public void testGetOwned() {
        String[] expected = {"41", "23", "40", "42", "58", "59", "60"};
        String[] actual = game.getOwned("1");
        Assert.assertArrayEquals(expected, actual);
    }


    @Test
    public void testIsWon() {
        Assert.assertNull(game.isWon());
        game.playerLeft("1",new LobbyManager(new ClientManager()), "lobby");
        Assert.assertEquals("2", game.isWon());
    }

    @Test
    public void testIsOwnedValidIdsOwned() {
        Assert.assertTrue(game.isOwned("40", "1"));
    }

    @Test
    public void testIsOwnedValidIdsNotOwned() {
        Assert.assertFalse(game.isOwned("50", "1"));
    }

    @Test
    public void testIsOwnedInvalidIds() {
        Assert.assertFalse(game.isOwned("398","1"));

    }

    @Test
    public void testGetBalanceExistingPlayer() {
        Assert.assertEquals("100", game.getBalance("1"));
    }

    @Test
    public void testGetBalanceNotExistingPlayer() {
        Assert.assertEquals("0", game.getBalance("*"));
    }

    @Test
    public void testGetBalanceChangeExistingPlayer() {
        Assert.assertEquals("7", game.getBalanceChange("1"));
    }

    @Test
    public void testGetBalanceChangeNotExistingPlayer() {
        Assert.assertEquals("0", game.getBalance("*"));
    }

    @Test
    public void testPossibleFieldsPlaceBuildingNoOwnedBuildings() {
        Container farm = new Building("farm");
        String expected = "0000000000000000000000010000000000000000101000000000000000111"
                + "000000000000000000000000000000000000000000000000000000000000000000"
                + "00000000000000000000000000000000000000000000000000000";
        String actual = game.possibleFieldsPlace(farm);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testPossibleFieldsPlaceBuildingOwnedBuildings() {
        //Possible Fields to Place a building, when you already have a building
        Container farm = new Building("farm");
        String expected = "000000000000000000000001000000000000000010100000000000000010"
                + "1000000000000000000000000000000000000000000000000000000000000000000"
                + "00000000000000000000000000000000000000000000000000000";
        game.buyContainer("59", farm);
        String actual = game.possibleFieldsPlace(farm);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testPossibleFieldsPlaceUnitWithNoUnit() {
        Container unit = new Unit();
        String expected = "0000000000000000000000010000000000000000101000000000000000"
                + "1110000000000000000000000000000000000000000000000000000000000000"
                + "0000000000000000000000000000000000000000000000000000000000";
        String actual = game.possibleFieldsPlace(unit);
        Assert.assertEquals(expected, actual);



    }

    @Test
    public void testPossibleFieldsPlaceUnitWithLv1Unit() {
        Container unit = new Unit();
        String expected = "0000000000000000000000010000000000000000101000000000000000"
                + "1110000000000000000000000000000000000000000000000000000000000000"
                + "0000000000000000000000000000000000000000000000000000000000";
        //Create Level 1 Unit
        game.buyContainer("59", unit);
        String actual = game.possibleFieldsPlace(unit);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testPossibleFieldsPlaceUnitWithLv2Unit() {
        Container unit = new Unit();
        String expected = "0000000000000000000000010000000000000000101000000000000000"
                + "1110000000000000000000000000000000000000000000000000000000000000"
                + "0000000000000000000000000000000000000000000000000000000000";
        //Create Level 2 Unit
        game.buyContainer("59", unit);
        game.buyContainer("59", unit);
        String actual = game.possibleFieldsPlace(unit);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testPossibleFieldsPlaceUnitWithLv3Unit() {
        Container unit = new Unit();
        String expected = "0000000000000000000000010000000000000000101000000000000000"
                + "1110000000000000000000000000000000000000000000000000000000000000"
                + "0000000000000000000000000000000000000000000000000000000000";
        //Create Level 3 Unit
        game.buyContainer("59", unit);
        game.buyContainer("59", unit);
        game.buyContainer("59", unit);
        String actual = game.possibleFieldsPlace(unit);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testPossibleFieldsPlaceUnitWithLv4Unit() {
        Container unit = new Unit();
        String expected = "0000000000000000000000010000000000000000101000000000000000"
                + "1010000000000000000000000000000000000000000000000000000000000000"
                + "0000000000000000000000000000000000000000000000000000000000";
        //create Level 4 Unit
        game.buyContainer("59", unit);
        game.buyContainer("59", unit);
        game.buyContainer("59", unit);
        game.buyContainer("59", unit);
        String actual = game.possibleFieldsPlace(unit);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testPossibleFieldsMove() {
        Container unit = new Unit();
        game.buyContainer("40", unit);
        String expected = "000000000000000000000011100000000000000001100000000000"
                + "00011110000000000000001000000000000000000000000000000000000000"
                + "0000000000000000000000000000000000000000000000000000000000000000";
        String actual = game.possibleFieldsMove("40");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testPossibleFieldsMoveInvalidId() {
        String expected = "00000000000000000000000000000000000000000000000000000"
                + "000000000000000000000000000000000000000000000000000000000000000000"
                + "0000000000000000000000000000000000000000000000000000000000000";
        String actual = game.possibleFieldsMove("938");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testPossibleFieldsMoveNoUnit() {
        String expected = "00000000000000000000000000000000000000000000000000000"
                + "000000000000000000000000000000000000000000000000000000000000000000"
                + "0000000000000000000000000000000000000000000000000000000000000";
        String actual = game.possibleFieldsMove("40");
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testIsPossibleMoveNotExistingField() {
        game.buyContainer("40", new Unit());
        Assert.assertFalse(game.isPossibleMove("40", "21"));
    }

    @Test
    public void testIsPossibleMovePossible() {
        game.buyContainer("40", new Unit());
        Assert.assertTrue(game.isPossibleMove("40", "24"));
    }

    @Test
    public void testIsPossibleMoveInvalidId() {
        game.buyContainer("40", new Unit());
        Assert.assertFalse(game.isPossibleMove("40", "099"));
    }

    @Test
    public void testGetPlayerNotExisting() {
        Assert.assertNull(game.getPlayer("*"));
    }

    @Test
    public void testGetPlayerExisting() {
        Assert.assertNotNull(game.getPlayer("1"));
    }

    @Test
    public void testBuyContainerFarm() {
        Container farm = new Building("farm");
        String owner =  game.getWorldObject().getField("40").getPlayer();
        //Balance of Player starts off at 100
        game.buyContainer("40", farm);
        //Check gold balance
        Assert.assertEquals(88, game.getPlayer(owner).getGold());
        //Buy a second farm, where the price should be 2 higher
        game.buyContainer("59", farm);
        //Check gold balance
        Assert.assertEquals(74, game.getPlayer(owner).getGold());
    }

    @Test
    public void testBuyContainerFarmIncrement() {
        Container farm = new Building("farm");
        String owner =  game.getWorldObject().getField("40").getPlayer();
        //Number of farms for owner should be 0
        Assert.assertEquals(0, game.getPlayer(owner).getFarms());
        game.buyContainer("40", farm);
        //Number of farms for owner should have incremented
        Assert.assertEquals(1, game.getPlayer(owner).getFarms());
    }



    @Test
    public void testBuyContainerKnight() {
        Container unit = new Unit();
        String owner =  game.getWorldObject().getField("40").getPlayer();
        //Check balance of owner
        Assert.assertEquals(100, game.getPlayer(owner).getGold());
        game.buyContainer("40", unit);
        //Check gold balance
        Assert.assertEquals(90, game.getPlayer(owner).getGold());
    }

    @Test
    public void testBuyContainerBuilding() {
        Container tower = new Building("tower");
        String owner =  game.getWorldObject().getField("40").getPlayer();
        //Check balance of owner
        Assert.assertEquals(100, game.getPlayer(owner).getGold());
        game.buyContainer("40", tower);
        //Check gold balance
        Assert.assertEquals(85, game.getPlayer(owner).getGold());

    }

    @Test
    public void testBuyContainerInvalidId() {
        Container unit = new Unit();
        String owner =  game.getWorldObject().getField("40").getPlayer();
        //Check balance of owner
        Assert.assertEquals(100, game.getPlayer(owner).getGold());
        //buy containers on invalid ids
        game.buyContainer("INVALID", unit);
        game.buyContainer("9890", unit);
        //Balance should have stayed the same
        Assert.assertEquals(100, game.getPlayer(owner).getGold());
    }

    @Test
    public void testMoveUnitToEmptyField() {
        //Creating Unit and Placing it on Field with Id 40
        Container container = new Unit();
        game.buyContainer("40", container);
        //Move Unit from Id 40 to Id 79
        game.moveUnit("79", "40");
        //Does the unit move
        Assert.assertEquals(container, game.getWorldObject().getField("79").getContent());
        //Does the field change its owner
        Assert.assertEquals("1", game.getWorldObject().getField("79").getPlayer());
        Assert.assertNull(game.getWorldObject().getField("40").getContent());
    }

    @Test
    public void testMoveUnitToFieldWithBuilding() {
        //Creating Unit and Placing it on Field with Id 40
        Container unit = new Unit();
        game.buyContainer("40", unit);
        //Creating Farm and placing it on Field with Id 50
        Container farm = new Building("farm");
        game.buyContainer("50", farm);
        //move Unit from Id 40 to Id 50
        game.moveUnit("50", "40");
        //Unit should now be on Field 50
        Assert.assertEquals(unit, game.getWorldObject().getField("50").getContent());
        //Content of Field 40 should now be null
        Assert.assertNull(game.getWorldObject().getField("40").getContent());

    }
    @Test
    public void testMoveUnitFarmDecrement() {
        //Creating Unit and Placing it on Field with Id 40
        Container unit = new Unit();
        game.buyContainer("40", unit);
        //Creating Farm and placing it on Field with Id 60
        Container farm = new Building("farm");
        game.buyContainer("60", farm);
        //Check farm counter
        String owner = game.getWorldObject().getField("60").getPlayer();
        Assert.assertEquals(1, game.getPlayer(owner).getFarms());
        //move Unit from Id 40 to Id 60
        game.moveUnit("60", "40");
        //Farm counter should have decremented
        Assert.assertEquals(0, game.getPlayer(owner).getFarms());
    }

    @Test
    public void testMoveUnitToFieldWithUnit() {
        //Creating Unit and Placing it on Field with Id 40
        Container unit = new Unit();
        game.buyContainer("40", unit);
        //Level Unit up to level 2
        game.buyContainer("40", unit);
        //Placing a Unit on Field with Id 50
        game.buyContainer("50", unit);
        Container Unit40 = game.getWorldObject().getField("40").getContent();
        //move Unit from Id 40 to 50
        game.moveUnit("50", "40");
        //Check if Unit from 40 is now on Field 50
        Assert.assertEquals(Unit40, game.getWorldObject().getField("50").getContent());
        //Field 40 should have no content
        Assert.assertNull(game.getWorldObject().getField("40").getContent());

    }

    @Test
    public void testMoveUnitToImpossibleField() {
        //Creating Unit and Placing it on Field with Id 40
        Container unit = new Unit();
        game.buyContainer("40", unit);
        //Try to move to Invalid Id
        game.moveUnit("199", "40");
        //Unit should stay on Id 40
        Assert.assertEquals(unit, game.getWorldObject().getField("40").getContent());
    }

    @Test
    public void testMoveUnitNoUnitToMove() {
        //should just return and throw no exception
        game.moveUnit("50", "41");
        //Fields with Ids 50 and 41 should no content
        Assert.assertNull(game.getWorldObject().getField("50").getContent());
        Assert.assertNull(game.getWorldObject().getField("40").getContent());
    }

}
