/*******************************************************************************
 * Copyright (c) 2003, 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.tra;

import java.io.PrintStream;
import java.io.Serializable;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.XATerminator;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;

import com.ibm.ejs.ras.Tr;
import com.ibm.ejs.ras.TraceComponent;
import com.ibm.tra.inbound.impl.ActivationSpecImpl;
import com.ibm.tra.trace.DebugTracer;

@SuppressWarnings("serial")
public class SimpleRAImpl implements ResourceAdapter, Serializable {

    static final String className = SimpleRAImpl.class.getName();

    static final String RAS_COMPONENT = "J2CResourceAdapter";
    static public final String RAS_GROUP = "ToyRA";

    private static final TraceComponent tc = Tr.register(className, RAS_GROUP,
                                                         null);

    @SuppressWarnings("unused")
    private transient BootstrapContext myBootstrapCtx = null;

    @SuppressWarnings("unused")
    private transient WorkManager myWorkManager = null;

    private transient XATerminator myXATerminator = null;

    // Resource Adapter Configuration Properties

    String serverName;

    String userName1;

    String password1;

    Boolean debugMessages = false;

    Boolean debugActivationSpec = false;

    String outputName = null;

    Boolean printClassLoader = false;

    Boolean dumpStack = false;

    /**
     * The start method of the ResourceAdapter JavaBean is called each time a
     * resource adapter instance is created. This may be during resource adapter
     * deployment, application server startup, or other situations.
     * <P>
     * The application server must use a new ResourceAdapter JavaBean for
     * managing the lifecycle of each resource adapter instance and must discard
     * the ResourceAdapter JavaBean after its stop method has been called. That
     * is, the application server must not reuse the same ResourceAdapter
     * JavaBean object to manage multiple instances of a resource adapter, since
     * the ResourceAdapter JavaBean object may contain resource adapter instance
     * specific state information.
     *
     */
    @Override
    public void start(BootstrapContext serverCtx) throws ResourceAdapterInternalException {
        final String methodName = "start";
        Tr.entry(tc, methodName, serverCtx);

        myBootstrapCtx = serverCtx;
        myWorkManager = serverCtx.getWorkManager();
        myXATerminator = serverCtx.getXATerminator();

        // Toggle these here just for good measure.
        DebugTracer.setDumpStack(dumpStack);
        DebugTracer.setPrintClassLoader(printClassLoader);
        if (myXATerminator == null)
            Tr
                            .debug(
                                   tc,
                                   methodName,
                                   "***** ERROR ***** The XATerminator instance returned from the BoostrapContext is null.");
        else
            Tr.debug(tc, methodName, "The XATerminator instance class is "
                                     + myXATerminator.getClass().getName());

        /*
         * == B@rkk ========================= Remove MBean-Registration-related
         * Test code ======================== // Register and Activate the MBean
         * try { MBeanFactory mbeanFac = AdminServiceFactory.getMBeanFactory();
         * _runtimeCollab = this.new Collab(); mbeanFac.activateMBean(
         * MBEAN_TYPE, _runtimeCollab, MBEAN_CONFIGID, MBEAN_DESCRIPTOR );
         * Tr.debug(tc, methodName, MBEAN_TYPE + " MBean activated." ); } catch
         * ( com.ibm.websphere.management.exception.AdminException ex ) {
         * traceLogger.exception( RASITraceEvent.TYPE_ERROR_EXC, this,
         * methodName, ex ); throw new ResourceAdapterInternalException(
         * "Unexpected Exception registering the MBean.", ex ); } == E@rkk ==
         */

        Tr.debug(tc, methodName,
                 "BURA0010I: Outbound ToyRA started successfully....");
        Tr.exit(tc, methodName);
    }

    /**
     * The application server calls the stop method on the ResourceAdapter
     * JavaBean to notify the resource adapter instance to stop functioning so
     * that it can be safely unloaded. This is a shutdown notification from the
     * application server, and this method is called by an application server
     * thread.
     * <P>
     * The ResourceAdapter JavaBean is responsible for performing an orderly
     * shutdown of the resource adapter instance during the stop method call.
     * This may involve closing network endpoints, relinquishing threads,
     * releasing all active Work instances, and flushing any cached data to the
     * EIS.
     *
     */
    @Override
    public void stop() {
        final String methodName = "stop";
        Tr.entry(tc, methodName);
        Tr.debug(tc, methodName, "BURA0090I: Outbound ToyRA stopped.");
        Tr.exit(tc, methodName);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * javax.resource.spi.ResourceAdapter#endpointActivation(javax.resource.
     * spi.endpoint.MessageEndpointFactory, javax.resource.spi.ActivationSpec)
     */
    @Override
    public void endpointActivation(MessageEndpointFactory msgEndpointFac,
                                   ActivationSpec activationSpec) throws ResourceException {

        boolean debug = debugActivationSpec;
        PrintStream out = DebugTracer.getPrintStream();
        if (debug) {
            out
                            .println("endpointActivation called with following parameters: ");
            out.println("MEF: " + msgEndpointFac.toString());
            out.println("ActivationSpec: " + activationSpec.toString());
        }
        if (activationSpec instanceof ActivationSpecImpl) {
            if (debug) {
                ActivationSpecImpl aSpec = (ActivationSpecImpl) activationSpec;
                out.println("Recieved com.ibm.inbound.impl.ActivationSpecImpl");
                out.println("Contents of ActivationSpec: ");
                out.println("prop1: " + aSpec.getProp1());
                out.println("destType: " + aSpec.getDestinationType());
                if (aSpec.getDestination() != null) {
                    out.println("destination: "
                                + aSpec.getDestination().toString());
                } else {
                    out.println("destination is null");
                }
            }
        } else {
            if (debug) {
                out.println("ActivationSpec of type: ["
                            + activationSpec.getClass().getName()
                            + "] and is not supported by this resource adapter.");
            }
            throw new NotSupportedException("The activationSpec passed is not supported by the resource adapter");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * javax.resource.spi.ResourceAdapter#endpointDeactivation(javax.resource
     * .spi.endpoint.MessageEndpointFactory, javax.resource.spi.ActivationSpec)
     */
    @Override
    public void endpointDeactivation(MessageEndpointFactory msgEndpointFac,
                                     ActivationSpec activationSpec) {

        boolean debug = debugActivationSpec;
        PrintStream out = DebugTracer.getPrintStream();
        if (debug) {
            out.println("endpointDeactivation called with: ");
            out.println("MEF: " + msgEndpointFac.toString());
            out.println("ActivationSpec: " + activationSpec.toString());
        }
        if (activationSpec instanceof ActivationSpecImpl) {
            ActivationSpecImpl aSpec = (ActivationSpecImpl) activationSpec;
            if (debug) {
                out.println("Recieved com.ibm.inbound.impl.ActivationSpecImpl");
                out.println("Contents of ActivationSpec: ");
                out.println("prop1:" + aSpec.getProp1());
                out.println("destType: " + aSpec.getDestinationType());
                if (aSpec.getDestination() != null) {
                    out.println("destination:"
                                + aSpec.getDestination().toString());
                } else {
                    out.println("destination is null");
                }
            }
        } else {
            if (debug) {
                out.println("Received activationSpec of type: "
                            + activationSpec.getClass().getName());
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * javax.resource.spi.ResourceAdapter#getXAResources(javax.resource.spi.
     * ActivationSpec[])
     */
    @Override
    public XAResource[] getXAResources(ActivationSpec[] arg0) throws ResourceException {

        final String methodName = "getXAResources";
        Tr.entry(tc, methodName);
        Tr.exit(tc, methodName);
        return null;
    }

    public Boolean getDebugActivationSpec() {
        return new Boolean(debugActivationSpec);
    }

    public Boolean getDebugMessages() {
        return new Boolean(debugMessages);
    }

    public Boolean getDumpStack() {
        System.out.println("In getDumpStack()");
        return new Boolean(dumpStack);
    }

    public String getOutputName() {
        return outputName;
    }

    public Boolean getPrintClassLoader() {
        System.out.println("In getPrintClassLoader()");
        return new Boolean(printClassLoader);
    }

    public void setDebugActivationSpec(Boolean val) {
        debugActivationSpec = val.booleanValue();
        DebugTracer.setDebugActivationSpec(debugActivationSpec);
    }

    public void setDebugMessages(Boolean val) {
        debugMessages = val.booleanValue();
        DebugTracer.setDebugMessages(debugMessages);
    }

    public void setDumpStack(Boolean val) {
        System.out.println("In setDumpStack(), val: " + val.booleanValue());
        dumpStack = val.booleanValue();
        DebugTracer.setDumpStack(dumpStack);
    }

    public void setOutputName(String fileName) {
        // if the fileName is null, is empty, equals "null", or equals
        // "System.out", use system.out
        if (fileName == null || fileName.equals("") || fileName.equals("null")
            || fileName.equals("System.out")) {
            outputName = "System.out";
            DebugTracer.setPrintStream(System.out);
        } else if (fileName.equals("System.err")) {
            outputName = "System.err";
            DebugTracer.setPrintStream(System.err);
        } else {
            outputName = fileName;
            try {
                DebugTracer.setPrintStream(new PrintStream(outputName));
            } catch (Exception e) {
                System.out
                                .println("An error occurred while trying to open the new file: "
                                         + fileName + " " + e);
            }
        }
    }

    public void setPrintClassLoader(Boolean val) {
        System.out.println("In setPrintClassLoader(), val: " + val.toString());
        printClassLoader = val.booleanValue();
        DebugTracer.setPrintClassLoader(val.booleanValue());
    }

    /*
     * @return the serverName
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * @param serverName the serverName to set
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    /**
     * @return the userName
     */
    public String getUserName1() {
        return userName1;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName1(String userName) {
        this.userName1 = userName;
    }

    /**
     * @return the password
     */
    public String getPassword1() {
        return password1;
    }

    /**
     * @param password the password to set
     */
    public void setPassword1(String password) {
        this.password1 = password;
    }
}
