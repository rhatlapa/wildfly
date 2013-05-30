package org.jboss.as.test.integration.deployment.resourcelisting;

import org.jboss.modules.ModuleClassLoader;
import org.jboss.modules.Resource;
import org.junit.Assert;

import java.util.*;

/**
 * @author: rhatlapa@redhat.com
 */
public class ResourceListingUtils {


    public static List<String> listResources(ModuleClassLoader classLoader, String rootDir, boolean recursive) {
        List<String> resourceList = new ArrayList<String>();
        Iterator<Resource> it = classLoader.iterateResources(rootDir, recursive);

        while (it.hasNext()) {
            resourceList.add(it.next().getName());
        }
        return resourceList;
    }


    public static String classToPath(Class clazz) {
        return clazz.getName().replaceAll("\\.", "/") + ".class";
    }

    public static void filterResources(Collection<String> resources, String rootDir, boolean removeRecursive) {
        String rootDirPrefix = "";
        if (rootDir.startsWith("/")) {
            rootDirPrefix = rootDir.substring(1);
        }
        Iterator<String> it = resources.iterator();
        while (it.hasNext()) {
            String resource = it.next();
            if (resource.startsWith(rootDirPrefix)) {
                // the rootDir needs to be removed from name for deciding if it is an recursive resource or not
                if (removeRecursive) {
                    String resourceWithoutPrefix = resource.substring(rootDirPrefix.length());
                    if (resourceWithoutPrefix.startsWith("/")) {
                        resourceWithoutPrefix = resourceWithoutPrefix.substring(1);
                    }
                    System.err.println("Original resource to check = " + resource);
                    System.err.println("Resource without its rootDir = " + resourceWithoutPrefix);
                    if (resourceWithoutPrefix.contains("/")) {
                        it.remove();
                    }
                }
            } else {
                it.remove();
            }

        }
    }



}
