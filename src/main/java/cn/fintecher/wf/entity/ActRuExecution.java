package cn.fintecher.wf.entity;
/**
 * 功能说明：
 * @author lss
 * 修改人: 
 * 修改原因：
 * 修改时间：
 * 修改内容：
 * 创建日期：2017年8月28日 下午5:04:22
 * Copyright zzl-apt
 */
public class ActRuExecution {
	//流程关联业务字段
	private String businessKey;
	//流程实例id
	private String id;
	
	public String getBusinessKey() {
		return businessKey;
	}
	public void setBusinessKey(String businessKey) {
		this.businessKey = businessKey;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

}
