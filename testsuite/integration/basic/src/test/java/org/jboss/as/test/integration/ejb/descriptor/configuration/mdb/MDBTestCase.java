/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.as.test.integration.ejb.descriptor.configuration.mdb;

import java.util.Date;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TextMessage;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.test.integration.common.jms.JMSOperations;
import org.jboss.as.test.integration.common.jms.JMSOperationsProvider;
import org.jboss.as.test.integration.ejb.mdb.DDBasedMDB;
import org.jboss.as.test.integration.ejb.mdb.JMSMessagingUtil;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author rhatlapa
 */
@RunWith(Arquillian.class)
@ServerSetup({org.jboss.as.test.integration.ejb.descriptor.configuration.mdb.MDBTestCase.JmsQueueSetup.class})
public class MDBTestCase {

    private static final Logger logger = Logger.getLogger(MDBTestCase.class);
    
    private static final String DEPLOYMENT_JBOSS_SPEC_ONLY = "jboss-spec";
    private static final String DEPLOYMENT_WITH_REDEFINITION = "ejb3-specVsJboss-spec";

    /**
     * Inner class which setups and destroys queues used for testing Message driven beans via
     * descriptors
     */
    static class JmsQueueSetup implements ServerSetupTask {

        private JMSOperations jmsAdminOperations;

        @Override
        public void setup(ManagementClient managementClient, String containerId) throws Exception {
            jmsAdminOperations = JMSOperationsProvider.getInstance(managementClient);
            jmsAdminOperations.createJmsQueue("mdbtest/queue", "java:jboss/mdbtest/queue");
            jmsAdminOperations.createJmsQueue("mdbtest/replyQueue", "java:jboss/mdbtest/replyQueue");
            jmsAdminOperations.createJmsQueue("mdbtest/redefinedQueue", "java:jboss/mdbtest/redefinedQueue");
        }

        @Override
        public void tearDown(ManagementClient managementClient, String containerId) throws Exception {
            if (jmsAdminOperations != null) {
                jmsAdminOperations.removeJmsQueue("mdbtest/queue");
                jmsAdminOperations.removeJmsQueue("mdbtest/replyQueue");
                jmsAdminOperations.removeJmsQueue("mdbtest/redefinedQueue");
                jmsAdminOperations.close();
            }
        }
    }

    @Deployment(name = DEPLOYMENT_WITH_REDEFINITION)
    public static Archive getDeployment() {
        final JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, "mdb.jar");
        ejbJar.addPackage(SimpleMessageDrivenBean.class.getPackage());
        ejbJar.addPackage(JMSOperations.class.getPackage());
        ejbJar.addClass(org.jboss.as.test.integration.ejb.mdb.JMSMessagingUtil.class);
        ejbJar.addClass(org.jboss.as.test.integration.ejb.descriptor.configuration.mdb.MDBTestCase.JmsQueueSetup.class);
        ejbJar.addAsManifestResource(MDBTestCase.class.getPackage(), "ejb-jar.xml", "ejb-jar.xml");
        ejbJar.addAsManifestResource(MDBTestCase.class.getPackage(), "jboss-ejb3.xml", "jboss-ejb3.xml");
        ejbJar.addAsManifestResource(new StringAsset("Dependencies: org.jboss.as.controller-client, org.jboss.dmr \n"), "MANIFEST.MF");
        return ejbJar;
    }

    @Deployment(name = DEPLOYMENT_JBOSS_SPEC_ONLY)
    public static Archive getDeploymentJbossSpec() {
        final JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, "mdb-jboss-spec.jar");
        ejbJar.addPackage(SimpleMessageDrivenBean.class.getPackage());
        ejbJar.addPackage(JMSOperations.class.getPackage());
        ejbJar.addClass(org.jboss.as.test.integration.ejb.mdb.JMSMessagingUtil.class);
        ejbJar.addClass(org.jboss.as.test.integration.ejb.descriptor.configuration.mdb.MDBTestCase.JmsQueueSetup.class);
        ejbJar.addAsManifestResource(MDBTestCase.class.getPackage(), "jboss-ejb3.xml", "jboss-ejb3.xml");
        ejbJar.addAsManifestResource(new StringAsset("Dependencies: org.jboss.as.controller-client, org.jboss.dmr \n"), "MANIFEST.MF");
        return ejbJar;
    }
    @EJB(mappedName = "java:module/JMSMessagingUtil")
    private JMSMessagingUtil util;
    // queue which ejb-spec defined MDB listens to     
    @Resource(mappedName = "java:jboss/mdbtest/queue")
    private Queue queue;
    // queue which jboss-spec defined MDB listens to     
    @Resource(mappedName = "java:jboss/mdbtest/redefinedQueue")
    private Queue redefinedQueue;
    /**
     * queue where MDB puts replies
     */
    @Resource(mappedName = "java:jboss/mdbtest/replyQueue")
    private Queue replyQueue;

    @Test
    @OperateOnDeployment(value = DEPLOYMENT_WITH_REDEFINITION)
    public void testMDBWithJBossSpecRedefinition() throws Exception {
        testMDB();
    }

    @Test
    @OperateOnDeployment(value = DEPLOYMENT_JBOSS_SPEC_ONLY)
    public void testMDBAsDefinedByJbossSpecDescriptor() throws Exception {
        testMDB();
    }

    /**
     * tests MDB by sending different message to two different queues and checking answer from reply
     * queue with defined timeout
     *
     * descriptor should defined queue which MDB listens to
     *
     * @throws Exception
     */
    private void testMDB() throws Exception {

        this.util.sendTextMessage("Hello ejb", this.queue, replyQueue);
        this.util.sendTextMessage("Hello jboss-spec", this.redefinedQueue, replyQueue);
        logger.info("Start time of waiting for message in MDB test: " + new Date().getTime());
        final Message reply = this.util.receiveMessage(replyQueue, 8000);
        logger.info("End time of waiting for message in MDB test: " + new Date().getTime());
        Assert.assertNotNull("Reply message was null on reply queue: " + this.replyQueue, reply);
        final String result = ((TextMessage) reply).getText();
        Assert.assertEquals("MDB should listen on redefinedQueue", "replying Hello jboss-spec", result);
    }
}
