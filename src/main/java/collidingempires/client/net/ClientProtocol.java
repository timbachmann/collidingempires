package collidingempires.client.net;

/**
 * The Client protocol for Colliding Empires.
 * <p>
 * A more detailed Documentation can be found in the following git
 * repository:
 * </p>
 * See and look at Projektunterlagen/Netzwerkdokument/Netplan.pdf:
 * <a href="https://git.scicore.unibas.ch/CS108-FS20/gruppe-12/">Repo</a>
 */
public enum ClientProtocol {
    /* ServerView */
    /**
     * MenuInServerView:
     * <p>
     * Arguments: (nickname)
     * </p>
     * Let's client know he successfully connected to the Server and the GUI is changed.
     * accordingly
     */
    MISV(1),
    /**
     * TextReceive:
     * <p>
     * Arguments:(@username/server/lobby, sender, message)
     * </p>
     * Displays the received Text to chat window.
     */
    TXTR(3),
    /**
     * NewClientOnServer:
     * <p>
     * Arguments: (nickname)
     * </p>
     * Updates client list by adding Client who joined the Server.
     */
    NCON(1),
    /**
     * ClientLeftServer:
     * <p>
     * Arguments: (nickname)
     * </p>
     * Updates client list by removing Client who disconnected.
     */
    CLSV(1),
    /**
     * ClientChangedNickName:
     * <p>
     * Arguments: (oldnickname, newnickname)
     * </p>
     * Updates nickname in client list.
     */
    CCNN(2),
    /**
     * ChangeHighScorelistEntry:
     * <p>
     * Arguments: (nickname, score)
     * </p>
     * Updates existing entry in the highscore list by adding 1 win.
     */
    CHSE(2),
    /**
     * NewFinishedGame:
     * <p>
     * Arguments: (lobbyname, nickname of winner)
     * </p>
     * Updates the list of past games.
     */
    NFGA(2),
    /**
     * NewLobbyOnServer:
     * <p>
     * Arguments: (lobbyname, number of Players in the lobby)
     * </p>
     * Adds the new lobby to the lobby list.
     */
    NLOS(2),
    /**
     * ALobbyDeletedonServer:
     * <p>
     * Arguments: (lobbyname)
     * </p>
     * Removes the Lobby for the lobby list.
     */
    ALDS(1),
    /**
     * ALobbyChangedStatus:
     * <p>
     * Arguments: (lobby, inGame, numberofplayers)
     * </p>
     * Updates the lobby information.
     */
    ALCS(3),
    /**
     * TextChatMessageStatus:
     * <p>
     * Arguments: (success/fail)
     * </p>
     * Updates chat window if sending was successful (true),
     * else client notifies user (false).
     */
    TCMS(1),
    /**
     * FailedToJoinLobby:
     * <p>
     * Arguments: -
     * </p>
     * Notifies the client that joining a Lobby failed.
     */
    FTJL(0),
    /* Lobby */
    /**
     * AClientLefttheLobby:
     * <p>
     * Arguments: (nickname)
     * </p>
     * Updates the client list of the lobby.
     */
    ACLL(1),
    /**
     * AClientJoinedtheLobby:
     * <p>
     * Arguments: (nickname)
     * </p>
     * Updates the client list of the lobby.
     */
    ACJL(1),
    /**
     * AClientChangeditsStatus:
     * <p>
     * Arguments: (nickname)
     * </p>
     * Updates ready status of a client in the lobby.
     */
    ACCS(1),
    /**
     * YouAreInaLobby:
     * <p>
     * Arguments: Lobbyname
     * </p>
     * Client prepares for receiving data.
     */
    YAIL(1),
    /**
     * TheGameStarts:
     * <p>
     * Arguments: (none)
     * </p>
     * Notifies the the user that the game starts
     * soon and waits for inGame message of Server.
     */
    TGST(0),
    /**
     * YouLeftTheLobby:
     * <p>
     * Arguments: (none)
     * </p>
     * Verifies that user successfully left lobby.
     */
    YLTL(0),
    /* Game */
    /**
     * NewMap:
     * <p>
     * Arguments: (sequence which fields are usable)
     * </p>
     * Updates data of current game.
     */
    NEMA(1),
    /**
     * FieldChangesOwner:
     * <p>
     * Arguments: (arrayCoordinates, username)
     * </p>
     * Updates data and GUI of current game.
     */
    FCOW(2),
    /**
     * NewObjectOnMap:
     * <p>
     * Arguments: (arrayCoordinates, building/soldier/tree, lvl)
     * </p>
     * Updates data and GUI of current game.
     */
    NOOM(3),
    /**
     * ObjectOnMapRemoved:
     * <p>
     * Arguments: (arrayCoordinates)
     * </p>
     * Updates data and GUI of current game.
     */
    OOMR(1),
    /**
     * YourCoinBalanceChanged:
     * <p>
     * Arguments: (newBalance)
     * </p>
     * Updates the coin balance of receiver.
     */
    YCBC(1),
    /**
     * BalanceChangeNextRound.
     * <p>
     * Arguments: (BalanceChange)
     * </p>
     */
    BCNR(1),
    /**
     * NewPlayerOnTurn:
     * <p>
     * Arguments: (nickname)
     * </p>
     * Notifies user whose turn it is.
     */
    NPOT(1),
    /**
     * GameEND:
     * <p>
     * Arguments: (nickname (Winner))
     * </p>
     * Notifies user which player won and waits for the return to lobby command.
     */
    GEND(1),
    /**
     * LeftTimeOfTurn:
     * <p>
     * Arguments: (time)
     * </p>
     * Timer on the clients side get synchronized with remaining Time for turn.
     */
    LTOT(1),
    /**
     * PossibleFieldsOfRequest:
     * <p>
     * Arguments: (fields)
     * </p>
     * Notifies the user why the desired turn is not valid.
     */
    PFOR(1),
    /**
     * YouAreSpectating:
     * <p>
     * Arguments: (none)
     * </p>
     * Notifies the user that he's spectating.
     */
    YASP(0);

    /**
     * Number of arguments.
     */
    private final int numArgs;

    /**
     * Sets the number of arguments required.
     *
     * @param numArgs Number of arguments
     */
    ClientProtocol(int numArgs) {
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
