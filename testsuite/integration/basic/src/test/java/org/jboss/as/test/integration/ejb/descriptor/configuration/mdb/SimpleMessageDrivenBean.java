/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.as.test.integration.ejb.descriptor.configuration.mdb;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.jms.*;

/**
 *
 * @author rhatlapa
 */
public class SimpleMessageDrivenBean implements MessageListener {

    @Resource(lookup = "java:/JmsXA")
    private ConnectionFactory factory;
    private Connection connection;
    private Session session;

    public void onMessage(Message message) {
        try {
            System.out.println("Message " + message);
            final Destination destination = message.getJMSReplyTo();
            // ignore messages that need no reply
            if (destination == null) {
                return;
            }
            final MessageProducer replyProducer = session.createProducer(destination);
            final Message replyMsg = session.createTextMessage("replying " + ((TextMessage) message).getText());
            replyMsg.setJMSCorrelationID(message.getJMSMessageID());
            replyProducer.send(replyMsg);
            replyProducer.close();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    protected void preDestroy() throws JMSException {
        session.close();
        connection.close();
    }

    @PostConstruct
    protected void postConstruct() throws JMSException {
        connection = factory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }
}
