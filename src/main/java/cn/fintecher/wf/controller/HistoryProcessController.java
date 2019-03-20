package cn.fintecher.wf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cn.fintecher.wf.admin.AbstractController;
import cn.fintecher.wf.utils.PageUtils;
import cn.fintecher.wf.utils.R;
import cn.fintecher.wf.utils.annotation.SysLog;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运行中流程
 */
@RestController
@RequestMapping("process/history")
public class HistoryProcessController extends AbstractController{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HistoryProcessController.class);

    @Autowired
    protected RepositoryService repositoryService;
    @Autowired
    protected IdentityService identityService;
    @Autowired
    protected RuntimeService runtimeService;
    @Autowired
    protected TaskService taskService;
    @Autowired
    ProcessEngineFactoryBean processEngine;
    @Autowired
    ProcessEngineConfiguration processEngineConfiguration;
    @Autowired
    HistoryService historyService;
    @Autowired
    ObjectMapper objectMapper;

    /**
     * 查询已结束流程实例
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/list")
  //@RequiresPermissions("historyprocess:list")
    public R finishedProcessInstanceList(@RequestParam Map<String, Object> params) {
		//查询列表数据		
        int page = Integer.parseInt(params.get("page").toString());
        int rowSize = Integer.parseInt(params.get("limit").toString());
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().finished().listPage(rowSize * (page - 1), rowSize);

        long count = historyService.createHistoricProcessInstanceQuery().finished().count();
		
		PageUtils pageUtil = new PageUtils(list, (int)count, rowSize, page);
		
		return R.ok().put("page", pageUtil);
    }
    

	
	/**
	 * 删除流程
	 */
	@SysLog("删除流程")
	@RequestMapping("/delete")
	//@RequiresPermissions("historyprocess:delete")
	public R delete(@RequestBody String[] instanceIds){
		for(String instanceId : instanceIds){
			historyService.deleteHistoricProcessInstance(instanceId);
		}
		
		return R.ok();
	}
	
	/**
	 * 查看流程
	 * @param processInstanceId
	 * @return R
	 */
    @RequestMapping(value = "/view/{processInstanceId}")
   //@RequiresPermissions("historyprocess:view")
    public R historyDatas(@PathVariable("processInstanceId") String processInstanceId) {

        List<HistoricActivityInstance> activityInstances = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).list();

        // 查询历史流程实例
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();

        // 查询流程有关的变量
        List<HistoricVariableInstance> variableInstances = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId).list();

        List<HistoricDetail> formProperties = historyService.createHistoricDetailQuery().processInstanceId(processInstanceId).formProperties().list();

        // 查询流程定义对象
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(historicProcessInstance.getProcessDefinitionId()).singleResult();

        Map<String, Object> map = new HashMap<>();
        map.put("historicProcessInstance", historicProcessInstance);
        map.put("variableInstances", variableInstances);
        map.put("activities", activityInstances);
        
        map.put("formProperties", formProperties);
        
        Map<String,Object> processDefinitionMap=new HashMap<>();
        processDefinitionMap.put("id",processDefinition.getId());
        processDefinitionMap.put("name",processDefinition.getName());
        processDefinitionMap.put("version",processDefinition.getVersion());
        processDefinitionMap.put("key",processDefinition.getKey());
        processDefinitionMap.put("deploymentId",processDefinition.getDeploymentId());
        processDefinitionMap.put("suspended",processDefinition.isSuspended());
        processDefinitionMap.put("resource",processDefinition.getResourceName());
        processDefinitionMap.put("image",processDefinition.getDiagramResourceName());
        map.put("processDefinition", processDefinitionMap);

        return R.ok(map);
    }
}
