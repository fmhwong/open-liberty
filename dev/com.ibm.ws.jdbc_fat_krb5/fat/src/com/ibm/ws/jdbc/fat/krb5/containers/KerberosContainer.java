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
package com.ibm.ws.jdbc.fat.krb5.containers;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.InternetProtocol;
import com.ibm.websphere.simplicity.log.Log;
import com.ibm.ws.jdbc.fat.krb5.FATSuite;

import componenttest.custom.junit.runner.FATRunner;
import componenttest.topology.utils.FileUtils;

public class KerberosContainer extends GenericContainer<KerberosContainer> {

    private static final Class<?> c = KerberosContainer.class;

    public static final String KRB5_REALM = "EXAMPLE.COM";
    public static final String KRB5_KDC = "kerberos";
    public static final String KRB5_PASS = "password";

    private static final String IMAGE = "aguibert/krb5-server:1.1";
    private static final Path reuseCache = Paths.get("..", "..", "cache", "krb5.properties");

    private boolean reused;
    private String reused_hostname;
    private int reused_port;
    private int udp_99;

    public KerberosContainer(Network network) {
        super(IMAGE);
        withNetwork(network);
    }

    @Override
    protected void configure() {
        withExposedPorts(99, 464, 749);
        withNetworkAliases(KRB5_KDC);
        withCreateContainerCmdModifier(cmd -> {
            cmd.withHostName(KRB5_KDC);
        });
        withEnv("KRB5_REALM", KRB5_REALM);
        withEnv("KRB5_KDC", "localhost");
        withEnv("KRB5_PASS", KRB5_PASS);
        withLogConsumer(KerberosContainer::log);
        waitingFor(new LogMessageWaitStrategy()
                        .withRegEx("^.*KERB SETUP COMPLETE.*$")
                        .withStartupTimeout(Duration.ofSeconds(FATRunner.FAT_TEST_LOCALRUN ? 15 : 300)));
        withReuse(true);
        withCreateContainerCmdModifier(cmd -> {
            List<ExposedPort> ports = new ArrayList<>();
            for (ExposedPort p : cmd.getExposedPorts()) {
                ports.add(p);
            }
            ports.add(new ExposedPort(99, InternetProtocol.UDP));
            cmd.withExposedPorts(ports);
        });
    }

    @Override
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        String udp99 = containerInfo.getNetworkSettings().getPorts().getBindings().get(new ExposedPort(99, InternetProtocol.UDP))[0].getHostPortSpec();
        udp_99 = Integer.valueOf(udp99);
    }

    @Override
    public void start() {
        if (hasCachedContainers()) {
            // If this is a local run and a cache file exists, that means a DB2 container is already running
            // and we can just read the host/port from the cache file
            Log.info(c, "start", "Found existing container cache file. Skipping container start.");
            Properties props = new Properties();
            try {
                props.load(new FileInputStream(reuseCache.toFile()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            reused = true;
            reused_hostname = props.getProperty("krb5.hostname");
            reused_port = Integer.valueOf(props.getProperty("krb5.port"));
            Log.info(c, "start", "Found existing container at host = " + reused_hostname);
        } else {
            super.start();
            if (FATSuite.REUSE_CONTAINERS) {
                Log.info(c, "start", "Saving KRB5 properties for future runs at: " + reuseCache.toAbsolutePath());
                try {
                    Files.createDirectories(reuseCache.getParent());
                    Properties props = new Properties();
                    if (reuseCache.toFile().exists()) {
                        try (FileInputStream fis = new FileInputStream(reuseCache.toFile())) {
                            props.load(fis);
                        }
                    }
                    props.setProperty("krb5.hostname", getContainerIpAddress());
                    props.setProperty("krb5.port", "" + getMappedPort(99));
                    props.store(new FileWriter(reuseCache.toFile()), "Generated by FAT run");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void stop() {
        if (FATSuite.REUSE_CONTAINERS) {
            Log.info(c, "stop", "Leaving container running so it can be used in later runs");
            return;
        } else {
            Log.info(c, "stop", "Stopping container");
            super.stop();
        }
    }

    @Override
    public String getContainerIpAddress() {
        return reused ? reused_hostname : super.getContainerIpAddress();
    }

    @Override
    public Integer getMappedPort(int originalPort) {
        // For this container assume we always want the UDP port when we ask for port 99
        if (originalPort == 99) {
            return reused ? reused_port : udp_99;
        } else {
            return super.getMappedPort(originalPort);
        }
    }

    public void generateConf(Path outputPath) throws IOException {
        String conf = "[libdefaults]\n" +
                      "        rdns = false\n" +
                      "        renew_lifetime = 7d\n" +
                      "        ticket_lifetime = 24h\n" +
                      "        dns_lookup_realm = false\n" +
                      "        default_realm = " + KRB5_REALM.toUpperCase() + "\n" +
                      "\n" +
                      "# The following krb5.conf variables are only for MIT Kerberos.\n" +
                      "        kdc_timesync = 1\n" +
                      "        ccache_type = 4\n" +
                      "        forwardable = true\n" +
                      "        proxiable = true\n" +
                      "\n" +
                      "# The following libdefaults parameters are only for Heimdal Kerberos.\n" +
                      "        fcc-mit-ticketflags = true\n" +
                      "\n" +
                      "[realms]\n" +
                      "        " + KRB5_REALM.toUpperCase() + " = {\n" +
                      "                kdc = " + getContainerIpAddress() + ":" + getMappedPort(99) + "\n" +
                      "                admin_server = " + getContainerIpAddress() + "\n" +
                      "        }\n" +
                      "\n" +
                      "[domain_realm]\n" +
                      "        ." + KRB5_REALM.toLowerCase() + " = " + KRB5_REALM.toUpperCase() + "\n" +
                      "        " + KRB5_REALM.toLowerCase() + " = " + KRB5_REALM.toUpperCase() + "\n";
        Files.write(outputPath, conf.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Use generateConf instead
     */
    @Deprecated
    public void configureKerberos() throws IOException {
        Path krbConfPath = Paths.get("/etc/krb5.conf");
        String krbConf = FileUtils.readFile(krbConfPath.toAbsolutePath().toString());

        krbConf = configureProperty(krbConf, "libdefaults", "default_realm", KRB5_REALM);
        krbConf = configureProperty(krbConf, "libdefaults", "dns_lookup_realm", "false");
        krbConf = configureProperty(krbConf, "libdefaults", "ticket_lifetime", "24h");
        krbConf = configureProperty(krbConf, "libdefaults", "renew_lifetime", "7d");
        krbConf = configureProperty(krbConf, "libdefaults", "forwardable", "true");
        krbConf = configureProperty(krbConf, "libdefaults", "rdns", "false");

        if (!krbConf.contains("[realms]")) {
            krbConf += "\n\n[realms]";
        }
        if (!krbConf.contains(KRB5_REALM + " = {")) {
            krbConf = krbConf.replace("[realms]", "[realms]\n\t" +
                                                  KRB5_REALM + " = {\n\t\t" +
                                                  "kdc = " + getContainerIpAddress() + ":" + getMappedPort(99) + "\n\t\t" +
                                                  "admin_server = " + getContainerIpAddress() + "\n\t}\n");
        }

        if (!krbConf.contains("[domain_realm]")) {
            krbConf += "\n\n[domain_realm]";
        }
        krbConf = configureProperty(krbConf, "domain_realm", KRB5_REALM.toLowerCase(), KRB5_REALM);
        krbConf = configureProperty(krbConf, "domain_realm", "." + KRB5_REALM.toLowerCase(), KRB5_REALM);

        Log.info(c, "configureKerberos", "Transformed kerberos config:\n" + krbConf);
        Files.write(krbConfPath, krbConf.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    private static String configureProperty(String krbConf, String section, String key, String value) {
        if (krbConf.contains(key + " = " + value)) {
            // already correct
        } else if (krbConf.contains(key + " = ")) {
            krbConf = krbConf.replaceAll(key.replace(".", "\\.") + " = .*", key + " = " + value);
        } else {
            krbConf = krbConf.replace("[" + section + "]", "[" + section + "]\n\t" + key + " = " + value);
        }
        return krbConf;
    }

    /**
     * This doesn't seem to be necessary because we can simply check in the keytab file
     */
    @Deprecated
    public void generateKeytab(String username, Path output) throws Exception {
        Files.deleteIfExists(output);
        Process proc = Runtime.getRuntime().exec("ktutil");
        OutputStream ktInput = proc.getOutputStream();
        ktInput.write(("add_entry -password -p " + username + "@" + KRB5_REALM +
                       " -k 1 -e aes256-cts\n" + KRB5_PASS + "\nwkt " + output.toAbsolutePath().toString()).getBytes());
        ktInput.flush();
        ktInput.close();
        if (!proc.waitFor(15, TimeUnit.SECONDS)) {
            Log.info(c, "generateKeytab", "Proc timed out... destroying forcibly");
            proc.destroyForcibly();
        }
        Log.info(c, "generateKeytab", "Process stdout:");
        String procOut = "STDOUT:\n" + readInputStream(proc.getInputStream());
        procOut += "\nSTDERR:\n" + readInputStream(proc.getErrorStream());
        Log.info(c, "generateKeytab", procOut);
        if (proc.exitValue() != 0) {
            throw new RuntimeException("Process failed with output: " + procOut);
        }
    }

    private static String readInputStream(InputStream is) {
        @SuppressWarnings("resource")
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private static void log(OutputFrame frame) {
        String msg = frame.getUtf8String();
        if (msg.endsWith("\n"))
            msg = msg.substring(0, msg.length() - 1);
        Log.info(c, "[KRB5]", msg);
    }

    private static boolean hasCachedContainers() {
        if (!FATSuite.REUSE_CONTAINERS)
            return false;
        if (!reuseCache.toFile().exists())
            return false;
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(reuseCache.toFile())) {
            props.load(fis);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return props.containsKey("krb5.hostname") &&
               props.containsKey("krb5.port");
    }
}
