package controllers;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.*;
import java.util.logging.Level;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import models.*;
import models.Core.*;
import models.mail.Invite;
import play.*;
import play.cache.Cache;
import play.data.validation.Valid;
import play.i18n.Messages;
import play.libs.Codec;
import play.libs.Images;
import play.mvc.*;

/**
 * Application controllern har hand om inloggning. och här är även startpunkten
 * för sidan
 *
 * @author weetech
 */
public class Application extends Controller
{

   @Before
   public static void getSessionUser()
   {
      UserBase user = Cache.get(session.getId() + "user", UserBase.class);
      if(user != null)
      {
         renderArgs.put("sessionuser", user);
      }
   }
   /**
    * Om sessionen finns med ett userid så blir det en redirect till User.mypage
    * Annars index-sidan
    */
   public static void start()
   {

      //Om sessionen lever så redirect till mypage
      if (session.get("userid") != null)
      {
         redirect("Users.mypage");
      }

      //kolla om det finns något meddelande
      if (flash.contains("message"))
      {
         renderArgs.put("message", flash.get("message"));
      }
      
       if (flash.contains("showLoginform"))
       {
          renderArgs.put("showLoginform", "1");
       }

      render("Application/index.html");
   }
   
   public static void showLoginForm()
   {
      flash.put("showLoginform", "true");
      Application.start();
   }

   /**
    * Visar loginformuläret
    */
   public static void loginform()
   {
      renderArgs.put("loginform", true);
      render("Application/login.html");
   }

   /**
    * Inloggning. 1: om det inte finns mail och lösen skickas med direkt
    * tillbaka 2: om det finns mail och lösen inskrivet hämtas användaren ut. 3:
    * om användaren är null skickas man direkt tillbaka igen 4: om den uthämtade
    * användaren inte är null kontrolleras om det finns flera företag kopplade
    * 5: om det finns flera företag kopplade skickas man till selectCompany för
    * att välja 6:
    */
   public static void login()
   {
      if (IP.toManyTimes(request.remoteAddress))
      {
         forbidden(Messages.get("spamprotection.activated.to.many.tries.wait.30.min"));
      }
      //lägger in till routingen så vi vet att vi kommer från loginform
      renderArgs.put("loginform", true);
      //Hämtar ut email
      String email = params.get("email", String.class);
      String password = params.get("password", String.class);


      //Om email eller lösen inte existerar, avbryt
      if (email == null || password == null || email.isEmpty() || password.isEmpty())
      {
         validation.addError("error", "you.need.to.type.pass.and.name");
         validation.keep();
         Application.start();
      }

      //om email och lösen finns kontrollera användaren
      if (email != null && !email.isEmpty() && password != null && !password.isEmpty())
      {
         Logger.info("check username %s", email);
         UserBase user = null;
         try
         {
            user = UserBase.login(email, password);

            if (user != null && !user.activated)
            {               
               validation.addError("error", "account.not.activated");               
               flash.put("resend", user.email);
               validation.keep();
               Application.start();
            }

         } 
         catch (Exception ex)
         {
            validation.email("error","some.error.occord.contact.site.help");
            Logger.info(ex.getMessage() + " " + ex.getCause());
         }
         if (user != null)
         {
            /*List<Invite> invites = Invite.getInvites(user);
             if(invites != null && invites.size() > 0)
             {
             flash.put("hasInvites", true);
             }
             */
            flash.put("message", Messages.get("login.ok"));
            //Vi sätter användaren som inloggad
            session.put("userid", user.id);
            Cache.set(session.getId() + "user", user, "30mn");

            //om användaren har varit användare i flera företag och borttaget från ett.
            //fixa med företaget
            if (user.company == null && user.companies.size() == 1)
            {
               user.company = user.companies.get(0);
               user.save();
            }
            //resetta ip.skyddet
            IP.ipReset(request.remoteAddress);

            //om användaren har mera än 1 företag kopplat till sig, gå till välj företag
            if (user.companies.size() > 1)
            {
               selectCompany(user);
            }

            loadComanyUserSetting(user, user.company);
            //gå till inloggningen
            redirect("users.mypage");
         }
      }

      validation.addError("error",  Messages.get("login.failed"));
      validation.keep();
      //render("Application/login.html");
      Application.start();
   }

   /**
    * Logger ut och skickar vidare till startsidan.
    */
   public static void logout()
   {
      session.clear();

      flash.put("message", Messages.get("you.have.been.logged.out"));
      Application.start();
   }

   public static void signup()
   {
      String codeid = Codec.UUID();
      List<Module> modules = Module.find("byReleased", true).fetch();
      Internt internt = Internt.findById(1L);
      if(internt == null) internt = new Internt();
      render(codeid, modules, internt);
   }

   /**
    * Om användaren har mera än 1 företag kopplat till sig så kommer denna vy
    * att visas. Användaren får här välja vilket företag denne vill agera i
    */
   private static void selectCompany(UserBase user)
   {
      List<Company> companies = null;
      if (user.companies != null)
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
      try{
      long userid = new Long(session.get("userid")).longValue();
      Company company = Company.find("select c from models.UserBase u left join u.companies c where u.id = :uid and c.id = :cid")
              .bind("cid", id).bind("uid", userid).first();
      UserBase user = UserBase.findById(userid);

      if (company == null)
      {
         forbidden("Felaktig inloggning");
      }
      user.company = company;
      user.save();

      loadComanyUserSetting(user, company);

      redirect("users.mypage");
      } catch(Exception ex){
         Logger.error("Error in Application.loadCompany");
         Application.showLoginForm();
      }
   }

   /**
    * Sätter Laddar in användarens företagsinställningar
    *
    * @param user
    * @param company
    */
   private static void loadComanyUserSetting(UserBase user, Company company)
   {

      //Om det inte finns något företag. Exempelvis vid nyregistrering
      if (company == null)
      {
         return;
      }

      CompanyUserSettings cus = CompanyUserSettings.find("byUserAndCompany", user, company).first();

      //Om det inte finns en cus (Exempelvis vid inbjudan)
      if (cus == null)
      {
         cus = new CompanyUserSettings(user, company);
         cus.save();
      }

      String usertype = cus.getUserType();

      //Sätt användartyp på den inloggade användaren.
      play.db.jpa.JPA.em().createNamedQuery("UserBase.changeUserType")
              .setParameter("type", usertype).setParameter("id", user.id)
              .executeUpdate();

      //Hämtar in alla moduler
      List<Module> modules = cus.getModules();

      if (user.getModules() != null)
      {
         user.clearModules();
         user.save();
      }

      for (Module module : modules)
      {
         if (!modules.contains(module) && company.modules.contains(module)) //så företaget fortfarande har access till modulen
         {
            user.addModule(module);
         }
      }
      user.save();
   }

   /**
    * Create an adminaccount Skapar ett adminkonto. Detta för registreringen
    * Kontrollerar först så kontot inte redan finns.
    *
    * @param user
    */
   public static void createAccount(@Valid Admin user, @Valid Company company, List<Long> modules) throws CloneNotSupportedException
   {
//      String codeid = params.get("codeid");
//      String captcha = params.get("captcha");
//      String code = (String) Cache.get(codeid);

      if (UserBase.find("byEmail", user.email).first() != null)
      {
         validation.addError("user.email", Messages.get("validation.email.exists.in.system"));
         validation.keep();
         params.flash();
         Application.signup();
      }

      boolean pwerror = false;
      try
      {
         if (!user.getPassword().equals(params.get("password")))
         {
            pwerror = true;
         }
      } catch (InvalidKeyException ex)
      {
         pwerror = true;
         java.util.logging.Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
      } catch (IllegalBlockSizeException ex)
      {
         pwerror = true;
         java.util.logging.Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
      } catch (BadPaddingException ex)
      {
         pwerror = true;
         java.util.logging.Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
      } catch (IOException ex)
      {
         pwerror = true;
         java.util.logging.Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
      } finally
      {
         if (pwerror)
         {
            validation.addError("user.password", Messages.get("validation.signup.pw.not.equal"));
         }
      }

      
//      validation.equals(captcha, code).message("validation.wrong.captcha.code");

      if(modules==null || modules.isEmpty()){
          validation.addError("modules.error","validation.need.a.module");
      }
      
      if(params.get("agrement")==null){
          validation.addError("modules.error","validation.need.agrement");
      }
      
      
      if (validation.hasErrors())
      {
         validation.keep();
         params.flash();
         Application.signup();
      }
      
      

      user.activated = false;
      user.save();
      
      //setting upp company and cus
      company.save();
      CompanyUserSettings cus = company.attachUserToCompanyToGetCUS(user);
      company.createDefaultGroup(user);
      user.save();
      
      cus.setUserType(user.getClass().getSimpleName());
      
      
      List<Module> mods = Module.find("select m from Module m where m.id in :list").bind("list", modules).fetch();
      company.modules = mods;      
      company.save();
      cus.modules = mods;
      cus.save();
      //addModules
      
      //create invoice
      Internt internt = Internt.findById(1L);
      if(internt == null) internt = new Internt();
      internt.createInvoice(company, mods, 12);

      notifiers.Mails.welcome(user);
      flash.put("message", Messages.get("Account.created.successful"));
      params.data.clear();
      Application.loginform();
   }

   /**
    *
    *
    */
   public static void resendActivationMail(String email)
   {
      UserBase user = UserBase.find("byEmail", email).first();
      if (user.activated)
      {
         flash.put("message", Messages.get("your.account.is.already.activated"));
         Application.start();
      }

      if (user.token == null)
      {
         try
         {
            user.token = utils.Cryptography.getPasswordToken();
         } catch (Exception ex)
         {
            flash.put("message", Messages.get("Error.with.password.token"));
            Application.start();
         }
      }
      user.save();
      notifiers.Mails.welcome(user);
      flash.put("message", Messages.get("Activationmail.sent"));
      Application.loginform();
   }

   public static void activate(String token)
   {
      UserBase user = UserBase.find("byToken", token).first();

      if (user == null)
      {
         renderText("NO USER");
      }

      user.activated = true;
      user.token = null;
      user.save();
      flash("message", Messages.get("you.are.activated", user.name));
      Application.loginform();
   }

   /**
    * Visar vyn för återställning av lösenord.
    */
   public static void recoveryView(String token)
   {
      if (token == null)
      {
         render("Application/recoveryView.html");
      } else
      {
         long count = UserBase.count("select count(u.id) from UserBase u where u.token = ?", token);
         UserBase user = null;
         if (count == 1)
         {
            user = UserBase.find("byToken", token).first();
         }

         if (user == null)
         {
            validation.addError("message", Messages.get("Wrong.token.not.able.to.recovery.resend.mail"));
            validation.keep();
            //render("Application/recoveryView.html");
            Application.recoveryView(null);
         } else
         {
            renderText("OK");
         }
      }
   }

   /**
    * Skickar återställningsmail, kontroll av emailadressen i systemet. Om
    * emailen inte finns ges felmeddelande. Går tillbaka till recoveryView.html
    */
   public static void sendRecovory(String email)
   {
      UserBase user = UserBase.find("byEmail", email).first();
      if (user != null)
      {
         notifiers.Mails.lostPassword(user);
         flash("message", Messages.get("email.sent"));
      } else
      {
         validation.addError("email.not.found", "validation.email.not.found");
      }
      render("Application/login.html");
   }

   public static void captcha(String codeid)
   {
      Images.Captcha captcha = Images.captcha();
      String code = captcha.getText("#111");
      Cache.set(codeid, code, "5mn");
      renderBinary(captcha);
   }

   //<editor-fold default-state="collapsed" desc=" helpmenu">
   public static void helpfiles(String module, String dir)
   { 
      File path = Play.applicationPath;
      MenuItem menus = new MenuItem(new File(path, "public/help"));
      getMenu(menus,".html");
      SortedMap<File,Integer> map = (SortedMap<File, Integer>) new TreeMap<File, Integer>();
      
      for(File file:(new File(path, "planningmodules")).listFiles())
      {
         File modulehelp = new File(file, "public/modulehelp");
         if(modulehelp.isDirectory())
         {
            MenuItem menu = new MenuItem(modulehelp);
            getMenu(menu,".html");
            menus.childs.add(menu);
         }
         else{
            Logger.info("Not Exists %s", modulehelp.getPath());
         }
      }
      
      get(map,menus,1);
      if(session.get("userid")!=null)
      {
         Logger.info("USERID REDIRECT TO PLANCONTROLLER");
         PlanController.help(map);
      }
      render(map) ;
   }   
   private static void get(Map<File, Integer> list, MenuItem item, int level)
   {
      if (item == null)
      {
         return;
      }
      list.put(item.file, level);

      level++;
      for (MenuItem m : item.childs)
      {
         list.put(m.file, level);
         if (m.items != null)
         {
            for (File s : m.items)
            {
               list.put(s, level);
            }
            get(list, m, level);
         }
      }
   }

   private static void getMenu(MenuItem menu, String filter)
   {
      File root = menu.file;
      File[] list = root.listFiles();

      if (list == null)
      {
         return;
      }

      for (File f : list)
      {
         if (f.isDirectory())
         {
            MenuItem child = new MenuItem(f);
            menu.childs.add(child);
            getMenu(child, filter);
         } else
         {
            if (f.getName().contains(filter) && f.exists())
            {
               menu.items.add(f);
            }
         }
      }
   }

   public static class MenuItem
   {
      public List<MenuItem> childs;
      public List<File> items;
      public MenuItem parent;
      File file;

      public MenuItem(File _file)
      {
         Logger.info("%s", _file);
         this.file = _file;
         this.childs = new ArrayList<MenuItem>();
         this.items = new ArrayList<File>();
      }

      public void addItem(File file)
      {
         this.items.add(file);
      }

      public MenuItem addChild(File file)
      {
         MenuItem temp = new MenuItem(file);
         this.childs.add(temp);
         return temp;
      }

      public String getPath()
      {
         return this.file.getName();
      }
   }
   //</editor-fold>
}