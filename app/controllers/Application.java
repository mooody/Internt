package controllers;

import play.*;
import play.mvc.*;
import java.util.*;
import models.*;
import play.cache.Cache;
import play.i18n.Messages;
import models.Core.CompanyUserSettings;

/**
 * Application controllern har hand om inloggning. 
 * och här är även startpunkten för sidan
 * @author weetech
 */
public class Application extends Controller {

	/**
	* Om sessionen finns med ett userid så blir det en redirect till User.mypage
	*/
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
        render("application/login.html");
    }
    
	/**
	* Inloggning.
	* 1: om det inte finns mail och lösen skickas med direkt tillbaka
	* 2: om det finns mail och lösen inskrivet hämtas användaren ut.
	* 3: om användaren är null skickas man direkt tillbaka igen
	* 4: om den uthämtade användaren inte är null kontrolleras om det finns flera företag kopplade
	* 5: om det finns flera företag kopplade skickas man till selectCompany för att välja
	* 6: 
	*/
    public static void login()      
    {
		//lägger in till routingen så vi vet att vi kommer från loginform
        renderArgs.put("loginform",true);
		//Hämtar ut email
        String email = params.get("email", String.class);
        String password = params.get("password", String.class);
        
        //fördefiniera resultatmeddelande
        String message = Messages.get("login.failed");
        
		//Om email eller lösen inte existerar, avbryt
        if(email==null||password==null||email.isEmpty()||password.isEmpty())
        {
            message = Messages.get("you.need.to.type.pass.and.name");
            Logger.info("just render");
            flash.put("message", message);
            render("Application/login.html");
        }
        
		//om email och lösen finns kontrollera användaren
        if(email!=null&&!email.isEmpty()&&password!=null&&!password.isEmpty())
        {
            Logger.info("check username %s", email);
            UserBase user = null;
			try {
				user = UserBase.login(email,password);
			} catch(Exception ex) {
				message = "some.error.occord.contact.site.help";
			}
            if(user != null)
            {
                message = Messages.get("login.ok");
                //Vi sätter användaren som inloggad
                session.put("userid", user.id);
                Cache.set(session.getId()+"user", user);
				
                flash.put("message", message);
				//om användaren har varit användare i flera företag och borttaget från ett.
				//fixa med företaget
				if(user.company==null && user.companies.size()==1)
				{
					user.company = user.companies.get(0);
					user.save();
				}
				//om användaren har mera än 1 företag kopplat till sig, gå till välj företag
				if(user.companies.size() > 1)
				{
					selectCompany(user);
					Logger.info("More Companys");
				}
				//gå till inloggningen
                redirect("users.mypage");
            }   
        }
        
        flash.put("message", message);
        render("Application/login.html");
    }
    
	/**
	* Logger ut och skickar vidare till startsidan.
	*/
    public static void logout()
    {
        Logger.info("Logout");
        session.clear();
        
        flash.put("message", Messages.get("you.have.been.logged.out"));
        Application.index();
    }
    
    public static void signup()
    {
        render();
    }
	
	/**
	* Om användaren har mera än 1 företag kopplat till sig så kommer denna vy att visas. 
	* Användaren får här välja vilket företag denne vill agera i
	*/
	private static void selectCompany(UserBase user)
	{
		List<Company> companies = null;
		if(user.companies != null)
        {
            companies = Company.find("select c from Company c left join c.usersWithMultipleAccounts u where u.id = :uid")
				.bind("uid", user.id)
				.fetch();
				
			Logger.info("Application.selectCompany_ size:%s", companies.size());
        }
		render("Application/selectcompany.html", companies);
	}
	
	//Sätter användarens företag till valt företag
	public static void loadCompany(long id)
	{
		long userid = new Long(session.get("userid")).longValue();
		UserBase user = UserBase.findById(userid);
		Company company = Company.findById(id);
		user.company = company;
		user.save();
		CompanyUserSettings cus = CompanyUserSettings.find("byUserAndCompany", user, company).first();
		
		if(cus!=null){
			String usertype = cus.getUserType();
			play.db.jpa.JPA.em().createNamedQuery("UserBase.changeUserType")
			.setParameter("type",usertype).setParameter("id",userid)
			.executeUpdate();
		}
		
		redirect("users.mypage");
	}
    
    /**
     * Create an adminaccount
     * Skapar ett adminkonto. Detta för registreringen
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