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
import play.mvc.Before;

/**
 *
 * @author mikael
 */
public class Pages extends PlanController{
    
        @Before(unless="showArticle")
        public static void checkAuthentification() 
        {
            if(!(user() instanceof models.Admin))
            {
                forbidden();
            }
        }
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
	public static void getHomePageAsJson()
        {
            HomePage home = HomePage.find("byCompany", user().company).first();
            if(home != null)
            {
                    Article article = home.frontpage;
                    article.company = null;
                    renderJSON(article);
            }
            ok();
	}
        
	public static void getMenuItemsAsJson()
        {
		
		List<Article> articles = Article.find("byCompanyAndMenuitem", user().company, true)
                        .fetch();
                 for(Article article : articles)
                {
                    article.company = null;
                }
		renderJSON(articles);
	}

	public static void getArticleAsJson(long id)
        {
		Article article = Article.findById(id);
                article.company = null;
		renderJSON(article);
	}
	
	public static void getArticlesAsJson()
        {		
		List<Article> articles = Article.find("byCompany", user().company)
                        .fetch();	
                for(Article article : articles)
                {
                    article.company = null;
                }
		renderJSON(articles);
	}
        
	public static void removeArticle(long id)
        {		
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
        
	public static void addArticle()
        {		
            //Hämtar in data från tinyMCE. Genom Angular blir det lite knas så 
            //sparar ett specielobject som adapter
            Gson gson = new Gson();
            Adapter adapter = gson.fromJson(params.get("body"),Adapter.class);
	
            //Strulade lite med hibernate när vi skulle förändra
            //objektet som skapades. Det fanns 2, en i databasen och en här lokalt
            //med samma id, så detta blir en liten workaround
            Article detached = adapter.article;

            Article article = null;
		
            //Kollar om det är en redan sparad artikel
            if(detached.id != null && detached.id > 0) {
                //Är det inte en ny artikel hämta den gamla
                article = Article.findById(detached.id);
                //om den inte hittades sätt över objectet som hämtades in
                if(article == null)
                {
                        article = detached;
                }
                else // Annars kopierar vi över allt
                {
                        article.copy(detached);
                }
            }
            else //annars sätter vi artikeln till det hämtade objectet
            {
                    article = detached;
            }
			
            //Hämta in startsidan
            HomePage home = HomePage.find("byCompany", user().company).first();

            article.company = user().company;

            //Det är endast superadministratörer som får skapa globala artiklar
            if(user() instanceof SuperAdmin)
            {
                if(adapter.global)
                {
                    article.global = true;
                }
            }
            
            //Kolla om sidan är startsidan
            if(adapter.frontpage)
            {
                //om det inte finns skapa den
                if(home == null) home = new HomePage();
                
                home.frontpage = article;
                article.menuitem = false;
                article.save();
                home.save();
            }
            else
            {		
                //om det inte är en startsida och artikeln var det innan. Nulla!
                if(home != null && home.frontpage!=null && home.frontpage.equals(article))
                {
                    home.frontpage = null;
                    home.save();
                }
                article.save();
            }
		
            //Nulla företaget för att kunna skicka ut med JSON
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
	
	public static void links()
        {
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
	
	static class tinyMceLinkAdapter
        {
            String title;
            String value;

            public tinyMceLinkAdapter(String _title, String _value)
            {
                    this.title = _title;
                    this.value = _value;
            }
	}
}
