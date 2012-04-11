/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.as.test.integration.ejb.descriptor.configuration;

/**
 *
 * @author rhatlapa
 */
public class SimpleBean {
    String name = "";
    public String sayHello() {
        return "Hello " + name;
    }
    
    public void setNameToAnonym() {
        this.name = "Anonym";
    }
    
    public void setNameToSomebody() {
        this.name = "Somebody";
    }
}
