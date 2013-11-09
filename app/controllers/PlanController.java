/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import controllers.admin.CompanyController;
import models.UserBase;
import models.Admin;
import models.User;
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
    private static void getArgs()
    {
        Logger.info("planController.getArgs()");
        //long userAuth = 0;
        /*
		try{
			userAuth = UserBase.count("select count(u.id) from UserBase u where u.id = ?", getUserId() );
		} catch (NumberFormatException ne) {
		}
        */
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
    private static void saveArgs()
    {
        if(flash.contains("message"))
        {
            String msg = flash.get("message");
            renderArgs.put("message", msg);
        }
        
    }
}
