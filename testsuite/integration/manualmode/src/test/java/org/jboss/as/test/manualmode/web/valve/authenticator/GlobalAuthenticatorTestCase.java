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
import java.util.HashMap;
import java.util.Map;
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
public class GlobalAuthenticatorTestCase {

    private static Logger log = Logger.getLogger(GlobalAuthenticatorTestCase.class);
    public static final String CONTAINER = "default-jbossas";
    @ArquillianResource
    private static ContainerController container;
    private static final String modulename = "org.jboss.testvalve";
    private static final String classname = TestAuthenticator.class.getName();
    private static final String baseModulePath = "/../modules/" + modulename.replace(".", "/") + "/main";
    private static final String jarName = "testvalve.jar";
    private static final String CUSTOM_AUTHENTICATOR = "authvalve";
    private static final String PARAM_NAME = "testparam";
    /**
     * the default value is hardcoded in {@link TestValve}
     */
    private static final String DEFAULT_PARAM_VALUE = "default";
    private static final String DEPLOYMENT_NAME = "valve";

    @Deployment(name = DEPLOYMENT_NAME)
    @TargetsContainer(CONTAINER)
    public static WebArchive Hello() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "global-valve-test.war");
        war.addClasses(HelloServlet.class);
        war.addAsWebInfResource(GlobalAuthenticatorTestCase.class.getPackage(),"web-custom-auth.xml", "web-custom-auth.xml");
//        war.addAsManifestResource(GlobalAuthenticatorTestCase.class.getPackage(), "MANIFEST.MF", "MANIFEST.MF");
        return war;
    }

    @Test
    @InSequence(-1)
    public void startServer() throws Exception {
        container.start(CONTAINER);
    }
    
    @Test
    @InSequence(1)
    public void testValveAuthOne(@ArquillianResource URL url, @ArquillianResource ManagementClient client) throws Exception {
        // as first test in sequence creating valve module
        ValveUtil.createValveModule(client, modulename, baseModulePath, jarName, TestAuthenticator.class);
        // adding valve based on the created module
        ValveUtil.addValve(client, CUSTOM_AUTHENTICATOR, modulename, classname, null);
        ValveUtil.reload(client);
        
        log.debug("Testing url " + url + " against one global valve authenticator named " + CUSTOM_AUTHENTICATOR);
        Header[] valveHeaders = ValveUtil.hitValve(url);
        assertEquals("There was one valve defined - it's missing now", 1, valveHeaders.length);
        assertEquals("One valve with not defined param expecting default param value", DEFAULT_PARAM_VALUE, valveHeaders[0].getValue());
    }        
    
    @Test
    @InSequence(99)
    public void cleanUp(@ArquillianResource ManagementClient client) throws Exception {
        ValveUtil.removeValve(client, CUSTOM_AUTHENTICATOR);
        container.stop(CONTAINER);
    }
}