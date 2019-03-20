package cn.fintecher.wf.jms;

import java.util.ArrayList;
import java.util.Arrays;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import cn.fintecher.wf.config.ConfigMQ;
import cn.fintecher.wf.entity.SysJmsNoticeMessageEntity;
/**
 * 功能说明：管理服务器连接  
 * 典型用法：该类的典型使用方法和用例
 * 特殊用法：该类在系统中的特殊用法的说明	
 * @author feixinwei
 * 修改人: 
 * 修改原因：
 * 修改时间：
 * 修改内容：
 * 创建日期：2016-11-16
 * Copyright zzl-apt
 */
public class JmsUtils {  
	
    private String url = ConfigMQ.getBrokerUrl(); //远程服务器地址   
	
    private String user = ConfigMQ.getUser(); //用于连接服务器的用户名  
	
    private String password = ConfigMQ.getPassword(); //用于连接服务器的密码  
	
    private String queue; //消息队列名  
	
    
    /** 
     * 与远程服务器取得连接 
     * @param url           服务器连接地址 
     * @param username      用户名 
     * @param password      密码 
     * @return              连接对象 
     * @throws JMSException JMS异常S 
     */  
    public Connection getConnection() {  
        Connection connection = null;  
        
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);
        factory.setTrustedPackages(new ArrayList(Arrays.asList("com.apt.service.model.crm,com.apt.webapp.model.crm,com.apt.model.jms".split(","))));
        try {  
            connection = factory.createConnection(user, password);  
        } catch (JMSException e) {  
            e.printStackTrace();  
        }  
    
        return connection;  
    }  
      
    /** 
     * 关闭连接 
     * @param connection    与远程服务器连接 
     * @throws JMSException JMS异常 
     */  
    public void closeConnection(Connection connection, Session session,  
            MessageProducer producer, MessageConsumer consumer) {  
        try {  
            if (producer != null) {  
                producer.close();  
            }  
            if (consumer != null) {  
                consumer.close();  
            }  
            if (session != null) {  
                session.close();  
            }  
            if (connection != null) {  
                connection.close();  
            }  
        } catch (JMSException e) {  
            e.printStackTrace();  
        }  
    }

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public String getQueue() {
		return queue;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}  
	
	
    /**
     * 功能说明：往消息队列发送消息			
     * feixinwei  2016-11-18
     *@param msgType:信息类型,有text,stream,object,bytes
     *@param msg:消息,根据类型的不同传不同的值
     * @return 该方法的返回值的类型，含义   
     * @throws  该方法可能抛出的异常，异常的类型、含义。
     * 最后修改时间：最后修改时间
     * 修改人：Administrator
     * 修改内容：
     * 修改注意点：
     */
    public void dataSend(String msgType,Object msg,String type, String queue) {  
        Connection connection;  
        Session session = null;  
        MessageProducer producer = null;  
        connection = this.getConnection();  
        try {  
            connection.start();  
            session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);  
            Destination destination = null;
            if("queue".equals(type)){
            	destination = session.createQueue(queue);
            }else {
            	destination = session.createTopic(queue);
            }
            producer = session.createProducer(destination);  
            if("text".equals(msgType)){
            	TextMessage message = session.createTextMessage(msg.toString());  
                producer.send(message);  
                session.commit();  
            }
            if ("stream".equals(msgType)) {
            	StreamMessage message = session.createStreamMessage();
                producer.send(message);  
                session.commit();  
			}
            if ("object".equals(msgType)) {
            	ObjectMessage objectMessage = session.createObjectMessage();
            	SysJmsNoticeMessageEntity jmsMessage = (SysJmsNoticeMessageEntity) msg;
            	objectMessage.setObject(jmsMessage);
            	producer.send(objectMessage);
                session.commit();
			}
            if ("bytes".equals(msgType)) {
            	BytesMessage bytesMessage = session.createBytesMessage();  
            	bytesMessage.writeBytes(msg.toString().getBytes());  
            	producer.send(bytesMessage);
			}
            if ("map".equals(msgType)) {
            	MapMessage mapMessage = session.createMapMessage();  
            	mapMessage.setObject("object", msg);
            	producer.send(mapMessage);
			}
            
        } catch (JMSException e) {  
            e.printStackTrace();  
        } finally {  
            closeConnection(connection, session, producer, null);  
        }  
    }  
}  