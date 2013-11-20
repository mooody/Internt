/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.pages;

import com.google.gson.Gson;
import controllers.PlanController;
import java.util.ArrayList;
import java.util.List;
import models.SuperAdmin;
import models.pages.Article;
import models.pages.HomePage;

/**
 *
 * @author mikael
 */
public class Pages extends PlanController{
    
	public static void index(){
		render("/pages/index.html");
	}
        
        public static List<Article> getGlobalMenuItems()
        {
             List<Article> globalArticles = Article.find("byGlobalAndMenuitem", true,true).fetch();
             return globalArticles;
        }
	
        public static void showArticle(long id)
        {
            
            Article article = Article.findById(id);
            article.company = null;
            
            render("/pages/article.html",article);
        }
	public static void getHomePageAsJson(){
		HomePage home = HomePage.find("byCompany", user().company).first();
		if(home != null)
		{
			Article article = home.frontpage;
                        article.company = null;
			renderJSON(article);
		}
		ok();
	}
	public static void getMenuItemsAsJson(){
		
		List<Article> articles = Article.find("byCompanyAndMenuitem", user().company, true)
                        .fetch();
                 for(Article article : articles)
                {
                    article.company = null;
                }
		renderJSON(articles);
	}

	public static void getArticleAsJson(long id){
		Article article = Article.findById(id);
                article.company = null;
		renderJSON(article);
	}
	
	public static void getArticlesAsJson(){
		
		List<Article> articles = Article.find("byCompany", user().company)
                        .fetch();	
                for(Article article : articles)
                {
                    article.company = null;
                }
		renderJSON(articles);
	}
	public static void removeArticle(long id){
		
		Article article = Article.findById(id);
		
                if(article.company != user().company){
                    forbidden();
                }
                
		HomePage home = HomePage.find("byCompany", user().company).first();
		if(home!=null)
		{
			if(home.frontpage.id == id)
			{
				home.delete();
			}
		}
		article.delete();
		
		article.company = null;
		renderJSON(article);
	}
	public static void addArticle(){
		
		Gson gson = new Gson();
		Adapter adapter = gson.fromJson(params.get("body"),Adapter.class);
		
		Article detached = adapter.article;

		Article article = null;
		
		if(detached.id != null && detached.id > 0) {
			article = Article.findById(detached.id);
			
			if(article == null){
				article = detached;
			}else{
				article.copy(detached);
			}
		}else{
			article = detached;
		}
			
		HomePage home = HomePage.all().first();
		
                article.company = user().company;

                if(user() instanceof SuperAdmin)
                {
                    if(adapter.global)
                    {
                        article.global = true;
                    }
                }
		if(adapter.frontpage)
		{
			if(home == null) home = new HomePage();
			home.frontpage = article;
			article.menuitem = false;
			article.save();
			home.save();
			
		}else{
		
			if(home != null && home.frontpage .equals(article)){
				home.frontpage = null;
				home.save();
			}
			article.save();
		}
		
		
		article.company = null;
		renderJSON(article);
	}
	//adapterclass för att hantera json
	private static class Adapter
	{
		Article article;
		boolean frontpage;
                boolean global;
	}
	
	public static void getCategorysAsJson(){
	
	}
	
	public static void addCategory(){
	
	}
	
	public static void getArticlesInCategory(){
	
	}
	
	public static void links(){
		List<Article> articles = Article.find("byCompany", user().company)
                        .fetch();
		
		List<tinyMceLinkAdapter> links = new ArrayList<tinyMceLinkAdapter>();
		tinyMceLinkAdapter temp = null;
		for(Article article: articles)
		{
                        article.company = null;
			temp = new tinyMceLinkAdapter(article.title, "#/article/"+article.id);
			links.add(temp);
		}
		
		renderJSON(links);
	}
	
	static class tinyMceLinkAdapter{
		String title;
		String value;
		
		public tinyMceLinkAdapter(String _title, String _value)
		{
			this.title = _title;
			this.value = _value;
		}
	}
}
