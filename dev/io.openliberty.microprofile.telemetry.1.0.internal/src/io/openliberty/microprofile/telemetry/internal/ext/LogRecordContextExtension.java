/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package io.openliberty.microprofile.telemetry.internal.ext;

import java.util.Map;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;

import com.ibm.websphere.logging.hpel.LogRecordContext;
import com.ibm.websphere.ras.Tr;
import com.ibm.websphere.ras.TraceComponent;

import io.opentelemetry.api.trace.Span;

@Component(service = LogRecordContextExtension.class, immediate = true, configurationPolicy = ConfigurationPolicy.IGNORE, property = "service.vendor=IBM")
public class LogRecordContextExtension {

    private static final TraceComponent tc = Tr.register(LogRecordContextExtension.class);

    private static final String TRACE_ID_KEY = "traceId";
    private static final String SPAN_ID_KEY = "spanId";

    /* LogRecordContext callback to retrieve trace ID */
    private final static LogRecordContext.Extension traceIdCallback = new LogRecordContext.Extension() {
        @Override
        public String getValue() {
            Span currentSpan = Span.current();
            if ((currentSpan != null) && (currentSpan.getSpanContext().isValid())) {
                return currentSpan.getSpanContext().getTraceId();
            }
            return null;
        }
    };

    /* LogRecordContext callback to retrieve span ID */
    private final static LogRecordContext.Extension spanIdCallback = new LogRecordContext.Extension() {
        @Override
        public String getValue() {
            Span currentSpan = Span.current();
            if ((currentSpan != null) && (currentSpan.getSpanContext().isValid())) {
                return currentSpan.getSpanContext().getSpanId();
            }
            return null;
        }
    };

    @Activate
    protected void activate(ComponentContext compcontext, Map<String, Object> properties) {
        Tr.info(tc, "FW registering LogRecordContext extensions");
        LogRecordContext.registerExtension(TRACE_ID_KEY, traceIdCallback);
        LogRecordContext.registerExtension(SPAN_ID_KEY, spanIdCallback);
    }

    @Deactivate
    protected void deactivate(ComponentContext compcontext) {
        Tr.info(tc, "FW unregistering LogRecordContext extensions");
        LogRecordContext.unregisterExtension(TRACE_ID_KEY);
        LogRecordContext.unregisterExtension(SPAN_ID_KEY);
    }
}
