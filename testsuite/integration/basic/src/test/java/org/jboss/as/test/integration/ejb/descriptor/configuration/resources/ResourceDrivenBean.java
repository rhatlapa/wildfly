/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.as.test.integration.ejb.descriptor.configuration.resources;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Queue;

/**
 *
 * @author rhatlapa
 */
public class ResourceDrivenBean {

    @Resource(name = "textResource")
    private String textResource;
    
    private Queue queueResource;

    public String getTextResource() {
        return textResource;
    }

    public String getQueueName() throws JMSException {
        if (queueResource == null) {
            return null;
        } else {
            return queueResource.getQueueName();
        }
    }
}
