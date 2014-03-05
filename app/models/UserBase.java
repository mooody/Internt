/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import controllers.ModuleController;
import controllers.PlanController;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import models.Core.Module;
import org.hibernate.annotations.AnyMetaDef;
import org.hibernate.annotations.ManyToAny;
import org.hibernate.annotations.MetaValue;
import play.Logger;
import play.data.validation.Required;
import play.db.jpa.Model;
import utils.language.Language;
import play.*;
import utils.Cryptography;
import java.security.InvalidKeyException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import models.Core.CompanyUserSettings;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.Email;
import play.db.jpa.JPABase;
import play.i18n.Messages;

/**
 *
 * @author weetech
 */
@Entity
@Inheritance
@DiscriminatorColumn(name = "UserType", discriminatorType = DiscriminatorType.STRING, length = 32)
@Table(name = "core_user")
@SqlResultSetMappings(
{
   @SqlResultSetMapping(name = "dummy", columns =
   {
      @ColumnResult(name = "nothing")
   })
})
@NamedNativeQuery(name = "UserBase.changeUserType",
        query = "update core_user set UserType = :type where id = :id",
        resultSetMapping = "dummy")
public class UserBase extends Model
{

   public UserBase()
   {
      this.activated = false;
      try
      {
         this.token = utils.Cryptography.getPasswordToken();
      } catch (UnsupportedEncodingException use)
      {
      } catch (NoSuchAlgorithmException nsa)
      {
      }
   }
   private static final String DES_ENCRYPTION_KEY = Play.configuration.getProperty("application.secret");
   @Required(message="validation.name.is.required")
   public String name;
   @CheckWith(UserBase.PwChecker.class)
   private String password;

   public String getPassword() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException
   {
      return Cryptography.decrypt(this.password, DES_ENCRYPTION_KEY);
   }

   public void setPassword(String _pw) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException
   {
      this.password = Cryptography.encrypt(_pw, DES_ENCRYPTION_KEY);
   }
   @Column(unique = true)
   @Required(message="validation.email.is.required")
   @Email
   public String email;
   public String street;
   public String zipcode;
   public String city;
   public String cellphone;
   public String phone;
   //Uses for activation and pw recovery
   public String token;
   @Column(nullable = false)
   public boolean activated = false;
   public boolean showTooltip = true;
   //public String lang;
   @Transient
   public Language language;
   @ManyToOne
   @JoinColumn(name = "user_company")
   public Company company;
   @ManyToMany(cascade = CascadeType.ALL)
   @JoinTable(name = "core_multiple_companies",
           joinColumns = {@JoinColumn(name = "user_id")},
           inverseJoinColumns ={@JoinColumn(name = "company_id")
   })
   public List<Company> companies;
  
   public void setCompany(Company _company)
   {
      this.company = _company;
   }
   /*
    @Basic(fetch=FetchType.LAZY)
    @ManyToMany(mappedBy="users")
    public List<AccessRights> rights;
    */
   @Basic(fetch = FetchType.LAZY)
   @OneToMany
   private List<Module> modules;
   @ManyToMany(targetEntity = Grupp.class, mappedBy = "users")
   private List<Grupp> groups;

   /**
    * Samma som this.groups
    *
    * @return
    */
   public List<Grupp> getGroups()
   {
      return Grupp.getUsersGroupsInCompany(this);
   }

   public List<Module> getModules()
   {
      return CompanyUserSettings.getUserModules(this, this.company);
   }

   public void setModules(List<Module> _modules)
   {
      this.modules = _modules;
   }

   public void addModule(Module module)
   {
      if (this.modules == null)
      {
         this.modules = new ArrayList<Module>();
      }
      this.modules.add(module);
   }

   public void clearModules()
   {
      this.modules.clear();
   }

   public List<Module> getUserAndGroupModules()
   {
      List<Module> modules = this.getModules();
      List<Grupp> groups = getAllGroups();
      for (Grupp group : groups)
      {
         modules.addAll(group.getModules());
      }

      return modules;
   }

   /**
    * Skapar en lista på alla grupper som användaren är med i. Den tar alla
    * grupper som användaren har, samt alla barngrupper som hör till dessa
    * grupper.
    *
    * @return
    */
   public List<Grupp> getAllGroups()
   {
      try
      {
         List<Grupp> usergroups = new ArrayList();
         usergroups.addAll(this.getGroups());
         //Gå igenom alla grupper och hämta barnen
         for (Grupp g : this.getGroups())
         {
            usergroups.addAll(Grupp.findChilds(g));
         }

         return usergroups;

      } catch (Exception ex)
      {
         Logger.warn("Exeption in UserBase.getAllGroups (user == null ??) %s", ex.getMessage());
         //throw ex;
      }
      //returnera en tom lista
      return new ArrayList<Grupp>();
   }

   public static String getCryptedPassword(String _pw) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException
   {
      return Cryptography.encrypt(_pw, DES_ENCRYPTION_KEY);
   }

   public static UserBase login(String email, String pw) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException
   {
      UserBase user = UserBase.find("select u from UserBase u where u.email = :email and u.password = :pw").bind("email", email).bind("pw", UserBase.getCryptedPassword(pw)).first();
      return user;
   }

   /**
    * Kopierar alla fält från den medskickade parameterns user till den här.
    *
    * @param user
    */
   public void copyFields(UserBase user)
   {
      if (user.id > 0)
      {
         this.id = user.id;
      }
      this.name = user.name;
      this.password = user.password;
      this.email = user.email;
      this.company = user.company;
      this.companies = user.companies;
      this.modules = user.modules;
      this.groups = user.groups;
   }

   /**
    * Returnerar true om användaren är av klassen Admin
    *
    * @return
    */
   public boolean isAdmin()
   {
      return this instanceof Admin;
   }

   /**
    * Returnerar true om användaren är av klassen SuperAdmin
    *
    * @return
    */
   public boolean isSuperAdmin()
   {
      return this instanceof SuperAdmin;
   }

   /**
    * Returnerar true om användaren är av klassen Admin
    *
    * @return
    */
   public boolean isUser()
   {
      return this instanceof User;
   }

   @PostLoad
   public void alert()
   {
      Logger.info("PostLoad UserBase");
   }

   @Override
   public String toString()
   {
      return this.name + " " + this.id;
   }
   private String sign;

   /**
    * Om användaren inte har en signature, skapa en
    *
    * @return this.sign
    * @throws Exception om det är 100 som har samma signature. Troligen ett fel
    * då!
    */
   public String getSign() throws Exception
   {
      Logger.info("UserBase.getSign");
      if (this.sign != null)
      {
         return this.sign;
      } else
      {
         //Skapa initialer av anv�ndaren         
         String[] initials_arr = this.name.split(" ");


         //för räkneverket endast, deklareras utanför pga whileloopen
         long count = 0;
         //om användarens sign redan finns, lägg till en siffra
         int signer = 0;
         do
         {
            StringBuilder sb = new StringBuilder();
            signer++;

            for (String s : initials_arr)
            {
               sb.append(s.toLowerCase().charAt(0));
               sb.append(s.toLowerCase().charAt(s.length() - 1));

            }

            if (signer > 1)
            {
               sb.append(signer);
            }

            this.sign = sb.toString();

            count = User.count("select count(u) from User u where u.sign = ?1 and u.company = ?2", this.sign, this.company);


            if (signer > 100)
            {
               throw new Exception("To many users with same sign, max 100");
            }
         } while (count > 0);

         this.save();
         return this.sign;
      }
   }
   //<editor-fold defaultstate="collapsed" desc=" Grupper">

   /**
    * Kontrollerar om användaren är i denna grupp. Detta sker genom ett anrop
    * till nedanstående isInGroupList
    *
    * @param group
    * @return gruppens id nummer
    */
   public long isUserInGroup(Grupp group)
   {
      try
      {

         List<Grupp> usergroups = new ArrayList();
         UserBase u = UserBase.findById(this.id);
         usergroups.addAll(u.groups);
         return this.isGroupInList(usergroups, group);

      } catch (Exception ex)
      {
         Logger.info("Ex in User.isUserInGroup %s", ex.getMessage());
      }


      return 0;
   }

   /**
    * Metod som kontrollerar om gruppen finns i listan som skickas med.
    *
    * @param grouplist Lista med grupper att kontrollera
    * @param group gruppen att kontrollera
    * @return true om gruppen hittades, annars false
    */
   public long isGroupInList(List<Grupp> grouplist, Grupp group)
   {
      Logger.info("UserBase.groupFound size:%d", grouplist.size());
      for (Grupp g : grouplist)
      {
         if (g.id == group.id)
         {
            Logger.info("UserBase.groupFound %s is in %s", this.name, group.name);
            return g.id;
         } else
         {
            long id = this.isGroupInList(g.childs, group);
            if (id > 0)
            {
               return id;
            }
         }
      }
      return 0;
   }

   /**
    * Lägger till en användare i gruppen.
    *
    * @param groupId id of group
    * @param userId id of user
    * @return 0 = ok, 1 = user already in group, 2 = error
    */
   public long addGroupToUser(Long groupId) throws Exception
   {
      Grupp grupp = Grupp.findById(groupId);
      //User tempUser = User.findById(userId);
      long gId = this.isUserInGroup(grupp);
      if (gId > 0)
      {
         Logger.info("UserBase.addGroupToUser - user in group");
         return gId;
      }

      try
      {
         grupp.users.add(this);
         grupp.save();
      } catch (Exception ex)
      {
         Logger.error("Catch in UserBase.addGroupToUser gid %s, uid%s", groupId, this.id);
         throw ex;
      }

      return 0;

   }

   /**
    * Funktionen tar bort användaren från gruppen
    *
    * @param groupId (gruppens id som ska tas bort)
    * @return 0 om ok, 1 om användaren inte är i gruppen och 2 om ett fel
    * inträffat
    */
   public int removeUserFromGroup(Long groupId)
   {
      Grupp grupp = Grupp.findById(groupId);

      if (this.isUserInGroup(grupp) > 0)
      {
         return 1;
      }


      try
      {
         grupp.users.remove(this);
         grupp.save();

      } catch (Exception ex)
      {
         Logger.error("Catch in UserBase.removeUserFromGroup gid %s, uid%s", groupId, this.id);
         return 2;
      }

      return 0;
   }

   public void sendWelcome()
   {
      notifiers.Mails.welcome(this);
   }
   //</editor-fold>
   
   static class PwChecker extends Check 
    {
        public boolean isSatisfied(Object user, Object password) 
        {
           String pw = "";
           boolean pwerror = false;
           try
           {
              pw = ((UserBase)user).getPassword();
           } catch (InvalidKeyException ex)
           {
              pwerror=true;
              java.util.logging.Logger.getLogger(UserBase.class.getName()).log(Level.SEVERE, null, ex);
           } catch (IllegalBlockSizeException ex)
           {
              pwerror=true;
              java.util.logging.Logger.getLogger(UserBase.class.getName()).log(Level.SEVERE, null, ex);
           } catch (BadPaddingException ex)
           {
              pwerror=true;
              java.util.logging.Logger.getLogger(UserBase.class.getName()).log(Level.SEVERE, null, ex);
           } catch (IOException ex)
           {
              pwerror=true;
              java.util.logging.Logger.getLogger(UserBase.class.getName()).log(Level.SEVERE, null, ex);
           }
           finally
           {
              if(pwerror)
              {
                 setMessage("validation.password.error");
                 return false;
              }
           }
  
            Logger.info("pw =%s %s %s %s", pw, pw.length() < 6,pw.matches("(.*)[0-9](.*)"),pw.matches("(.*)[a-zA-Z](.*)"));
            
           if(pw.length() < 6 || !((pw.matches("(.*)[0-9](.*)")) && pw.matches("(.*)[a-zA-Z](.*)")) )
           {
              setMessage("validation.password.failed");
              return false;
           }
           
           
           return true;
            
        }
    }
}
