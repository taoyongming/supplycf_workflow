package cn.fintecher.wf.service;

import cn.fintecher.wf.entity.ActRuExecution;

/**
 * 功能说明：
 * @author lss
 * 修改人: 
 * 修改原因：
 * 修改时间：
 * 修改内容：
 * 创建日期：2017年8月28日 下午5:10:59
 * Copyright zzl-apt
 */
public interface ExecutionService {
	
	void update(ActRuExecution actRuExecution);
	
	ActRuExecution queryObject(ActRuExecution actRuExecution);

}
