/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.as.test.integration.ejb.descriptor.configuration;

import java.util.logging.Logger;
import javax.ejb.EJBAccessException;
import javax.naming.InitialContext;
import javax.security.auth.login.LoginContext;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.test.integration.ejb.security.EjbSecurityDomainSetup;
import org.jboss.as.test.integration.security.common.AbstractSecurityDomainSetup;
import org.jboss.as.test.shared.integration.ejb.security.Util;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author rhatlapa
 */
@RunWith(Arquillian.class)
//@ServerSetup({EjbSecurityDomainSetup.class})
public class SecurityRolesTestCase {

    @ArquillianResource
    private InitialContext ctx;
    private static final Logger log = Logger.getLogger(SecurityRolesTestCase.class.getName());
    
//    @Deployment
//    public static Archive<?> deployment() {
//        final JavaArchive jar = ShrinkWrap.create(JavaArchive.class,
//                "ejb-descriptor-configuration-test.jar").
//                addPackage(RoleProtectedBean.class.getPackage()).                
//                addClass(org.jboss.as.test.shared.integration.ejb.security.Util.class).
////                addAsResource(SecurityRolesTestCase.class.getPackage(), "users.properties", "users.properties").
////                addAsResource(SecurityRolesTestCase.class.getPackage(), "roles.properties", "roles.properties").
//                addAsResource(SecurityRolesTestCase.class.getPackage(), "jboss-ejb3.xml", "jboss-ejb3.xml")
//                //.
////                addAsResource(SecurityRolesTestCase.class.getPackage(), "ejb-jar.xml", "ejb-jar.xml").
//                //.addAsResource(SecurityRolesTestCase.class.getPackage(), "jboss-web.xml").
////                addAsManifestResource(new StringAsset("Manifest-Version: 1.0\nDependencies: org.jboss.as.controller-client,org.jboss.dmr\n"), "MANIFEST.MF")
//                ;
//        return jar;
//    }
    
//     @Deployment
//    public static Archive<?> deployment() {
//        final WebArchive jar = ShrinkWrap.create(WebArchive.class,
//                "ejb-descriptor-configuration-test.war").
//                addPackage(RoleProtectedBean.class.getPackage()).                
//                addClass(org.jboss.as.test.shared.integration.ejb.security.Util.class).
////                addAsResource(SecurityRolesTestCase.class.getPackage(), "users.properties", "users.properties").
////                addAsResource(SecurityRolesTestCase.class.getPackage(), "roles.properties", "roles.properties").
//                addAsResource(SecurityRolesTestCase.class.getPackage(), "jboss-ejb3.xml", "jboss-ejb3.xml")
//                .addAsWebInfResource(SecurityRolesTestCase.class.getPackage(), "web.xml")
//                .addAsResource(SecurityRolesTestCase.class.getPackage(), "jboss-web.xml")
//                ;
//        return jar;
//    }


    @Deployment
    public static Archive<?> deployment() {
        final WebArchive war = ShrinkWrap.create(WebArchive.class,
                "ejb-descriptor-configuration-test.war").addPackage(SecurityRolesTestCase.class.
                getPackage()).
//                addClasses(AbstractSecurityDomainSetup.class, EjbSecurityDomainSetup.class).
                addClass(org.jboss.as.test.shared.integration.ejb.security.Util.class).
                addAsResource(SecurityRolesTestCase.class.getPackage(), "users.properties", "users.properties").
                addAsResource(SecurityRolesTestCase.class.getPackage(), "roles.properties", "roles.properties").
                addAsWebInfResource(SecurityRolesTestCase.class.getPackage(), "jboss-ejb3.xml", "jboss-ejb3.xml").
                addAsWebInfResource(SecurityRolesTestCase.class.getPackage(), "ejb-jar.xml", "ejb-jar.xml").
//                addAsWebInfResource(SecurityRolesTestCase.class.getPackage(), "web.xml").
                addAsResource(SecurityRolesTestCase.class.getPackage(), "jboss-web.xml")
//                .
//                addAsManifestResource(new StringAsset("Manifest-Version: 1.0\nDependencies: org.jboss.as.controller-client,org.jboss.dmr\n"), "MANIFEST.MF")
                ;
        return war;
    }

    @Test
    public void testSecurityRolesUser1() throws Exception {
        final RoleProtectedBean bean = (RoleProtectedBean) ctx.lookup("java:global/ejb-descriptor-configuration-test/RoleProtectedBean");
        LoginContext lc = Util.getCLMLoginContext("user1", "password");
        lc.login();
        String response;
        try {

            Assert.assertTrue("User should be in role1", bean.isInRole("role1"));
            Assert.assertFalse("User is expected not being in role2", bean.isInRole("role2"));

            try {
                response = bean.defaultEcho("1");
                Assert.assertEquals("1", response);
            } catch (EJBAccessException ex) {
                Assert.fail("Not expected thrown exception for defaultEcho");
            }
            try {
                bean.denyAllEcho("2");
                Assert.fail("Expected EJBAccessException not thrown");
            } catch (EJBAccessException ignored) {
            }

            try {
                response = bean.permitAllEcho("3");
                Assert.assertEquals("3", response);
            } catch (EJBAccessException ex) {
                Assert.fail("PermitAllEcho should be allowed for all security roles");
            }

            try {
                bean.role2Echo("4");
                Assert.fail("Expected EJBAccessException to be thrown");
            } catch (EJBAccessException ex) {
            }

        } finally {
            lc.logout();
        }
    }

    @Test
    public void testSecurityRolesUser2() throws Exception {
        final RoleProtectedBean bean = (RoleProtectedBean) ctx.lookup("java:global/ejb-descriptor-configuration-test/RoleProtectedBean");
        LoginContext lc = Util.getCLMLoginContext("user2", "password");
        lc.login();
        String response;
        try {
            Assert.assertTrue("User should be in role2", bean.isInRole("role2"));
            Assert.assertFalse("User is expected not being in role1", bean.isInRole("role1"));

            try {
                response = bean.defaultEcho("1");
                Assert.assertEquals("1", response);
            } catch (EJBAccessException ex) {
                Assert.fail("Not expected thrown exception for defaultEcho");
            }

            try {
                bean.denyAllEcho("2");
                Assert.fail("Expected EJBAccessException not thrown");
            } catch (EJBAccessException ignored) {
            }

            try {
                response = bean.permitAllEcho("3");
                Assert.assertEquals("3", response);
            } catch (EJBAccessException ex) {
                Assert.fail("PermitAllEcho should be allowed for all security roles");
            }

            try {
                response = bean.role2Echo("4");
                Assert.assertEquals("user2 should have permission to access method role2Echo", "4", response);
            } catch (EJBAccessException ex) {
                Assert.fail("role2Echo should be allowed for security role role2");
            }

        } finally {
            lc.logout();
        }
    }
}
