/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.as.test.integration.ejb.descriptor.configuration;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.*;

/**
 *
 * @author rhatlapa
 */
@Stateless
public class MessagingUtils {

    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;

    @PreDestroy
    protected void preDestroy() throws JMSException {
        session.close();
        connection.close();
    }

    @PostConstruct
    protected void postConstruct() throws JMSException {
        connection = this.connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    @Resource(mappedName = "java:/ConnectionFactory")
    public void setConnectionFactory(final ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void sendTextMessage(final String msg, final Destination destination) throws JMSException {
        final TextMessage message = session.createTextMessage(msg);
        this.sendMessage(message, destination);
    }

    public void sendObjectMessage(final Serializable msg, final Destination destination) throws JMSException {
        final ObjectMessage message = session.createObjectMessage();
        message.setObject(msg);
        this.sendMessage(message, destination);
    }

    public void reply(final Message message) throws JMSException {
        final Destination destination = message.getJMSReplyTo();
        // ignore messages that need no reply
        if (destination == null) {
            return;
        }
        final javax.jms.Message replyMsg = session.createTextMessage("replying to message: " + message.toString());
        replyMsg.setJMSCorrelationID(message.getJMSMessageID());
        this.sendMessage(replyMsg, destination);
    }

    public Message receiveMessage(final Destination destination, final long waitInMillis) throws JMSException {
        MessageConsumer consumer = this.session.createConsumer(destination);
        return consumer.receive(waitInMillis);
    }

    private void sendMessage(final Message message, final Destination destination) throws JMSException {
        final MessageProducer messageProducer = session.createProducer(destination);
        messageProducer.send(message);
        messageProducer.close();
    }
}
