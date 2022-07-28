package org.example;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.management.HotSpotDiagnosticMXBean;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;

public class Main {

    private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";


    public static void main(String[] args) {
        System.out.println("Dump memory heap of Java process!");
        String pid = "5786";
        String dumpFile = System.getProperty("user.home") + "/Desktop/dump_" + System.currentTimeMillis() + ".hprof";
        try {
            connectAndDumpHeap(pid, dumpFile);
            System.out.println("finish dump heap to file " + dumpFile);
        } catch (IOException | AttachNotSupportedException e) {
            System.err.println("failed to dump heap");
            e.printStackTrace();
        }
    }

    static void dumpHeap(MBeanServerConnection beanServerConn, String dumpFile) throws IOException {
        HotSpotDiagnosticMXBean mxBean = ManagementFactory.newPlatformMXBeanProxy(
                    beanServerConn, HOTSPOT_BEAN_NAME, HotSpotDiagnosticMXBean.class);
        mxBean.dumpHeap(dumpFile, Boolean.TRUE);

    }

    static void connectAndDumpHeap(String pid, String dumpFile) throws IOException, AttachNotSupportedException {
        // attach to target VM
        VirtualMachine vm = VirtualMachine.attach(pid);
        String jmxUrl = vm.startLocalManagementAgent();

        System.out.println("local jmx url: " + jmxUrl);

        JMXServiceURL url = new JMXServiceURL(jmxUrl);
        JMXConnector connector = JMXConnectorFactory.connect(url);
        MBeanServerConnection beanServerConnection = connector.getMBeanServerConnection();
        dumpHeap(beanServerConnection, dumpFile);
        //close
        connector.close();
        // detach
        vm.detach();

    }
}
