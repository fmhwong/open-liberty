-include= ~${workspace}/cnf/resources/bnd/feature.props
symbolicName=io.openliberty.restfulWS-3.0
visibility=public
singleton=true
IBM-API-Package: com.ibm.websphere.jaxrs.server; type="ibm-api", \
IBM-App-ForceRestart: uninstall, \
 install
IBM-ShortName: restfulWS-3.0
Subsystem-Name: Java RESTful Web Services 3.0
-features=\
 io.openliberty.restfulWSClient-3.0, \
 io.openliberty.internal.restfulWS-3.0, \
 com.ibm.websphere.appserver.eeCompatible-9.0, \
 io.openliberty.cdi-3.0
kind=noship
edition=full
WLP-Activation-Type: parallel
