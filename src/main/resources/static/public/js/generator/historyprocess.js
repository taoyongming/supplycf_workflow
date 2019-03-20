$(function () {
    $("#jqGrid").jqGrid({
        url: '../process/history/list',
        datatype: "json",
        colModel: [	
            { label: '实例ID', name: 'id', index: 'id', width: 100 ,
				formatter: function(cellvalue, options, rowObject){
					return "<a onclick=\"view("+cellvalue+")\">"+cellvalue+"</a>";
				}
			},
			{ label: '所属流程', name: 'processDefinitionName', index: 'processDefinitionName', width: 120 },
			{ label: '流程ID', name: 'processDefinitionId', index: 'processDefinitionId', width: 100 },
			{ label: '启动时间', name: 'startTime', index: 'startTime', width: 130 ,
				formatter: function(cellvalue, options, rowObject){
					return timeStamp2String(cellvalue);
				}
			},
			{ label: '启动人', name: 'startUserId', index: 'startUserId', width: 50 },
			{ label: '结束时间', name: 'endTime', index: 'endTime', width: 130 ,
				formatter: function(cellvalue, options, rowObject){
					return timeStamp2String(cellvalue);
				}
			},
			{ label: '父流程ID', name: 'superProcessInstanceId', index: 'superProcessInstanceId', width: 100},
			{ label: '结束原因', name: 'deleteReason', index: 'deleteReason', width: 100}
        ],
		viewrecords: true,
        height: 385,
        rowNum: 10,
		rowList : [10,30,50],
        rownumbers: true, 
        rownumWidth: 25, 
        autowidth:true,
        multiselect: true,
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

function view(id){
	$.get("../process/history/view/"+id, function(r){
		vm.showList = false;
		vm.processDefinition = r.processDefinition;
		vm.formProperties = r.formProperties;
		vm.historicProcessInstance = r.historicProcessInstance;
		vm.activities = r.activities;
		vm.variableInstances = r.variableInstances;
  });
}

var vm = new Vue({
	el:'#rrapp',
	data:{
		showList: true,
		isReadOnly:true,
		title: null,
		q:{
			number:null
		},
		hisotryprocess: {},
		processDefinition: {},
		formProperties:[],
		historicProcessInstance: {},
		activities:[],
		variableInstances:[]
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
		del: function (event) {
			var ids = getSelectedRows();
			if(ids == null){
				return ;
			}
			
			confirm('确定要删除选中的记录？', function(){
				$.ajax({
					type: "POST",
				    url: "../process/history/delete",
				    data: JSON.stringify(ids),
				    success: function(r){
						if(r.code == 0){
							alert('操作成功', function(index){
								$("#jqGrid").trigger("reloadGrid");
							});
						}else{
							alert(r.msg);
						}
					}
				});
			});
		},
		reload: function (event) {
			vm.showList = true;
			vm.isReadOnly = true;
			var page = $("#jqGrid").jqGrid('getGridParam','page');
			$("#jqGrid").jqGrid('setGridParam',{ 
				postData:{'number': vm.q.number},
                page:page
            }).trigger("reloadGrid");
		}
	}

});