/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.as.test.integration.ejb.descriptor.configuration;

/**
 *
 * @author rhatlapa
 */
public class SessionTypeSpecifiedBean {
    String name;
    
    public void setName(String name) {
        this.name = name;
    }
    public String greet(String name) {
        return "Hi " + name;
    }
    
    public String greet() {
        return "Hi " + name;
    }
}
