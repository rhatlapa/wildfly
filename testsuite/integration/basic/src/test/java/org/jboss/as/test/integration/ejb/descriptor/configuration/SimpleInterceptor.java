/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.as.test.integration.ejb.descriptor.configuration;

import javax.interceptor.InvocationContext;

/**
 *
 * @author rhatlapa
 */
public class SimpleInterceptor {

    public Object helloIntercept(InvocationContext ctx)
            throws Exception {
        Object[] params = ctx.getParameters();
        String name = (String) params[0];
        params[0] = "Intercepted" + name;
        ctx.setParameters(params);
        Object res = ctx.proceed();
        return res;
    }
    
    public Object redefinedHelloIntercept(InvocationContext ctx)
            throws Exception {
        Object[] params = ctx.getParameters();
        String name = (String) params[0];
        params[0] = "JbossSpecIntercepted" + name;
        ctx.setParameters(params);
        Object res = ctx.proceed();
        return res;
    }
}
