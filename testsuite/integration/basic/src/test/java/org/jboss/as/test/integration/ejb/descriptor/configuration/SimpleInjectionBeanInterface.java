package org.jboss.as.test.integration.ejb.descriptor.configuration;

/**
 * Created by IntelliJ IDEA.
 * User: rhatlapa
 * Date: 3/5/12
 * Time: 3:23 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SimpleInjectionBeanInterface {
    public void checkInjection() throws RuntimeException;
    public String greetInjectedBean(String greetedBy);
}
