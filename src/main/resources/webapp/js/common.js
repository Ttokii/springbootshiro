angular.module("apiApp",[])
.controller("apiController",function($scope){
	var params = parseURL(window.location + "");
    $scope.webPageName = decodeURIComponent(params.title);
    $scope.templete = null;
    $.getScript("http://localhost:8089/api_code/"+params.file+".js", function(){
    	$scope.$applyAsync(function (){
    		$scope.templete = templete;
        });
    });
    $scope.startQuery = function(){
    	response($scope.templete);
    }
    $scope.responseBody = null;
    function response(templete){
    	var params = templete.params;
		var ajaxData = {};
//		if(templete.method == "GET"){
//			for(var i=0;i<params.length;i++){
//				ajaxData += params[i].name + "=" + params[i].val + "&";
//			}
//			if(ajaxData != null){
//				ajaxData = ajaxData.substring(0,ajaxData.length-1);
//			}
//		}else{
			for(var i=0;i<params.length;i++){
				ajaxData[params[i].name] =  params[i].val;
			}
//		}
		
		console.debug(ajaxData);
		$.ajax({
			url : templete.url,
			type : templete.method,
			data : ajaxData,
			dataType:"json",
			success : function(msg){
				$scope.$applyAsync(function (){
		    		$scope.responseBody = angular.toJson(msg,true);
		    		extractImg($scope.responseBody)
		        });
			},
			error : function(responseStr){
			    alert("请求异常!");
			}
		});
    }
    //抽取图片
    $scope.imageList = null;
    function extractImg(msg){
    	var imageList = [];
    	var reg = /http:\S*(jpg|gif|JPG|GIF)/g;
    	var tmpImgs = reg.exec(msg);
    	while(tmpImgs != null && tmpImgs.length > 0){
    		imageList.push(tmpImgs[0]);
    		reg.index = tmpImgs.index;
    		tmpImgs = reg.exec(msg);
    	}
    	$scope.imageList = imageList;
    }
});