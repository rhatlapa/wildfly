/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.as.test.integration.deployment.resourcelisting;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.test.integration.deployment.classloading.war.WebInfLibClass;
import org.jboss.logging.Logger;
import org.jboss.modules.ModuleClassLoader;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(Arquillian.class)
public class WarResourceListingTestCase {

    private static Logger log = Logger.getLogger(WarResourceListingTestCase.class);
    //    private static final String jarLibName = "jarLibrary" + String.valueOf(new Date().getTime()) + ".jar";
    private static final String jarLibName = "innerJarLibrary.jar";


    /**
     * @return war archive with different types of web.xml
     */
    @Deployment
    public static Archive<?> deploy() {
        WebArchive war = ShrinkWrap.create(WebArchive.class);
        war.addClass(WarResourceListingTestCase.class);
        war.addClass(ResourceListingUtils.class);
        war.add(EmptyAsset.INSTANCE, "META-INF/properties/nested.properties");
        war.add(EmptyAsset.INSTANCE, "META-INF/example.txt");
        war.add(EmptyAsset.INSTANCE, "example2.txt");
        war.addAsResource(WarResourceListingTestCase.class.getPackage(), "TextFile1.txt", "TextFile1.txt");
        war.addAsWebInfResource(WarResourceListingTestCase.class.getPackage(), "web.xml", "web.xml");
        JavaArchive libJar = ShrinkWrap.create(JavaArchive.class, jarLibName);
        libJar.addClass(WebInfLibClass.class);
        war.addAsLibraries(libJar);
//        war.as(org.jboss.shrinkwrap.api.exporter.ZipExporter.class).exportTo(new java.io.File("/tmp/" + war.getName()), true);
        return war;
    }

    @Test()
    public void testRecursiveResourceRetrieval() {
        log.info("Test recursive listing of resources");
        doTestResourceRetrieval(true, "/");

    }

    @Test()
    public void testNonRecursiveResourceRetrieval() {
        log.info("Test nonrecursive listing of resources");
        doTestResourceRetrieval(false, "/");
    }

    @Test()
    public void testRecursiveResourceRetrievalForSpecifiedRootDir() {
        log.info("Test recursive listing of resources in specific directory");
        doTestResourceRetrieval(true, "/WEB-INF");
    }

    @Test()
    public void testNonRecursiveResourceRetrievalForSpecifiedRootDir() {
        log.info("Test recursive listing of resources in specific directory");
        doTestResourceRetrieval(false, "/WEB-INF");
    }

    private void doTestResourceRetrieval(boolean recursive, String rootDir) {
        ModuleClassLoader classLoader = (ModuleClassLoader) getClass().getClassLoader();

        // checking that resource under META-INF is accessible
        URL manifestResource = classLoader.getResource("META-INF/example.txt");
        assertNotNull(manifestResource);

        // checking that resource which is not under META-INF is not accessible
        URL nonManifestResource = classLoader.getResource("example2.txt");
        assertNull(nonManifestResource);

        List<String> resourcesInDeployment = getActualResourcesInWar(recursive, rootDir);

        List<String> foundResources = ResourceListingUtils.listResources(classLoader, rootDir, recursive);

        Collections.sort(foundResources);

        log.info("List of expected resources:");
        for (String expectedResource : resourcesInDeployment) {
            log.info(expectedResource);
        }
        log.info("List of found resources: ");
        for (String foundResource : foundResources) {
            log.info(foundResource);
        }

        Assert.assertArrayEquals("Not all resources from WAR archive are correctly listed", resourcesInDeployment.toArray(), foundResources.toArray());
    }

    /**
     * At the moment it gives all resources including those outside of META_INF, this is the current way it works,
     * but it should be probably corrected to not list certain resources twice and resources in WEB-INF/classes and WEB-INF/lib
     * see https://issues.jboss.org/browse/WFLY-1428
     * @param recursive -- if even recursive resources (taken from rootDir) shall be provided
     * @param rootDir -- can be used for getting resources only from specific rootDir
     * @return list of resources in WAR filtered based on specified arguments
     */
    public static List<String> getActualResourcesInWar(boolean recursive, String rootDir) {

        Class[] libJarClasses = new Class[]{
                WebInfLibClass.class
        };

        String[] otherResources = new String[]{
                "META-INF/example.txt",
                "META-INF/MANIFEST.MF",
                "META-INF/properties/nested.properties",
                "example2.txt",
                "TextFile1.txt",
                "WEB-INF/classes/TextFile1.txt",
                "WEB-INF/web.xml"
        };

        Class[] warClasses = new Class[]{
                WarResourceListingTestCase.class,
                ResourceListingUtils.class,
        };

        List<String> resourcesInWar = new ArrayList<String>(Arrays.asList(otherResources));
        for (Class warClass : warClasses) {
            String warClassName = ResourceListingUtils.classToPath(warClass);
            resourcesInWar.add(warClassName);
            resourcesInWar.add("WEB-INF/classes/" + warClassName);
        }

        resourcesInWar.addAll(getClassResourcesForInnerLib(libJarClasses, jarLibName));

        ResourceListingUtils.filterResources(resourcesInWar, rootDir, !recursive);

        Collections.sort(resourcesInWar);
        return resourcesInWar;
    }

    public static Collection<String> getClassResourcesForInnerLib(Class[] classes, String innerArchiveName) {
        Collection<String> collectionWithResources = new ArrayList<>();
        for (Class clazz : classes) {
            collectionWithResources.add("WEB-INF/lib/" + innerArchiveName + "/" + ResourceListingUtils.classToPath(clazz));
            collectionWithResources.add(ResourceListingUtils.classToPath(clazz));
        }
        return collectionWithResources;
    }

}
