package cn.fintecher.wf.jms.listener;

import java.io.Serializable;

public class JmsNoticeMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id; // 主键编号
	/**
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
	 */
	private String code; // 
	
	private String queue; // 队列名称
	
	private String businessKey; // 业务主键如申请单编号、订单编号
	
	private String processInstanceId; //流程实例编号

	private String sendTime; // 消息发送操作时间
	
	private String confirmTime; // 消息确认时间
	
	private String isConfirmed; // 消息是否被确认0：未确认；1：已确认；

	private String status; // 状态
	
	private String data; // Json封装

	public JmsNoticeMessage() {
		super();
	}

	public String getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getQueue() {
		return queue;
	}

	public String getBusinessKey() {
		return businessKey;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public String getSendTime() {
		return sendTime;
	}

	public String getConfirmTime() {
		return confirmTime;
	}

	public String getIsConfirmed() {
		return isConfirmed;
	}

	public String getStatus() {
		return status;
	}

	public String getData() {
		return data;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

	public void setBusinessKey(String businessKey) {
		this.businessKey = businessKey;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}

	public void setConfirmTime(String confirmTime) {
		this.confirmTime = confirmTime;
	}

	public void setIsConfirmed(String isConfirmed) {
		this.isConfirmed = isConfirmed;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setData(String data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "JmsNoticeMessage [id=" + id + ", code=" + code + ", queue=" + queue + ", businessKey=" + businessKey
				+ ", processInstanceId=" + processInstanceId + ", sendTime=" + sendTime + ", confirmTime=" + confirmTime
				+ ", isConfirmed=" + isConfirmed + ", status=" + status + ", data=" + data + "]";
	}
}
