/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.ws.wssecurity.fat.cxf.samltoken1.common;

import org.junit.Test;
import org.junit.runner.RunWith;

import componenttest.annotation.SkipForRepeat;

import com.gargoylesoftware.htmlunit.WebClient;
import com.ibm.ws.security.saml20.fat.commonTest.SAMLCommonTest;
import com.ibm.ws.security.saml20.fat.commonTest.SAMLCommonTestHelpers;
import com.ibm.ws.security.saml20.fat.commonTest.SAMLConstants;
import com.ibm.ws.security.saml20.fat.commonTest.SAMLTestSettings;
import componenttest.annotation.AllowedFFDC;
import componenttest.custom.junit.runner.FATRunner;
import componenttest.custom.junit.runner.Mode;
import componenttest.custom.junit.runner.Mode.TestMode;
import componenttest.topology.impl.LibertyServerWrapper;
import static componenttest.annotation.SkipForRepeat.EE8_FEATURES;
import static componenttest.annotation.SkipForRepeat.EE9_FEATURES;
import static componenttest.annotation.SkipForRepeat.NO_MODIFICATION;
import componenttest.rules.repeater.JakartaEE9Action;


/**
 * The testcases in this class were ported from tWAS' test SamlWebSSOTests.
 * If a tWAS test is not applicable, it will be noted in the comments below.
 * If a tWAS test fits better into another test class, it will be noted
 * which test project/class it now resides in.
 * In general, these tests perform a simple IdP initiated SAML Web SSO, using
 * httpunit to simulate browser requests. In this scenario, a Web client
 * accesses a static Web page on IdP and obtains a a SAML HTTP-POST link
 * to an application installed on a WebSphere SP. When the Web client
 * invokes the SP application, it is redirected to a TFIM IdP which issues
 * a login challenge to the Web client. The Web Client fills in the login
 * form and after a successful login, receives a SAML 2.0 token from the
 * TFIM IdP. The client invokes the SP application by sending the SAML
 * 2.0 token in the HTTP POST request.
 */

@LibertyServerWrapper
@RunWith(FATRunner.class)
public class CxfSSLSAMLBasicTests extends SAMLCommonTest {

    private static final Class<?> thisClass = CxfSSLSAMLBasicTests.class;
    protected static String servicePort = null;
    protected static String serviceSecurePort = null;
    protected static CXFSAMLCommonUtils commonUtils = new CXFSAMLCommonUtils();
    //issue 18363
    protected static String featureVersion = "";

    //issue 18363
    public static String getFeatureVersion() {
        return featureVersion;
    }
    
    public static void setFeatureVersion(String version) {
        featureVersion = version;
    } //End of issue 18363

    /**
     * TestDescription:
     * 
     * This test invokes a jax-ws cxf service client, which invokes
     * a jax-ws cxf SAML web service.
     * Transport/Https is in the server side policy.
     * The service client uses the server side policy.
     * The service client invokes the request using https.
     * Test should succeed in accessing the server side service.
     * 
     */
 
    @Mode(TestMode.LITE)
    @SkipForRepeat({ NO_MODIFICATION, EE8_FEATURES })
    @AllowedFFDC(value = { "java.util.MissingResourceException" }, repeatAction = { JakartaEE9Action.ID })
    @Test
    public void testSAMLCxfSvcClient_TransportEnabled() throws Exception {
    	
        if (testSAMLServer2 == null) {
            //1 server reconfig
    	    testSAMLServer.reconfigServer(buildSPServerName("server_2_in_1_ee8.xml"), _testName, SAMLConstants.NO_EXTRA_MSGS, SAMLConstants.JUNIT_REPORTING);
    	} else {
    	    //2 servers reconfig
    	    testSAMLServer2.reconfigServer("server_2_ee8.xml", _testName, SAMLConstants.NO_EXTRA_MSGS, SAMLConstants.JUNIT_REPORTING);
    	    testSAMLServer.reconfigServer("server_1_wss4j.xml", _testName, SAMLConstants.NO_EXTRA_MSGS, SAMLConstants.JUNIT_REPORTING);
    	} 
    	
        WebClient webClient = SAMLCommonTestHelpers.getWebClient();

        SAMLTestSettings updatedTestSettings = testSettings.copyTestSettings();
        updatedTestSettings.updatePartnerInSettings("sp1", true);
        updatedTestSettings.setCXFSettings(_testName, null, servicePort, serviceSecurePort, "user1", "user1pwd", "SamlTokenTransportSecure",
                "SamlTokenTransportSecurePort", "", "False", null, null);

        genericSAML(_testName, webClient, updatedTestSettings, standardFlow, helpers.setDefaultGoodSAMLCXFExpectations(null, flowType, updatedTestSettings, SAMLConstants.CXF_SSL_SAML_TOKEN_SERVICE));
    
    }
    
    /**
     * TestDescription:
     * 
     * This test invokes a jax-ws cxf service client, which invokes
     * a jax-ws cxf SAML web service.
     * Transport/Https is in the server side policy.
     * The service client uses the server side policy.
     * The service client invokes the request using http only.
     * Test should fail as policy can't be enforced.
     * 
     */
    
    @Mode(TestMode.FULL)
    @SkipForRepeat({ EE9_FEATURES })
    @Test
    public void testSAMLCxfSvcClient_TransportEnabled_httpFromClient() throws Exception {

    	//issue 18363
    	if ("EE8".equals(getFeatureVersion())) {
    		if (testSAMLServer2 == null) {
                //1 server reconfig
        		testSAMLServer.reconfigServer(buildSPServerName("server_2_in_1_ee8.xml"), _testName, SAMLConstants.NO_EXTRA_MSGS, SAMLConstants.JUNIT_REPORTING);
        	} else {
        	    //2 servers reconfig
        		testSAMLServer2.reconfigServer("server_2_ee8.xml", _testName, SAMLConstants.NO_EXTRA_MSGS, SAMLConstants.JUNIT_REPORTING);
        		testSAMLServer.reconfigServer("server_1_wss4j.xml", _testName, SAMLConstants.NO_EXTRA_MSGS, SAMLConstants.JUNIT_REPORTING);
        	} 
    	} //End of 18363
    	
        WebClient webClient = SAMLCommonTestHelpers.getWebClient();

        SAMLTestSettings updatedTestSettings = testSettings.copyTestSettings();
        updatedTestSettings.updatePartnerInSettings("sp1", true);
        updatedTestSettings.setCXFSettings(_testName, null, servicePort, null, "user1", "user1pwd", "SamlTokenTransportSecure",
                "SamlTokenTransportSecurePort", "", "False", null, null);

        //issue 18363
    	if ("EE7".equals(getFeatureVersion())) {
            genericSAML(_testName, webClient, updatedTestSettings, standardFlow, helpers.setErrorSAMLCXFExpectations(null, flowType, updatedTestSettings, SAMLConstants.CXF_SAML_TOKEN_SERVICE_HTTPS_NOT_USED));
    	}
    	if ("EE8".equals(getFeatureVersion())) {
    		String CXF_SAML_TOKEN_SERVICE_HTTPS_NOT_USED = "HttpsToken could not be asserted: Not an HTTPs connection"; // @AV999 slightly different error with new runtime
            genericSAML(_testName, webClient, updatedTestSettings, standardFlow, helpers.setErrorSAMLCXFExpectations(null, flowType, updatedTestSettings, CXF_SAML_TOKEN_SERVICE_HTTPS_NOT_USED));
    	} //End of 18363
    }
  
    /**
     * TestDescription:
     * 
     * This test invokes a jax-ws cxf service client, which invokes
     * a jax-ws cxf SAML web service.
     * Transport/Https is NOT in the server side policy.
     * The service client uses the server side policy.
     * The service client invokes the request using https.
     * Test should succeed in accessing the server side service as the client side is
     * "more secure" than the server
     * 
     */
 
    @Mode(TestMode.LITE)
    @SkipForRepeat({ NO_MODIFICATION, EE8_FEATURES })
    @AllowedFFDC(value = { "java.util.MissingResourceException" }, repeatAction = { JakartaEE9Action.ID })
    @Test
    public void testSAMLCxfSvcClient_TransportNotEnabled_httpsFromClient() throws Exception {
  
    	if (testSAMLServer2 == null) {
            //1 server reconfig
    	    testSAMLServer.reconfigServer(buildSPServerName("server_2_in_1_ee8.xml"), _testName, SAMLConstants.NO_EXTRA_MSGS, SAMLConstants.JUNIT_REPORTING);
    	} else {
    	    //2 servers reconfig
    	    testSAMLServer2.reconfigServer("server_2_ee8.xml", _testName, SAMLConstants.NO_EXTRA_MSGS, SAMLConstants.JUNIT_REPORTING);
		    testSAMLServer.reconfigServer("server_1_wss4j.xml", _testName, SAMLConstants.NO_EXTRA_MSGS, SAMLConstants.JUNIT_REPORTING);
    	} 
    	
        WebClient webClient = SAMLCommonTestHelpers.getWebClient();

        SAMLTestSettings updatedTestSettings = testSettings.copyTestSettings();
        updatedTestSettings.updatePartnerInSettings("sp1", true);
        updatedTestSettings.setCXFSettings(_testName, null, servicePort, serviceSecurePort, "user1", "user1pwd", "SAMLSOAPService2",
                "SAMLSoapPort2", "", "False", null, null);

        genericSAML(_testName, webClient, updatedTestSettings, standardFlow, helpers.setDefaultGoodSAMLCXFExpectations(null, flowType, updatedTestSettings));
    	
    }
    
}
