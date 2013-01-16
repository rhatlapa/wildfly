/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.as.test.manualmode.web.valve.authenticator;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.jboss.logging.Logger;

/**
 *
 * @author rhatlapa
 */
public class CustomAuthenticator extends AuthenticatorBase {
     private static final Logger log = Logger.getLogger(CustomAuthenticator.class);


    private String testparam = "global";

    public void setTestparam(String testparam) {
        this.testparam = testparam;
    }

    public String getTestparam() {
        return this.testparam;
    }

//    @Override
    protected boolean authenticate(Request request, HttpServletResponse response, LoginConfig lc) throws IOException {
        log.info("Authentication via custom valve authenticator");
        response.addHeader("valve", testparam);
        log.info("Valve " + CustomAuthenticator.class.getName() + " was hit and adding header parameter 'authenticated' with value " + testparam);
        return true;
    }
    
    @Override
    public void invoke(Request request, Response response)
        throws IOException, ServletException {
        log.info("Valve invocation");
        authenticate(request, response, new LoginConfig());
        getNext().invoke(request, response);
        
    }
}
