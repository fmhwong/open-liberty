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
package com.ibm.ws.opentracing.tracer;

import java.util.List;

import org.apache.cxf.jaxrs.ext.Nullable;

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.BinaryFormat;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.context.propagation.TraceContextFormat;
import io.opentelemetry.internal.Utils;
import io.opentelemetry.resource.Resource;
import io.opentelemetry.trace.BlankSpan;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Sampler;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanData;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceOptions;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.Tracestate;
import io.opentelemetry.trace.unsafe.ContextUtils;

/**
 *
 */
public class OpenTelemetryTracer implements Tracer {

    private static final BinaryFormat<SpanContext> BINARY_FORMAT = new NoopBinaryFormat();
    private static final HttpTextFormat<SpanContext> HTTP_TEXT_FORMAT = new TraceContextFormat();

    @Override
    public Span getCurrentSpan() {
        return ContextUtils.getValue();
    }

    @Override
    public Scope withSpan(Span span) {
        return SpanInScope.create(span);
    }

    @Override
    public Span.Builder spanBuilder(String spanName) {
        return NoopSpanBuilder.create(this, spanName);
    }

    @Override
    public void recordSpanData(SpanData spanData) {
        Utils.checkNotNull(spanData, "spanData");
    }

    @Override
    public void setResource(Resource resource) {
        // do nothing
    }

    @Override
    public Resource getResource() {
        return Resource.getEmpty();
    }

    @Override
    public BinaryFormat<SpanContext> getBinaryFormat() {
        return BINARY_FORMAT;
    }

    @Override
    public HttpTextFormat<SpanContext> getHttpTextFormat() {
        return HTTP_TEXT_FORMAT;
    }

//    private OpenTelemetryTracer() {}

    // Noop implementation of Span.Builder.
    private static final class NoopSpanBuilder implements Span.Builder {
        static NoopSpanBuilder create(Tracer tracer, String spanName) {
            return new NoopSpanBuilder(tracer, spanName);
        }

        private final Tracer tracer;
        private boolean isRootSpan;
        private SpanContext spanContext;

        @Override
        public Span startSpan() {
            if (spanContext == null && !isRootSpan) {
                spanContext = tracer.getCurrentSpan().getContext();
            }

//            return spanContext != null && !SpanContext.BLANK.equals(spanContext) ? new BlankSpan(spanContext) : BlankSpan.INSTANCE;
            return BlankSpan.INSTANCE;
        }

        @Override
        public NoopSpanBuilder setParent(Span parent) {
            Utils.checkNotNull(parent, "parent");
            spanContext = parent.getContext();
            return this;
        }

        @Override
        public NoopSpanBuilder setParent(SpanContext remoteParent) {
            Utils.checkNotNull(remoteParent, "remoteParent");
            spanContext = remoteParent;
            return this;
        }

        @Override
        public NoopSpanBuilder setNoParent() {
            isRootSpan = true;
            return this;
        }

        @Override
        public NoopSpanBuilder setSampler(@Nullable Sampler sampler) {
            return this;
        }

        @Override
        public NoopSpanBuilder addLink(Link link) {
            return this;
        }

        @Override
        public NoopSpanBuilder addLinks(List<Link> links) {
            return this;
        }

        @Override
        public NoopSpanBuilder setRecordEvents(boolean recordEvents) {
            return this;
        }

        @Override
        public NoopSpanBuilder setSpanKind(Span.Kind spanKind) {
            return this;
        }

        private NoopSpanBuilder(Tracer tracer, String name) {
            Utils.checkNotNull(tracer, "tracer");
            Utils.checkNotNull(name, "name");
            this.tracer = tracer;
        }
    }

    private static final class NoopBinaryFormat implements BinaryFormat<SpanContext> {

        @Override
        public byte[] toByteArray(SpanContext spanContext) {
            Utils.checkNotNull(spanContext, "spanContext");
            return new byte[0];
        }

        @Override
        public SpanContext fromByteArray(byte[] bytes) {
            Utils.checkNotNull(bytes, "bytes");
//            return SpanContext.BLANK;
            return SpanContext.create(TraceId.INVALID, SpanId.INVALID, TraceOptions.DEFAULT, Tracestate.builder().build());
        }

        private NoopBinaryFormat() {}
    }

    private static final class SpanInScope implements Scope {
        private final Context previous;
        private final Context current;

        private SpanInScope(Span span) {
            current = ContextUtils.withValue(span);
            previous = current.attach();
        }

        public static SpanInScope create(Span span) {
            return new SpanInScope(span);
        }

        @Override
        public void close() {
            current.detach(previous);
        }
    }

}
