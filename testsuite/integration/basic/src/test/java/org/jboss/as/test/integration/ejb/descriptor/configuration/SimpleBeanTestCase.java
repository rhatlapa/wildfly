/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.as.test.integration.ejb.descriptor.configuration;

import org.jboss.as.test.integration.ejb.descriptor.configuration.interceptors.SimpleHelloBean;
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
    
    private static final String DEPLOYMENT_JBOSS_SPEC_ONLY = "jboss-spec";
    private static final String DEPLOYMENT_WITH_REDEFINITION = "ejb3-specVsJboss-spec";

    @Deployment(name = DEPLOYMENT_JBOSS_SPEC_ONLY)
    public static Archive<?> deploymentJbossSpec() {
        final JavaArchive jar = ShrinkWrap.create(JavaArchive.class,
                "ejb-descriptor-configuration-test-jboss-spec.jar").addPackage(SessionTypeSpecifiedBean.class.
                getPackage()).addAsManifestResource(SimpleBeanTestCase.class.getPackage(), "jboss-ejb3.xml", "jboss-ejb3.xml");
        return jar;
    }

    @Deployment(name = DEPLOYMENT_WITH_REDEFINITION)
    public static Archive<?> deployment() {
        final JavaArchive jar = ShrinkWrap.create(JavaArchive.class,
                "ejb-descriptor-configuration-test.jar").addPackage(SessionTypeSpecifiedBean.class.
                getPackage()).addAsManifestResource(SimpleBeanTestCase.class.getPackage(), "jboss-ejb3.xml", "jboss-ejb3.xml").
                addAsManifestResource(SimpleBeanTestCase.class.getPackage(), "ejb-jar.xml", "ejb-jar.xml");
        return jar;
    }

    @Test
    @OperateOnDeployment(value = DEPLOYMENT_JBOSS_SPEC_ONLY)
    public void testTransactionStatusJbossSpec() throws NamingException, SystemException, NotSupportedException {
        testTransactionStatus();
    }

    @Test
    @OperateOnDeployment(value = DEPLOYMENT_JBOSS_SPEC_ONLY)
    public void testBeanInjectionJbossSpec() throws NamingException {
        testBeanInjection();
    }

    @Test
    @OperateOnDeployment(value = DEPLOYMENT_JBOSS_SPEC_ONLY)
    public void testStatelessBeanJbossSpec() throws NamingException {
        testSimpleBeanStateless();
    }

    @Test
    @OperateOnDeployment(value = DEPLOYMENT_JBOSS_SPEC_ONLY)
    public void testSingletonBeanJbossSpec() throws NamingException {
        testSimpleBeanSingleton();
    }

    @Test
    @OperateOnDeployment(value = DEPLOYMENT_WITH_REDEFINITION)
    public void testTransactionStatusWithJbossSpecRedefinition() throws NamingException, SystemException, NotSupportedException {
        testTransactionStatus();
    }

    @Test
    @OperateOnDeployment(value = DEPLOYMENT_WITH_REDEFINITION)
    public void testBeanInjectionWithJbossSpecRedefinition() throws NamingException {
        testBeanInjection();
    }

    @Test
    @OperateOnDeployment(value = DEPLOYMENT_WITH_REDEFINITION)
    public void testStatelessBeanWithJbossSpecRedefinition() throws NamingException {
        testSimpleBeanStateless();
    }

    @Test
    @OperateOnDeployment(value = DEPLOYMENT_WITH_REDEFINITION)
    public void testSingletonBeanWithJbossSpecRedefinition() throws NamingException{
        testSimpleBeanSingleton();
    }
    
    @Test
    @OperateOnDeployment(value = DEPLOYMENT_WITH_REDEFINITION)
    public void testAfterBeginMethodWithJbossSpecRedefinition() throws NamingException {
        testAfterBeginMethod();
    }
    
    @Test
    @OperateOnDeployment(value = DEPLOYMENT_JBOSS_SPEC_ONLY)
    public void testAfterBeginMethodJbossSpec() throws NamingException {
        testAfterBeginMethod();
    }
    
    private void testAfterBeginMethod() throws NamingException {
        final SimpleBean bean = (SimpleBean) ctx.lookup("java:module/SimpleBean");
        Assert.assertEquals("Hello Somebody", bean.sayHello());
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
