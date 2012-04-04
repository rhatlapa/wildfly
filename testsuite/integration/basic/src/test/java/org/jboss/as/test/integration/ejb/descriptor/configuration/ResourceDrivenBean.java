/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.as.test.integration.ejb.descriptor.configuration;

import javax.annotation.Resource;

/**
 *
 * @author rhatlapa
 */
public class ResourceDrivenBean {
    
    @Resource(name="textResource")
    private String textResource;

    public String getTextResource() {
        return textResource;
    }
    
    
}
