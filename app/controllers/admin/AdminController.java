/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.admin;

import controllers.Application;
import controllers.PlanController;
import controllers.admin.CompanyController;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import models.Admin;
import models.Core.Module;
import models.Grupp;
import models.User;
import models.UserBase;
import play.Logger;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;
import models.Company;
import models.Core.CompanyUserSettings;

/**
 *
 * @author weetech
 */
public class AdminController extends PlanController
{

   /**
    * Kontrollerar så användaren är admin. Samt om användaren ej har ett
    * företag, gå in och fixa ett företag
    */
   @Before
   public static void authority() throws NoAdminException
   {
      try
      {
         if (PlanController.user() instanceof Admin)
         {
            Logger.info("AdminController.authority: ADMIN");
            //Om användaren ännu inte skapat ett företag. Gå och hantera detta!
            if (user().company == null && !((Controller.request.action.equals("admin.CompanyController.index"))
                    || (Controller.request.action.equals("admin.CompanyController.create"))))
            {
               flash.put("message", Messages.get("admin.you.need.to.create.a.company.first"));
               CompanyController.index();
            }

         } else
         {
            flash.put("message", Messages.get("You.need.to.login.as.administrator"));
            Application.showLoginForm();
         }

      } catch (Exception ex)
      {
         flash.put("showLoginform", "true");
         Application.start();
      }
   }

   public static void index()
   {
      render("admin/index.html");
   }

   /**
    * Functions witch handles the adminfunctions of modules
    */
//<editor-fold defaultstate="collapsed" desc=" Modules" >
   public static void modules()
   {
      String flashid = flash.get("user");
      Long id = params.get("userid", Long.class);

      Logger.info("userid %s", id);
      UserBase user = UserBase.findById(id != null ? id.longValue() : (flashid != null ? new Long(flashid).longValue() : 0));

      List<Module> modules = user().company.modules;
      List<Module> usermodules = CompanyUserSettings.getUserModules(user, user().company);
      render("admin/modules/modules.html", modules, usermodules, user);
   }

   public static void removeModuleFromUser(Long userid, Long moduleid)
   {
      UserBase user = UserBase.findById(userid);

      CompanyUserSettings cus = CompanyUserSettings.findByUserAndCompany(user, user().company);
      if (cus != null)
      {
         for (Module module : cus.modules)
         {
            if (module.id == moduleid)
            {
               cus.modules.remove(module);
               cus.save();
               flash.put("message", Messages.get("module.removed", module.name, user.name));
               break;
            }
         }
      }
      flash.put("user", user.id);
      AdminController.modules();
   }

   public static void addModuleToUser(long uId, List<Long> modid)
   {
      UserBase user = UserBase.findById(uId);
      //Lägg företaget i flash för att vi ska kunna få tag i det i metoden modules()

      flash.put("user", user.id);
      if (user == null)
      {
         flash.put("message", Messages.get("admin.NoUserSelected"));
         AdminController.modules();
      }


      //Kontrollerar så någon modul är vald om den inte är vald så tömmer vi hela medulslistan för företaget
      if (modid == null || modid.size() == 0)
      {
         flash.put("message", "SuperAdmin.NoModuleSelected");
         AdminController.modules();
      }

      List<String> added = new ArrayList<String>();
      for (Long moduleId : modid)
      {
         if (moduleId != null)
         {
            Module module = Module.findById(moduleId);
            if (module != null)
            {
               if (addModuleToCompanyUserSettings(user, module))
               {
                  added.add(module.name);
               }
            }
         }
      }
      if (added.size() > 0)
      {
         flash.put("message", Messages.get("Modules.added.to.user", added, user.name));
      }
      modules();
   }

   protected static boolean addModuleToCompanyUserSettings(UserBase user, Module module)
   {
      boolean companyHasAccessToModule = false;
      for (Module mod : user().company.modules)
      {
         if (module.id == mod.id)
         {
            companyHasAccessToModule = true;
            break;
         }
      }

      if (!companyHasAccessToModule)
      {
         forbidden("Company has not access to Module " + module.name);
      }

      try
      {
         Company company = PlanController.user().company;
         CompanyUserSettings cus = CompanyUserSettings.findByUserAndCompany(user, company);
         if (cus.modules == null)
         {
            cus.modules = new ArrayList<Module>();
         }
         cus.modules.add(module);
         cus.save();
         return true;
      } catch (Exception ex)
      {
         Logger.error("Fel i AdminController.addModuleToCompanySettings");
         Logger.error(ex.getMessage() + "\n" + ex.getCause().toString());
         return false;
      }
   }

   //</editor-fold> 
   public static class NoAdminException extends Exception
   {

      public NoAdminException()
      {
         super("User with no admin-rights try to access AdminController");
      }
   }
}
