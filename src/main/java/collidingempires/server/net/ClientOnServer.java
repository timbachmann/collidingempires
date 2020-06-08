package collidingempires.server.net;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.prometheus.client.Summary;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


/**
 * This class is used as a Thread and manages the connection to one Client.
 * It holds the functionality to send and react to orders via the connection.
 */
public class ClientOnServer {

    private Socket connection;
    private String nickname;
    private BufferedWriter sendBuffer;
    private BufferedReader inputBuffer;
    private static final Logger LOGGER =
            LogManager.getLogger(ClientOnServer.class.getName());
    private static final Summary RECEIVED_BYTES = Summary.build()
            .name("requests_size_bytes").help("Request size in bytes").register();

    /**
     * Constructor: sets the fields and create crucial objects.
     *
     * @param connection The connectonsocket for the client
     * @param nickname   the unique nickname
     */
    public ClientOnServer(Socket connection, String nickname) {
        this.connection = connection;
        this.nickname = nickname;
        try {
            InputStream in = connection.getInputStream();
            OutputStream out = connection.getOutputStream();
            this.sendBuffer = new BufferedWriter(new OutputStreamWriter(
                    out, StandardCharsets.UTF_8));
            this.inputBuffer = new BufferedReader(new InputStreamReader(
                    in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            System.out.println(e.getMessage());
        }
    }

    /**
     * Packs a protocolsequence(String) to the send_buffer and flushes it.
     *
     * @param order The String to be added to the send_buffer
     */
    public void send(final String[] order) {
        try {
            String pack = Packager.pack(order);
            this.sendBuffer.write(pack);
            this.sendBuffer.newLine();
            this.sendBuffer.flush();
            System.out.println(this.nickname + " sent: " + pack);
        } catch (IOException e) {
            System.out.println("failed to send to " + nickname);
        }
    }

    /**
     * Reads the next line of the Inputbuffer unpacks and returns it.
     *
     * @return next line of the inputbuffer;
     * @throws IOException thrown if the connection is closed
     */
    public String[] receive() throws IOException {
        String order = inputBuffer.readLine();
        if (order == null) {
            throw new IOException("Connection lost!! - " + getNickname());
        } else {
            System.out.println(getNickname() + " got: "
                    + order);
            LOGGER.info("Executor of " + getNickname() + " got: "
                    + order);
            RECEIVED_BYTES.observe(order.getBytes().length);
            return Packager.unpack(order, "server");
        }
    }

    /**
     * Causes this Thread to Stop by setting the running flag to false.
     *
     * @throws IOException thrown if not closed properly
     */
    public void stop() throws IOException {
        sendBuffer.flush();
        sendBuffer.close();
        inputBuffer.close();
        connection.close();
    }

    /**
     * getter for field nickname.
     *
     * @return current value of the field nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * setter for the field nickname.
     *
     * @param nname the new nickname
     */
    public void setNickname(final String nname) {
        this.nickname = nname;
    }
}
