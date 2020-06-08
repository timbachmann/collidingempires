package collidingempires.server.net;

/**
 * The Server protocol for Colliding Empires.
 * <p>
 * A more detailed Documentation can be found in the following git
 * repository:
 * </p>
 * See and look at Projektunterlagen/Netzwerkdokument/Netplan.pdf:
 * <a href="https://git.scicore.unibas.ch/CS108-FS20/gruppe-12/">Repo</a>
 */
public enum ServerProtocol {
    /* ServerView */
    /**
     * ClientWantstoJoinLobby:
     * <p>
     * Arguments: (lobbyname)
     * </p>
     * Updates data, if joining is possible, else it sends back an errormessage.
     */
    CWJL(1),
    /**
     * ClientWantstoCreateLobby:
     * <p>
     * Arguments: (lobbyname)
     * </p>
     * Updates data, if creation is possible, else it sends back an errormessage.
     */
    CWCL(1),
    /**
     * ClientWantstoLeaveServer:
     * <p>
     * Arguments: (none)
     * </p>
     * Updates/Deletes data and imforms all other clients, closes socket to said client.
     */
    CWLS(0),
    /**
     * ClientWantsToSendText:
     * <p>
     * Arguments: (@username/server/lobby,message)
     * </p>
     * Updates data and informs clients, if sending is possible,
     * else sends back errormessage.
     */
    CWST(2),
    /**
     * ClientWantsToChangeNickname:
     * <p>
     * Arguments: (newNickname)
     * </p>
     * Updates data and informs clients, if change is possible,
     * else sends back errormessage.
     */
    CWCN(1),

    /* Lobby */
    /**
     * ClientWantstoToggleReady:
     * <p>
     * Arguments: (none)
     * </p>
     * Changes status of client and informs all clients in said lobby
     */
    CWTR(0),
    /**
     * ClientWantstoLeaveLobby:
     * <p>
     * Arguments: (none)
     * </p>
     * Updates data and informs clients, if leaving is possible,
     * else sends back errormessage.
     */
    CWLL(0),
    /* Game */
    /**
     * ClientWantstoFinishTurn:
     * <p>
     * Arguments: (none)
     * </p>
     * Updates data and inform clients about the flow of the game
     */
    CWFT(0),
    /**
     * ClientWantstoRequestBuy:
     * <p>
     * Arguments: (knight/tower)
     * </p>
     * Request buy item.
     */
    CWRB(1),
    /**
     * ClientWantstoRequestMove:
     * <p>
     * Arguments: (field)
     * </p>
     * Request move knight.
     */
    CWRM(1),
    /**
     * ClientWantsToPlaceItem:
     * <p>
     * Arguments: (knight/tower, field)
     * </p>
     * Place item if field is empty and upgrade otherwise.
     */
    CWPI(2),
    /**
     * ClientWantsToMove:
     * <p>
     * Arguments: (from, to)
     * </p>
     * Move knight from Field to another.
     */
    CWTM(2),
    /**
     * ClientWantsToCheat:
     * <p>
     * Arguments: (none)
     * </p>
     * Execute cheat code.
     */
    CWTC(1),
    /**
     * ClientWantsToSpectate:
     * <p>
     * Arguments: (lobby)
     * </p>
     * Activates spectator mode for given lobby.
     */
    CWTS(1);

    private final int numArgs;

    /**
     * Sets the number of arguments required.
     *
     * @param numArgs Number of arguments
     */
    ServerProtocol(int numArgs) {
        this.numArgs = numArgs;
    }

    /**
     * Getter for the number of arguments.
     *
     * @return the variable numArgs
     */

    public int getNumArgs() {
        return this.numArgs;
    }
}
