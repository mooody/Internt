/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.admin;

import java.util.ArrayList;
import java.util.List;
import models.Admin;
import models.Company;
import models.Grupp;
import models.SuperAdmin;
import play.Logger;
import play.i18n.Messages;
import models.Core.CompanyUserSettings;
import play.data.validation.Valid;

/**
 * Vid skapande av ett företag kommer företaget först och främst läggar in i multiple_companies
 * sedan kommer en default grupp att skapas.
 * 
 * För att radera krävs det att man kollar användarens aktiva company, får ej vara samma som det 
 * företaget som ska raderas. Sedan måste default grupp raderas, samt i tabellen multiple company
 * och sist själva företaget.
 * Sist men inte minst behöver alla moduler raderas
 * @author weetech
 */
public class CompanyController extends AdminController {
    
    /**
     * Skapar ett företag.
     * @param company 
     */
    public static void create(@Valid Company company)
    {
            if(validation.hasErrors())
            {
                    validation.keep();
                    params.flash();
                    //redirect to createView
                    if(user().company != null && company.id == null) 
                    {
                            CompanyController.addCompany();
                    }
                    else
                    {
                        CompanyController.index();
                    }
            }
		
        /* om användaren redan har ett företag! lägg till ytterligare ett */
        if(user().company != null && company.id == null) 
        {
                addMultipleCompany(company);

        }
        //Update företaget
        if(company.id != null)
        {
            company.save();
            flash.put("message",Messages.get("company %s updated",company.name));
        }
        else //create skapa företaget
        {
            try{
         
                company.addUser(user());
                company.save();

                //Skapa en cus för användaren
                CompanyUserSettings cus = new CompanyUserSettings(user(), company);
                cus.setUserType(user().getClass().getSimpleName());
				
                Logger.info("CompanyControllercreate user %s", user());

                Admin admin = Admin.findById(user().id);
                admin.company = company;
                admin.save();

                CompanyController.createDefaultGroup(admin,company);
                
                flash.put("message",Messages.get("company.created",company.name));
            } catch (Exception ex)
            {
                StackTraceElement[] sex = ex.getStackTrace();
                for(StackTraceElement e: sex)
                {
                    Logger.info("%s", e.toString());
                }
                Logger.info("%s\n%s\n%s\n", ex.getMessage(), ex.getCause(), ex.getLocalizedMessage());
            }
        }
        
        CompanyController.index();
    }
	
	private static void addMultipleCompany(Company company)
	{
                if(!user().companies.contains(user().company))
                {
                    user().companies.add(user().company);
                }
		company.addUser(user());
		user().companies.add(company);
		company.save();
		CompanyUserSettings cus = new CompanyUserSettings(user(), company);
		if(company.users.size() == 1)
		{
			cus.setUserType(user().getClass().getSimpleName());
		}
		flash.put("message",Messages.get("company %s created",company.name));
		
		Admin admin = Admin.findById(user().id);
		try
                {
                    CompanyController.createDefaultGroup(admin,company);
		} catch(Exception ex)
		{
                    Logger.error("CompanyController.addMultipleCompany %s", ex.getMessage());
                }
		
		CompanyController.index();
	}
	
    private static void createDefaultGroup(Admin admin, Company company) throws Exception
    {	
        /*
        String defaultgrupp = play.Play.configuration.getProperty("defaultuser.grupp");
        String defaultmail = play.Play.configuration.getProperty("defaultuser.email");
        
        //Hämta ut superadmin
        SuperAdmin superadmin = SuperAdmin.find("byEmail", defaultmail).first();
        Logger.info("CompanyController.createDefaultgroupp superadmin is %s", superadmin.name);
        //Hämta superAdmins grupper
        List<Grupp> groups = superadmin.getGroups();
            
        Logger.info("CompanyController.createDefaultgroupp 1");
        //Hämta huvudgruppen
        Grupp topGroup = null;
        for(Grupp g: groups)
        {
            if(g.name.equals(defaultgrupp))
            {
               topGroup = g;
               break;
            }
        }
        
        Logger.info("CompanyController.createDefaultgroupp 2");
        
        if(topGroup==null)
        {
            throw new Exception("NO DEFAULT GROUP!");
        }
        */
        //Skapa nya användarens topgrupp
        Grupp newGroup = new Grupp(company.id);
        newGroup.name = company.name;
        newGroup.users = new ArrayList();
        newGroup.users.add(admin);
        newGroup.save();
        
        
        //lägg till den till default gruppen
       /* if(topGroup.childs == null) topGroup.childs = new ArrayList();
        topGroup.childs.add(newGroup);
        newGroup.parent = topGroup;
        newGroup.save(); 
        topGroup.save();
        */
   
    }
    
    public static void index()
    {
        Company company = null;
        if(user().company != null)
        {
            company = Company.findById(user().company.id);
        }
		List<Company> companies = null;
		
		if(user().companies != null)
		{
			companies = Company.find("select c from Company c left join c.usersWithMultipleAccounts u where u.id = :uid")
				.bind("uid", user().id)
				.fetch();
		}
		
        render("admin/company/create.html", company, companies);
    }
	
	public static void addCompany()
	{
		List<Company> companies = null;
                if(user().companies != null)
                {
                    companies = Company.find("select c from Company c left join c.usersWithMultipleAccounts u where u.id = :uid")
                                        .bind("uid", user().id)
                                        .fetch();

                                Logger.info("CompanyController.addCompany more comps %s", companies.size());
                }
		
		boolean newCompany = true;
                render("admin/company/create.html", companies,newCompany);
	}
}
