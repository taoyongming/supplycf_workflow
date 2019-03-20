package cn.fintecher.wf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cn.fintecher.wf.admin.AbstractController;
import cn.fintecher.wf.utils.PageUtils;
import cn.fintecher.wf.utils.R;
import cn.fintecher.wf.utils.RRException;
import org.activiti.engine.FormService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Attachment;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 附件管理
 */
@RestController
@RequestMapping("attachment")
public class AttachmentController extends AbstractController{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentController.class);

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
     * 添加附件
     * @param file
     * @return
     */
    @RequestMapping(value = "/upload",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public R deploy(@RequestParam("file") MultipartFile file, HttpServletRequest request, @RequestParam Map<String, String> params) {
        String fileName = file.getOriginalFilename();
        try {
            String attachmentType = file.getContentType() + ";" + FilenameUtils.getExtension(file.getOriginalFilename());
            identityService.setAuthenticatedUserId(this.getUser().getUsername());
            String attachmentDescription = null;
            String attachmentName = fileName;
            String taskId = params.get("taskId");
            String processInstanceId = params.get("processInstanceId");
            Attachment attachment = taskService.createAttachment(attachmentType, taskId, processInstanceId, attachmentName, attachmentDescription,
                    file.getInputStream());
            taskService.saveAttachment(attachment);
        } catch (IOException e) {
        	LOGGER.error("添加附件失败", e);
        	throw new RRException("添加附件失败");
        }
        return R.ok();
    }
    
    /**
     * 删除附件
     */
    @RequestMapping(value = "delete/{attachmentId}")
    public R delete(@PathVariable("attachmentId") String attachmentId) {
        taskService.deleteAttachment(attachmentId);
        return R.ok();
    }

    /**
     * 下载附件
     *
     * @throws IOException
     */
    @RequestMapping(value = "download/{attachmentId}")
    public void downloadFile(@PathVariable("attachmentId") String attachmentId, HttpServletResponse response) throws IOException {
        Attachment attachment = taskService.getAttachment(attachmentId);
        InputStream attachmentContent = taskService.getAttachmentContent(attachmentId);
        String contentType = StringUtils.substringBefore(attachment.getType(), ";");
        response.addHeader("Content-Type", contentType + ";charset=UTF-8");
        String extensionFileName = StringUtils.substringAfter(attachment.getType(), ";");
        String fileName = attachment.getName() + "." + extensionFileName;
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        IOUtils.copy(new BufferedInputStream(attachmentContent), response.getOutputStream());
    }
    
}
