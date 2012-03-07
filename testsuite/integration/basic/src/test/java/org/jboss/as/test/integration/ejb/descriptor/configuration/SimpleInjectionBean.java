package org.jboss.as.test.integration.ejb.descriptor.configuration;

/**
 * Created by IntelliJ IDEA.
 * User: rhatlapa
 * Date: 3/2/12
 * Time: 12:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleInjectionBean implements SimpleInjectionBeanInterface {
    private SessionBean injectedBean;
    
    public void checkInjection() throws RuntimeException {
        if (injectedBean == null) {
            throw new NullPointerException("Bean not injected");
        }
    }
    
    public String greetInjectedBean(String greetedBy) {
        return injectedBean.greet();
    }
}
