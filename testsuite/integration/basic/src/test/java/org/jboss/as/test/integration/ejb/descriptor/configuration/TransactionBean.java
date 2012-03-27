/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.as.test.integration.ejb.descriptor.configuration;

import javax.annotation.Resource;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

/**
 *
 * @author rhatlapa
 */
public class TransactionBean {
    
    @Resource(lookup="java:jboss/TransactionManager")
    private TransactionManager transactionManager;

    public int transactionStatus() {
        try {
            return transactionManager.getStatus();
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }
}
