$(function () {
    $("#jqGrid").jqGrid({
        url: '../models/list',
        datatype: "json",
        colModel: [	
            { label: '编号', name: 'id', index: 'id', width: 200 },
			{ label: '名称', name: 'name', index: 'name', width: 200 },
			{ label: '版本', name: 'version', index: 'version', width: 200 },
			{ label: 'Key', name: 'key', index: 'key', width: 200 },
			{ label: '创建时间', name: 'createTime', index: 'createTime', width: 200,
				formatter: function(cellvalue, options, rowObject){
					return timeStamp2String(cellvalue);
		
				}
			},
			{ label: '最后更新时间', name: 'lastUpdateTime', index: 'lastUpdateTime', width: 200,
				formatter: function(cellvalue, options, rowObject){
					return timeStamp2String(cellvalue);
		
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
		model: {}
	},
	methods: {
		query: function () {
			vm.reload();
		},
		add: function(){
			vm.showList = false;
			vm.title = "新增";
			vm.model = {};
			

		},
		update: function (event) {
			var id = getSelectedRow();
			if(id == null){
				return ;
			}
            window.open("../modeler.html?modelId="+id,'_blank');
		},
		saveOrUpdate: function (event) {			
			var url = "../models/newModel";
			$.ajax({
				type: "POST",
			    url: url,
			    data: JSON.stringify(vm.model),
			    success: function(r){
			    	if(r.code === 0){
			    		vm.reload();
			    		window.open(r.modelUrl,'_blank');
					}else{
						alert(r.msg);
					}
				}
			});
		},
		del: function (event) {
			var ids = getSelectedRows();
			if(ids == null){
				return ;
			}
			
			confirm('确定要删除选中的记录？', function(){
				$.ajax({
					type: "POST",
				    url: "../models/delete",
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
		deploy: function(){
			var id = getSelectedRow();
			if(id == null){
				return ;
			}
			
			var url = "../models/deploy/"+id;
			$.ajax({
				type: "POST",
			    url: url,
			    data: JSON.stringify(vm.model),
			    success: function(r){
			    	if(r.code === 0){
						alert('操作成功', function(index){
							$("#jqGrid").trigger("reloadGrid");
						});
					}else{
						alert(r.msg);
					}
				}
			});
		},
		
		exportModel: function(){
			var id = getSelectedRow();
			if(id == null){
				return ;
			}
			
			var url = "../models/export/"+id;
			
			window.location.href=url;
/*			$.ajax({
				type: "POST",
			    url: url,
			    data: JSON.stringify(vm.model),
			    success: function(r){
			    	if(r.code === 0){
						alert('操作成功', function(index){
							$("#jqGrid").trigger("reloadGrid");
						});
					}else{
						alert(r.msg);
					}
				}
			});*/
		},
		getInfo: function(id){
			$.get("../model/info/"+id, function(r){
                vm.model = r.model;
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