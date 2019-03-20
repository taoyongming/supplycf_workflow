package cn.fintecher.wf.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.fintecher.wf.entity.ActRuExecution;
import cn.fintecher.wf.entity.ActRuTask;
import cn.fintecher.wf.service.ExecutionService;
import cn.fintecher.wf.service.RuTaskService;
import cn.fintecher.wf.utils.R;
import cn.fintecher.wf.utils.annotation.IgnoreAuth;
import cn.fintecher.wf.utils.validator.Assert;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 功能说明：
 * 
 * @author lss 修改人: 修改原因： 修改时间： 修改内容： 创建日期：2017年8月28日 下午2:19:09 Copyright
 *         zzl-apt
 */
@RestController
@RequestMapping("/api/process")
@Api("流程管理")
public class ApiProcessManageController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApiProcessManageController.class);

	@Autowired
	protected RepositoryService repositoryService;

	@Autowired
	protected IdentityService identityService;

	@Autowired
	protected RuntimeService runtimeService;

	@Autowired
	protected TaskService taskService;

	@Autowired
	protected FormService formService;

	@Autowired
	protected ExecutionService executionService;

	@Autowired
	protected RuTaskService ruTaskService;

	@Autowired
	HistoryService historyService;

	@Autowired
	ObjectMapper objectMapper;

	/**
	 * 启动流程
	 *
	 * @return
	 */
	@IgnoreAuth
	@PostMapping(value = "start")
	@ApiOperation(value = "开启流程", notes = "")
	@ApiImplicitParams({
	@ApiImplicitParam(paramType = "query", dataType = "string", name = "processKey", value = "流程定义key", required = true),
	@ApiImplicitParam(paramType = "query", dataType = "string", name = "variables", value = "参数列表；请严格按照net.sf.json.JSONObject传递，详情见接口文档", required = true),
	@ApiImplicitParam(paramType = "query", dataType = "string", name = "param", value = "需要存储的流程变量参数", required = true)})
	public R startProcess(String processKey,String variables,String param) {
		LOGGER.info("开启流程 startProcess:processKey=" + processKey );

		Assert.isBlank(processKey, "not null:流程编号");
//		Assert.isBlank(id, "not null:业务编号");
		// 返回实例对象
		Map<String, Object> map = new HashMap<String, Object>();
		map = JSONObject.fromObject(variables);
	
		// 流程定义的key
		ProcessInstance pi = null;
		try {
			pi = runtimeService.startProcessInstanceByKey(processKey, map);// 启动流程
			runtimeService.setVariable(pi.getProcessInstanceId(),"param",param);
			List<Task> list = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
			for (Task task2 : list) {
				map.put("userId", task2.getAssignee());
				map.put("name",task2.getName());
				map.put("taskId",task2.getId());
				map.put("id",pi.getActivityId());
			}			
			if (pi == null) {
				return R.error("流程启动错误！");
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage());
			return R.error("系统异常！");
		}
		map.put("processInstanceId", pi.getProcessInstanceId());
		map.put("processDefinitionId", pi.getProcessDefinitionId());
//		map.put("processDefinitionName",task.get);
		map.put("processDefinitionKey",pi.getProcessDefinitionKey());
		return R.ok(map);
	}

	/**
	 * 查询当前人的个人任务
	 *
	 * @return
	 */
	@IgnoreAuth
	@PostMapping(value = "queryTasks")
	@ApiOperation(value = "查询任务", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", dataType = "string", name = "assignee", value = "任领值", required = true)
			// @ApiImplicitParam(paramType = "query", dataType = "string", name
			// = "firstResult", value = "页码", required = true),
			// @ApiImplicitParam(paramType = "query", dataType = "string", name
			// = "maxResults", value = "页数", required = true)
	})
	public R queryTasks(String assignee
	// String firstResult,
	// String maxResults
	) {
		LOGGER.info("查询任务 assignee=" + assignee);
		// ",firstResult=" + firstResult +
		// ",maxResults=" + maxResults

		Assert.isBlank(assignee, "not null:任领值");
		// Assert.isBlank(firstResult, "not null:页码");
		// Assert.isBlank(maxResults, "not null:页数");
	
		// 查询当前申请值对应的活动任务
		List<Task> tasks = taskService.createTaskQuery().taskAssignee(assignee)// 指定个人任务查询，指定办理人
				.orderByTaskCreateTime().desc()// 使用创建时间的升序排列
				.list();

		// 返回实例对象
		Map<String, Object> map = new HashMap<String, Object>();

		List<Object> list = new ArrayList<Object>();

		Map<String, Object> json = null;
		if (tasks != null && tasks.size() > 0) {
			for (Task task : tasks) {
				json = new HashMap<String, Object>();
				json.put("param",runtimeService.getVariableLocal(task.getExecutionId(),"param"));
				// 任务对应的流程唯一编号
				String processInstanceId = task.getProcessInstanceId();
				// 查询当前执行信息
				ActRuExecution actRuExecution = new ActRuExecution();
				actRuExecution.setId(processInstanceId);
				ActRuExecution execution = executionService.queryObject(actRuExecution);
				
				// json.put("task",task.get);
				json.put("processInstanceId", processInstanceId);
				json.put("name", task.getName());
				json.put("businessKey", (execution == null ? "" : execution.getBusinessKey()));
				list.add(json);
			}
		}
		map.put("data", list);
		return R.ok(map);
	}

	/**
	 * 审核
	 */

	@IgnoreAuth
	@PostMapping(value = "next")
	@ApiOperation(value = "节点跳转接口", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", dataType = "string", name = "processInstanceId", value = "流程实例编号", required = true),
			@ApiImplicitParam(paramType = "query", dataType = "string", name = "variables", value = "参数列表；请严格按照net.sf.json.JSONObject<String>传递，详情见接口文档", required = true) })
	public R check(@RequestParam("processInstanceId") String processInstanceId,
			@RequestParam("variables") String variables) {
		LOGGER.info("查询任务 check processInstanceId=" + processInstanceId + ",variables=" + variables);

		Assert.isBlank(processInstanceId, "not null:流程编号：processInstanceId");
		Map<String,Object> resultMap = new HashMap<String,Object>();
		ActRuTask actRuTask = new ActRuTask();
		actRuTask.setProcInstId(processInstanceId);
		ActRuTask task = null;
		try {
			task = ruTaskService.queryObject(actRuTask);
			Assert.isNull(task, "查无此进行中的流程实例！");
			taskService.complete(task.getId(), JSONObject.fromObject(variables));
			 ProcessInstance rpi =runtimeService.createProcessInstanceQuery()
			.processInstanceId(processInstanceId)
			.singleResult();
			 Map<String,Object> map = new HashMap<String,Object>();
			 task = ruTaskService.queryObject(actRuTask);
			 //判断流程是否结束
			 if (rpi == null) {
				 map.put("verProcessIsEnd","true");
			 } else {
				 map.put("verProcessIsEnd","false");
				 List<Task> list = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
					for (Task task2 : list) {
						map.put("userId", task2.getAssignee());
						map.put("name",task2.getName());
						map.put("id",rpi.getActivityId());
						map.put("taskId",task2.getId());
					}
			 }
			return R.ok(map);
		} catch (Exception e) {
			e.printStackTrace();
			return R.error("系统异常！");
		}
	}

	@IgnoreAuth
	@PostMapping(value = "queryHistoricTasks")
	@ApiOperation(value = "查询已完成任务", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", dataType = "string", name = "assignee", value = "任务节点的Assignee", required = true),
			@ApiImplicitParam(paramType = "query", dataType = "string", name = "firstResult", value = "页码", required = true),
			@ApiImplicitParam(paramType = "query", dataType = "string", name = "maxResults", value = "页数", required = true) })
	public R queryHistoricTasks(@RequestParam("assignee") String assignee,
			@RequestParam("firstResult") String firstResult, @RequestParam("maxResults") String maxResults) {

		// 返回实例对象
		Map<String, Object> map = new HashMap<String, Object>();

		List<Object> list = new ArrayList<Object>();

		Map<String, Object> json = null;
		List<HistoricTaskInstance> tasks = historyService// 与历史数据（历史表）相关的Service
				.createHistoricTaskInstanceQuery()// 创建历史任务实例查询
				.taskAssignee(assignee).orderByTaskCreateTime().desc().listPage(
						Integer.valueOf(maxResults) * (Integer.valueOf(firstResult) - 1), Integer.valueOf(maxResults));

		for (HistoricTaskInstance task : tasks) {

			// 任务对应的流程唯一编号
			String processsInstanceId = task.getProcessInstanceId();
			HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
					.processInstanceId(processsInstanceId).singleResult();
			json = new HashMap<String, Object>();
			json.put("historicProcessInstance", historicProcessInstance);
			list.add(json);
		}
		map.put("data", list);
		return R.ok(map);
	}
	
	
	//
	// @IgnoreAuth
	// @PostMapping(value = "running/send")
	// @ApiOperation(value = "发送消息", notes = "")
	// @ApiImplicitParams({
	// @ApiImplicitParam(paramType = "query", dataType = "string", name =
	// "queue", value = "队列名称", required = true) })
	// public void dataSend(@RequestParam("queue") String queue) {
	// try { // 发送队列
	// new JmsUtils().dataSend("text", "123456", "", queue);
	//
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	@IgnoreAuth
	@PostMapping(value = "authButtons")
	@ApiOperation(value = "查询当前任务节点的操作按钮（排它分支的所有走向）", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", dataType = "string", name = "processInstanceId", value = "流程编号", required = true) })
	public R authButtons(@RequestParam("processInstanceId") String processInstanceId) {
		LOGGER.info("查询当前任务节点的操作按钮（排它分支的所有走向） authButtons processInstanceId=" + processInstanceId);

		Assert.isBlank(processInstanceId, "not null:流程编号");

		// 返回实例对象
		Map<String, Object> map = new HashMap<String, Object>();

		List<Object> list = new ArrayList<Object>();

		try {
				
			// 当前任务实体
			Task task = taskService.createTaskQuery().executionId(processInstanceId).singleResult();
			
			// 流程定义ID
			String processDefinitionId = task.getProcessDefinitionId();
			// 流程定义实体
			ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) repositoryService
					.getProcessDefinition(processDefinitionId);

			// 流程实例实体
			ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
					.processInstanceId(processInstanceId).singleResult();
			// 节点ID
			String activityId = processInstance.getActivityId();
			ActivityImpl activityImpl = processDefinition.findActivity(activityId);
			// 后序节点1，2，3；
			List<PvmTransition> pvmTransitions = activityImpl.getOutgoingTransitions();
			String exclusiveGateWayId = "";// 排它网关编号
			String roleId = "";//下一步处理人的角色id
			for (PvmTransition pvmTransition : pvmTransitions) {
				exclusiveGateWayId = pvmTransition.getDestination().getId();
				roleId = pvmTransition.getDestination().getProperty("name")==null?"":pvmTransition.getDestination().getProperty("name").toString();
			}
			// 根据排它节点ID查找后序的平行排它网关名称，以便在程序中实现动态控制
			activityImpl = processDefinition.findActivity(exclusiveGateWayId);
			pvmTransitions = activityImpl.getOutgoingTransitions();
			
			// 循环输出一下各排它节点名称
			for (PvmTransition pvmTransition : pvmTransitions) {
				Map<String,Object> resultMap = new HashMap<String,Object>();
				 PvmActivity ac = pvmTransition.getDestination();
				 resultMap.put("name",pvmTransition.getProperty("name"));
				 resultMap.put("id", ac.getId());
				 list.add(resultMap);
			}
			map.put("data", list);
			map.put("id",processInstance.getActivityId());
			map.put("name", task.getName());
			map.put("userId", task.getAssignee());
			map.put("roleId",roleId);
			map.put("createUserId",JSONObject.fromObject(runtimeService.getVariableLocal(task.getExecutionId(),"param")).get("createUserId"));
			return R.ok(map);
		} catch (Exception e) {
			e.printStackTrace();
			return R.error("系统异常！");
		}
	}

	@IgnoreAuth
	@PostMapping(value = "claim")
	@ApiOperation(value = "任领", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", dataType = "string", name = "processInstanceId", value = "流程编号", required = true),
			@ApiImplicitParam(paramType = "query", dataType = "string", name = "assgin", value = "任领值", required = true) })
	public R claim(@RequestParam("processInstanceId") String processInstanceId, @RequestParam("assgin") String assgin) {
		Assert.isBlank(processInstanceId, "not null:流程编号");
		Assert.isBlank(assgin, "not null:任领值");

		ActRuTask actRuTask = new ActRuTask();
		actRuTask.setProcInstId(processInstanceId);
		ActRuTask task = ruTaskService.queryObject(actRuTask);
		// 完成任务的同时，设置流程变量，使用流程变量用来指定完成任务后
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("assgin", assgin);
		taskService.complete(task.getId(), variables);
		return R.ok();
	}
	
	
	@IgnoreAuth
	@PostMapping(value = "weiwai")
	@ApiOperation(value = "weiwai", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", dataType = "string", name = "processInstanceId", value = "流程编号", required = true),
			@ApiImplicitParam(paramType = "query", dataType = "string", name = "assgin", value = "任领值", required = true) })
	public R weiwai(@RequestParam("processInstanceId") String processInstanceId, @RequestParam("assgin") String assgin) {
		Assert.isBlank(processInstanceId, "not null:流程编号");
		Assert.isBlank(assgin, "not null:任领值");

		ActRuTask actRuTask = new ActRuTask();
		actRuTask.setProcInstId(processInstanceId);
		ActRuTask task = ruTaskService.queryObject(actRuTask);
		List<Task> list = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
		for (Task task2 : list) {
			System.out.println(task2.getName()+"|"+task2.getId()+"|"+task2.getAssignee());
			taskService.setOwner(task2.getId(), task2.getAssignee());
			taskService.setAssignee(task2.getId(), assgin);
		}
		
		// 完成任务的同时，设置流程变量，使用流程变量用来指定完成任务后
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("assgin", assgin);
//		taskService.complete(task.getId(), variables);
		return R.ok();
	}
	
}
