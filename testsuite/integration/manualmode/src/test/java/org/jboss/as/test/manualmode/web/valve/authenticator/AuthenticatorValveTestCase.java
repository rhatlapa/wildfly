/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.test.manualmode.web.valve.authenticator;

import java.net.URL;
import java.util.Arrays;
import org.apache.http.Header;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This class tests a global valve.
 *
 * @author Jean-Frederic Clere
 * @author Ondrej Chaloupka
 * @author Radim Hatlapatka
 */
@RunWith(Arquillian.class)
@RunAsClient
public class AuthenticatorValveTestCase {

    private static Logger log = Logger.getLogger(AuthenticatorValveTestCase.class);
    public static final String CONTAINER = "default-jbossas";
    @ArquillianResource
    private static ContainerController container;
    @ArquillianResource
    private Deployer deployer;
    private static final String modulename = "org.jboss.testvalve";
    private static final String classname = CustomAuthenticator.class.getName();
    private static final String baseModulePath = "/../modules/" + modulename.replace(".", "/") + "/main";
    private static final String jarName = "testvalve.jar";
    private static final String CUSTOM_AUTHENTICATOR = "authvalve";
    private static final String PARAM_NAME = "testparam";
    /**
     * the default value is hardcoded in {@link TestValve}
     */
    private static final String GLOBAL_PARAM_VALUE = "global";
    private static final String WEB_PARAM_VALUE = "webdescriptor";
    private static final String DEPLOYMENT_NAME = "valve";

    @Deployment(name = DEPLOYMENT_NAME, managed = false)
    @TargetsContainer(CONTAINER)
    public static WebArchive Hello() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "global-valve-test.war");
        war.addClasses(HelloServlet.class);
        war.addAsWebInfResource(AuthenticatorValveTestCase.class.getPackage(), "jboss-web.xml", "jboss-web.xml");
        war.addAsWebInfResource(AuthenticatorValveTestCase.class.getPackage(),"web-custom-auth.xml", "web-custom-auth.xml");
        war.addAsManifestResource(AuthenticatorValveTestCase.class.getPackage(), "MANIFEST.MF", "MANIFEST.MF");
        return war;
    }

    @Test
    @InSequence(-1)
    public void startServer() throws Exception {
        container.start(CONTAINER);
    }

    @Test
    @InSequence(0)
    public void createValveAndDeploy(@ArquillianResource ManagementClient client) throws Exception {
        // as first test in sequence creating valve module
        ValveUtil.createValveModule(client, modulename, baseModulePath, jarName);
        // valve is ready - let's deploy
        deployer.deploy(DEPLOYMENT_NAME);
    }
    
    @Test
    @InSequence(1)
    public void testWebDescriptor(@ArquillianResource URL url, @ArquillianResource ManagementClient client) throws Exception {
        log.debug("Testing url " + url + " against one authenticator valve defined in jboss-web.xml descriptor");
        Header[] valveHeaders = ValveUtil.hitValve(url);
        assertEquals("There was one valve defined - it's missing now", 1, valveHeaders.length);
        assertEquals(WEB_PARAM_VALUE, valveHeaders[0].getValue());
    }

    @Test
    @InSequence(2)
    public void testValveGlobal(@ArquillianResource URL url, @ArquillianResource ManagementClient client) throws Exception {
        // as first test in sequence creating valve module
        ValveUtil.createValveModule(client, modulename, baseModulePath, jarName);
        // adding valve based on the created module
        ValveUtil.addValve(client, CUSTOM_AUTHENTICATOR, modulename, classname, null);
        ValveUtil.reload(client);
               
        log.debug("Testing url " + url + " against two authenticators - one defined in web descriptor other is defined globally in server configuration");
        Header[] valveHeaders = ValveUtil.hitValve(url);
        assertEquals("There were two valves defined (but detected these valve headers: "+ Arrays.toString(valveHeaders) +")", 2, valveHeaders.length);
        assertEquals(GLOBAL_PARAM_VALUE, valveHeaders[0].getValue());
        assertEquals(WEB_PARAM_VALUE, valveHeaders[1].getValue());
    }

    @Test
    @InSequence(99)
    public void cleanUp(@ArquillianResource ManagementClient client) throws Exception {
//        deployer.undeploy(DEPLOYMENT_NAME);
        ValveUtil.removeValve(client, CUSTOM_AUTHENTICATOR);
        container.stop(CONTAINER);
    }
}