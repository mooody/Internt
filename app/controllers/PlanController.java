/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import controllers.admin.CompanyController;
import java.util.List;
import models.UserBase;
import models.Admin;
import models.User;
import models.pages.Article;
import models.pages.HomePage;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.GenericModel;
import play.i18n.Messages;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Finally;

/**
 * Controller som kontrollerar så att användaren är inloggad.
 * !OBS ! vid omskrivning... sök redan på all användning av sessionuser i Catch!
 * @author weetech
 */
public class PlanController extends Controller{
    
    /**
     * Ger ut den inloggade användaren
     * @return 
     */
    protected static UserBase user()
	{
		UserBase user = Cache.get(session.getId()+"user", UserBase.class);
	
		if(user == null)
		{
                    play.Logger.info("USER=NULL");
                    try
                    {
                            Long id = new Long(session.get("userid"));
                            
                            user = UserBase.find("byId",id).first();
                            Cache.set(session.getId()+"user", user, "30mn");
                    } 
                    catch(Exception ex)
                    {
                        play.Logger.error("PlanController.user() = %s", ex.getMessage());
                    }
 
		}
                
                //Om vi inte hittat någon användare gå till login
                if(user==null)
                {
                    renderArgs.put("loginform",1);
                    Application.loginform();
                }
		return user;
	}
   
	protected static long getUserId(){
		return new Long(session.get("userid")).longValue();
	}
	
	protected static long getCompanyId(){
		return user().company.id;
	}
	
    @Before
    /**
     * Plockar in olika argument. Globala sidor från pages.
     * Företagets startsida och artiklar
     */
    private static void getArgs()
    {
       
       
        UserBase userAuth = user();
        if(userAuth==null)
        {
            Logger.info("planController.getArgs() USER = NULL");
            renderArgs.put("loginform",1);
            Application.loginform();
        }
        else
        {
           Logger.info("planController.getArgs() USER");
           UserBase user = UserBase.findById(getUserId());
           renderArgs.put("sessionuser",user);
           Cache.set(session.getId()+"user", user, "30mn");
        }
    }
    
    @After
    private static void getArticles()
    {
        //TODO:se till att spara undan i Cache:en lägg en uppdate grej på något sätt så man vet när man ska hämta ut det.
        //Alternativt spara till Cachen direkt när du sparar
         //Hämtar in globla artiklar
        List<Article> globalArticles = Article.find("byGlobalAndMenuitem", true, true)
                .fetch();
        //Hämtar in de sidor som finns kopplade till företaget för att visa i menyn
        List<Article> articles = Article.find("byCompanyAndMenuitemAndGlobal", user().company, true, false)
                .fetch();
       
        //Hämtar in företagets startsida
        HomePage home = HomePage.find("byCompany", user().company).first();
               
        //lägger menyföremål samt hemsida som parametrar
        renderArgs.put("articles", articles);
        renderArgs.put("globalArticles", globalArticles);
        renderArgs.put("home", home);
    }
    
    @After
    private static void saveArgs()
    {
        if(flash.contains("message"))
        {
            String msg = flash.get("message");
            renderArgs.put("message", msg);
        }
        
    }
}
