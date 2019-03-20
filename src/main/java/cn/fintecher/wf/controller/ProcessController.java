package cn.fintecher.wf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cn.fintecher.wf.admin.AbstractController;
import cn.fintecher.wf.utils.PageUtils;
import cn.fintecher.wf.utils.R;
import cn.fintecher.wf.utils.RRException;
import cn.fintecher.wf.utils.annotation.SysLog;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.FormService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * 流程管理
 */
@RestController
@RequestMapping("process")
public class ProcessController extends AbstractController{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessController.class);

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
		
		List<Map<String,Object>> list=new ArrayList<>();
		//查询列表数据		
        int page = Integer.parseInt(params.get("page").toString());
        int rowSize = Integer.parseInt(params.get("limit").toString());
        List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery().listPage(rowSize * (page - 1), rowSize);
        for (ProcessDefinition processDefinition : processDefinitionList) {
            Map<String,Object> map=new HashMap<>();
            String deploymentId = processDefinition.getDeploymentId();
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();

            map.put("id",processDefinition.getId());
            map.put("name",processDefinition.getName());
            map.put("version",processDefinition.getVersion());
            map.put("key",processDefinition.getKey());
            map.put("deploymentId",processDefinition.getDeploymentId());
            map.put("suspended",processDefinition.isSuspended());
            map.put("resource",processDefinition.getResourceName());
            map.put("image",processDefinition.getDiagramResourceName());

            map.put("deploymentTime",deployment.getDeploymentTime());

            list.add(map);
        }
		
        long count = repositoryService.createDeploymentQuery().count();
		
		PageUtils pageUtil = new PageUtils(list, (int)count, rowSize, page);
		
		return R.ok().put("page", pageUtil);
	}
	
    /**
     * 启动流程
     *
     * @return
     */
    @RequestMapping(value = "/start/{id}")
    public R startProcess(@PathVariable("id") String id) {
    	Map<String, Object> map = new HashMap<>();
    	
    	ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
        Map<String,Object> processDefinitionMap = new HashMap<>();
        processDefinitionMap.put("id",processDefinition.getId());
        processDefinitionMap.put("name",processDefinition.getName());
        processDefinitionMap.put("version",processDefinition.getVersion());
        processDefinitionMap.put("key",processDefinition.getKey());
        processDefinitionMap.put("deploymentId",processDefinition.getDeploymentId());
        processDefinitionMap.put("suspended",processDefinition.isSuspended());
        processDefinitionMap.put("resource",processDefinition.getResourceName());
        processDefinitionMap.put("image",processDefinition.getDiagramResourceName());
        map.put("processDefinition", processDefinitionMap);
        
    	boolean hasForm = false;
    	boolean hasStartFormKey = processDefinition.hasStartFormKey();
    	
        // 判断是否有formkey属性
        if (hasStartFormKey) {
        	hasForm = true;
            Object renderedStartForm = formService.getRenderedStartForm(id);
            map.put("startFormData", renderedStartForm);
            
        } else { // 动态表单字段
            StartFormData startFormData = formService.getStartFormData(id);
            if(startFormData.getFormProperties().size() > 0){
            	hasForm = true;
                Map<String,Object> startFormDataMap = new HashMap<>();
                startFormDataMap.put("deploymentId", startFormData.getDeploymentId());
                startFormDataMap.put("formKey", startFormData.getFormKey());
                startFormDataMap.put("formProperties", startFormData.getFormProperties());   
                map.put("startFormData", startFormDataMap);
            }
        }
        if(!hasForm){
        	runtimeService.startProcessInstanceById(id);
        }
        map.put("hasForm", hasForm);
        map.put("hasFormKey", hasStartFormKey);
        return R.ok(map);
    }
    
    /**
     * 转换为模型
     * @param processDefinitionId
     * @return
     * @throws UnsupportedEncodingException
     * @throws XMLStreamException
     */
    @RequestMapping(value = "/convert-to-model/{id}")
        public R convertToModel(@PathVariable("id") String processDefinitionId)
            throws UnsupportedEncodingException, XMLStreamException {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();
            InputStream bpmnStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(),
                processDefinition.getResourceName());
            XMLInputFactory xif = XMLInputFactory.newInstance();
            InputStreamReader in = new InputStreamReader(bpmnStream, "UTF-8");
            XMLStreamReader xtr = xif.createXMLStreamReader(in);
            BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);

            BpmnJsonConverter converter = new BpmnJsonConverter();
            ObjectNode modelNode = converter.convertToJson(bpmnModel);
            Model modelData = repositoryService.newModel();
            modelData.setKey(processDefinition.getKey());
            modelData.setName(processDefinition.getResourceName());
            modelData.setCategory(processDefinition.getDeploymentId());

            ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
            modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, processDefinition.getName());
            modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
            modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, processDefinition.getDescription());
            modelData.setMetaInfo(modelObjectNode.toString());

            repositoryService.saveModel(modelData);

            repositoryService.addModelEditorSource(modelData.getId(), modelNode.toString().getBytes("utf-8"));

            return R.ok();
        }
    
    /**
     * 流程部署
     * @param file
     * @return
     */
    @RequestMapping(value = "/deploy",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public R deploy(@RequestParam("file") MultipartFile file) {
        String fileName = file.getOriginalFilename();

        try {
            InputStream fileInputStream = file.getInputStream();

            String extension = FilenameUtils.getExtension(fileName);
            if (extension.equals("zip") || extension.equals("bar")) {
                ZipInputStream zip = new ZipInputStream(fileInputStream);
                repositoryService.createDeployment().addZipInputStream(zip).deploy();
            } else {
                repositoryService.createDeployment().addInputStream(fileName, fileInputStream).deploy();
            }
        } catch (Exception e) {
        	LOGGER.error("流程部署失败", e);
        	throw new RRException("流程部署失败");
        }
        return R.ok();
    }
	/**
	 * 删除流程
	 */
	@SysLog("删除流程")
	@RequestMapping("/delete")
	//@RequiresPermissions("model:delete")
	public R delete(@RequestBody String[] deploymentIds){
		for(String deploymentId : deploymentIds){
			repositoryService.deleteDeployment(deploymentId, true);
		}
		
		return R.ok();
	}
	
    /**
     * 读取资源，通过部署ID
     *
     * @param processDefinitionId 流程定义
     * @param resourceType        资源类型(xml|image)
     * @throws Exception
     */
    @RequestMapping(value = "/resource/read")
    public void loadByDeployment(@RequestParam("processDefinitionId") String processDefinitionId, @RequestParam("resourceType") String resourceType,
                                 HttpServletResponse response) throws Exception {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
        String resourceName = "";
        if (resourceType.equals("image")) {
            resourceName = processDefinition.getDiagramResourceName();
        } else if (resourceType.equals("xml")) {
            resourceName = processDefinition.getResourceName();
        }
        InputStream resourceAsStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), resourceName);
        byte[] b = new byte[1024];
        int len = -1;
        while ((len = resourceAsStream.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
    }

    @RequestMapping(value = "/startWithForm/{processDefinitionId}")
    public R startWithForm(@PathVariable("processDefinitionId") String pdid, @RequestBody Map<String, String> params) {

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(pdid).singleResult();
        boolean hasStartFormKey = processDefinition.hasStartFormKey();

        Map<String, String> formValues = new HashMap<String, String>();

        if (hasStartFormKey) { // formkey表单
            formValues = params;
        } else { // 动态表单
            // 先读取表单字段在根据表单字段的ID读取请求参数值
            StartFormData formData = formService.getStartFormData(pdid);

            // 从请求中获取表单字段的值
            List<FormProperty> formProperties = formData.getFormProperties();
            for (FormProperty formProperty : formProperties) {
                String value = params.get(formProperty.getId());
                formValues.put(formProperty.getId(), value);
            }
        }

        // 获取当前登录的用户
        identityService.setAuthenticatedUserId(this.getUser().getUsername());

        // 提交表单字段并启动一个新的流程实例
        ProcessInstance processInstance = formService.submitStartFormData(pdid, formValues);
        logger.debug("start a processinstance: {}", processInstance);
        return R.ok();
    }
    
}
