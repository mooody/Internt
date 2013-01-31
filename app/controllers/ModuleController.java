/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import controllers.PlanController;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import models.Core.Module;
import models.UserBase;
import play.mvc.Before;
import play.Logger;
import play.data.validation.Validation;
import play.i18n.Messages;

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
        Logger.info("Loading peek from %s", moduleId);
        Module module = Module.findById(moduleId);
        redirect(module.getModuleControllerActionRoute("peek"));
        //render(module.name+"/peek.html");
    }  
     
    
}
