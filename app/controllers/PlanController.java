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
    
    protected static UserBase user()
	{
		return Cache.get(session.getId()+"user", UserBase.class);
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
        long userAuth = 0;
         
		try{
			userAuth = UserBase.count("select count(u.id) from UserBase u where u.id = ?", getUserId() );
		} catch (NumberFormatException ne) {
		}
        
        if(userAuth==0)
        {
            renderArgs.put("loginform",1);
            Application.loginform();
        }
        
        //om användaren inte är null så 
        if(userAuth!=0)
        {
           UserBase user = UserBase.findById(getUserId());
           renderArgs.put("sessionuser",user);
		   Cache.set(session.getId()+"user", user);
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
