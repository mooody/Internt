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
@DiscriminatorValue("PrivilegeUser")
public class PrivilegeUser extends User{
    
}
