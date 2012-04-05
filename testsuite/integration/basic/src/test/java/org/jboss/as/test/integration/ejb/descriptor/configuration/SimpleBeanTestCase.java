/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.as.test.integration.ejb.descriptor.configuration;

import org.jboss.as.test.integration.ejb.descriptor.configuration.resources.ResourceDrivenBean;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.validation.constraints.AssertTrue;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.metadata.ejb.spec.MethodMetaData;
import org.jboss.security.SimplePrincipal;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.fail;
import org.junit.Ignore;

/**
 *
 * @author rhatlapa
 */
@RunWith(Arquillian.class)
public class SimpleBeanTestCase {

    @ArquillianResource
    private InitialContext ctx;

    @Deployment
    public static Archive<?> deployment() {
        final JavaArchive jar = ShrinkWrap.create(JavaArchive.class,
                "ejb-descriptor-configuration-test.jar")
                .addPackage(SessionTypeSpecifiedBean.class.getPackage())
                .addAsManifestResource(SimpleBeanTestCase.class.getPackage(), "jboss-ejb3.xml", "jboss-ejb3.xml")
                .addAsManifestResource(SimpleBeanTestCase.class.getPackage(), "ejb-jar.xml", "ejb-jar.xml");
        return jar;
    }

    @Test
    public void testSimpleBeanStateless() throws NamingException {
        try {
            final SessionTypeSpecifiedBean bean = (SessionTypeSpecifiedBean) ctx.lookup("java:module/simpleBeanDefinitionUnknown");
            fail("The SimpleBean should not be available");
        } catch (NameNotFoundException e) {
            // good
        }
    }

    @Test
    public void testSimpleBeanSingleton() throws NamingException {
        SessionTypeSpecifiedBean bean = (SessionTypeSpecifiedBean) ctx.lookup("java:module/simpleBeanDefinition");

        bean.setName("Singleton");
        bean = (SessionTypeSpecifiedBean) ctx.lookup("java:module/simpleBeanDefinition");

        Assert.assertEquals("As singleton the name should remained set", "Hi Singleton", bean.greet());

    }

    @Test
    public void testBeanInjection() throws NamingException {
        final SimpleInjectionBeanInterface bean = (SimpleInjectionBeanInterface) ctx.lookup("java:module/simpleBeanWithInjection");
        try {
            bean.checkInjection();
            Assert.assertEquals("Bean wasn't redefined by JBoss specific descriptor to use SimpleSessionBean",
                    "Redefined Greetings", bean.greetInjectedBean("InjectionTest"));
        } catch (RuntimeException e) {
            fail("Bean is not correctly injected");

        }
    }

    @Test
    public void testInterceptor() throws NamingException {
        final SimpleHelloBean helloBean = (SimpleHelloBean) ctx.lookup("java:module/simpleHelloBean");
        Assert.assertEquals("Interception method wasn't changed by jboss spec descriptor", "Hello JbossSpecInterceptedEjbIntercepted", helloBean.hello(""));
    }

    @Test
    public void testTransactionStatus() throws SystemException, NotSupportedException, NamingException {
        final UserTransaction userTransaction = (UserTransaction) new InitialContext().lookup("java:jboss/UserTransaction");
        final TransactionBean bean = (TransactionBean) ctx.lookup("java:module/transactionBean");
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, bean.transactionStatus());
        try {
            userTransaction.begin();
            bean.transactionStatus();
            throw new RuntimeException("Expected an exception");
        } catch (EJBException ex) {
            // ignore
        } finally {
            userTransaction.rollback();
        }
    }
}
