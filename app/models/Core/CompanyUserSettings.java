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

@Entity
public class CompanyUserSettings extends Model{
    
	@OneToOne
    public Company company;
	@OneToOne
	public UserBase user;      
	@ManyToMany
	public List<Module> modules;
	
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
}