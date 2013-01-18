/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.admin;

import controllers.PlanController;
import java.util.List;
import models.Admin;
import models.Company;
import models.Grupp;
import models.UserBase;
import models.User;
import play.Logger;
import play.i18n.Messages;
import play.mvc.Controller;
import java.util.ArrayList;
import models.Core.CompanyUserSettings;

/**
 *
 * @author weetech
 */
public class UsersController extends AdminController {
    
    public static void index()
    {
        UserBase tUser = UserBase.findById(getUserId());
        //List<Grupp> groups = tUser.getAllGroups();
        /*List<UserBase> users = UserBase.find("select u from UserBase u "
                + "where u.company.id = :crit").bind("crit", tUser.company.id).fetch();
		*/
		List<UserBase> users = UserBase.find("select u from UserBase u left join u.companies uc where uc.id = :company")
			.bind("company", getCompanyId()).fetch();
				
		Logger.info("size %s", users.size());
        render("admin/users/show.html", users);
    }   
     
	
    public static void create()
    {
        List<Grupp> groups = Grupp.findAll();
		//THIS IS NOT GOOD! find ALL
        //notFound("SEE UsersController.create()");
        render("admin/users/create.html", groups);
    }
    
	/**
	* Sparar en ny användar till ett företag
	*/
    public static void save()
    {
        //hämta ut administratörens företag
        UserBase user = params.get("user", User.class);
        
        boolean update = (user.id!=null&&user.id>0)?true:false;
        
        Controller.notFoundIfNull(user, Messages.get("user.not.found"));
        
        if(update)
        {
            user.save();
            flash.put("message", Messages.get("user.updated"));
            edit(user.id);
        }
        else
        {
		
			long count = UserBase.count("select count(u.id) from UserBase u where u.email = ?", user.email);

			if(count>0)
			{
				flash.put("message", Messages.get("send.invite"));
				index();
			}
			
            Company company = PlanController.user().company;

            user.company = company;
			if(user.companies == null) user.companies = new ArrayList<Company>();
			user.companies.add(company);

            if(user!=null)
            {
                user.save();
            }
			
			CompanyUserSettings cus = new CompanyUserSettings(user, company);
            cus.setUserType("User");
			
            flash.put("message", Messages.get("user.created"));
            index();

        }        
    }
	
    
    public static void edit(Long id)
    {
        UserBase user = UserBase.findById(id);
        render("admin/users/edit.html", user);
    }

    public static void addGroupToUser(Long userid, Long groupId)
    {
        Logger.info("Users.addGroupToUser - adding");
        
        if(PlanController.user() == null || !(PlanController.user() instanceof Admin))
        {
                Controller.forbidden();
        }
        
        //Grupp g = Grupp.findById(groupId);
        UserBase u = UserBase.findById(userid);
                
        String message = Messages.get("Group.added.to.user %s %s",groupId, u.name);  
        try {
            long gId = u.addGroupToUser(groupId);
            
            if(gId>0)
            {
                Grupp parent= Grupp.find("select g from Grupp g where g.id = :crit").bind("crit", gId).first();
                message = Messages.get("User.inready.in.parent.group %s",parent.name);  
            }
        } catch (Exception ex) {
            message = Messages.get("Some.error.occured");
        }
        
    
        
        flash.put("message", message);
        edit(u.id);
        
    }
    
    public static void removeUserFromGroup(Long userId, Long groupId)
    {
        Grupp group = Grupp.findById(groupId);
        
        UserBase user = UserBase.findById(userId);
        
        String message = Messages.get("user.removed.from.group %s %s",group.name, user.name);
        try 
        {
            if(!group.users.remove(user))
            {
                message = Messages.get("user.not.removed.from.group %s %s",group.name, user.name);
            }
            
            group.save();   
        } catch(Exception ex) {
            message = Messages.get("error.occured.when.try.to.remove.user %s %s",group.name, user.name);
        }
        
        Logger.info("Users.removeUserFromGroup (%s)", message);
        
        flash.put("message", message);
        edit(user.id);
        
    }
    
    public static void setUserType(long id, short usertype) throws Exception
    {
        long count = UserBase.count("select count(id) from UserBase u where u.id = ?", id);
		if(count==0)
		{
			notFound();
		}
       
		String discriminator = null;
		
		switch(usertype)
		{
			case 1: discriminator = "Admin"; break;
			case 2: discriminator = "Super"; break;
			default: discriminator = "User"; break;
		}

		play.db.jpa.JPA.em().createNamedQuery("UserBase.changeUserType")
	    .setParameter("type",discriminator).setParameter("id",id)
	    .executeUpdate();
		
		CompanyUserSettings cus = CompanyUserSettings.find("select c from CompanyUserSettings c where c.user.id = :uid and c.company.id = :company").bind("uid", id).bind("company", user().company.id).first();
		
		cus.setUserType(discriminator);

        edit(id);
        
    }
    
    public static void deleteUser(Long id)
    {
	
        UserBase user = UserBase.findById(id);
        Controller.notFoundIfNull(user, "Not.found.user");
        
        List<Grupp> groups = Grupp.find("select g from Grupp g left join g.users u where u.id = :userid and g.companyId = :companyId").bind("userid", id).bind("companyId", PlanController.getCompanyId()).fetch();

		Admin admin = (Admin)PlanController.user();
        for(Grupp g: groups)
        {
			g.users.remove(user);
			g.save();
        }
		
		user.companies.remove(admin.company);
		admin.company.users.remove(user);
		admin.company.save();
		
		CompanyUserSettings cus = CompanyUserSettings.find("byUserAndCompany", user, admin.company).first();
		if(cus!=null){
			cus.delete();
		}

		//Om användaren är inloggad i flera företag
		if(user.companies.size()==0)
		{
			flash.put("message", Messages.get("user.deleted"));
			user.delete();
		}
		else
		{
			flash.put("message", Messages.get("user.removed.from.company"));
			//se till användaren har ett aktivt företag som inställning...
			user.company = user.companies.get(0);
			user.save();
		}
        
        index();
    }
    
}
