package models.Core;


/**
 * Entitet som hanterar r�ttighetss�ttning f�r grupper och anv�ndare
 * Varje anv�ndare har en Lista av modulerights p� sig, samt varje grupp.
 * Vid intr�de till en module det f�rst kontrolleras om anv�ndaren har en r�ttighet satt p� sig.
 * finns det ej, s� kontrolleras anv�ndarens grupper.
 * Finns det ej n�gra r�ttigheter satta s� kommer anv�ndaren att avvisas.
 */
//@Entity
public class ModuleRights{// extends Model{
}