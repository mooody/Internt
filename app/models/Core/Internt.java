/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package models.Core;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import models.Company;
import models.invoice.CustomerInvoice;
import models.invoice.Invoice;
import models.invoice.InvoiceProfile;
import models.invoice.InvoiceRow;
import models.invoice.InvoiceRowBlock;
import play.db.jpa.Model;
import play.i18n.Messages;

/**
 *
 * @author weetech
 */
@Entity
@Table(name="core_internt")
public class Internt extends Model{
    
    //<editor-fold defaultstate="collapsed" desc=" getters and setters">
    public double getAdminCost() {
        return adminCost;
    }

    public void setAdminCost(double adminCost) {
        this.adminCost = adminCost;
    }

    public double getUserCost() {
        return userCost;
    }

    public void setUserCost(double userCost) {
        this.userCost = userCost;
    }
    
    public void setbillingProfile(InvoiceProfile profile)
    {
        this.billingProfile = profile;
    }
    
    //</editor-fold>
    @OneToOne
    InvoiceProfile billingProfile;
    double adminCost = 49.0;
    double userCost = 9.0;
    
    public InvoiceProfile getBillingProfile()
    {
        if(this.billingProfile != null) return this.billingProfile;
        else
        {
            InvoiceProfile profile = new InvoiceProfile();
            
            profile.setPaymentTerms(30);
            profile.setCompanyName("Internt.nu");
            Company owner = Company.findById(new Long(1));
            profile.setCompany(owner);
            return profile;
        }
    }
    
    public void createInvoice(Company company, List<Module> modules, int period) throws CloneNotSupportedException
    {  

        CustomerInvoice invoice = new CustomerInvoice();
        invoice.copy( this.getBillingProfile().createInvoice());
        invoice.setReciverField(company.name+"\n"+company.street+"\n"+company.zipcode+" "+company.city);
        InvoiceRowBlock block = new InvoiceRowBlock();
        block.setMomssats(25);        
        
        block.save();
        for(Module m : modules){
            InvoiceRow row = new InvoiceRow();
            row.setDescription(Messages.get(m.name));
            row.setAntal(period);
            row.setUnit(InvoiceRow.ROW_UNITS.MONTH);
            row.setPrisPerEnhet(m.getCost());
            row.setBlock(block);
            row.save();
        }
        
        invoice.setInvoiceNumberWithSave(invoice.getCompany());
        Invoice result = invoice.save();
        block.setOwner(invoice);
        block.save();

    }
    
}
