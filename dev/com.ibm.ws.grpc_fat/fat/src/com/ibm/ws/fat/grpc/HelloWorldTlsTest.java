/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.ws.fat.grpc;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import com.ibm.websphere.simplicity.ShrinkHelper;
import com.ibm.websphere.simplicity.log.Log;

import componenttest.annotation.AllowedFFDC;
import componenttest.annotation.Server;
import componenttest.custom.junit.runner.FATRunner;
import componenttest.topology.impl.LibertyServer;

@RunWith(FATRunner.class)
public class HelloWorldTlsTest extends HelloWorldBasicTest {

    protected static final Class<?> c = HelloWorldTlsTest.class;

    private static final Logger LOG = Logger.getLogger(c.getName());

    @Rule
    public TestName name = new TestName();

    @Server("HelloWorldServerTls")
    public static LibertyServer helloWorldTlsServer;

    private static final Set<String> clientAppName = Collections.singleton("HelloWorldClient");
    private static final String TLS_MUTUAL_AUTH = "grpc.server.tls.mutual.auth.xml";
    private static final String TLS_INVALID_CLIENT_TRUST_STORE = "grpc.server.tls.invalid.trust.xml";
    private static final String TLS_OUTBOUND_FILTER = "grpc.server.tls.outbound.xml";

    @BeforeClass
    public static void setUp() throws Exception {
        // add all classes from com.ibm.ws.grpc.fat.helloworld.service and io.grpc.examples.helloworld
        // to a new app HelloWorldService.war
        ShrinkHelper.defaultDropinApp(helloWorldTlsServer, "HelloWorldService.war",
                                      "com.ibm.ws.grpc.fat.helloworld.service",
                                      "io.grpc.examples.helloworld");

        // add all classes from com.ibm.ws.grpc.fat.helloworld.client, io.grpc.examples.helloworld,
        // and com.ibm.ws.fat.grpc.tls to a new app HelloWorldClient.war.
        ShrinkHelper.defaultDropinApp(helloWorldTlsServer, "HelloWorldClient.war",
                                      "com.ibm.ws.grpc.fat.helloworld.client",
                                      "io.grpc.examples.helloworld");

        helloWorldTlsServer.startServer(HelloWorldTlsTest.class.getSimpleName() + ".log");
        assertNotNull("CWWKO0219I.*ssl not recieved", helloWorldTlsServer.waitForStringInLog("CWWKO0219I.*ssl"));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        // SRVE0777E for testHelloWorldWithTlsInvalidClientTrustStore case
        helloWorldTlsServer.stopServer("SRVE0777E");
    }

    @Before
    public void preTest() {
        serverRef = helloWorldTlsServer;
    }

    @After
    public void afterTest() {
        serverRef = null;
    }

    /**
     * testHelloWorld() with TLS enabled.
     * This test will only be performed if the native JDK 9+ ALPN provider is available.
     *
     * @throws Exception
     */
    @Test
    public void testHelloWorldWithTls() throws Exception {
        if (!checkJavaVersion()) {
            return;
        }
        String response = runHelloWorldTlsTest();
        assertTrue("the gRPC request did not complete correctly", response.contains("us3r2"));
    }

    /**
     * testHelloWorld() with TLS mutual authentication.
     * This test will only be performed if the native JDK 9+ ALPN provider is available.
     *
     * @throws Exception
     */
    @Test
    public void testHelloWorldWithTlsMutualAuth() throws Exception {
        if (!checkJavaVersion()) {
            return;
        }
        GrpcTestUtils.setServerConfiguration(helloWorldTlsServer, null, TLS_MUTUAL_AUTH, clientAppName, LOG);
        String response = runHelloWorldTlsTest();
        assertTrue("the gRPC request did not complete correctly", response.contains("us3r2"));
    }

    /**
     * testHelloWorld() an invalid client trust store configured.
     * This test will only be performed if the native JDK 9+ ALPN provider is available.
     *
     * @throws Exception
     */
    @Test
    @AllowedFFDC("io.grpc.StatusRuntimeException")
    public void testHelloWorldWithTlsInvalidClientTrustStore() throws Exception {
        if (!checkJavaVersion()) {
            return;
        }
        GrpcTestUtils.setServerConfiguration(helloWorldTlsServer, null, TLS_INVALID_CLIENT_TRUST_STORE, clientAppName, LOG);
        Exception clientException = null;

        try {
            testHelloWorldWithTls();
        } catch (Exception e) {
            clientException = e;
            Log.info(c, name.getMethodName(), "exception caught: " + e);
        }
        assertTrue("An error is expected for this case", clientException != null);

    }

    /**
     * testHelloWorld() with TLS and outbound filter enabled.
     * This test will only be performed if the native JDK 9+ ALPN provider is available.
     *
     * @throws Exception
     */
    @Test
    public void testHelloWorldWithTlsFilter() throws Exception {
        if (!checkJavaVersion()) {
            return;
        }
        GrpcTestUtils.setServerConfiguration(helloWorldTlsServer, null, TLS_OUTBOUND_FILTER, clientAppName, LOG);
        String response = runHelloWorldTlsTest();
        assertTrue("the gRPC request did not complete correctly", response.contains("us3r2"));
    }
}
