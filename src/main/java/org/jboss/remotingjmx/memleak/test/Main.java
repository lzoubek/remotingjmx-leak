package org.jboss.remotingjmx.memleak.test;


import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;


public class Main implements Runnable {

    /**
     * change this to your location
     */
    private static final File JBOSS_CLIENT = new File("/data/jboss/jboss-eap-6.4/bin/client/jboss-client.jar");

    public static ClassLoader buildClassLoader() throws Exception {
        ClassLoader loader = new URLClassLoader(new URL[] { JBOSS_CLIENT.toURI().toURL() }, Main.class.getClassLoader());
        return loader;
    }

    private JMXConnector jmxConnector;
    private MBeanServerConnection serverConnection;

    //private MBeanServer mbeanServer;

    public void connect(String url, String user, String pass) throws Exception {

        JMXServiceURL jmxUrl = new JMXServiceURL(url);
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
        String[] credentials = new String[] { user, pass };
        env.put(JMXConnector.CREDENTIALS, credentials);
        this.jmxConnector = JMXConnectorFactory.connect(jmxUrl, env);
        //this.serverConnection = this.jmxConnector.getMBeanServerConnection();
    }

    public void disconnect() throws Exception {
        this.jmxConnector.close();
        this.jmxConnector = null;
    }

    public static void main(String[] args) throws Exception {
        int count = 1000;
        for (int i = 0; i < count; i++) {
            Main main = new Main();
            System.out.println("Starting client thread");
            Thread t = new Thread(main);
            t.setContextClassLoader(buildClassLoader());
            t.start();
            Thread.currentThread().join(5000L);
        }

    }

    public void run() {
        try {
            // only load some class (verify in visualvm, that it gets GCed)
            //Thread.currentThread().getContextClassLoader().loadClass("org.jboss.remotingjmx.RemotingConnector");

            // connect & disconnect
            connect("service:jmx:remoting-jmx://127.0.0.1:9999", "rhqadmin", "rhqadmin");
            disconnect();
            Thread.currentThread().join(1000L);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
