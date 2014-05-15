package models.Core;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import models.Company;
import models.Core.Module;
import models.UserBase;
import play.Logger;
import play.db.jpa.Model;

@Entity
@Table(name = "core_company_user_settings")
public class CompanyUserSettings extends Model{
    
	@OneToOne
        public Company company;
	@OneToOne
	public UserBase user;      
	@ManyToMany
	public List<Module> modules;

	@Column(nullable=false)
	String usertype = "UserBase";
	
	public CompanyUserSettings(UserBase _user, Company _company) throws CUSException{
            long count = CompanyUserSettings.count("byCompanyAndUser", _company, _user);
            if(count == 0)
            {
		this.user = _user;
		this.company = _company;
                //this.usertype = _user.getClass().
		this.save();
            }
            else
            {
                CompanyUserSettings cus = CompanyUserSettings.find("byCompanyAndUser", _company, _user).first();
                throw new CUSException("core.user.has.cus.already",cus);
            }
	}
	
	public List<Module> getModules()
	{
            //return this.modules!=null?modules:new ArrayList<Module>();
            return this.modules;
	}
	
	public boolean addModule(Module module)
        {
            try
            {
                if(this.modules == null) this.modules = new ArrayList<Module>();
                this.modules.add(module);
            } 
            catch(Exception ex)
            {
                Logger.error("%s", ex.getMessage());
                return false;
            }
            this.save();

            return true;
        }
	
	public String getUserType() {return this.usertype;}
	
	public void setUserType(String _usertype)
	{
            if("SuperAdmin".equals(_usertype))
            {
                this.usertype = "Super";
            }
            else
            {
            	this.usertype = _usertype;
            }
            
            if(this.modules == null)
            {
                Logger.info("modules == null");
            }
            else
            {
                Logger.info("CUS modules NOT null, size = %s", modules.size());
                for(Module m : modules)
                {
                    Logger.info("CUS HAS %s", m.name);
                }
            }
		
            this.save();
	}
	
	public static CompanyUserSettings findByUserAndCompany(UserBase _user, Company _company)
	{
		if(CompanyUserSettings.count("byUserAndCompany", _user, _company)>1)
		{
			Logger.error("MULTIPLE COMPANYUSERSETTINGS TO %s AND %s", _user.id, _company.name);
		}
		CompanyUserSettings cus = CompanyUserSettings.find("byUserAndCompany", _user, _company).first();
		return cus;
	}
	
	public static List<Module> getUserModules(UserBase _user, Company _company)
	{
		CompanyUserSettings cus = findByUserAndCompany(_user, _company);
		return cus==null?null:cus.modules;
	}
        
        public class CUSException extends Exception{
            CompanyUserSettings cus;
            
            public CompanyUserSettings getCus()
            {
                return this.cus;
            }
            public CUSException(String msg, CompanyUserSettings _cus)
            {
                super(msg);
                this.cus =_cus;
            }
        }

}