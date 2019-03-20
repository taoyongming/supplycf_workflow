package cn.fintecher.wf.jms;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import cn.fintecher.wf.entity.SysJmsNoticeMessageEntity;


@Component
public class Consumer {
	
	// 使用JmsListener配置消费者监听的队列，其中text是接收到的消息
	@JmsListener(destination = "WF.Topic")
	public void receiveQueueDQL(SysJmsNoticeMessageEntity text) {
		System.out.println("WF.Topic:Consumer收到的报文为:" + text);
	}
}