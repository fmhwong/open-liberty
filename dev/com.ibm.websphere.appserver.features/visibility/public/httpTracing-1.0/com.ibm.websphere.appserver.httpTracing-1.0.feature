-include= ~${workspace}/cnf/resources/bnd/feature.props
symbolicName=com.ibm.websphere.appserver.httpTracing-1.0
visibility=public
singleton=true
IBM-App-ForceRestart: install, \
 uninstall
IBM-ShortName: httpTracing-1.0
Subsystem-Name: HTTP Distributed Tracing 1.0
-features=\
    com.ibm.websphere.appserver.servlet-4.0
-bundles=\
    io.openliberty.http.tracing.internal
kind=noship
edition=core
WLP-Activation-Type: parallel
