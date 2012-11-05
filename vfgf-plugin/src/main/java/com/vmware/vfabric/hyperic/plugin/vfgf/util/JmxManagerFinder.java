package com.vmware.vfabric.hyperic.plugin.vfgf.util;
/**
 * Some of the code in this class was provided by the GemFire team. 
 * The byte code is ripped from gemfire.jar. This prevents us from 
 * needing to load gemfire.jar. The same code is used in pulse
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import org.apache.http.conn.ssl.SSLSocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.agent.AgentKeystoreConfig;
import org.hyperic.hq.product.PluginException;
import org.hyperic.util.security.DefaultSSLProviderImpl;
import org.hyperic.util.security.SSLProvider;

import com.vmware.vfabric.hyperic.plugin.vfgf.detector.GF7ServerDetector;

/**
 * This class can be used to connect to a locator and ask it to find a jmx manager.
 * If the locator can find a jmx manager that is already running it returns it.
 * Otherwise the locator will attempt to start a jmx manager and then return it.
 * 
 * This code does not depend on gemfire.jar but in order to do this some of GemFire's
 * internal serialization codes and byte sequences have been hard coded into this code.
 * 
 * @author darrel
 *
 */
public class JmxManagerFinder {
    
    private static final Log log = LogFactory.getLog(JmxManagerFinder.class);
    
    private static final short JMX_MANAGER_LOCATOR_REQUEST = 2150;
    private static final short JMX_MANAGER_LOCATOR_RESPONSE = 2151;
    private static final byte DS_FIXED_ID_SHORT = 2;
    private static final int GOSSIPVERSION = 1001;
    private static final byte STRING_BYTES = 87;
    private static final byte NULL_STRING = 69;
    
    private static AgentKeystoreConfig keystoreConfig;
   
    /**
     * Takes a string of comma separated locators and finds the current manager
     * 
     * @param locators
     * @return The Current Jmx Manager or null if not found
     */
    
    public static JmxManagerInfo getJmxManager(String locators) {
        String locArray[] = locators.split(",");
        if(locArray.length > 3) {
            log.warn("More than 3 locators defined. This could lead to issues");
        }
        for(String loc : locArray) {
            String address[] = loc.split(":");
            try {
                InetAddress addr = InetAddress.getByName(address[0]);
                return askLocatorForJmxManager(addr, Integer.parseInt(address[1]), 10000, false);
            } catch (UnknownHostException e) {
                log.debug("[getJmxManager] Unable to connect to " + address[0] + ":" + address[1] + ": " + 
                    e.getMessage(), e);
            } catch (NumberFormatException e) {
                log.debug("[getJmxManager] Invalid Port:" + address[1] + ": " + 
                    e.getMessage(), e);
            } catch (IOException e) {
                log.debug("[getJmxManager] Unable to connect to " + address[0] + ":" + address[1] + ": " + 
                    e.getMessage(), e);
            }
        }
        return null;
    }
    /**
     * Takes a list of locators and returns the current jmx url
     * @param locators
     * @return jmx url or null if none found
     */
    public static String getJmxUrl(String locators) {
        JmxManagerInfo info = getJmxManager(locators);
        if(info == null) {
            return null;
        }
        return "service:jmx:rmi:///jndi/rmi://" + info.getHost() + ":" + info.getPort() + "/jmxrmi";
    }
   
    
    /**
     * Ask a locator to find a jmx manager. The locator will start one if one is not already running.
     * 
     * @param addr the host address the locator is listening on
     * @param port the port the locator is listening on
     * @param timeout the number of milliseconds to wait for a response; 15000 is a reasonable default
     * @return describes the location of the jmx manager. The port will be zero if no jmx manager was found.
     * @throws IOException if a problem occurs trying to connect to the locator or communicate with it.
     */
    public static JmxManagerInfo askLocatorForJmxManager(InetAddress addr,
                                                         int port, int timeout, boolean isSsl) throws IOException {
        Socket sock;
        SocketAddress sockaddr = new InetSocketAddress(addr, port);

        if(isSsl) {
            keystoreConfig = new AgentKeystoreConfig();
            boolean accept = true;
            SSLProvider sslProvider = new DefaultSSLProviderImpl(keystoreConfig, accept);
            SSLSocketFactory factory = sslProvider.getSSLSocketFactory();
            sock = factory.createSocket();
        } else {
            sock = new Socket();    
        }
        try {
            sock.connect(sockaddr, timeout);
            sock.setSoTimeout(timeout);
            DataOutputStream out = new DataOutputStream(sock.getOutputStream());               

            out.writeInt(GOSSIPVERSION);
            out.writeByte(DS_FIXED_ID_SHORT);
            out.writeShort(JMX_MANAGER_LOCATOR_REQUEST);
            out.flush();

            DataInputStream in = new DataInputStream(sock.getInputStream());
            byte header = in.readByte();
            if (header != DS_FIXED_ID_SHORT) {
                throw new IllegalStateException("Expected " + DS_FIXED_ID_SHORT + " but found " + header);
            }
            int msgType = in.readShort();
            if (msgType != JMX_MANAGER_LOCATOR_RESPONSE) {
                throw new IllegalStateException("Expected " + JMX_MANAGER_LOCATOR_RESPONSE + " but found " + msgType);
            }
            byte hostHeader = in.readByte();
            String host;
            if (hostHeader == NULL_STRING) {
                host = "";
            } else if (hostHeader == STRING_BYTES) {
                int len = in.readUnsignedShort();
                byte[] buf = new byte[len];
                in.readFully(buf, 0, len);
                @SuppressWarnings("deprecation")
                String str = new String(buf, 0);
                host = str;
            } else {
                throw new IllegalStateException("Expected " + STRING_BYTES + " or " + NULL_STRING + " but found " + hostHeader);
            }
            int jmport = in.readInt();
            boolean ssl = in.readBoolean();
            if (host.equals("")) {
                jmport = 0;
            }
            return new JmxManagerInfo(host, jmport, ssl);
        } catch (IllegalStateException e) {
            // Try this as ssl only try once to avoid loop
            if(!isSsl) {
                return askLocatorForJmxManager(addr, port, timeout, true);
            } else {
                return null;
            }
        } finally {
            try {
                sock.close();
            } catch(Exception e) {
            }
        }
    }
}
