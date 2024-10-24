/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.security.mp.jwt11.fat.envVarsTests;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.ibm.ws.security.jwt.fat.mpjwt.MpJwtFatConstants;
import com.ibm.ws.security.mp.jwt11.fat.sharedTests.MPJwtGoodMPConfigAsEnvVars;
import com.ibm.ws.security.mp.jwt11.fat.utils.MPConfigSettings;

import componenttest.custom.junit.runner.FATRunner;
import componenttest.custom.junit.runner.Mode;
import componenttest.custom.junit.runner.Mode.TestMode;

/**
 * This is the test class that will verify that we get the correct behavior when we
 * have mp-config defined as environment variables.
 * We'll test with a server.xml that will NOT have a mpJwt config, the app will NOT have mp-config specified
 * Therefore, we'll be able to show that the config is coming from the environment variables
 *
 * (we're just proving that we can obtain the mp-config via environment variables. It's easier/quicker
 * to test the behavior of each config attribute by setting them in an app (environment variables would
 * require a different server for each config setting).
 **/

@Mode(TestMode.FULL)
@RunWith(FATRunner.class)
public class MPJwtGoodMPConfigAsEnvVars_UsePublicKey_NoKeyLoc extends MPJwtGoodMPConfigAsEnvVars {

    public static Class<?> thisClass = MPJwtGoodMPConfigAsEnvVars_UsePublicKey_NoKeyLoc.class;

    @BeforeClass
    public static void setUp() throws Exception {

        setUpAndStartBuilderServer(jwtBuilderServer, "server_using_buildApp.xml");

        MPConfigSettings mpConfigSettings = new MPConfigSettings(MPConfigSettings.PublicKeyLocationNotSet, MPConfigSettings.ComplexPublicKey, MPConfigSettings.IssuerNotSet, MpJwtFatConstants.X509_CERT);
        setUpAndStartRSServerForTests(resourceServer, "rs_server_AltConfigNotInApp_noServerXmlConfig.xml", mpConfigSettings, MPConfigLocation.ENV_VAR);

    }

}
