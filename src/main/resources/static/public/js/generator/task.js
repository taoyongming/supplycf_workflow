$(function () {
    $("#jqGrid").jqGrid({
        url: '../task/list',
        datatype: "json",
        colModel: [	
            { label: '编号', name: 'id', index: 'id', width: 100 },
			{ label: '名称', name: 'name', index: 'name', width: 100 },
			{ label: '流程实例ID', name: 'processInstanceId', index: 'processInstanceId', width: 100 ,
				formatter: function(cellvalue, options, rowObject){
					return "<a onclick=\"view("+cellvalue+")\">"+cellvalue+"</a>";
				}
			},
			{ label: '流程定义ID', name: 'processDefinitionId', index: 'processDefinitionId', width: 100 },
			{ label: '创建时间', name: 'createTime', index: 'createTime', width: 130,
				formatter: function(cellvalue, options, rowObject){
					return timeStamp2String(cellvalue);
				}	
			},
			{ label: '办理人', name: 'assignee', index: 'assignee', width: 100 },
			{ label: '操作', name: 'assignee', index: 'assignee', width: 100,
				formatter: function(cellvalue, options, rowObject){
					var returnStr;
					if(cellvalue == null){
						returnStr = "<a class=\"btn  btn-primary\" onclick=\"claim("+rowObject.id+")\">签收</a>";
					}else{
						returnStr = "<a class=\"btn  btn-primary\" onclick=\"getform("+rowObject.id+")\">办理</a>";
					}
					return returnStr;
		
				}
			}
        ],
		viewrecords: true,
        height: 385,
        rowNum: 10,
		rowList : [10,30,50],
        rownumbers: true, 
        rownumWidth: 25, 
        autowidth:true,
        //multiselect: true,
        pager: "#jqGridPager",
        jsonReader : {
            root: "page.list",
            page: "page.currPage",
            total: "page.totalPage",
            records: "page.totalCount"
        },
        prmNames : {
            page:"page", 
            rows:"limit", 
            order: "order"
        },
        gridComplete:function(){
        	//隐藏grid底部滚动条
        	$("#jqGrid").closest(".ui-jqgrid-bdiv").css({ "overflow-x" : "hidden" }); 
        	//$("#jqGrid").closest(".ui-jqgrid-bdiv").css({ "overflow-y" : "hidden" }); 
        }
    });
    
    



    
});



function claim(id){
	$.get("../task/claim/"+id, function(r){
		alert('操作成功', function(index){
			$("#jqGrid").trigger("reloadGrid");
		});
  });
}

function getform(id){
	$.get("../task/getform/"+id, function(r){
		vm.showProcess = false;
		vm.showList = false;
		vm.showTask = true;
		vm.title = "任务办理";
		vm.taskFormData = r.taskFormData;
		vm.task = r.taskFormData.task;
		readComments(r.taskFormData.task.processInstanceId, r.taskFormData.task.id);
		vm.hasFormKey = false;
		Vue.nextTick(function () { 
			$("#taskFormKey").remove();
			if(r.hasFormKey){
				vm.hasFormKey = true;
				$('.form-horizontal').prepend(r.taskFormDataStr);
			}
		});
  });
}

function view(id){
	$.get("../process/history/view/"+id, function(r){
		vm.showProcess = true;
		vm.showList = false;
		vm.showTask = false;
		vm.processDefinition = r.processDefinition;
		vm.formProperties = r.formProperties;
		vm.historicProcessInstance = r.historicProcessInstance;
		vm.activities = r.activities;
		vm.variableInstances = r.variableInstances;
		vm.processDiagramUrl = "../process/running/trace?executionId="+id;
  });
}

// 保存意见
function saveComment(){
	if(!$('#comment').val()) {
		return false;
	}
	$.ajax({
		type: "POST",
	    url: "../comment/save",
	    data: JSON.stringify({taskId: vm.task.id,processInstanceId: vm.task.processInstanceId,message: $('#comment').val()}),
	    success: function(r){
	    	if(r.code === 0){
	    		readComments();
			}else{
				alert(r.msg);
			}
		}
	});
}

/**
 * 读取事件列表
 * @return {[type]} [description]
 */
function readComments(processInstanceId, taskId) {
	$('#commentList ol').html('');
	var processInstanceId = vm.task.processInstanceId;
	var taskId = vm.task.id;
	var url = "../comment/list";
	$.ajax({
		type: "POST",
	    url: url,
	    data: JSON.stringify({taskId: vm.task.id,processInstanceId: vm.task.processInstanceId}),
	    success: function(datas){
	    	if(datas.code === 0){
	    		$.each(datas.events, function(i, v) {
	    			$('<li/>', {
	    				html: function() {
	    					var user = (v.userId || '');
	    					if(user) {
	    						user = "<span style='margin-right: 1em;'><b>" + user + "</b></span>"
	    					}
	    					var msg = v.message || v.fullMessage;
	    					var content = eventHandler[v.action](v, user, msg);
	    					var taskName = datas.taskNames ? datas.taskNames[v.taskId] : '';
	    					content += "<span style='margin-left:1em;'></span>";

	    					// 名称不为空时才显示
	    					if(taskName) {
	    						content += "（<span class='text-info'>" + taskName + "</span>）";
	    					}

	    					content += "<span class='text-muted'>" + new Date(v.time).toLocaleString() + "</span>";
	    					return content;
	    				}
	    			}).appendTo('#commentList ol');
	    		});
			}else{
				alert(datas.msg);
			}
		}
	});
}

/*
事件处理器
 */
var eventHandler = {
	'DeleteAttachment': function(event, user, msg) {
		return user + '<span class="text-error">删除</span>了附件：' + msg;
	},
	'AddAttachment': function(event, user, msg) {
		return user + '添加了附件：' + msg;
	},
	'AddComment': function(event, user, msg) {
		return user + '发表了意见：' + msg;
	},
	'DeleteComment': function(event, user, msg) {
		return user + '<span class="text-error">删除</span>了意见：' + msg;
	},
	AddUserLink: function(event, user, msg) {
		return user + '邀请了<span class="text-info">' + event.messageParts[0] + '</span>作为任务的[<span class="text-info">' + translateType(event) + '</span>]';
	},
	DeleteUserLink: function(event, user, msg) {
        return user + '<span class="text-error">取消了</span><span class="text-info">' + event.messageParts[0] + '</span>的[<span class="text-info">' + translateType(event) + '</span>]角色';
	},
	AddGroupLink: function(event, user, msg) {
		return user + '添加了[<span class="text-info">' + translateType(event) + ']</span>' + event.messageParts[0];
	},
	DeleteGroupLink: function(event, user, msg) {
		return user + '从[<span class="text-info">' + translateType(event) + '</span>]中<span class="text-error">移除了</span><span class="text-info">' + event.messageParts[0] + '</span>';
	}
}


// 删除附件
function deleteAttachment(id,taskid){
	$.get('../attachment/delete/' + id, function(r) {
    	if(r.code === 0){
    		getform(taskid);
		}else{
			alert(r.msg);
		}
	});
}

var vm = new Vue({
	el:'#rrapp',
	data:{
		showList: true,
		isReadOnly:true,
		showProcess: false,
		showTask: false,
		title: null,
		q:{
			number:null
		},
		task: {},
		taskFormData: {},
		hisotryprocess: {},
		processDefinition: {},
		formProperties:[],
		historicProcessInstance: {},
		activities:[],
		variableInstances:[],
		processDiagramUrl:null,
		hasFormKey: null
	},
	filters: {  
	    time: function (value) {
	      return timeStamp2String(value);  
	    },
	    date: function (value) {
		      return timeStamp2DateString(value);  
		}
	},
	methods: {
		query: function () {
			vm.reload();
		},

		reload: function (event) {
			vm.showList = true;
			vm.showProcess = false;
			vm.showTask = false;
			vm.isReadOnly = true;
			var page = $("#jqGrid").jqGrid('getGridParam','page');
			$("#jqGrid").jqGrid('setGridParam',{ 
				postData:{'number': vm.q.number},
                page:page
            }).trigger("reloadGrid");
		},
		complete: function(){
			var taskform = $('#task').serializeObject();
			var url = "../task/complete/"+taskform.id;
			$.ajax({
				type: "POST",
			    url: url,
			    data: JSON.stringify(taskform),
			    success: function(r){
			    	if(r.code === 0){
						alert('操作成功', function(index){
							vm.showList = true;
							vm.showProcess = false;
							vm.showTask = false;
							$("#jqGrid").trigger("reloadGrid");
						});
					}else{
						alert(r.msg);
					}
				}
			});
		},
		
		upload: function(){
		    new AjaxUpload('#upload', {
		        action: '../attachment/upload',
		        name: 'file',
		        autoSubmit:true,
		        data: {'taskId': vm.task.id,'processInstanceId': vm.task.processInstanceId},
		        responseType:"json",
		        onComplete : function(file, r){
		            if(r.code == 0){
						alert('操作成功', function(index){
							getform(vm.task.id);
						});
		            }else{
		                alert(r.msg);
		            }
		        }
		    });
		}
		
	}

});