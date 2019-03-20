package cn.fintecher.wf.admin;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.fintecher.wf.entity.SysUserEntity;
import cn.fintecher.wf.utils.ShiroUtils;

/**
 * Controller公共组件
 * 
 */
public abstract class AbstractController {
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	protected SysUserEntity getUser() {
		return ShiroUtils.getUserEntity();
	}

	protected Long getUserId() {
		return getUser().getUserId();
	}
}
