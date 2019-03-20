package cn.fintecher.wf.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cn.fintecher.wf.admin.AbstractController;
import cn.fintecher.wf.entity.ModelEntity;
import cn.fintecher.wf.utils.PageUtils;
import cn.fintecher.wf.utils.R;
import cn.fintecher.wf.utils.RRException;
import cn.fintecher.wf.utils.annotation.SysLog;
import cn.fintecher.wf.utils.validator.ValidatorUtils;
import cn.fintecher.wf.utils.validator.group.AddGroup;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 模型管理
 */
@RestController
@RequestMapping("models")
public class ModelController extends AbstractController{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ModelController.class);

    @Autowired
    RepositoryService repositoryService;
    @Autowired
    ObjectMapper objectMapper;

	/**
	 * 所有模板列表
	 */
	@RequestMapping("/list")
	//@RequiresPermissions("sys:user:list")
	public R list(@RequestParam Map<String, Object> params){
		
		//查询列表数据
		List<Model> list = null;
		
        int page = Integer.parseInt(params.get("page").toString());
        int rowSize = Integer.parseInt(params.get("limit").toString());
       
        String sidx = params.get("sidx")==null || "".equals(params.get("sidx").toString()) ? "createTime" : params.get("sidx").toString(); 
        String order = params.get("order")==null || "".equals(params.get("order").toString()) ? "asc" : params.get("order").toString();
        if("createTime".equals(sidx)){
        	if("asc".equals(order)){
        		list = repositoryService.createModelQuery().orderByCreateTime().asc().listPage(rowSize * (page - 1), rowSize);
        	}else{
        		list = repositoryService.createModelQuery().orderByCreateTime().desc().listPage(rowSize * (page - 1), rowSize);
        	}
        }else if("lastUpdateTime".equals(sidx)){
        	if("asc".equals(order)){
        		list = repositoryService.createModelQuery().orderByLastUpdateTime().asc().listPage(rowSize * (page - 1), rowSize);
        	}else{
        		list = repositoryService.createModelQuery().orderByLastUpdateTime().desc().listPage(rowSize * (page - 1), rowSize);
        	}
        }else if("key".equals(sidx)){
        	if("asc".equals(order)){
        		list = repositoryService.createModelQuery().orderByModelKey().asc().listPage(rowSize * (page - 1), rowSize);
        	}else{
        		list = repositoryService.createModelQuery().orderByModelKey().desc().listPage(rowSize * (page - 1), rowSize);
        	}
        }else if("name".equals(sidx)){
        	if("asc".equals(order)){
        		list = repositoryService.createModelQuery().orderByModelName().asc().listPage(rowSize * (page - 1), rowSize);
        	}else{
        		list = repositoryService.createModelQuery().orderByModelName().desc().listPage(rowSize * (page - 1), rowSize);
        	}
        }else if("id".equals(sidx)){
        	if("asc".equals(order)){
        		list = repositoryService.createModelQuery().orderByModelId().asc().listPage(rowSize * (page - 1), rowSize);
        	}else{
        		list = repositoryService.createModelQuery().orderByModelId().desc().listPage(rowSize * (page - 1), rowSize);
        	}
        }else if("version".equals(sidx)){
        	if("asc".equals(order)){
        		list = repositoryService.createModelQuery().orderByModelVersion().asc().listPage(rowSize * (page - 1), rowSize);
        	}else{
        		list = repositoryService.createModelQuery().orderByModelVersion().desc().listPage(rowSize * (page - 1), rowSize);
        	}
        }else{
        	list = repositoryService.createModelQuery().orderByCreateTime().desc().listPage(rowSize * (page - 1), rowSize);
        }
        
        long count = repositoryService.createModelQuery().count();
		
		PageUtils pageUtil = new PageUtils(list, (int)count, rowSize, page);
		
		return R.ok().put("page", pageUtil);
	}
	
    /**
     * 新建一个空模型
     * @return
     * @throws UnsupportedEncodingException
     */
    @PostMapping("newModel")
    public R newModel(@RequestBody ModelEntity newModel,HttpServletRequest request) throws UnsupportedEncodingException {
    	ValidatorUtils.validateEntity(newModel, AddGroup.class);
    	
        //初始化一个空模型
        Model model = repositoryService.newModel();

        //设置一些默认信息
        String name = newModel.getName();
        String description = newModel.getDescription();
        int revision = 1;
        String key = newModel.getKey();

        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put(ModelDataJsonConstants.MODEL_NAME, name);
        modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
        modelNode.put(ModelDataJsonConstants.MODEL_REVISION, revision);

        model.setName(name);
        model.setKey(key);
        model.setMetaInfo(modelNode.toString());

        repositoryService.saveModel(model);
        String id = model.getId();

        //完善ModelEditorSource
        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.put("stencilset", stencilSetNode);
        repositoryService.addModelEditorSource(id,editorNode.toString().getBytes("utf-8"));
        
        return R.ok().put("modelUrl",request.getContextPath()+"/modeler.html?modelId="+id);
    }
    
	/**
	 * 删除模型
	 */
	@SysLog("删除模型")
	@RequestMapping("/delete")
	//@RequiresPermissions("model:delete")
	public R delete(@RequestBody String[] modelIds){
		for(String modelId : modelIds){
			repositoryService.deleteModel(modelId);
		}
		
		return R.ok();
	}
	
    /**
     * 根据Model部署流程
     */
    @RequestMapping(value = "deploy/{modelId}")
    public R deploy(@PathVariable("modelId") String modelId, RedirectAttributes redirectAttributes) {
        try {
            Model modelData = repositoryService.getModel(modelId);
            ObjectNode modelNode = (ObjectNode) new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
            byte[] bpmnBytes = null;

            BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            bpmnBytes = new BpmnXMLConverter().convertToXML(model);

            String processName = modelData.getName() + ".bpmn20.xml";
            repositoryService.createDeployment().name(modelData.getName()).addString(processName, new String(bpmnBytes,"UTF-8")).deploy();

        } catch (Exception e) {
        	LOGGER.error("根据模型部署流程失败：modelId={}", modelId, e);
        	throw new RRException("流程部署失败");
        }
        return R.ok();
    }
    
    /**
     * 导出model对象为指定类型
     *
     * @param modelId 模型ID
     * @param type    导出文件类型(bpmn)
     */
    @RequestMapping(value = "export/{modelId}")
    public void export(@PathVariable("modelId") String modelId, HttpServletResponse response) {
        try {
            Model modelData = repositoryService.getModel(modelId);
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            byte[] modelEditorSource = repositoryService.getModelEditorSource(modelData.getId());

            JsonNode editorNode = new ObjectMapper().readTree(modelEditorSource);
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);

            // 处理异常
            if (bpmnModel.getMainProcess() == null) {
                response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
                response.getOutputStream().println("no main process, can't export");
                response.flushBuffer();
                return;
            }

            String filename = "";
            byte[] exportBytes = null;

            String mainProcessId = bpmnModel.getMainProcess().getId();


            BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
            exportBytes = xmlConverter.convertToXML(bpmnModel);

            filename = mainProcessId + ".bpmn20.xml";

            ByteArrayInputStream in = new ByteArrayInputStream(exportBytes);
            IOUtils.copy(in, response.getOutputStream());

            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            response.flushBuffer();
        } catch (Exception e) {
        	LOGGER.error("导出model的xml文件失败：modelId={}", modelId, e);
            throw new RRException("导出模型失败");
        }
    }

}
