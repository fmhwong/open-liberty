/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.opentracing.cdi;

import javax.enterprise.inject.Produces;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.opentracingshim.TracerShim;

public class OpentracingProducerBean {
    @Produces
    public TracerShim getTracer() {
//        return OpentracingTracerManager.getTracer();
        return new TracerShim(OpenTelemetry.getTracer());
    }
}
