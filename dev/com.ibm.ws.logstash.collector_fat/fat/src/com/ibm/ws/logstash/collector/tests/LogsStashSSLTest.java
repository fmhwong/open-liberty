/*******************************************************************************
 * Copyright (c) 2011, 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.logstash.collector.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.utility.MountableFile;

import com.ibm.websphere.simplicity.Machine;
import com.ibm.websphere.simplicity.RemoteFile;
import com.ibm.websphere.simplicity.ShrinkHelper;
import com.ibm.websphere.simplicity.log.Log;

import componenttest.annotation.AllowedFFDC;
import componenttest.custom.junit.runner.FATRunner;
import componenttest.custom.junit.runner.Mode;
import componenttest.custom.junit.runner.Mode.TestMode;
import componenttest.topology.impl.LibertyServer;
import componenttest.topology.impl.LibertyServerFactory;
import componenttest.topology.impl.Logstash;

@RunWith(FATRunner.class)
@Mode(TestMode.LITE)
public class LogsStashSSLTest extends LogstashCollectorTest {
    private static LibertyServer server = LibertyServerFactory.getLibertyServer("LogstashServer");
    protected static Machine machine = null;
    private static boolean connected = false;

    private String testName = "";
    private static Class<?> c = LogsStashSSLTest.class;
    private static String JVMSecurity = System.getProperty("Djava.security.properties");
    public static String pathToAutoFVTTestFiles = "lib/LibertyFATTestFiles/";
    private static String os = "";

    protected static boolean runTest;

    // Can be added to the FATSuite to make the resource lifecycle bound to the entire
    // FAT bucket. Or, you can add this to any JUnit test class and the container will
    // be started just before the @BeforeClass and stopped after the @AfterClass
    @ClassRule
    public static GenericContainer<?> logstashContainer = new GenericContainer<>("docker.elastic.co/logstash/logstash:7.2.0") //
                    .withExposedPorts(5043) //
                    .withCopyFileToContainer(MountableFile.forHostPath("logstash.conf"), "/usr/share/logstash/pipeline/logstash.conf") //
                    .withCopyFileToContainer(MountableFile.forHostPath("logstash.yml"), "/usr/share/logstash/config/logstash.yml") //
                    .withCopyFileToContainer(MountableFile.forHostPath("ca.key"), "/usr/share/logstash/config/ca.key") //
                    .withCopyFileToContainer(MountableFile.forHostPath("ca.crt"), "/usr/share/logstash/config/ca.crt") //
                    .withLogConsumer(LogsStashSSLTest::log);

    private static StringBuilder logstashOutput = new StringBuilder();

    // This helper method is passed into `withLogConsumer()` of the container
    // It will consume all of the logs (System.out) of the container, which we will
    // use to pipe container output to our standard FAT output logs (output.txt)
    private static void log(OutputFrame frame) {
        String msg = frame.getUtf8String();
        logstashOutput.append(msg);
        if (msg.endsWith("\n"))
            msg = msg.substring(0, msg.length() - 1);
        Log.info(c, "somecontainer", msg);
    }

    private static void clearOutput() {
        logstashOutput = new StringBuilder();
    }

    private static String waitForStringInOutput(String str) {
        String line = null;
        int startline = 0;
        int timeout = 120 * 1000; // 120 seconds
        while ((timeout > 0) && (line == null)) {
            String output = logstashContainer.toString();
            String[] lines = output.split(System.getProperty("line.separator"));
            for (int i = startline; i < lines.length; i++) {
                if (lines[i].indexOf(str) > 0) {
                    return lines[i];
                }
            }
            startline = lines.length;
            timeout -= 1000;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return line;
    }

    @BeforeClass
    public static void setUp() throws Exception {
        os = System.getProperty("os.name").toLowerCase();
        Log.info(c, "setUp", "os.name = " + os);

        String host = logstashContainer.getContainerIpAddress();
        String port = String.valueOf(logstashContainer.getMappedPort(5984));
        Log.info(c, "setUp", "Logstaash container: host=" + host + "  port=" + port);
        server.addEnvVar("LOGSTASH_HOST", host);
        server.addEnvVar("LOGSTASH_PORT", port);

        // Change the logstash config file so that the SSL tests create their own output file.
        Logstash.CONFIG_FILENAME = "logstash.conf";
        Logstash.OUTPUT_FILENAME = "logstash_output.txt";

        Log.info(c, "setUp", "---> Setting default logstash configuration.");
        server.setServerConfigurationFile("server_logs_all.xml");
        String extendedPath = "usr/servers/LogstashServer/jvm.options";
        if (server.getServerRoot().contains(server.getInstallRoot())) {
            extendedPath = server.getServerRoot().replaceAll(server.getInstallRoot(), "").substring(1);
        }
        server.copyFileToLibertyInstallRoot(extendedPath, "jvm.options");
        server.copyFileToLibertyInstallRoot(extendedPath.replace("jvm.options", "java.security"), "java.security");
        ShrinkHelper.defaultDropinApp(server, "LogstashApp", "com.ibm.logs");

        serverStart();
    }

    @Before
    public void setUpTest() throws Exception {
        Assume.assumeTrue(runTest); // runTest must be true to run test

        testName = "setUpTest";
        if (!server.isStarted()) {
            serverStart();
        }
    }

    @Test
    //@Ignore("Ignoring testLogstashDefaultConfig for now, need to rewrite the logic later")
    public void testLogstashDefaultConfig() throws Exception {
        testName = "testLogstashDefaultConfig";
        server.setMarkToEndOfLog();

        setConfig("server_default_conf.xml");
        clearOutput();
        // Run App to generate events
        Log.info(c, testName, "---> Running the application.. ");
        for (int i = 1; i <= 10; i++) {
            createMessageEvent(testName + " " + i);
        }

        assertNotNull("Cannot find TRAS0218I from messages.log", server.waitForStringInLogUsingMark("TRAS0218I", 10000));
        assertNotNull("Cannot find message " + testName + " from Logstash output", waitForStringInOutput(testName));
    }

    @Test
    public void testLogstash() throws Exception {
        testName = "testLogstash";

        //Look for feature started message.
        boolean feature = this.isConnected();

        Log.info(c, testName, "---> Did Logstash feature start ? : " + feature);
        assertTrue("logstashCollector-1.0 did not show as started..", feature);

    }

    @Test
    @AllowedFFDC({ "java.lang.NullPointerException" })
    public void testLogstashEvents() throws Exception {
        testName = "testLogstashEvents";

        server.setMarkToEndOfLog();
        setConfig("server_logs_all.xml");

        clearOutput();

        createMessageEvent(testName);
        assertNotNull(waitForStringInOutput(LIBERTY_MESSAGE));
        createTraceEvent();
        assertNotNull(waitForStringInOutput("LIBERTY_TRACE"));
        createFFDCEvent(1);
        assertNotNull(waitForStringInOutput("LIBERTY_FFDC"));
        assertNotNull(waitForStringInOutput(LIBERTY_ACCESSLOG));
    }

    @Test
    public void testLogstashForMessageEvent() throws Exception {
        testName = "testLogstashForMessageEvent";
        server.setMarkToEndOfLog();
        setConfig("server_logs_msg.xml");
        createMessageEvent(testName);
        assertNotNull("Cannot find TRAS0218I from messages.log", server.waitForStringInLogUsingMark("TRAS0218I", 10000));

        boolean found = false;
        int timeout = 0;
        try {
            while (!(found = waitForStringInOutput(LIBERTY_MESSAGE).equals(testName)) && timeout < 120000) {
                timeout += 1000;
                Thread.sleep(1000);
            }
            Log.info(c, testName, "------> found message event types : " + found);
        } catch (Exception e) {
            Log.info(c, testName, "------>Exception occured while reading logstash output file : \n" + e.getMessage());
        }
        assertTrue("Did not find message log events..", found);
    }

    @Test
    public void testLogstashForAccessEvent() throws Exception {
        testName = "testLogstashForAccessEvent";
        server.setMarkToEndOfLog();
        setConfig("server_logs_access.xml");
        createAccessLogEvent(testName);
        assertNotNull("Cannot find TRAS0218I from messages.log", server.waitForStringInLogUsingMark("TRAS0218I", 10000));

        boolean found = false;
        found = waitForStringInOutput(testName) != null;
        assertTrue("Did not find access log events..", found);
    }

//    @Test
//    public void testLogstashForGCEvent() throws Exception {
//        testName = "testLogstashForGCEvent";
//        server.setMarkToEndOfLog();
//        setConfig("server_logs_gc.xml");
//
//        // Do some work and hopefully some GC events will be created
//        for (int i = 1; i <= 10; i++) {
//            createMessageEvent(testName + " " + i);
//        }
//
//        boolean found = true;
//        try {
//            if (!checkGcSpecialCase()) {
//                int timeout = 0;
//                while (!(found = lookForStringInLogstashOutput(LIBERTY_GC, null)) && timeout < 120000) {
//                    timeout += 1000;
//                    Thread.sleep(1000);
//                }
//            }
//        } catch (Exception e) {
//            Log.info(c, testName, "------>Exception occured while reading logstash output file : \n" + e.getMessage());
//        }
//        assertTrue("Did not find gc log events..", found);
//    }

//    @Test
//    @AllowedFFDC({ "java.lang.ArithmeticException", "java.lang.ArrayIndexOutOfBoundsException" })
//    public void testLogstashForFFDCEvent() throws Exception {
//        testName = "testLogstashForFFDCEvent";
//
//        server.setMarkToEndOfLog();
//        setConfig("server_logs_ffdc.xml");
//        Log.info(c, testName, "------> starting ffdc2(ArithmeticException), "
//                              + "ffdc3(ArrayIndexOutOfBoundsException)");
//
//        List<String> exceptions = new ArrayList<String>();
//        createFFDCEvent(2);
//        Log.info(c, testName, "------> finished ffdc2(ArithmeticException)");
//        exceptions.add("ArithmeticException");
//        createFFDCEvent(3);
//        Log.info(c, testName, "------> finished ffdc3(ArrayIndexOutOfBoundsException)");
//        exceptions.add("ArrayIndexOutOfBoundsException");
//        assertNotNull("Cannot find TRAS0218I from messages.log", server.waitForStringInLogUsingMark("TRAS0218I", 10000));
//
//        boolean found = false;
//        try {
//            int timeout;
//            for (String exception : exceptions) {
//                timeout = 0;
//                while (!(found = lookForStringInLogstashOutput(LIBERTY_FFDC, exception)) && timeout < 120000) {
//                    Thread.sleep(1000);
//                    timeout += 1000;
//                }
//                Log.info(c, testName, "------> " + exception + " : " + found);
//                if (!found) {
//                    break;
//                }
//            }
//            Log.info(c, testName, "------> found ffdc event types : " + found);
//        } catch (Exception e) {
//            Log.info(c, testName, "------>Exception occured while reading logstash output file : \n" + e.getMessage());
//            found = false;
//        }
//        assertTrue("Did not find some or all ffdc log events..", found);
//    }

    @Test
    public void testLogstashForTraceEvent() throws Exception {
        testName = "testLogstashForTraceEvent";
        server.setMarkToEndOfLog();
        clearOutput();
        setConfig("server_logs_trace.xml");
        createTraceEvent(testName);

        boolean found = false;
        try {
            found = waitForStringInOutput(LIBERTY_TRACE) != null;
            Log.info(c, testName, "------> found trace event types : " + found);
        } catch (Exception e) {
            Log.info(c, testName, "------>Exception occured while reading logstash output file : \n" + e.getMessage());
        }
        assertTrue("Did not find trace log events..", found);
    }

    @Test
    public void testLogstashForAuditEvent() throws Exception {
        testName = "testLogstashForAuditEvent";
        server.setMarkToEndOfLog();
        clearOutput();
        setConfig("server_logs_audit.xml");
        createTraceEvent(testName);

        boolean found = false;
        try {
            found = waitForStringInOutput(LIBERTY_AUDIT) != null;
            Log.info(c, testName, "------> found audit event types : " + found);
        } catch (Exception e) {
            Log.info(c, testName, "------>Exception occured while reading logstash output file : \n" + e.getMessage());
        }
        assertTrue("Did not find audit log events..", found);
    }

    @Test
    public void testLogstashEntryExitEvents() throws Exception {
        testName = "testLogstashEntryExitEvents";
        server.setMarkToEndOfLog();
        setConfig("server_logs_trace.xml");
        clearOutput();
        createTraceEvent(testName);

        boolean entry = (waitForStringInOutput(ENTRY) != null);
        boolean exit = (waitForStringInOutput(EXIT) != null);
        server.setMarkToEndOfLog();
        if (entry && !exit) {
            assertTrue("Exit Events are missing..", exit);
        } else if (!entry && exit) {
            assertTrue("Entry Events are missing..", entry);
        }
        assertTrue("Entry and Exit Events are missing..", entry && exit);
    }

    @Test
    public void testLogstashDynamicDisableFeature() throws Exception {
        testName = "testLogstashDynamicDisableFeature";
        server.setMarkToEndOfLog();
        setConfig("server_disable.xml");
        server.waitForStringInLogUsingMark("CWWKF0013I", 10000);

        boolean removed = false;
        List<String> lines = server.findStringsInLogsAndTraceUsingMark("CWWKF0013I");
        assertTrue("Feature not removed..", lines.size() > 0);
        String line = lines.get(lines.size() - 1);
        Log.info(c, testName, "---> line : " + line);
        if (line.contains("logstashCollector-1.0")) {
            removed = true;
        }

        Log.info(c, testName, "---> Did Logstash feature STOP ? : " + removed);
        assertTrue("logstashCollector-1.0 show as started..", removed);

        server.setMarkToEndOfLog();
        setConfig("server_logs_msg.xml");

        boolean feature = this.isConnected();
        Log.info(c, testName, "---> Did LogstashCollector feature START ? : " + feature);
        assertTrue("logstashCollector-1.0 did not show as started..", feature);

    }

//    @Test
//    public void testLogstashDynamicDisableEventType() throws Exception {
//        testName = "testLogstashDynamicDisableEventType";
//        server.setMarkToEndOfLog();
//        clearOutput();
//
//        setConfig("server_logs_msg.xml");
//        createMessageEvent(testName + " 1 - should appear in logstash output");
//        assertNotNull("Did not find " + LIBERTY_MESSAGE + ":" + testName, waitForStringInOutput(testName));
//
//        setConfig("server_logs_trace.xml");
//        clearOutput();
//        createMessageEvent(testName + " 2 - should NOT appear in logstash output");
//        createTraceEvent(testName + " 3 - should appear in logstash output");
//
//        waitForStringInOutput(testName + " 3");
//
//        List<JSONObject> jObjs = logstash.parseOutputFile(lastLine + 1);
//
//        boolean found1 = false;
//        boolean found2 = false;
//        boolean found3 = false;
//        String msg = null;
//        for (JSONObject jObj : jObjs) {
//            msg = jObj.getString(KEY_MESSAGE);
//            if (msg.contains(testName + " 1")) {
//                found1 = true;
//            } else if (msg.contains(testName + " 2")) {
//                found2 = true;
//            } else if (msg.contains(testName + " 3")) {
//                found3 = true;
//            }
//        }
//        assertTrue(testName + " 1 is not found", found1);
//        assertFalse(testName + " 2 should not appear in logstash output", found2);
//        assertTrue(testName + " 3 is not found", found3);
//    }

    /*
     * This test determines whether source subsriptions are kept when they are present both
     * before and after a server configuration change, and additionally if source subscriptions
     * are unsubscribed when a source is no longer present in the server configuration
     */
    @Test
    public void testModifiedSourceSubscription() throws Exception {
        RemoteFile traceFile = server.getMostRecentTraceFile();
        testName = "testModifiedSourceSubscription";

        //Clearing all sources
        server.setMarkToEndOfLog(traceFile);
        server.setMarkToEndOfLog();
        Log.info(c, testName, "Initializing: Unsubscribing from all sources");
        setConfig("server_no_sources.xml");

        //Specify two sources: message and trace
        //Check if both sources are subscribed to initially
        server.setMarkToEndOfLog(traceFile);
        server.setMarkToEndOfLog();
        setConfig("server_message_trace.xml");

        //listOfSourcesToSubscribe [com.ibm.ws.logging.source.message|memory, com.ibm.ws.logging.source.trace|memory]
        List<String> lines = server.findStringsInLogsAndTraceUsingMark("listOfSourcesToSubscribe.\\[com\\.ibm\\.ws\\.logging\\.source\\.message\\|memory,.com\\.ibm\\.ws\\.logging\\.source\\.trace\\|memory\\]");
        Log.info(c, testName,
                 "Number of lines containing \"listOfSourcesToSubscribe.\\[com\\.ibm\\.ws\\.logging\\.source\\.message\\|memory,.com\\.ibm\\.ws\\.logging\\.source\\.trace\\|memory\\]\":"
                              + lines.size());
        //Check for both orderings, just in case
        if (lines.size() == 0) {
            lines = server.findStringsInLogsAndTraceUsingMark("listOfSourcesToSubscribe.\\[com\\.ibm\\.ws\\.logging\\.source\\.trace\\|memory,.com\\.ibm\\.ws\\.logging\\.source\\.message\\|memory\\]");
            Log.info(c, testName,
                     "Number of lines containing \"listOfSourcesToSubscribe.\\[com\\.ibm\\.ws\\.logging\\.source\\.trace\\|memory,.com\\.ibm\\.ws\\.logging\\.source\\.message\\|memory\\]\":"
                                  + lines.size());
        }
        assertTrue("Initialization failure: Sources message and/or trace were not subscribed to", lines.size() > 0);

        //Specify two sources: message and accessLog
        //Check if message is kept, trace is unsubscribed, and accessLog is subscribed
        server.setMarkToEndOfLog(traceFile);
        server.setMarkToEndOfLog();
        setConfig("server_message_access.xml");
        Log.info(c, testName, "Checking for unsubscription from trace and subscription to access");

        //Message was present both before and after, so it shouldn't be unsubscribed or resubscribed after this config change
        lines = server.findStringsInLogsAndTraceUsingMark("listOfSourcesToUnsubscribe.\\[com\\.ibm.ws\\.logging\\.source\\.message\\|memory\\]");
        Log.info(c, testName, "Number of lines containing \"listOfSourcesToUnsubscribe.\\[com\\.ibm.ws\\.logging\\.source\\.message\\|memory\\]\":" + lines.size());
        assertTrue("Message was unsubscribed when it was supposed to be kept", lines.size() == 0);
        lines = server.findStringsInLogsAndTraceUsingMark("listOfSourcesToSubscribe.\\[com\\.ibm\\.ws\\.logging\\.source\\.message\\|memory\\]");
        Log.info(c, testName, "Number of lines containing \"listOfSourcesToSubscribe.\\[com\\.ibm\\.ws\\.logging\\.source\\.message\\|memory\\]\":" + lines.size());
        assertTrue("Message was subscribed to again after configuration change ", lines.size() == 0);

        //Trace is no longer present, and should have been unsubscribed
        lines = server.findStringsInLogsAndTraceUsingMark("listOfSourcesToUnsubscribe.\\[com\\.ibm\\.ws\\.logging\\.source\\.trace\\|memory\\]");
        Log.info(c, testName, "Number of lines containing \"listOfSourcesToUnsubscribe.\\[com\\.ibm\\.ws\\.logging\\.source\\.trace\\|memory\\]\":" + lines.size());
        assertTrue("Trace source was not unsubscribed when it was supposed to be unsubscribed", lines.size() > 0);

        //AccessLog is newly present, and should be subscribed
        lines = server.findStringsInLogsAndTraceUsingMark("listOfSourcesToSubscribe.\\[com\\.ibm\\.ws\\.http\\.logging\\.source\\.accesslog\\|memory\\]");
        Log.info(c, testName, "Number of lines containing \"listOfSourcesToSubscribe.\\[com\\.ibm\\.ws\\.http\\.logging\\.source\\.accesslog\\|memory\\]\":" + lines.size());
        assertTrue("AccessLog source was not subscribed when it was supposed to be subscribed", lines.size() > 0);
    }

    @After
    public void tearDown() {
    }

    private static void resetServerSecurity() {
        //Reset JVM security to its original value
        System.setProperty("Djava.security.properties", JVMSecurity);
    }

    private boolean isConnected() throws Exception {
        if (!connected) {
            List<String> lines = server.findStringsInLogs("CWWKF0012I");
            for (String line : lines) {
                if (line.contains("logstashCollector-1.0")) {
                    Log.info(c, testName, "---> line : " + line);
                    connected = true;
                }
            }
        }
        return connected;
    }

    private static void serverStart() throws Exception {
        Log.info(c, "serverStart", "--->  Starting Server.. ");
        server.startServer();

        Log.info(c, "serverStart", "---> Wait for feature to start ");
        assertNotNull("Cannot find CWWKZ0001I from messages.log", server.waitForStringInLogUsingMark("CWWKZ0001I", 15000));

        Log.info(c, "serverStart", "---> Wait for application to start ");
        assertNotNull("Cannot find CWWKT0016I from messages.log", server.waitForStringInLogUsingMark("CWWKT0016I", 10000));
    }

    private boolean checkGcSpecialCase() {
        Log.info(c, testName, "Cannot find event type liberty_gc in logstash output file");
        /**
         * Check if if belongs to the special case where build machine does not have Health centre installed, which prevents gc event to be produced
         * by checking 1. whether the operating system is Mac or linux 2. whether the machine is running IBM JDK
         * if both checks pass, this is the case
         **/
        Log.info(c, testName, "os_name: " + os.toLowerCase() + "\t java_jdk: " + System.getProperty("java.vendor"));
        String JAVA_HOME = System.getenv("JAVA_HOME");
        Log.info(c, testName, "JAVA_HOME: " + JAVA_HOME);
        boolean healthCenterInstalled = false;
        if (JAVA_HOME == null) {
            Log.info(c, testName, " unable to find JAVA_HOME variable");
        } else if (JAVA_HOME.endsWith("jre")) {
            if (new File(JAVA_HOME + "/lib/ext/healthcenter.jar").exists()) {
                healthCenterInstalled = true;
                Log.info(c, testName, " jar file for health center under path " + JAVA_HOME + "/lib/ext/healthcenter.jar");
            }
            Log.info(c, testName, " jar file for health center under path " + JAVA_HOME + "/lib/ext/healthcenter.jar exist:"
                                  + new File(JAVA_HOME + "/lib/ext/healthcenter.jar").exists());
        } else if (JAVA_HOME.endsWith("bin")) {
            healthCenterInstalled = findHealthCenterDirecotry(JAVA_HOME.substring(0, JAVA_HOME.indexOf("bin") + 1));
            if (!healthCenterInstalled) {
                Log.info(c, testName, " unable to find heathcenter.jar, thus unable to produce gc events. Thus, this check will be by-passed");
            }
        } else {
            healthCenterInstalled = findHealthCenterDirecotry(JAVA_HOME);
            if (!healthCenterInstalled) {
                Log.info(c, testName, " unable to find heathcenter.jar, thus unable to produce gc events. Thus, this check will be by-passed");
            }
        }
        if (os.toLowerCase().contains("mac") || !System.getProperty("java.vendor").toLowerCase().contains("ibm")
            || System.getProperty("java.vendor.url").toLowerCase().contains("sun") || !healthCenterInstalled) {
            return true;
        }
        return false;
    }

    private boolean findHealthCenterDirecotry(String directoryPath) {
        boolean jarFileExist = false;
        File[] files = new File(directoryPath).listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                jarFileExist = findHealthCenterDirecotry(file.getAbsolutePath());
                if (jarFileExist == true) {
                    return true;
                }
            } else {
                if (file.getAbsolutePath().contains("healthcenter.jar")) {
                    Log.info(c, testName, " healthcetner.jar is found under path " + file.getAbsolutePath());
                    return true;
                }
            }
        }
        return jarFileExist;
    }

    /** {@inheritDoc} */
    @Override
    protected LibertyServer getServer() {
        return server;
    }

}
