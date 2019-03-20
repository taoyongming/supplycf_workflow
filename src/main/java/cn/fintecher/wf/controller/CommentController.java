package cn.fintecher.wf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cn.fintecher.wf.admin.AbstractController;
import cn.fintecher.wf.utils.R;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Event;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论管理
 */
@RestController
@RequestMapping("comment")
public class CommentController extends AbstractController{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CommentController.class);
	
    @Autowired
    protected IdentityService identityService;
    @Autowired
    protected TaskService taskService;
    @Autowired
    HistoryService historyService;
    @Autowired
    ObjectMapper objectMapper;

    /**
     * 保存意见
     */
    @RequestMapping(value = "save", method = RequestMethod.POST)
    public R addComment(@RequestBody Map<String, String> params) {
        identityService.setAuthenticatedUserId(this.getUser().getUsername());
        String taskId = params.get("taskId");
        String processInstanceId = params.get("processInstanceId");
        String message = params.get("message");
        taskService.addComment(taskId, processInstanceId, message);
        return R.ok();
    }
    
    /**
     * 读取意见
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     */
    @RequestMapping(value = "list")
  //@RequiresPermissions("comment:list")
    public R list(@RequestBody Map<String, String> params) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException{

        String taskId = params.get("taskId");
        String processInstanceId = params.get("processInstanceId");
        Map<String, Object> result = new HashMap<String, Object>();

        Map<String, Object> commentAndEventsMap = new HashMap<String, Object>();

    /*
     * 根据不同情况使用不同方式查询
     */
        if (StringUtils.isNotBlank(processInstanceId)) {
            List<Comment> processInstanceComments = taskService.getProcessInstanceComments(processInstanceId);
            for (Comment comment : processInstanceComments) {
                String commentId = (String) PropertyUtils.getProperty(comment, "id");
                commentAndEventsMap.put(commentId, comment);
            }

            // 提取任务任务名称
            List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).list();
            Map<String, String> taskNames = new HashMap<String, String>();
            for (HistoricTaskInstance historicTaskInstance : list) {
                taskNames.put(historicTaskInstance.getId(), historicTaskInstance.getName());
            }
            result.put("taskNames", taskNames);

        }

    /*
     * 查询所有类型的事件
     */
        if (StringUtils.isNotBlank(taskId)) { // 根据任务ID查询
            List<Event> taskEvents = taskService.getTaskEvents(taskId);
            for (Event event : taskEvents) {
                String eventId = (String) PropertyUtils.getProperty(event, "id");
                commentAndEventsMap.put(eventId, event);
            }
        }

        result.put("events", commentAndEventsMap.values());

        return R.ok(result);
    }
 
    
}
