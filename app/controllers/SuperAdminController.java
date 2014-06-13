/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import controllers.admin.AdminController;
import java.util.ArrayList;
import java.util.List;
import models.Admin;
import models.Company;
import models.Core.Internt;
import models.Core.Module;
import models.Grupp;
import models.SuperAdmin;
import models.UserBase;
import models.invoice.InvoiceProfile;
import play.Logger;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;

/**
 * Hanterar alla superadminfunktioner. 
 * Först kontolleras att användaren är superadministratör
 * @author weetech
 */
public class SuperAdminController extends PlanController {
    
	/*
    @Before
    public static void authority() throws AdminController.NoAdminException
    {
       try{
            
           if(!(PlanController.user instanceof SuperAdmin))
            {
                flash.put("message", "You.need.to.login.as.superadministrator");
                Application.loginform();
            }
            
       } catch(Exception ex)       {
           Application.loginform();
           
       }
    }*/  
    
    /**
     * Visar första sidan för superadministratörs panelen
     */
    public static void index()
    {
        render("bigadmin/index.html");
    }
    
    /**
     * Visar superadministratörspanelen för moduler
     */
    public static void modules()
    {
		String flashid = flash.get("company");
		
		Long id = params.get("cId", Long.class);
		Company company = Company.findById(id!=null?id.longValue():(flashid!=null?new Long(flashid).longValue():0));
		List<Company> companies = Company.findAll();
		List<Module> modules = Module.findAll();

        render("bigadmin/modules.html", companies, modules, company);
    }
    
    
    
    /** 
     * Visar superadministratörspanelen för grupper
     */
    public static void groups()
    {
        List<Grupp> groups = Grupp.find("select g from Grupp g where g.parent = null").fetch();
        render("bigadmin/groups.html", groups);
    }
    
     /**
     * Visar superadministratörs panelen för användare, (sorterad enligt company.name)
     */
    public static void users()
    {
        List<UserBase> users = UserBase.find("select u from UserBase u order by u.company.name, u.name").fetch();
        render("bigadmin/users.html", users);
    }
    
    /**
     * NO USE!
     * @param id 
     */
    public static void getGroupById(long id) throws Exception
    {
        throw new Exception("NO IMPLEMENTED! - superadmin.getGroupById");
        //return Grupp.findById(id);
    }
	
	/**
	*	Tar bort modulen med module-id från ett företag.
	* Visar notFound "Felaktigt företag" om företagsid inte kan hittas.
	*/
	public static void removeModuleFromCompany(long companyid, long moduleid)
	{
		Company company = Company.findById(companyid);

		notFoundIfNull(company, "Felaktig företag");
		flash.put("company", company.id);
		
		String modulename = null;
		for(Module module : company.modules)
		{
			if(module.id == moduleid)
			{
				company.modules.remove(module);
				modulename = module.name;
				company.save();
				break;
			}
		}
		
		if(modulename!=null)
		{
			flash.put("message", Messages.get("superAdmin.module.was.removed", modulename));
		}
		else
		{
			flash.put("message", Messages.get("No.modules.was.removed.from", company.name));
		}
		SuperAdminController.modules();
	}
    
	/**
	* Sparar undan alla valda moduler till ett företag
	* @param cId = företagets id
	* @param modid = array med modul id:n
	*/
    public static void addModuleToCompany(long cId, List<Long> modid)
    {        
		//kontrollerar företaget
		Company company = Company.findById(cId);
		//Lägg företaget i flash för att vi ska kunna få tag i det i metoden modules()
		flash.put("company", company.id);
	
		if(company == null )
		{
			flash.put("message", Messages.get("SuperAdmin.NoCompanySelected"));
			SuperAdminController.modules();
		}
		//Kontrollerar så någon modul är vald om den inte är vald så tömmer vi hela medulslistan för företaget
		if(modid == null || modid.size()==0){
			flash.put("message", "SuperAdmin.NoModuleSelected");
			SuperAdminController.modules();
		}
		
		//hämta in alla module id:n
		List<Module> modules = new ArrayList<Module>();
		for(Long moduleId: modid)
		{
			if(moduleId != null)
			{
				Module temp = Module.findById(moduleId);
				if(temp != null)
				{
					modules.add(temp);
				}
			}
		}        
	   
	   //lista för vilka moduler som är tillagda, endast till för flash meddelandet
	   List<String> added = new ArrayList<String>();
	   
	   
	   //Går igenom alla måduler som är medskickade
	   for(Module module: modules)
	   {
			try
			{
				if(company.modules==null) company.modules = new ArrayList<Module>();
				//Lägg till modulerna
				company.modules.add(module);
				//För flashmeddelandet
				added.add(module.name);
			} catch(Exception ex)
			{
				Logger.error("error in SuperAdmin.addModuleToCompany %s and module %s", company.name, module.name);
				Logger.error(ex.getMessage()+"\n"+ex.getCause());
				
			}
		}
		company.save();
		if(added.size()>0)
		{
			
			flash.put("message", Messages.get("Module.was.added.to.company", added, company.name));
		}
		else //Om det blev något fel
		{
			flash.put("message", Messages.get("No.modules.was.added.to", company.name));
		}
        SuperAdminController.modules();
    }
	
	public static void showCompanies()
	{
		List<Company> companies = Company.findAll();
		
		render(companies);
	}
	
	public static void showCompany(long id)
	{
		Company company = Company.findById(id);
		render(company);
	}
	
	public static void updateCompany(Company company)
	{
		company.save();
		flash.put("message", Messages.get("Company.updated"));
		showCompany(company.id);
	}
        
        public static void billing()
        {
            Internt internt = Internt.findById(1L);
            
            if(internt == null)
            {
                internt = new Internt();                
            }
            List<Module> modules = Module.findAll();
            
            List<InvoiceProfile> profiles = InvoiceProfile.find("select p from InvoiceProfile p where p.company.id = ?",1L).fetch();
            render(internt, modules,profiles);
        }
        
        public static void updateModule(Module module){
            if(params.get("module.released")==null)
            {
                module.setReleased(false);
            }
            module.save();
            billing();
        }
        
        public static void saveInternt(Internt internt)
        {
            internt.save();
            billing();
        }
}
