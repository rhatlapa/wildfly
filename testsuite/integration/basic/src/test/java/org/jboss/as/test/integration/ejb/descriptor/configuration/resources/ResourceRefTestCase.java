/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.as.test.integration.ejb.descriptor.configuration.resources;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.test.jms.auxiliary.CreateQueueSetupTask;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author rhatlapa
 */
@RunWith(Arquillian.class)
@ServerSetup(CreateQueueSetupTask.class)
public class ResourceRefTestCase {

    private static final Logger log = Logger.getLogger(ResourceRefTestCase.class);
    @ArquillianResource
    private InitialContext ctx;

    /**
     * deploys with both EJB specific descriptor and JBoss specific descriptor
     * @return 
     */
    @Deployment(name="ejb3-specVsJboss-spec")
    public static Archive<?> deployment() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "resource-ref-test.jar");
        jar.addClasses(ResourceDrivenBean.class, CreateQueueSetupTask.class, ResourceRefTestCase.class);
        jar.addAsManifestResource(ResourceRefTestCase.class.getPackage(), "jboss-ejb3.xml", "jboss-ejb3.xml");
        jar.addAsManifestResource(ResourceRefTestCase.class.getPackage(), "ejb-jar.xml", "ejb-jar.xml");
        return jar;
    }
    
    /**
     * deploys only with JBoss specific descriptor
     * @return 
     */
    @Deployment(name="jboss-spec")
    public static Archive<?> deploymentJbossSpec() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "resource-ref-test.jar");
        jar.addClasses(ResourceDrivenBean.class, CreateQueueSetupTask.class, ResourceRefTestCase.class);
        jar.addAsManifestResource(ResourceRefTestCase.class.getPackage(), "jboss-ejb3.xml", "jboss-ejb3.xml");
        return jar;
    }

    @Test
    @OperateOnDeployment(value="jboss-spec")
    public void testDescriptorSetOfEntriesJbossSpec() throws NamingException {
        testDescriptorSetOfEntries();
    }
    
    @Test
    @OperateOnDeployment(value="ejb3-specVsJboss-spec")
    public void testDescriptorSetOfEntriesWithJbossSpecRedefinition() throws NamingException {
        testDescriptorSetOfEntries();
    }
    
    @Test
    @OperateOnDeployment(value="jboss-spec")
    public void testDescriptorSetOfResourceJbossSpec() throws NamingException, JMSException {
        testDescriptorSetOfResource();
    }
    
    @Test
    @OperateOnDeployment(value="ejb3-specVsJboss-spec")
    public void testDescriptorSetOfResourceWithJbossSpecRedefinition() throws NamingException, JMSException {
        testDescriptorSetOfResource();
    }
        
    private void testDescriptorSetOfEntries() throws NamingException {
        final ResourceDrivenBean bean = (ResourceDrivenBean) ctx.lookup("java:module/ResourceDrivenBean");
        Assert.assertEquals("Hello jboss-spec", bean.getTextResource());
    }
    
    private void testDescriptorSetOfResource() throws NamingException, JMSException {
        final ResourceDrivenBean bean = (ResourceDrivenBean) ctx.lookup("java:module/ResourceDrivenBean");
        Assert.assertEquals("myAwesomeQueue", bean.getQueueName());
    }
}
