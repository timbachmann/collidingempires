package collidingempires.server;

import collidingempires.server.net.ClientOnServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server Thread.
 * Waits for new connections and adds them into the ecosystem.
 */
public class Server extends Thread {
  private boolean shutdownRequested;
  private final int connectionPort;
  private final ClientManager clients;
  private final LobbyManager lobbies;
  private HTTPServer promServer;
  private static final Logger LOGGER =
          LogManager.getLogger(Server.class.getName());
  private static final Gauge CONN_REQUESTS = Gauge.build()
          .name("connection_requests_total")
          .help("total number of connection requests").register();
  private ServerSocket listening;


  /**
   * Constructor.
   * Creates crucial Objects for the server: {@link ClientManager}
   * , {@link LobbyManager}, assigns the given connection Port.
   *
   * @param port the Port the Server should listen on
   */
  public Server(String port) {
    startPrometheusHTTP();
    this.shutdownRequested = false;
    this.connectionPort = Integer.parseInt(port);
    this.clients = new ClientManager();
    this.lobbies = new LobbyManager(clients);
  }

  /**
   * Tries to make a Serversocket and constantly listens to new Connections.
   * The new connections are wrapped into {@link ClientOnServer} objects
   * and assigned to a {@link Executor} Thread that is finally added to
   * the servers {@link ClientManager}.
   */
  public void run() {
    try {
      this.listening = new ServerSocket(connectionPort);
      while (true) {
        LOGGER.info("Waiting for new Connections");
        System.out.println("Waiting for new Connections");
        Socket newClientSocket = listening.accept();
        if (!shutdownRequested) {
          LOGGER.info("New connection!");
          CONN_REQUESTS.inc();
          clients.setNumClients(clients.getNumClients() + 1);
          String initialNickname = "client-" + clients.getNumClients();
          ClientOnServer newClient = new ClientOnServer(newClientSocket,
                  initialNickname);
          Executor clientExecutor =
                  new Executor(newClient, this.clients, this.lobbies);
          clients.put(initialNickname, clientExecutor);
          LOGGER.info("Successfully connected " + initialNickname);
        } else {
          break;
        }
      }
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
      System.out.println(e.getMessage());
      LOGGER.trace("Exiting...");
    }
  }

  /**
   * Closes the accepting socket, stops HTTP-Exporter,
   * terminates the thread by setting shutdownRequested.
   * @throws IOException when an error occurs during closing
   *                     the listening or prometheus server
   */
  public void shutdown() throws IOException {
    shutdownRequested = true;
    listening.close();
    promServer.stop();
    for (String ex: clients.getNicknames()) {
      System.out.println("stop executor: " + ex);
      clients.closeConnection(ex);
    }
  }

  /**
   * Starts a simple HTTP-Exporter for the implemented
   * Prometheus-Metrics.
   */
  public void startPrometheusHTTP() {
    try {
      this.promServer =  new HTTPServer(1234);
      LOGGER.info("Prometheus-Exporter running!");
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
    }

  }
}
