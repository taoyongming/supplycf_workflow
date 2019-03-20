package cn.fintecher.wf.service;

import cn.fintecher.wf.entity.ShortMessageEntity;

public interface MsgService {
	
	public String sendMsg(ShortMessageEntity shortMessageEntity);
	
	public void receiveResponseState(String msgid,String mobile,String status);
	
	public void retryBatchSend(Long[] ids) throws Exception ;

}
