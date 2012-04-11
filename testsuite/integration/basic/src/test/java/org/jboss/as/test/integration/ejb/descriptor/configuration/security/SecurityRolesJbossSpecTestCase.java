/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.as.test.integration.ejb.descriptor.configuration.security;

import javax.naming.InitialContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.test.integration.ejb.security.EjbSecurityDomainSetup;
import org.jboss.as.test.integration.security.common.AbstractSecurityDomainSetup;
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
@ServerSetup({EjbSecurityDomainSetup.class})
public class SecurityRolesJbossSpecTestCase extends SecurityRolesTest {

    /**
     * deploys only with JBoss specific descriptor
     *
     * @return
     */
    @Deployment(name = "jboss-spec")
    public static Archive<?> deploymentJbossSpec() {
        final JavaArchive jar = ShrinkWrap.create(JavaArchive.class,
                "ejb-descriptor-configuration-test-jboss-spec.jar").
                addPackage(RoleProtectedBean.class.getPackage()).
                addClasses(AbstractSecurityDomainSetup.class, EjbSecurityDomainSetup.class).
                addClass(org.jboss.as.test.shared.integration.ejb.security.Util.class).
                addAsResource(SecurityRolesTest.class.getPackage(), "users.properties", "users.properties").
                addAsResource(SecurityRolesTest.class.getPackage(), "roles.properties", "roles.properties").
                addAsManifestResource(SecurityRolesTest.class.getPackage(), "jboss-ejb3.xml", "jboss-ejb3.xml");
        return jar;
    }
    
    @Test
    @OperateOnDeployment(value = "jboss-spec")
    public void testSecurityRolesUser1JbossSpec(@ArquillianResource InitialContext ctx) throws Exception {
        testSecurityRolesUser1(ctx);
    }

    @Test
    @OperateOnDeployment(value = "jboss-spec")
    public void testSecurityRolesUser2JbossSpec(@ArquillianResource InitialContext ctx) throws Exception {
        testSecurityRolesUser2(ctx);
    }

}
