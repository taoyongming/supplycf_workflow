$(function () {
    $("#jqGrid").jqGrid({
        url: '../process/running/list',
        datatype: "json",
        colModel: [	
            { label: '实例ID', name: 'id', index: 'id', width: 100 },
			{ label: '名称', name: 'name', index: 'name', width: 100 },
			{ label: '流程ID', name: 'definitionId', index: 'definitionId', width: 100 },
			{ label: '部署ID', name: 'deploymentId', index: 'deploymentId', width: 100 },
			{ label: '当前活动节点ID', name: 'activityId', index: 'activityId', width: 100 },
			{ label: '当前活动节点名称', name: 'nodeName', index: 'nodeName', width: 100 },
			{ label: '是否挂起', name: 'suspended', index: 'suspended', width: 70}
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

var vm = new Vue({
	el:'#rrapp',
	data:{
		showList: true,
		isReadOnly:true,
		title: null,
		q:{
			number:null
		},
		runningprocess: {}
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
				    url: "../process/running/delete",
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
		},
		view: function (event) {
			var id = getSelectedRow();
			if(id == null){
				return ;
			}
			window.open("../process/running/trace?executionId="+id,'_blank');
		},
		view_: function (event) {
			var id = getSelectedRow();
			var processDefinitionId = $("#jqGrid").jqGrid('getCell',id,'definitionId');
			if(id == null || processDefinitionId ==null){
				return ;
			}
			window.open("../diagram-viewer/index.html?processDefinitionId="+processDefinitionId+"&processInstanceId="+id+"",'_blank');
		}
	}

});