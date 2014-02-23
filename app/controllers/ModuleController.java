/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import controllers.admin.AdminController;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import models.Core.Module;
import models.Core.ModuleRights;
import models.UserBase;
import play.mvc.Before;
import play.Logger;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Controller;

/**
 * ModuleController. 
 * 
 * Extend all module controllers with this controller.
 * this controller is saved in the db.
 * @author weetech
 */
@Entity
public class ModuleController extends PlanController{
	
    @Id 
    @GeneratedValue
    public Long id;
    
    public Long getId() {
        return id;
    }

    public Object _key() {
        return getId();
    }
   
    
    @ManyToMany
    public List<Module> modules;
    
    /**
     * Fixar en länk till indexmetoden för modulen
	 * - I index metoden ser du även till och hämtar in aktuella sessionvariabler till modulen.
     * @param moduleId 
     */
    public static void index(Long moduleId)
    {
        Module module = Module.findById(moduleId);
		
		//först kontrollerar vi vilken användaraccess det finns på modulen
		if(module==null) notFound("Modulen finns inte");
		try{
			if(!user().company.modules.contains(module))
			{
				forbidden("Company has not rights to see content");
			}
			//Kontrollerar att det är rätt användartyp
			if((Class.forName("models."+module.getUserAccessType())).isInstance(user()))
			{
				//hämtar ut användarens moduler
				List<Module> modules = user().getUserAndGroupModules();
				
				for(Module m : modules)
				{
					Logger.info("Users modules %s (%s = %s)", m.name, moduleId, m.id);
					if(m.id == moduleId.longValue())
					{
						Logger.info("Found");
						redirect(module.getModuleControllerActionRoute("index"));
					}
				}
			}
			
		}catch(Exception ex){

			Logger.error("ModuleController.index: %s %s",ex.getMessage(), ex.getClass().getName());
			Validation.addError("error", "Error in loading module");
			Validation.keep();
			controllers.Users.mypage();
		}
		//TODO Kolla detta... varför privilegad användare
		if(!(user() instanceof models.PrivilegeUser))
		{
			flash.put("message", Messages.get("users.need.to.be.privileged"));
		}
        forbidden();
    }
    
   
	/*
	* Laddar in en liten "peek"-vy från modulen. I denna är det även bra att anropa clearSession().
	*   - private void clearSession()
		- Programeringsmetod. Denna metod ska finnas i huvudcontrollern till modulen. 
		- Anropa denna från peek där du rensar modulens sessionvariabler.
	*
	*/
    public static void peek(Long moduleId)
    {
        Module module = Module.findById(moduleId);
        redirect(module.getModuleControllerActionRoute("peek"));
    }  
    
    /**
     * Hämtar ut modulrättigheterna med ajax
     * @param moduleId 
     */
    public static void setUserAccess(Long moduleId, long userId)
    {
        
        ModuleRights rights = null;
        SortedMap<String,Integer> list = null;
        Integer userRights = null;
        
        //hämta in användrens rättigheter
        rights = ModuleRights.find("select r from ModuleRights r where r.user.id = :uid and r.module.id = :module and r.company.id = :companyId")
                    .bind("uid",userId)    
                    .bind("module",moduleId)
                    .bind("companyId",user().company.id)
                    .first();
        Logger.info("ModuleRights %s", rights);
        
        //om de inte finns hämta modules rättighetsklass
        if(rights == null)
        {
             rights = ModuleRights.find("select r from ModuleRights r where r.user.id is null and r.module.id = :module")  
                    .bind("module",moduleId)
                    .first();
        }
        else // om de finns använd användarens rättigheter
        {
            userRights = rights.getRights();
        }
            
        if(rights!=null) 
        {
            list = rights.getListOfRights();
            Logger.info("RIGHTS: %s = %s",rights.getClass().getName(), rights.getListOfRights().size());
        }
        
        
        
        render("/admin/modules/showModuleRights.html", list, userId, moduleId,userRights);
    } 
    /**
     * Är till för företagsspecifika moduleinställningar
     * @param moduleId
     * @param companyId 
     */
    public static void settings(Long moduleId, Long companyId)
    {
        Module module = Module.findById(moduleId);
        String route = module.getModuleControllerActionRoute("settings");
        if(route!=null)
            redirect(route+"?companyId="+companyId);
        else
            renderText(Messages.get("core.settings.avalible"));
    }  
    
    /**
     * Är till för globala modulinställningar
     * @param moduleId 
     */
     public static void globalSettings(Long moduleId)
    {
        Module module = Module.findById(moduleId);
        String route = module.getModuleControllerActionRoute("globalsettings");
        if(route!=null)
            redirect(route);
        else
            render("SuperAdminController/modules/noAvalibleGlobalSettings.html");
    }  
     
     /**
      * Ajaccall - sparar undan användarrättigheter
      * @param module_id
      * @param user_id
      * @param list Lista med rättigheter satta
      */
     public static void saveUserAccess(long module_id, long user_id, List<Integer> list)
     {
         UserBase user = UserBase.findById(user_id);
         Module module = Module.findById(module_id);
 
         Controller.notFoundIfNull(user, Messages.get("user.is.missing"));
         Controller.notFoundIfNull(user, Messages.get("module.is.missing"));
         ModuleRights rights = ModuleRights.find("byModule", module).first();
         ModuleRights userRights = ModuleRights.find("select r from ModuleRights r where"
                 + " r.user = :user and"
                 + " r.module = :module and"
                 + " r.company = :company")
                 .bind("module",module)
                 .bind("user",user)
                 .bind("company",user().company)
                  .first();

         if(userRights == null)
         {
            try {
               userRights= rights.getClass().newInstance();
               userRights.company = user().company;
               userRights.user = user;
               userRights.module = module;
               
           } catch (InstantiationException ex) {
               java.util.logging.Logger.getLogger(ModuleController.class.getName()).log(Level.SEVERE, null, ex);
           } catch (IllegalAccessException ex) {
               java.util.logging.Logger.getLogger(ModuleController.class.getName()).log(Level.SEVERE, null, ex);
           }
         }
         else
         {
             //nollställ alla rättigheter
            userRights.setRights(0);
         }
                  
         //lägg till rättigheterna
         if(list!=null)
         {
            for(Integer i:list)
            {
                userRights.addRight(i);
            }
         }
         userRights.save();
         ok();
     }
     
    
}
