package collidingempires.server.net;

import org.junit.Test;
import org.junit.Assert;

/**
 * Test class for the {@link Packager} class.
 */
public class PackagerTest {
    /**
     * Test for the inProtocol method, for the server-protocol.
     */
    @Test
    public void inProtocolTestServer() {
        String existingOrder = "CWCN";
        String falseOrder = "AAAA";
        Assert.assertTrue(Packager.inProtocol(existingOrder, "server"));
        Assert.assertFalse(Packager.inProtocol(falseOrder, "server"));
    }
    /**
     * Test for the inProtocol method, for the client-protocol.
     */
    @Test
    public void inProtocolTestClient() {
        String existingOrder = "NLOS";
        String falseOrder = "AAAA";
        Assert.assertTrue(Packager.inProtocol(existingOrder, "client"));
        Assert.assertFalse(Packager.inProtocol(falseOrder, "client"));
    }
    /**
     * Test for the pack method.
     */
    @Test
    public void packTest() {
        String[] toPack = new String[]{"A", "278", "25.4", "true"};
        Assert.assertEquals("A_278_25.4_true", Packager.pack(toPack));
    }
    /**
     * Test for the unpack method, for the server-protocol.
     */
    @Test
    public void unpackTestServer() {
        String toUnpack = "CWTR";
        String[] unpacked = Packager.unpack(toUnpack, "server");
        Assert.assertEquals(1, unpacked.length);
        Assert.assertEquals("CWTR", unpacked[0]);
        toUnpack = "ABCD";
        unpacked = Packager.unpack(toUnpack, "server");
        Assert.assertNull(unpacked);
        toUnpack = "CWST_server_Halloichbin_eineNachricht_";
        unpacked = Packager.unpack(toUnpack, "server");
        Assert.assertEquals(3,  unpacked.length);
        Assert.assertEquals("Halloichbin_eineNachricht_", unpacked[2]);
    }
    /**
     * Test for unpack method, for the client-protocol.
     */
    @Test
    public void unpackTestClient() {
        String toUnpack = "YLTL";
        String[] unpacked = Packager.unpack(toUnpack, "client");
        Assert.assertEquals(1, unpacked.length);
        Assert.assertEquals("YLTL", unpacked[0]);
        toUnpack = "ABCD";
        unpacked = Packager.unpack(toUnpack, "client");
        Assert.assertNull(unpacked);
        toUnpack = "ACLL_testnickname";
        unpacked = Packager.unpack(toUnpack, "client");
        Assert.assertEquals(2,  unpacked.length);
        Assert.assertEquals("testnickname", unpacked[1]);
        Assert.assertEquals("ACLL", unpacked[0]);
        toUnpack = "_ACLL";
        unpacked = Packager.unpack(toUnpack, "client");
        Assert.assertNull(unpacked);
    }

    @Test
    public void unpackEmptyArguments() {
        String toUnpack = "ALCS_lobbyname_false_";
        String[] unpacked = Packager.unpack(toUnpack, "client");
        Assert.assertEquals(4, unpacked.length);
        Assert.assertEquals("", unpacked[3]);
        toUnpack = "ALCS_lobbyname__4";
        unpacked = Packager.unpack(toUnpack, "client");
        Assert.assertEquals(4, unpacked.length);
        Assert.assertEquals("", unpacked[2]);
        Assert.assertEquals("4", unpacked[3]);
    }
}
