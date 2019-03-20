package cn.fintecher.wf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cn.fintecher.wf.admin.AbstractController;
import cn.fintecher.wf.utils.PageUtils;
import cn.fintecher.wf.utils.R;
import cn.fintecher.wf.utils.annotation.SysLog;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

/**
 * 运行中流程
 */
@RestController
@RequestMapping("process/running")
public class RunningProcessController extends AbstractController{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RunningProcessController.class);

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
    ObjectMapper objectMapper;

	/**
	 * 所有运行中流程
	 */
	@RequestMapping("/list")
	//@RequiresPermissions("sys:user:list")
	public R list(@RequestParam Map<String, Object> params){
		
		List<Map<String,Object>> list=new ArrayList<>();
		//查询列表数据		
        int page = Integer.parseInt(params.get("page").toString());
        int rowSize = Integer.parseInt(params.get("limit").toString());
        List<ProcessInstance> processInstanceList = runtimeService.createProcessInstanceQuery().listPage(rowSize * (page - 1), rowSize);
        for (ProcessInstance processInstance : processInstanceList) {
            Map<String,Object> map=new HashMap<>();
            map.put("id",processInstance.getId());
            map.put("name",processInstance.getName());
            map.put("definitionId",processInstance.getProcessDefinitionId());
            map.put("deploymentId",processInstance.getDeploymentId());
            map.put("activityId",processInstance.getActivityId());
            map.put("nodeName","");
            map.put("suspended",processInstance.isSuspended());

            //取当前节点
            ProcessDefinitionEntity entity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getProcessDefinition(processInstance.getProcessDefinitionId());
            for (ActivityImpl bean :entity.getActivities()){
                if (bean.getId().equals(processInstance.getActivityId())){
                    map.put("nodeName",bean.getProperty("name"));
                }
            }

            list.add(map);
        }
		
        long count = runtimeService.createProcessInstanceQuery().count();
		
		PageUtils pageUtil = new PageUtils(list, (int)count, rowSize, page);
		
		return R.ok().put("page", pageUtil);
	}
	
	/**
	 * 删除流程
	 */
	@SysLog("删除流程")
	@RequestMapping("/delete")
	//@RequiresPermissions("model:delete")
	public R delete(@RequestBody String[] instanceIds){
		for(String instanceId : instanceIds){
			runtimeService.deleteProcessInstance(instanceId,"删除流程实例["+instanceId+"]");
		}
		
		return R.ok();
	}
	
    /**
     * 读取带跟踪的图片
     */
    @RequestMapping(value = "/trace")
    public void readResource(@RequestParam("executionId") String executionId, HttpServletResponse response)
        throws Exception {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(executionId).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        List<String> activeActivityIds = runtimeService.getActiveActivityIds(executionId);
        // 不使用spring请使用下面的两行代码
//    ProcessEngineImpl defaultProcessEngine = (ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine();
//    Context.setProcessEngineConfiguration(defaultProcessEngine.getProcessEngineConfiguration());

        // 使用spring注入引擎请使用下面的这行代码
        processEngineConfiguration = processEngine.getProcessEngineConfiguration();
        Context.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngineConfiguration);

        ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
        InputStream imageStream = diagramGenerator.generateDiagram(
            bpmnModel,
            "png",
            activeActivityIds,
            new ArrayList<String>(),
            "宋体",
            "宋体",
            "宋体",
            null,
            1.0);
        // 输出资源内容到相应对象
        byte[] b = new byte[1024];
        int len;
        while ((len = imageStream.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
    }

}
