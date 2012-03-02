/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.as.test.integration.ejb.descriptor.configuration;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.fail;

/**
 *
 * @author rhatlapa
 */
@RunWith(Arquillian.class)
public class SimpleBeanTestCase {
    @Deployment
    public static Archive<?> deployment() {
        final JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "ejb-descriptor-configuration-test.jar")
            .addPackage(SessionTypeSpecifiedBean.class.getPackage())
            .addAsManifestResource(SimpleBeanTestCase.class.getPackage(), "jboss-ejb3.xml", "jboss-ejb3.xml")
            .addAsManifestResource(SimpleBeanTestCase.class.getPackage(), "ejb-jar.xml", "ejb-jar.xml");
        return jar;
    }
    
    @Test
    public void testSimpleBeanStateless() throws NamingException {
        final InitialContext ctx = new InitialContext();
        try {
            final SessionTypeSpecifiedBean bean = (SessionTypeSpecifiedBean) 
                    ctx.lookup("java:global/ejb-descriptor-configuration-test/SimpleBeanDefinition!"
                    + "org.jboss.as.test.integration.ejb.descriptor.configuration.SessionTypeSpecifiedBean");
       
            fail("The SimpleBean should not be available");
        } catch (NameNotFoundException e) {
            // good
        }
    }
    
    @Test
    public void testSimpleBeanSingleton() throws NamingException {
        final InitialContext ctx = new InitialContext();
        try {
            SessionTypeSpecifiedBean bean = (SessionTypeSpecifiedBean) 
                    ctx.lookup("java:global/ejb-descriptor-configuration-test/SimpleBeanDefinition!"
                    + "org.jboss.as.test.integration.ejb.descriptor.configuration.SessionTypeSpecifiedBean");
       
            bean.setName("Singleton");
//            bean = new SessionTypeSpecifiedBean();
//            bean.setName("Instance");
            bean = (SessionTypeSpecifiedBean) ctx.lookup("java:global/ejb-descriptor-configuration-test/SimpleBeanDefinition!"
                    + "org.jboss.as.test.integration.ejb.descriptor.configuration.SessionTypeSpecifiedBean");

            Assert.assertEquals("As singleton the name should remained set","Hi Singleton", bean.greet());
        } catch (NameNotFoundException e) {
            
        }
    }
}
