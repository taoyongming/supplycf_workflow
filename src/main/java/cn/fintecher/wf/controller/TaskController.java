package cn.fintecher.wf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cn.fintecher.wf.admin.AbstractController;
import cn.fintecher.wf.utils.PageUtils;
import cn.fintecher.wf.utils.R;
import org.activiti.engine.FormService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务管理
 */
@RestController
@RequestMapping("task")
public class TaskController extends AbstractController{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskController.class);

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
    ObjectMapper objectMapper;

	/**
	 * 所有流程
	 */
	@RequestMapping("/list")
	//@RequiresPermissions("sys:user:list")
	public R list(@RequestParam Map<String, Object> params){
		
		//查询列表数据		
        int page = Integer.parseInt(params.get("page").toString());
        int rowSize = Integer.parseInt(params.get("limit").toString());        
        List<Task> taskList = taskService.createTaskQuery().taskCandidateOrAssigned(this.getUser().getUsername()).listPage(rowSize * (page - 1), rowSize);
        List<Map<String,Object>> list = new ArrayList<>();
        for(Task task : taskList){
        	Map<String,Object> taskMap = new HashMap<>();
        	taskMap.put("id", task.getId());
        	taskMap.put("processDefinitionId", task.getProcessDefinitionId());
        	taskMap.put("processInstanceId", task.getProcessInstanceId());
        	taskMap.put("name", task.getName());
        	taskMap.put("assignee", task.getAssignee());
        	taskMap.put("createTime", task.getCreateTime());
        	list.add(taskMap);
        }
		
        long count = taskService.createTaskQuery().taskCandidateOrAssigned(this.getUser().getUsername()).count();
		
		PageUtils pageUtil = new PageUtils(list, (int)count, rowSize, page);
		
		return R.ok().put("page", pageUtil);
	}
	
    /**
     * 签收任务
     */
    @RequestMapping(value = "/claim/{id}")
    public R claim(@PathVariable("id") String taskId) {
        taskService.claim(taskId, this.getUser().getUsername());
        return R.ok();
    }
    
    /**
     * 读取用户任务的表单字段
     */
    @RequestMapping(value = "/getform/{taskId}")
    public R readTaskForm(@PathVariable("taskId") String taskId) {        
        Map<String, Object> map = new HashMap<>();
        TaskFormData taskFormData = formService.getTaskFormData(taskId);
        Map<String,Object> taskFormDataMap = new HashMap<>();
        Task task = null;
        if (taskFormData.getFormKey() != null) {
            Object renderedTaskForm = formService.getRenderedTaskForm(taskId);
            map.put("taskFormDataStr", renderedTaskForm);
            map.put("hasFormKey", true);
            
            task = taskService.createTaskQuery().taskId(taskId).singleResult();
        } else {
        	map.put("hasFormKey", false);
 
            List<Map<String,Object>> formProperties = new ArrayList<>();
            for(FormProperty formProperty : taskFormData.getFormProperties()){
            	Map<String,Object> formPropertyMap = new HashMap<>(); 
            	formPropertyMap.put("id", formProperty.getId());
            	formPropertyMap.put("name", formProperty.getName());
            	formPropertyMap.put("type", formProperty.getType());
            	formPropertyMap.put("value", formProperty.getValue());
            	Object values = formProperty.getType().getInformation("values");
            	formPropertyMap.put("values", values);
            	formPropertyMap.put("readable", formProperty.isReadable());
            	formPropertyMap.put("writable", formProperty.isWritable());
            	formPropertyMap.put("required", formProperty.isRequired());
            	formProperties.add(formPropertyMap);
            }
            taskFormDataMap.put("formProperties", formProperties);
            
            task = taskFormData.getTask();

        }

        // 读取附件
        List<Attachment> attachments = null;
        if (task.getTaskDefinitionKey() != null) {
            attachments = taskService.getTaskAttachments(taskId);
        } else {
            attachments = taskService.getProcessInstanceAttachments(task.getProcessInstanceId());
        }
        taskFormDataMap.put("attachments", attachments);
        
        taskFormDataMap.put("deploymentId", taskFormData.getDeploymentId());
        taskFormDataMap.put("formKey", taskFormData.getFormKey());
        
    	Map<String,Object> taskMap = new HashMap<>();
    	taskMap.put("id", task.getId());
    	taskMap.put("processDefinitionId", task.getProcessDefinitionId());
    	taskMap.put("processInstanceId", task.getProcessInstanceId());
    	taskMap.put("name", task.getName());
    	taskMap.put("assignee", task.getAssignee());
    	taskMap.put("createTime", task.getCreateTime());
    	taskFormDataMap.put("task", taskMap);
    	
    	map.put("taskFormData", taskFormDataMap);
    	
        return R.ok(map);
    }
    
    /**
     * 读取启动流程的表单字段
     */
    @RequestMapping(value = "/complete/{taskId}")
    public R completeTask(@PathVariable("taskId") String taskId, @RequestBody Map<String, String> params) {
        TaskFormData taskFormData = formService.getTaskFormData(taskId);
        String formKey = taskFormData.getFormKey();
        // 从请求中获取表单字段的值
        List<FormProperty> formProperties = taskFormData.getFormProperties();
        Map<String, String> formValues = new HashMap<String, String>();

        if (StringUtils.isNotBlank(formKey)) { // formkey表单
        	formValues = params;
        } else { // 动态表单
            for (FormProperty formProperty : formProperties) {
                if (formProperty.isWritable()) {
                    String value = params.get(formProperty.getId());
                    formValues.put(formProperty.getId(), value);
                }
            }
        }
        formService.submitTaskFormData(taskId, formValues);
        return R.ok();
    }
    
}
