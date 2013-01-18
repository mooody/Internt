package models.Core;
import models.Company;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Column;
import javax.persistence.ManyToMany;
import play.db.jpa.Model;
import java.util.List;
import models.UserBase;
import models.Core.Module;
import play.Logger;

@Entity
public class CompanyUserSettings extends Model{
    
	@OneToOne
    public Company company;
	@OneToOne
	public UserBase user;      
	@ManyToMany
	public List<Module> modules;
	//@ManyToMany
	//public List<Grupp> groups;
	
	@Column(nullable=false)
	String usertype = "UserBase";
	
	public CompanyUserSettings(UserBase _user, Company _company){
		this.user = _user;
		this.company = _company;
		this.save();
	}
	
	public String getUserType() {return this.usertype;}
	
	public void setUserType(String _usertype)
	{
		this.usertype = _usertype;
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

}