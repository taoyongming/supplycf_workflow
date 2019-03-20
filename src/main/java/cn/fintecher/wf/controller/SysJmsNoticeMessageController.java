package cn.fintecher.wf.controller;

import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.fintecher.wf.entity.SysJmsNoticeMessageEntity;
import cn.fintecher.wf.service.SysJmsNoticeMessageService;
import cn.fintecher.wf.utils.PageUtils;
import cn.fintecher.wf.utils.Query;
import cn.fintecher.wf.utils.R;


/**
 * 
 * 
 * @author integration
 * @email integration@fintecher.cn
 * @date 2017-09-28 17:21:51
 */
@RestController
@RequestMapping("sysjmsnoticemessage")
public class SysJmsNoticeMessageController {
	@Autowired
	private SysJmsNoticeMessageService sysJmsNoticeMessageService;
	
	/**
	 * 列表
	 */
	@RequestMapping("/list")
	@RequiresPermissions("sysjmsnoticemessage:list")
	public R list(@RequestParam Map<String, Object> params){
		//查询列表数据
        Query query = new Query(params);

		List<SysJmsNoticeMessageEntity> sysJmsNoticeMessageList = sysJmsNoticeMessageService.queryList(query);
		int total = sysJmsNoticeMessageService.queryTotal(query);
		
		PageUtils pageUtil = new PageUtils(sysJmsNoticeMessageList, total, query.getLimit(), query.getPage());
		
		return R.ok().put("page", pageUtil);
	}
	
	
	/**
	 * 信息
	 */
	@RequestMapping("/info/{id}")
	@RequiresPermissions("sysjmsnoticemessage:info")
	public R info(@PathVariable("id") Long id){
		SysJmsNoticeMessageEntity sysJmsNoticeMessage = sysJmsNoticeMessageService.queryObject(id);
		
		return R.ok().put("sysJmsNoticeMessage", sysJmsNoticeMessage);
	}
	
	/**
	 * 保存
	 */
	@RequestMapping("/save")
	@RequiresPermissions("sysjmsnoticemessage:save")
	public R save(@RequestBody SysJmsNoticeMessageEntity sysJmsNoticeMessage){
		sysJmsNoticeMessageService.save(sysJmsNoticeMessage);
		
		return R.ok();
	}
	
	/**
	 * 修改
	 */
	@RequestMapping("/update")
	@RequiresPermissions("sysjmsnoticemessage:update")
	public R update(@RequestBody SysJmsNoticeMessageEntity sysJmsNoticeMessage){
		sysJmsNoticeMessageService.update(sysJmsNoticeMessage);
		
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@RequestMapping("/delete")
	@RequiresPermissions("sysjmsnoticemessage:delete")
	public R delete(@RequestBody Long[] ids){
		sysJmsNoticeMessageService.deleteBatch(ids);
		
		return R.ok();
	}
	
}
