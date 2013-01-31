package models.Core;


/**
 * Entitet som hanterar rättighetssättning för grupper och användare
 * Varje användare har en Lista av modulerights på sig, samt varje grupp.
 * Vid inträde till en module det först kontrolleras om användaren har en rättighet satt på sig.
 * finns det ej, så kontrolleras användarens grupper.
 * Finns det ej några rättigheter satta så kommer användaren att avvisas.
 */
//@Entity
public class ModuleRights{// extends Model{
}