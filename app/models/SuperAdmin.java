/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import models.Admin;

/**
 *
 * @author weetech
 */
@Entity
@DiscriminatorValue("Super")
public class SuperAdmin extends Admin{
    
}
