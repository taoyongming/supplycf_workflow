$(function () {
    $("#jqGrid").jqGrid({
        url: '../sysjmsnoticemessage/list',
        datatype: "json",
        colModel: [			
			{ label: 'id', name: 'id', index: 'id', width: 50, key: true },
			{ label: '', name: 'code', index: 'code', width: 80 }, 			
			{ label: '', name: 'queue', index: 'queue', width: 80 }, 			
			{ label: '', name: 'businessKey', index: 'business_key', width: 80 }, 			
			{ label: '', name: 'processInstanceId', index: 'process_instance_id', width: 80 }, 			
			{ label: '', name: 'sendTime', index: 'send_time', width: 80 }, 			
			{ label: '', name: 'confirmTime', index: 'confirm_time', width: 80 }, 			
			{ label: '', name: 'isConfirmed', index: 'is_confirmed', width: 80 }, 			
			{ label: '', name: 'status', index: 'status', width: 80 }, 			
			{ label: '', name: 'data', index: 'data', width: 80 }			
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
        }
    });
});

var vm = new Vue({
	el:'#rrapp',
	data:{
		showList: true,
		title: null,
		sysJmsNoticeMessage: {}
	},
	methods: {
		query: function () {
			vm.reload();
		},
		add: function(){
			vm.showList = false;
			vm.title = "新增";
			vm.sysJmsNoticeMessage = {};
		},
		update: function (event) {
			var id = getSelectedRow();
			if(id == null){
				return ;
			}
			vm.showList = false;
            vm.title = "修改";
            
            vm.getInfo(id)
		},
		saveOrUpdate: function (event) {
			var url = vm.sysJmsNoticeMessage.id == null ? "../sysjmsnoticemessage/save" : "../sysjmsnoticemessage/update";
			$.ajax({
				type: "POST",
			    url: url,
			    data: JSON.stringify(vm.sysJmsNoticeMessage),
			    success: function(r){
			    	if(r.code === 0){
						alert('操作成功', function(index){
							vm.reload();
						});
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
				    url: "../sysjmsnoticemessage/delete",
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
		getInfo: function(id){
			$.get("../sysjmsnoticemessage/info/"+id, function(r){
                vm.sysJmsNoticeMessage = r.sysJmsNoticeMessage;
            });
		},
		reload: function (event) {
			vm.showList = true;
			var page = $("#jqGrid").jqGrid('getGridParam','page');
			$("#jqGrid").jqGrid('setGridParam',{ 
                page:page
            }).trigger("reloadGrid");
		}
	}
});