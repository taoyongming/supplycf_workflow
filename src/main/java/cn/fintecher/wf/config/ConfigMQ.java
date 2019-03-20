package cn.fintecher.wf.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.activemq")
public class ConfigMQ {
	
	private static String brokerUrl;
	private static String user;
	private static String password;
	private static String DQL;
	private static String ERP;
	private static String ARC;
	private static String TOPIC;
	
	public static String getBrokerUrl() {
		return brokerUrl;
	}
	public static String getUser() {
		return user;
	}
	public static String getPassword() {
		return password;
	}
	public static String getDQL() {
		return DQL;
	}
	public static String getERP() {
		return ERP;
	}
	public static String getARC() {
		return ARC;
	}
	public static void setBrokerUrl(String brokerUrl) {
		ConfigMQ.brokerUrl = brokerUrl;
	}
	public static void setUser(String user) {
		ConfigMQ.user = user;
	}
	public static void setPassword(String password) {
		ConfigMQ.password = password;
	}
	public static void setDQL(String dQL) {
		DQL = dQL;
	}
	public static void setERP(String eRP) {
		ERP = eRP;
	}
	public static void setARC(String aRC) {
		ARC = aRC;
	}
	public static String getTOPIC() {
		return TOPIC;
	}
	public static void setTOPIC(String tOPIC) {
		TOPIC = tOPIC;
	}
}