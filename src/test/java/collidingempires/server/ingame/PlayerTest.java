package collidingempires.server.ingame;


import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

public class PlayerTest {

    private static String[] players2 = {"1", "2"};
    private Game game;
    private Player player;
    private String mapString = "00000011010000000000001111110011010000001111101111110011"
            + "0111111111111000111111111111111110111111111111111100011111111"
            + "111110000111111111111110000111111111111111100001000111111111100";


    @Before
    public void setUp() throws Exception {
        //creates a new Game with 2 Players and a given Map Layout.
        //where starting Fields are already assigned
        game = new Game(players2, mapString);
        player = game.getPlayer("1");
    }



    @Test
    public void getGoldPerRoundStart() {
        //every Player starts with 7 Fields so GPR should be 7
        Assert.assertEquals(7, player.getGoldPerRound());
    }



    @Test
    public void getGoldPerRoundFarm() {
        //Starting GPR is 7
        //Farms should add an additional 3 GPR
        game.buyContainer("40", new Building("farm"));
        Assert.assertEquals(10, player.getGoldPerRound());
    }

    @Test
    public void getGoldPerRoundAddUnitsLv1() {
        //Starting GPR is 7
        //Level 1 Unit has a cost of -1
        int expected = 7 - 1;
        //placing Level 1 Unit
        game.buyContainer("40", new Unit());
        Assert.assertEquals(expected, player.getGoldPerRound());

    }

    @Test
    public void getGoldPerRoundAddUnitsLv2() {
        //Starting GPR is 7
        //Level 2 Unit has a cost of -5
        int expected = 7 - 5;
        //placing Level 2 Unit
        game.buyContainer("40", new Unit());
        game.buyContainer("40", new Unit());

        Assert.assertEquals(expected, player.getGoldPerRound());

    }

    @Test
    public void getGoldPerRoundAddUnitsLv3() {
        //Starting GPR is 7
        //Level 3 Unit has a cost of -10
        int expected = 7 - 10;
        //placing Level 3 Unit
        game.buyContainer("40", new Unit());
        game.buyContainer("40", new Unit());
        game.buyContainer("40", new Unit());
        Assert.assertEquals(expected, player.getGoldPerRound());

    }

    @Test
    public void getGoldPerRoundAddUnitsLv4() {
        //Starting GPR is 7
        //Level 4 Unit has a cost of -20
        int expected = 7 - 20;
        //placing Level 4 Unit
        game.buyContainer("40", new Unit());
        game.buyContainer("40", new Unit());
        game.buyContainer("40", new Unit());
        game.buyContainer("40", new Unit());
        Assert.assertEquals(expected, player.getGoldPerRound());

    }


    @Test
    public void getGoldPerRoundUnitsRemoved() {
        //Starting GPR is 7
        //placing Level 1 Unit
        game.buyContainer("40", new Unit());
        //GRP should be 6
        Assert.assertEquals(6, player.getGoldPerRound());
        //removing Unit
        game.getWorldObject().removeContent("40");
        //Since Unit is gone GRP should be back to 7
        Assert.assertEquals(7, player.getGoldPerRound());

    }

}
