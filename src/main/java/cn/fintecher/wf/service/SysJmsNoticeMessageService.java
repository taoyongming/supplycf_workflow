package cn.fintecher.wf.service;

import cn.fintecher.wf.entity.SysJmsNoticeMessageEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 * 
 * @author integration
 * @email integration@fintecher.cn
 * @date 2017-09-28 17:21:51
 */
public interface SysJmsNoticeMessageService {
	
	SysJmsNoticeMessageEntity queryObject(Long id);
	
	List<SysJmsNoticeMessageEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(SysJmsNoticeMessageEntity sysJmsNoticeMessage);
	
	void update(SysJmsNoticeMessageEntity sysJmsNoticeMessage);
	
	void delete(Long id);
	
	void deleteBatch(Long[] ids);
}
