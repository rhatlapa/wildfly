/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.as.test.integration.ejb.descriptor.configuration.interceptors;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import junit.framework.Assert;

/**
 *
 * @author rhatlapa
 */
public class InterceptorTests {

    protected void testInterceptor(InitialContext iniCtx, String expected) throws NamingException {
        final SimpleHelloBean helloBean = (SimpleHelloBean) iniCtx.lookup("java:module/SimpleHelloBean");
        Assert.assertEquals("Interception method wasn't changed by jboss spec descriptor", expected, helloBean.
                hello(""));
    }
}
