/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.as.test.integration.ejb.descriptor.configuration;

import javax.annotation.Resource;
import javax.ejb.SessionContext;

/**
 *
 * @author rhatlapa
 */

public class RoleProtectedBean {
    
    @Resource
    SessionContext ctx;
    
    public String defaultEcho(final String message) {
        return message;
    }

    public String permitAllEcho(final String message) {
        return message;
    }

    public String denyAllEcho(final String message) {
        return message;
    }

    public String role2Echo(final String message) {
        return message;
    }
    
    public boolean isInRole(String role) {
        return ctx.isCallerInRole(role);
    }
}
