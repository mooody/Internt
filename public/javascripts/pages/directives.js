var directives =  angular.module("HomeDirectives",[])

directives.directive('script', function() {
	return { 
		restrict: 'E',
		scope: false,
		link: function(scope, elem, attr) {
			if (attr.type=='text/javascript-lazy') {
				var code = elem.text();
				var f = new Function(code);
				f();
			}
		}
	}
})

directives.directive("breadcrumb", function(){
	return { 
		restrict: 'E',
		template:'<ol id="breadcrumb" class="breadcrumb">'
				+'<li>'
					+'<div ng-show="home.title"><a href="#">{{home.title}}</a></div>'
					+'<div ng-hide="home.title"><a href="/app">none</a></div>'
				+'</li>'
				+'<li ng-repeat="crumbs in breadcrumbs"><a href="#/article/{{crums.id}}">{{crumbs.title}}</a></li>'
				+'</ol>'/*,
		link:function(scope, element, attr){
			element.bind("click", function(){
				console.log("breadcrumb", element.href);
			});
		},
		controller:function($scope){
			$scope.$watch('article', function(){
				console.log("art");
			});
		}*/
	}
});