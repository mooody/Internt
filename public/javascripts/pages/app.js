var HomeApp = angular.module("HomeApp", ['ngRoute', 'ngResource', 'ngSanitize','ui.tinymce', 'HomeServices','HomeControllers','HomeDirectives']);	
			
//<editor-fold default-state="collapsed" desc=" config">
HomeApp.config(['$routeProvider',
	function($routeProvider) {
		$routeProvider	
		.when('/', {
			templateUrl:'public/pages/frontpage.html',
			controller:function($scope,HomePageService){
				console.log("frontpage");
				console.log($scope.breadcrumbs);
				$scope.breadcrumbs.length = 0;
				$scope.home = HomePageService.query();
			}
		})
		.when('/admin', {
			templateUrl: 'public/pages/admin.html',
			controller:'AdminCtrl'
		})
                .when('/admin/:id', {
			templateUrl: 'public/pages/admin.html',
			controller:'AdminCtrl'
		})
		.when('/article/:id',{
			templateUrl:'public/pages/article.html',
			controller:'ArticleCtrl'
		})
		.otherwise({
			redirectTo: '/'
		});
	}]);
