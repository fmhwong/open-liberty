/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.opentracing;

import java.util.HashMap;
import java.util.Map;

import com.ibm.websphere.ras.Tr;
import com.ibm.websphere.ras.TraceComponent;
import com.ibm.ws.opentracing.tracer.OpenTelemetryTracer;

import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.spi.TracerProvider;
import io.opentracing.Span;

/**
 *
 */
public class LibertyTracerProvider implements TracerProvider {

    private static final TraceComponent tc = Tr.register(LibertyTracerProvider.class);

    /** <p>Storage for the open tracing context variable.</p> */
    private static final ThreadLocal<OpentracingContext> OPEN_TRACING_CONTEXT_VAR = new OpentracingThreadLocal();

    private static final Map<String, Tracer> tracerMap = new HashMap<String, Tracer>();

    private static class ApplicationTracersLock {
        // EMPTY
    }

    private static final ApplicationTracersLock applicationTracersLock = new ApplicationTracersLock();

    /**
     * <p>Class for the open tracing context thread local variable.</p>
     *
     * <p>Note the use of type {@link InheritableThreadLocal}. Use of this type
     * means that threads which are spawned from the thread which initially handles
     * a request are be given a reference to the thread local value. That provides
     * the capability of handling outgoing requests in spawned threads.</p>
     *
     * <p>As a consequence, which must be handled then within the {@link Span}
     * implementation, is that the relationship of parent incoming requests to
     * child outgoing requests is not one to zero-or-one. The relationship is
     * one to zero-or-more.</p>
     */
    private static final class OpentracingThreadLocal extends InheritableThreadLocal<OpentracingContext> {
        /**
         * <p>Provide value obtained by the first call to {@link ThreadLocal#get()} for
         * the open tracing context variable. The value which is returned is used unless
         * a prior call is made to {@link ThreadLocal#set(Object)}.</p>
         *
         * TODO: The open tracing context is never cleared. This needs to be reviewed against
         * the probably reuse of threads for handling requests, both from the perspective
         * of a new request seeing a previously set tracing context, and from the
         * perspective of open tracing objects lasting longer than is necessary.
         *
         * @return The initial open tracing context value.
         */
        @Override
        protected OpentracingContext initialValue() {
            return new OpentracingContext();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Tracer get() {
        OpentracingContext tracerContext = getOpentracingContext();
        String appName = tracerContext.getAppName();
        Tracer tracer = tracerContext.getTracer();

        if (tracer == null) {
            appName = OpentracingUtils.lookupAppName();
            tracer = ensureTracer(appName);
            tracerContext.setTracer(appName, tracer);
        }

        return tracer;
    }

    private static Tracer ensureTracer(String appName) {

        Tracer tracer;

        synchronized (applicationTracersLock) {
            tracer = getTracer(appName);
            if (tracer == null) {
                tracer = createTracer(appName);
                putTracer(appName, tracer);
            }
        }
        return tracer;
    }

    /**
     * <p>Have the open tracer factory service create the tracer. That
     * bridges to a user feature, which enables user supplied tracer
     * implementations.</p>
     *
     * @param appName The name of the application for which to create a tracer.
     *
     * @return The new tracer.
     */
    private static Tracer createTracer(String appName) {
        return new OpenTelemetryTracer();
    }

    private static Tracer getTracer(String appName) {
        return tracerMap.get(appName);
    }

    private static Tracer putTracer(String appName, Tracer tracer) {
        return tracerMap.put(appName, tracer);
    }

    private static Tracer removeTracer(String appName, Tracer tracer) {
        return tracerMap.remove(appName);
    }

    /**
     * <p>Answer the active open tracing context.</p>
     *
     * <p>This is currently stored as a thread local value, with the association
     * to the thread which received an incoming service request. Placement in
     * a thread local variable makes the value available to outgoing requests
     * which are made in the same thread.</p>
     *
     * <p>An inheritable thread local variable (see {@link InheritableThreadLocal}) is
     * used for the storage, which means that new threads will be given a reference
     * to the context, which in turn means that asynchronous outgoing requests are
     * handled.</p>
     *
     * <p>Implementations other than a thread local variable are being considered.
     * The problem which must be solved is the association of outgoing requests
     * to incoming requests. That association is needed to associate the open
     * tracing span which was created for the incoming request with the open tracing
     * span which is created for an outgoing request. Service APIs do not provide
     * a mechanism to convey this association.</p>
     *
     * @return The active open tracing context. This should never be null.
     */
    private static OpentracingContext getOpentracingContext() {
        return OPEN_TRACING_CONTEXT_VAR.get();
        // Rely on 'initialValue' to supply a non-null open tracing context.
        // There is currently no code which clears the context.
    }

}
