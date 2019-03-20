package cn.fintecher.wf.jms.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.fintecher.wf.config.ConfigMQ;
import cn.fintecher.wf.entity.ActRuExecution;
import cn.fintecher.wf.entity.SysJmsNoticeMessageEntity;
import cn.fintecher.wf.jms.JmsUtils;
import cn.fintecher.wf.service.ExecutionService;
import cn.fintecher.wf.service.SysJmsNoticeMessageService;
import cn.fintecher.wf.utils.ChkUtil;
import cn.fintecher.wf.utils.DateUtils;
import cn.fintecher.wf.utils.SpringContextUtils;

/**
 * 
 * @author asus
 * @title 流程管理节点（create时）触发JMS消息收发策略
 * 流程节点ASSIGN唯一编码：
	DQL-001-INIT-ASN： 贷企来初始节点
	DQL-002-RECO-ASN：贷企来担保人recognizor信息填写节点
	ARC-001-ENDO-ASN：	自动化Auto风控Risk Control企业主体enterprise dominant-Assign
	## ARC-002-DEBT-ASN:	自动化Auto风控Risk Control借贷人DEBTOR –Assign
	## ARC-003-MATE-ASN：	自动化Auto风控 配偶mate-Assign
	ARC-002-RECO-ASN:    自动化Auto风控担保人审核recognizor-Assign
	ERP-001-ENDO-ASN ： ERP-(借贷主体总部初审企业主体enterprise dominant)-Assign
	ERP-002-RECO -ASN ： ERP-(担保人总部初审check)-Assign
	ERP-003-SHMA-ASN ： ERP-(门店shop资料materia上传节点)-Assign
	ERP-004-HQRK-ASN ： ERP-(总部 (=headquarters)复审recheck)-Assign
	ERP-005-LOCK-ASN ： ERP-(现场local勘查check)-Assign
	ERP-006-GEFIN-ASN ： ERP-(总部general终审final check)-Assign
	ERP-007-FCRP-ASN ： ERP-(面Face审check资源池)-Assign
	ERP-008-FMRP-ASN ： ERP-(资金fund匹配match资源池)-Assign
	ERP-009-OARP-ASN ： ERP-(开立open账户account资源池)-Assign
	ERP-010-SCRP-ASN ： ERP-(签约Assign客户client资源池)-Assign
	ERP-011-PCRP-ASN ： ERP-(电话phone审核check资源池)-Assign
	ERP-012-CCRP-ASN ： ERP-(合同contract审核check资源resource池pool)-Assign
	ERP-013-FLRP-ASN ： ERP-(财务finance放款loan资源resource池pool)-Assign
	
	mq消息队列名：
	DQL-Q 表示贷企来APP需要监听的队列名
	ARC-Q 表示自动化风控引擎需要监听的队列名
	ERP-Q 表示ERP系统需要监听的队列名
	mq topic:
	WF-TOPIC
 *
 */
public class AssignListener{
	
}
//public class AssignListener implements TaskListener {
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = 1L;
//	
//	private static final Logger LOGGER = LoggerFactory.getLogger(AssignListener.class);
//	
//	private SysJmsNoticeMessageService jmsService = (SysJmsNoticeMessageService)SpringContextUtils.getBean("sysJmsNoticeMessageService");
//	
//	protected ExecutionService executionService = (ExecutionService)SpringContextUtils.getBean("executionService");
//
//	@Override
//	public void notify(DelegateTask delegateTask) {
//		
//		LOGGER.info("notify(DelegateTask delegateTask) begin :" + delegateTask);
//		String assign = delegateTask.getAssignee();//流程配置的固定Assign，如DQL-001-INIT-ASN表示贷企来初始节点
//		String queue = "";//队列或TOPIC名称
//		String processInstanceId = delegateTask.getProcessInstanceId();
//		try {
//			queue = ConfigMQ.getTOPIC();//topic消息机制
//			SysJmsNoticeMessageEntity jmsMessage = new SysJmsNoticeMessageEntity();
//			
//			// 查询当前执行信息
//			ActRuExecution actRuExecution = new ActRuExecution();
//			actRuExecution.setId(processInstanceId);
//			ActRuExecution execution = executionService.queryObject(actRuExecution);
//			jmsMessage.setBusinessKey((ChkUtil.isEmpty(execution) || ChkUtil.isEmpty(execution.getBusinessKey()))? "" : execution.getBusinessKey());
//			jmsMessage.setCode(assign);
//			jmsMessage.setIsConfirmed("0"); //消息未确认
//			jmsMessage.setProcessInstanceId(processInstanceId);
//			jmsMessage.setQueue(queue);
//			jmsMessage.setSendTime(DateUtils.getCurrentTime());
//			jmsMessage.setStatus("1");
//			jmsService.save(jmsMessage);
//			//将text消息发送至对应的MQ队列中
//			new JmsUtils().dataSend("object", jmsMessage, "topic", queue);
//			LOGGER.info("notify(DelegateTask delegateTask) end :" + jmsMessage);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}finally{
//		}
//	}
//}