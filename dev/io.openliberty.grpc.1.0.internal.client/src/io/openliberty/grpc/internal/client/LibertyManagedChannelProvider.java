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
package io.openliberty.grpc.internal.client;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.ibm.websphere.ras.Tr;
import com.ibm.websphere.ras.TraceComponent;
import com.ibm.ws.managedobject.ManagedObjectException;

import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannelProvider;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.openliberty.grpc.internal.GrpcManagedObjectProvider;
import io.openliberty.grpc.internal.client.config.GrpcClientConfigHolder;

/**
 * io.grpc.ManagedChannelProvider that takes care of any required Liberty
 * configuration, and then delegates to the NettyChannelBuilder
 */
public class LibertyManagedChannelProvider extends ManagedChannelProvider {

	private static final TraceComponent tc = Tr.register(LibertyManagedChannelProvider.class, GrpcClientMessages.GRPC_TRACE_NAME, GrpcClientMessages.GRPC_BUNDLE);

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public int priority() {
		return 10;
	}

	@Override
	public NettyChannelBuilder builderForAddress(String name, int port) {

		NettyChannelBuilder builder = NettyChannelBuilder.forAddress(name, port);
		configureLibertyBuilder(builder, name, String.valueOf(port));

		return builder;
	}

	@Override
	public NettyChannelBuilder builderForTarget(String target) {
		NettyChannelBuilder builder = NettyChannelBuilder.forTarget(target);
		configureLibertyBuilder(builder, target, "");
		return builder;
	}

	private void configureLibertyBuilder(NettyChannelBuilder builder, String target, String port) {
		addLibertyInterceptors(builder);
		addUserInterceptors(builder, target);
		addLibertySSLConfig(builder, target, port);
		addKeepAliveConfiguration(builder, target);
		addMaxInboundMessageSize(builder, target);
	}

	private void addLibertyInterceptors(NettyChannelBuilder builder) {
		builder.intercept(new LibertyClientInterceptor());
		ClientInterceptor monitoringInterceptor = createMonitoringClientInterceptor();
		if (monitoringInterceptor != null) {
			builder.intercept(monitoringInterceptor);
		}
	}

	private void addLibertySSLConfig(NettyChannelBuilder builder, String target, String port) {
		String sslRef = GrpcClientConfigHolder.getSSLConfig(target);
		SslContext context = null;
		GrpcSSLService sslService = GrpcClientComponent.getGrpcSSLService();
		if (sslService != null) {
			context = sslService.getOutboundClientSSLContext(sslRef, target, port);
			if (context != null) {
				builder.sslContext(context);
			}
		}
	}

	private void addKeepAliveConfiguration(NettyChannelBuilder builder, String target) {
		String keepAliveTime = GrpcClientConfigHolder.getKeepAliveTime(target);
		String keepAlive = GrpcClientConfigHolder.getEnableKeepAlive(target);
		String keepAliveTimeout = GrpcClientConfigHolder.getKeepAliveTimeout(target);

		if (keepAliveTime != null && !keepAliveTime.isEmpty()) {
			int time = Integer.parseInt(keepAliveTime);
			builder.keepAliveTime(time, TimeUnit.SECONDS);
		}
		if (keepAlive != null && !keepAlive.isEmpty()) {
			Boolean enabled = Boolean.parseBoolean(keepAlive);
			builder.keepAliveWithoutCalls(enabled);
		}
		if (keepAliveTimeout != null && !keepAliveTimeout.isEmpty()) {
			int timeout = Integer.parseInt(keepAliveTimeout);
			builder.keepAliveTimeout(timeout, TimeUnit.SECONDS);
		}
	}

	private void addMaxInboundMessageSize(NettyChannelBuilder builder, String target) {
		String maxMsgSizeString = GrpcClientConfigHolder.getMaxInboundMessageSize(target);
		if (maxMsgSizeString != null && !maxMsgSizeString.isEmpty()) {
			int maxSize = Integer.parseInt(maxMsgSizeString);
			if (maxSize == -1) {
				builder.maxInboundMessageSize(Integer.MAX_VALUE);
			} else if (maxSize > 0) {
				builder.maxInboundMessageSize(maxSize);
			}
		}
	}

	private void addUserInterceptors(NettyChannelBuilder builder, String target) {
		String interceptorListString = GrpcClientConfigHolder.getClientInterceptors(target);

		if (interceptorListString != null) {
			List<String> items = Arrays.asList(interceptorListString.split("\\s*,\\s*"));
			if (!items.isEmpty()) {
				for (String className : items) {
					try {
						// use the managed object service to load the interceptor 
						ClientInterceptor interceptor = 
								(ClientInterceptor) GrpcManagedObjectProvider.createObjectFromClassName(className);
						if (interceptor != null) {
							builder.intercept(interceptor);
						}
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
							IllegalArgumentException | InvocationTargetException | NoSuchMethodException |
							SecurityException | ManagedObjectException e) {
						Tr.warning(tc, "invalid.clientinterceptor", e.getMessage());
					}
				}
			}
		}
	}
	
	private ClientInterceptor createMonitoringClientInterceptor() {
		// create the interceptor only if the monitor feature is enabled
		if (!GrpcClientComponent.isMonitoringEnabled()) {
			return null;
		}
		ClientInterceptor interceptor = null;
		// monitoring interceptor 
		final String className = "io.openliberty.grpc.internal.monitor.GrpcMonitoringClientInterceptor";
		try {
			Class<?> clazz = Class.forName(className);
			interceptor = (ClientInterceptor) clazz.getDeclaredConstructor()
					.newInstance();
		} catch (Exception e) {
			// an exception can happen if the monitoring package is not loaded 
        }

		return interceptor;
	}

}
