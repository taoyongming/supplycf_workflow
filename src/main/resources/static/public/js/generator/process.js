$(function () {
    $("#jqGrid").jqGrid({
        url: '../process/list',
        datatype: "json",
        colModel: [	
            { label: '编号', name: 'id', index: 'id', width: 150 },
			{ label: '名称', name: 'name', index: 'name', width: 200 },
			{ label: '版本', name: 'version', index: 'version', width: 50 },
			{ label: 'Key', name: 'key', index: 'key', width: 100 },
			{ label: '部署编号', name: 'deploymentId', index: 'deploymentId', width: 100 },
			{ label: '流程文件', name: 'resource', index: 'resource', width: 100 },
			{ label: '流程图片', name: 'image', index: 'image', width: 100 },
			{ label: '部署时间', name: 'deploymentTime', index: 'deploymentTime', width: 150,
				formatter: function(cellvalue, options, rowObject){
					return timeStamp2String(cellvalue);
		
				}
			},
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
    
    new AjaxUpload('#upload', {
        action: '../process/deploy',
        name: 'file',
        autoSubmit:true,
        responseType:"json",
        onSubmit:function(file, extension){
            if (!(extension && /^(xml|bpmn|zip|bar)$/.test(extension.toLowerCase()))){
                alert('只支持xml、bpmn、zip、bar格式的图片！');
                return false;
            }
        },
        onComplete : function(file, r){
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

var vm = new Vue({
	el:'#rrapp',
	data:{
		showList: true,
		isReadOnly:true,
		title: null,
		q:{
			number:null
		},
		process: {},
		startFormData : {},
		startform : {},
		currentPDID:null,
		hasFormKey : null
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
			var deploymentIds = new Array();
			for (var i = 0; i < ids.length; i++)  {
				var rowData = $("#jqGrid").jqGrid('getRowData',ids[i]);
				deploymentIds.push(rowData.deploymentId);
			};
			
			confirm('确定要删除选中的记录？', function(){
				$.ajax({
					type: "POST",
				    url: "../process/delete",
				    data: JSON.stringify(deploymentIds),
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
			
			var url = "../process/deploy/"+id;
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
			
			var url = "../process/export/"+id;
			
			window.location.href=url;
		},
		getInfo: function(id){
			$.get("../process/info/"+id, function(r){
                vm.process = r.process;
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
		viewXML: function (event) {
			var id = getSelectedRow();
			if(id == null){
				return ;
			}
	        window.open("../process/resource/read?processDefinitionId="+id+"&resourceType=xml",'_blank');
		},
		viewPic: function (event) {
			var id = getSelectedRow();
			if(id == null){
				return ;
			}
			window.open("../process/resource/read?processDefinitionId="+id+"&resourceType=image",'_blank');
		},
		start: function(){
			var id = getSelectedRow();
			if(id == null){
				return ;
			}
			
			var url = "../process/start/"+id;
			$.ajax({
				type: "POST",
			    url: url,
			    //data: JSON.stringify(vm.model),
			    success: function(r){
			    	if(r.code === 0){
						if(r.hasForm){
							vm.showList = false;
							vm.title = "启动";
							vm.startFormData = r.startFormData;
							vm.hasFormKey = r.hasFormKey;
							vm.currentPDID = r.processDefinition.id;
	
							Vue.nextTick(function () { 								
								if(r.hasFormKey){
									$('.form-horizontal').prepend(r.startFormData);
								}
								$('.datepicker').datepicker();
							});
							
						}else{
							alert('操作成功', function(index){
								$("#jqGrid").trigger("reloadGrid");
							});
			    		}
					}else{
						alert(r.msg);
					}
				}
			});
		},
		convertToModel: function(){
			var id = getSelectedRow();
			if(id == null){
				return ;
			}
			
			var url = "../process/convert-to-model/"+id;
			$.ajax({
				type: "POST",
			    url: url,
			    //data: JSON.stringify(vm.model),
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
		startWithForm: function(){
			var startform = $('#process').serializeObject();
			var url = "../process/startWithForm/"+startform.pdid;
			$.ajax({
				type: "POST",
			    url: url,
			    data: JSON.stringify(startform),
			    success: function(r){
			    	if(r.code === 0){
						alert('操作成功', function(index){
							vm.showList = true;
							$("#jqGrid").trigger("reloadGrid");
						});
					}else{
						alert(r.msg);
					}
				}
			});
		}
	}
	

});