/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author weetech
 */
@Entity
@DiscriminatorValue("Admin")
public class Admin extends User{

    public Admin(UserBase user){
        this.copyFields(user);
    }

    public Admin() {
        //throw new UnsupportedOperationException("Not yet implemented");
    }
}
