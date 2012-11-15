package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;
import play.cache.Cache;
import play.i18n.Messages;


/**
 * Application controllern har hand om inloggning. 
 * och här är även startpunkten för sidan
 * @author weetech
 */
public class Application extends Controller {

    public static void index() {
        
        //Om sessionen lever så redirect till mypage
        if(session.get("userid")!=null)
        {
            redirect("users.mypage");
        }
        
        //kolla om det finns något meddelande
        if(flash.contains("message"))
        {
            renderArgs.put("message", flash.get("message"));
        }

        render();
    }
    
  public static void loginform()
    {
        renderArgs.put("loginform",true);
        Logger.info("Loginform");
        render("Application/login.html");
    }
    
    public static void login()      
    {
        renderArgs.put("loginform",true);
        String email = params.get("email", String.class);
        String password = params.get("password", String.class);
        
        Logger.info("******");
        for(String s:params.allSimple().keySet())
        {
            Logger.info("key:%s value:%s", s, params.get(s));
        }
        Logger.info("******");
        
        String message = Messages.get("login.failed");
        
        if(email==null||password==null||email.isEmpty()||password.isEmpty())
        {
            message = Messages.get("you.need.to.type.pass.and.name");
            Logger.info("just render");
            flash.put("message", message);
            render("Application/login.html");
        }
        
        if(email!=null&&!email.isEmpty()&&password!=null&&!password.isEmpty())
        {
            Logger.info("check username %s", email);
            
            UserBase user = UserBase.login(email,password);
            if(user != null)
            {
                Logger.info("User OK");
                message = Messages.get("login.ok");
                //Cache.set("sessionuser",user);  
                session.put("userid", user.id);
                
                flash.put("message", message);
                redirect("users.mypage");
            }   
        }
        
        flash.put("message", message);
        render("Application/login.html");
    }
    
    public static void logout()
    {
        Logger.info("Logout");
        //Cache.safeDelete("sessionuser");
        
        session.clear();
        
        flash.put("message", Messages.get("you.have.been.logged.out"));
        Application.index();
    }
    
    public static void signup()
    {
        render();
    }
    
    /**
     * Create an adminaccount
     * 
     * @param user 
     */
    public static void createAccount(Admin user)
    {
        user.save();
        flash.put("message", "Account.created.successful");
        params.data.clear();
        Application.loginform();
    }
}