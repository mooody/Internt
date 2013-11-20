var services = angular.module('HomeServices',[]);

services.factory("ArticleService", function($resource){
				return $resource('/home/article/:id',{id:'@id'},{})			
			});
//hämtar ut artiklar satt med menuitem
services.service("MenuService", function($resource){

	return $resource('/home/menu',{},{
			query:{method:'GET',isArray:true}
		})
});
//hämtar ut artikeln satt med frontpage
services.service("HomePageService", function($resource){

	return $resource('/homepage',{},{
			query:{method:'GET',isArray:false}
		})
})

services.service("CategoryService", function($resource){

	return $resource('/home/categorys',{},{
			query:{method:'GET',isArray:false}
		})
})

