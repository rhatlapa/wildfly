/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.as.test.integration.ejb.descriptor.configuration;

import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
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

    @Deployment(name="jboss-spec")
    public static Archive<?> deploymentJbossSpec() {
        final JavaArchive jar = ShrinkWrap.create(JavaArchive.class,
                "ejb-descriptor-configuration-test.jar")
                .addPackage(SessionTypeSpecifiedBean.class.getPackage())
                .addAsManifestResource(SimpleBeanTestCase.class.getPackage(), "jboss-ejb3.xml", "jboss-ejb3.xml");
        return jar;
    }
    
    @Deployment(name="ejb3-specVsJboss-spec")
    public static Archive<?> deployment() {
        final JavaArchive jar = ShrinkWrap.create(JavaArchive.class,
                "ejb-descriptor-configuration-test.jar")
                .addPackage(SessionTypeSpecifiedBean.class.getPackage())
                .addAsManifestResource(SimpleBeanTestCase.class.getPackage(), "jboss-ejb3.xml", "jboss-ejb3.xml")
                .addAsManifestResource(SimpleBeanTestCase.class.getPackage(), "ejb-jar.xml", "ejb-jar.xml");
        return jar;
    }

    @Test
    @OperateOnDeployment(value="jboss-spec")
    public void testTransactionStatusJbossSpec() throws NamingException, SystemException, NotSupportedException {
        testTransactionStatus();
    }
    
    @Test
    @OperateOnDeployment(value="jboss-spec")
    public void testBeanInjectionJbossSpec() throws NamingException, SystemException, NotSupportedException {
        testBeanInjection();
    }
    
    @Test
    @OperateOnDeployment(value="jboss-spec")
    public void testStatelessBeanJbossSpec() throws NamingException, SystemException, NotSupportedException {
        testSimpleBeanStateless();
    }
    
    @Test
    @OperateOnDeployment(value="jboss-spec")
    public void testSingletonBeanJbossSpec() throws NamingException, SystemException, NotSupportedException {
        testSimpleBeanSingleton();
    }
    
        @Test
    @OperateOnDeployment(value="ejb3-specVsJboss-spec")
    public void testTransactionStatusWithJbossSpecRedefinition() throws NamingException, SystemException, NotSupportedException {
        testTransactionStatus();
    }
    
    @Test
    @OperateOnDeployment(value="ejb3-specVsJboss-spec")
    public void testBeanInjectionWithJbossSpecRedefinition() throws NamingException, SystemException, NotSupportedException {
        testBeanInjection();
    }
    
    @Test
    @OperateOnDeployment(value="ejb3-specVsJboss-spec")
    public void testStatelessBeanWithJbossSpecRedefinition() throws NamingException, SystemException, NotSupportedException {
        testSimpleBeanStateless();
    }
    
    @Test
    @OperateOnDeployment(value="ejb3-specVsJboss-spec")
    public void testSingletonBeanWithJbossSpecRedefinition() throws NamingException, SystemException, NotSupportedException {
        testSimpleBeanSingleton();
    }
    
    @Ignore
    @Test
    @OperateOnDeployment(value="ejb3-specVsJboss-spec")
    public void testInterceptorWithJbossSpecRedefinition() throws NamingException, SystemException, NotSupportedException {
        testInterceptor("Hello JbossSpecInterceptedEjbIntercepted");
    }
    
    @Test
    @OperateOnDeployment(value="jboss-spec")
    public void testInterceptorJbossSpec() throws NamingException, SystemException, NotSupportedException {
        testInterceptor("Hello JbossSpecIntercepted");
    }
    
    private void testSimpleBeanStateless() throws NamingException {
        try {
            final SessionTypeSpecifiedBean bean = (SessionTypeSpecifiedBean) ctx.lookup("java:module/SimpleBeanDefinitionUnknown");
            fail("The SimpleBean should not be available");
        } catch (NameNotFoundException e) {
            // good
        }
    }

    private void testSimpleBeanSingleton() throws NamingException {
        SessionTypeSpecifiedBean bean = (SessionTypeSpecifiedBean) ctx.lookup("java:module/SimpleBeanDefinition");

        bean.setName("Singleton");
        bean = (SessionTypeSpecifiedBean) ctx.lookup("java:module/SimpleBeanDefinition");

        Assert.assertEquals("As singleton the name should remained set", "Hi Singleton", bean.greet());
    }

    private void testBeanInjection() throws NamingException {
        final SimpleInjectionBeanInterface bean = (SimpleInjectionBeanInterface) ctx.lookup("java:module/SimpleBeanWithInjection");
        try {
            bean.checkInjection();
            Assert.assertEquals("Bean wasn't redefined by JBoss specific descriptor to use SimpleSessionBean",
                    "Redefined Greetings", bean.greetInjectedBean("InjectionTest"));
        } catch (RuntimeException e) {
            fail("Bean is not correctly injected");

        }
    }
    
    private void testInterceptor(String expected) throws NamingException {
        final SimpleHelloBean helloBean = (SimpleHelloBean) ctx.lookup("java:module/SimpleHelloBean");
        Assert.assertEquals("Interception method wasn't changed by jboss spec descriptor", expected, helloBean.hello(""));
    }

    private void testTransactionStatus() throws SystemException, NotSupportedException, NamingException {
        final UserTransaction userTransaction = (UserTransaction) ctx.lookup("java:jboss/UserTransaction");
        final TransactionBean bean = (TransactionBean) ctx.lookup("java:module/TransactionBean");
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
