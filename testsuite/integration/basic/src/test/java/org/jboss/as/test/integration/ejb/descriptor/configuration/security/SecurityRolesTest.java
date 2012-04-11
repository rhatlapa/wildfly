/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.as.test.integration.ejb.descriptor.configuration.security;

import javax.ejb.EJBAccessException;
import javax.naming.InitialContext;
import javax.security.auth.login.LoginContext;
import junit.framework.Assert;
import org.jboss.as.test.shared.integration.ejb.security.Util;

/**
 *
 * @author rhatlapa
 */
public class SecurityRolesTest {

//    private static final Logger log = Logger.getLogger(SecurityRolesTestCase.class.getName());

    /**
     * deploys with both EJB specific descriptor and JBoss specific descriptor
     *
     * @return
     */
//    @Deployment(name = "ejb3-specVsJboss-spec")
//    public static Archive<?> deployment() {
//        final JavaArchive jar = ShrinkWrap.create(JavaArchive.class,
//                "ejb-descriptor-configuration-test.jar").
//                addPackage(RoleProtectedBean.class.getPackage()).
//                addClasses(AbstractSecurityDomainSetup.class, EjbSecurityDomainSetup.class).
//                addClass(org.jboss.as.test.shared.integration.ejb.security.Util.class).
//                addAsResource(SecurityRolesTestCase.class.getPackage(), "users.properties", "users.properties").
//                addAsResource(SecurityRolesTestCase.class.getPackage(), "roles.properties", "roles.properties").
//                addAsManifestResource(SecurityRolesTestCase.class.getPackage(), "jboss-ejb3.xml", "jboss-ejb3.xml").
//                addAsManifestResource(SecurityRolesTestCase.class.getPackage(), "ejb-jar.xml", "ejb-jar.xml");
//        return jar;
//    }
//
//    /**
//     * deploys only with JBoss specific descriptor
//     *
//     * @return
//     */
//    @Deployment(name = "jboss-spec")
//    public static Archive<?> deploymentJbossSpec() {
//        final JavaArchive jar = ShrinkWrap.create(JavaArchive.class,
//                "ejb-descriptor-configuration-test-jboss-spec.jar").
//                addPackage(RoleProtectedBean.class.getPackage()).
//                addClasses(AbstractSecurityDomainSetup.class, EjbSecurityDomainSetup.class).
//                addClass(org.jboss.as.test.shared.integration.ejb.security.Util.class).
//                addAsResource(SecurityRolesTestCase.class.getPackage(), "users.properties", "users.properties").
//                addAsResource(SecurityRolesTestCase.class.getPackage(), "roles.properties", "roles.properties").
//                addAsManifestResource(SecurityRolesTestCase.class.getPackage(), "jboss-ejb3.xml", "jboss-ejb3.xml");
//        return jar;
//    }
//
//    @Test
//    @OperateOnDeployment(value = "jboss-spec")
//    public void testSecurityRolesUser1JbossSpec(@ArquillianResource InitialContext ctx) throws Exception {
//        testSecurityRolesUser1(new InitialContext());
//    }
//
//    @Test
//    @OperateOnDeployment(value = "ejb3-specVsJboss-spec")
//    public void testSecurityRolesUser1WithJbossSpecRedefinition(@ArquillianResource InitialContext ctx) throws Exception {
//        testSecurityRolesUser1(new InitialContext());
//    }
//
//    @Test
//    @OperateOnDeployment(value = "jboss-spec")
//    public void testSecurityRolesUser2JbossSpec(@ArquillianResource InitialContext ctx) throws Exception {
//        testSecurityRolesUser2(new InitialContext());
//    }
//
//    @Test
//    @OperateOnDeployment(value = "ejb3-specVsJboss-spec")
//    public void testSecurityRolesUser2WithJbossSpecRedefinition(@ArquillianResource InitialContext ctx) throws Exception {
//        testSecurityRolesUser2(new InitialContext());
//    }

    protected void testSecurityRolesUser1(InitialContext ctx) throws Exception {
        final RoleProtectedBean bean = (RoleProtectedBean) ctx.lookup("java:module/RoleProtectedBean");
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

    protected void testSecurityRolesUser2(InitialContext ctx) throws Exception {
        final RoleProtectedBean bean = (RoleProtectedBean) ctx.lookup("java:module/RoleProtectedBean");
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
