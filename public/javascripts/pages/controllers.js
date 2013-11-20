var controllers = angular.module('HomeControllers',['HomeServices'])
/*
 tinymce.on('mceNewDocument', function(e) {
    alert("!");
});
 **/
controllers.controller("AdminCtrl", function($scope,ArticleService){
	//Lägger artiklar i AdminCtrl.$scope.artiklar
	$scope.fetch = function(){
		var promise = ArticleService.query();
		promise.$promise.then(function(data){
			$scope.articles = data;

			$scope.links = [];
			angular.forEach($scope.articles, function(a){
				$scope.links.push({title:a.title, value:a.id});
			});
		});
	}

	$scope.tinymceOptions = {
		handle_event_callback: function (e) {
				
		}

	};
	
	
	
	$scope.fetch();
})
//<editor-fold default-state="collapsed" desc=" menu">
controllers.controller("MenuCtrl", function($scope,MenuService,HomePageService){
	
	$scope.breadcrumbs = [];
	$scope.updateMenu = function(){
		//Hämtar ut menuposter, dvs de artiklar som är markerade med menuitem
		$scope.menuItems = MenuService.query();
		//hämtar ut den artikeln som är markerad med frontpage
		$scope.home = HomePageService.query();
	}

	//metod som anropas när man klickar i menyn, sätter active på menyvalet
	$scope.active = function(menuItem){

		$scope.updateBreadcrumbs();
		angular.forEach($scope.menuItems, function(item){
			item.active = false;
		});

		if(menuItem){
			menuItem.active = true;
			$scope.activeArticle = menuItem;
		}

	}

	$scope.updateMenu();

	$scope.updateBreadcrumbs = function(article){
		if(article && article.menuitem)
		{
			$scope.breadcrumbs.length = 0;
			$scope.breadcrumbs.push(article);
		}
		else if(article)
		{
			var found = false;
			jQuery.each($scope.breadcrumbs,function(index, value){
				if(value.id == article.id)
				{
					found = index;
				}
			});
			
			
			if(found===false)
			{
				$scope.breadcrumbs.push(article);
			}
			else
			{
				$scope.breadcrumbs=$scope.breadcrumbs.slice(0,found+1);
			}
		}	
	}
	
	$scope.$on('articleChange', function(event,article){
		
		$scope.updateBreadcrumbs(article);
	});
});

controllers.controller("ArticleCtrl", function($scope, $routeParams, ArticleService){

	//sparar en artikel, mottagaren har en adapter innehållande en artikel samt boolean med frontpage
	$scope.save = function(article, frontpage){					
		var Adapter = new ArticleService();
		Adapter.article = article;
		Adapter.frontpage = frontpage;
		console.log("ArticleCtrl Save");
		var promise = Adapter.$save();

		promise.then(function(data){

			$scope.article = {id:null, title:null, content:null, menuitem:null};
			tinymce.activeEditor.execCommand('mceSetContent', false, ' ');
			$scope.fetch();		
			$scope.updateMenu();
		});
	}

	//tar bort en artikel
	$scope.removeArticle = function(article){
		var Article = new ArticleService();
		if(confirm("Radera artikeln "+article.title+"?"))
		{
			var promise = Article.$remove({id:article.id});

			promise.then(function(data){
				$scope.fetch();
				$scope.updateMenu();
			});
		}
	}

	//hämtar en artikel
	$scope.getArticle = function(id){
		var Article = new ArticleService();
		var promise = Article.$get({id:id});

		console.log("waiting...");
		promise.then(function(data){
			console.log("Gotit");
			$scope.article = data;
			$scope.$emit('articleChange',data);
			if(tinymce && tinymce.activeEditor){
				tinymce.activeEditor.execCommand('mceSetContent', false, data.content);
			}
		})
	}
	
	if($routeParams.id){
		console.log('getting article '+$routeParams.id);
		$scope.getArticle($routeParams.id);
		
	}
	
	
});


