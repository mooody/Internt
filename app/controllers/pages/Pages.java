/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.pages;

import com.google.gson.Gson;
import controllers.PlanController;
import controllers.admin.AdminController;
import java.util.ArrayList;
import java.util.List;
import models.SuperAdmin;
import models.pages.Article;
import models.pages.HomePage;
import play.Logger;
import play.cache.Cache;
import play.mvc.Before;

/**
 *
 * @author mikael
 */
public class Pages extends AdminController{
    
        
	public static void index(){
		render("/pages/index.html");
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
                
                if(!(user() instanceof SuperAdmin))
                {
                    if(!article.global && article.company.id != user().company.id){
                        forbidden("You dont have access to this article");
                    }
                }
                article.company = null;
		renderJSON(article);
	}
	
	public static void getArticlesAsJson()
        {		
		List<Article> articles = Article.find("byCompany", user().company)
                        .fetch();	
                //Om superadmin, lägg till de globala artiklarna
                if(user() instanceof SuperAdmin)
                {
                       List<Article> globals = Article.find("byGlobal", true).fetch();
                       articles.addAll(globals);
                }
                
                for(Article article : articles)
                {
                    article.company = null;
                }
		renderJSON(articles);
	}
        
	public static void removeArticle(long id)
        {		
		Article article = Article.findById(id);
		
                if(!(user() instanceof SuperAdmin))
                {
                    if(article.company != user().company){
                        forbidden();
                    }
                }
                
		HomePage home = HomePage.find("byCompany", user().company).first();
		if(home!=null)
		{
                    //Ligger något fel!                       
                    if(home.frontpage.id == id)
                    {
                        home.frontpage = null;
                        home.save();
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
            Logger.info("%s = %s", detached, adapter.frontpage);
            Article article = null;
		
            //Kollar om det är en redan sparad artikel
            if(detached.id != null && detached.id > 0) {
                //Är det inte en ny artikel hämta den gamla
                article = Article.findById(detached.id);
                //om den inte hittades sätt över objectet som hämtades in
                if(article == null)
                {
                    detached.id=0L;
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
			
            Logger.info("%s", article);
            //Hämta in startsidan
            HomePage home = HomePage.find("byCompany", user().company).first();

            article.company = user().company;
            
            Logger.info("%s", article);

            //Det är endast superadministratörer som får skapa globala artiklar
            if(user() instanceof SuperAdmin)
            {
                if(article.global)
                {
                    article.global = true;
                    article.company = null;
                    
                }
            }
            else
            {
                 article.global = false;
            }
            
            
            //Kolla om sidan är startsidan
            if(adapter.frontpage)
            {
                //om det inte finns skapa den
                if(home == null) home = new HomePage();
                
                home.frontpage = article;
                home.company = article.company;
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
                    home.delete();
                }
                article.save();
            }
		
            //Nulla företaget för att kunna skicka ut med JSON
            article.company = null;
            renderJSON(article);
	}
	//adapterclass för att hantera json
	private class Adapter
	{
            Article article;
            boolean frontpage;

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
