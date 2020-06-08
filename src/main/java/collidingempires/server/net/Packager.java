package collidingempires.server.net;

import collidingempires.client.net.ClientProtocol;

/**
 * Packs and unpacks String and String-arrays.
 * This is important for the networkconnectivity of this Project,
 * because the Sequences are Protocol-orders and its arguments
 * divided by a DIVISOR.
 */
public class Packager {
    static final String DIVISOR = "_";

    /**
     * Splits a given string-sequence by the DIVISOR.
     * and checks whether the sequence is defined in the protocol.
     *
     * @param order    the String to unpack
     * @param protocol server = serverprotocol, client = clientprotocol
     * @return String[] containing the parts of the Sequence
     */
    public static String[] unpack(final String order, final String protocol) {
        if (order != null) {
            String protocolSeq = order.split(DIVISOR)[0];
            if (protocol.equals("server")) {
                if (inProtocol(protocolSeq, protocol)) {
                    int numArgs = ServerProtocol.valueOf(protocolSeq).getNumArgs();
                    if (numArgs == 0) {
                        return new String[]{order};
                    }
                    String[] unpacked = order.split(DIVISOR, numArgs + 1);
                    if (unpacked.length == numArgs + 1) {
                        return unpacked;
                    }
                }
                return null;
            } else if (protocol.equals("client")) {
                if (inProtocol(protocolSeq, protocol)) {
                    int numArgs = ClientProtocol.valueOf(protocolSeq).getNumArgs();
                    if (numArgs == 0) {
                        return new String[]{order};
                    }
                    String[] unpacked = order.split(DIVISOR, numArgs + 1);
                    if (unpacked.length == numArgs + 1) {
                        return unpacked;
                    }
                }
                return null;
            }
        }
        return null;
    }

    /**
     * Connects a given list of Strings to a Sequence divided by the DIVISOR.
     *
     * @param topack the list of Strings that should be packed as a Sequence
     * @return The built sequence: topack[0]_topack[1]_....._topack[n]
     */
    public static String pack(String... topack) {
        StringBuilder order = new StringBuilder();
        order.append(topack[0]);
        for (int i = 1; i < topack.length; i++) {
            order.append(DIVISOR).append(topack[i]);
        }
        return order.toString();
    }

    /**
     * Checks if the received order is a valid sequence/defined in the Protocol and
     * the number of arguments is correct.
     *
     * @param order    the received order
     * @param protocol server = Serverprotocol, client = Clientprotocol
     * @return true: sequence is in protocol and number of arguments is correct
     * false: sequence not in protocol and/or number of arguments is incorrect
     */
    public static boolean inProtocol(final String order, final String protocol) {
        // check if the received order is defined in the protocol
        if (protocol.equals("server")) {
            for (ServerProtocol s : ServerProtocol.values()) {
                if (s.name().equals(order)) {
                    return true;
                }
            }
            return false;
        } else if (protocol.equals("client")) {
            for (ClientProtocol s : ClientProtocol.values()) {
                if (s.name().equals(order)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }
}
