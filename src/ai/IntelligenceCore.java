package footsiebot.ai;

import footsiebot.nlp.ParseResult;
import footsiebot.nlp.Intent;
import footsiebot.nlp.TimeSpecifier;
import footsiebot.database.*;

import java.util.*;
import java.lang.*;
/**
 * Intelligence Core class. Global fields are the number of top companies (extendable to be set by the user)
 * list of companies and groups, the database , the start up hour for the summary, and a one-element stack
 * to keep track of the last suggestion given.
 */
public class IntelligenceCore implements IIntelligenceUnit {


   private byte TOP = 5;// To be possibly set by the user
   private ArrayList<Company> companies;
   private ArrayList<Group> groups;
   private double startupHour;
   private Suggestion lastSuggestion;
   private IDatabaseManager db;
   /**
    * Public constructor
    * @param  IDatabaseManager db            the database
    * @return                  an instance of this class
    */
   public IntelligenceCore(IDatabaseManager db) {
     this.db = db;
     onStartUp();
   }

   /**
    * Main suggestion method. Decides whether it is appropriate to make a suggestion and , if so,
    * returns it to the core.
    * @param  ParseResult pr            data from the user
    * @return             a suggestion object that contains the information for the user
    */
   public Suggestion getSuggestion(ParseResult pr) {
     // Fetch operand and intent and increment intent priority
     // TODO needs converting to AIIntent

     if(companies == null){
         System.out.println("companies was null, cannot make suggestion");//DEBUG
         return null;
     }
     // intent detect
     AIIntent notToSuggestIntent = null;
     Intent oldIntent = pr.getIntent();
     //System.out.println("User has just asked : " + oldIntent);
     boolean doNotSuggestNews = false;
     switch (oldIntent) {
       case SPOT_PRICE: notToSuggestIntent = AIIntent.SPOT_PRICE;
       break;
       case OPENING_PRICE : notToSuggestIntent = AIIntent.OPENING_PRICE;
       break;
       case CLOSING_PRICE : notToSuggestIntent = AIIntent.CLOSING_PRICE;
       break;
       case PERCENT_CHANGE : notToSuggestIntent = AIIntent.PERCENT_CHANGE;
       break;
       case ABSOLUTE_CHANGE : notToSuggestIntent = AIIntent.ABSOLUTE_CHANGE;
       break;
       case TREND: notToSuggestIntent = AIIntent.TREND;
       break;
       case TRADING_VOLUME: notToSuggestIntent = AIIntent.TRADING_VOLUME;
       break;
       case NEWS : doNotSuggestNews = true;
       break;
     }

     String companyOrGroup = pr.getOperand();
     Group targetGroup = null;
     Company targetCompany = null;
     // If operand is a GROUP
     if(pr.isOperandGroup()) {
         // DOES NOT MAKE SENSE TO SUGGEST FOR GROUPS
        return null;
     } else {
       // operand is a company
       for(Company c: companies) {
         if(c.getCode().equals(companyOrGroup)) {
           targetCompany = c;
           break;
         }
       }
       if(targetCompany == null) {
           System.out.println("No company found for suggestion making");//DEBUG
           return null;
        }

       boolean doSuggestion = false;
       for(int i = 0; i < companies.size(); i++) {
         if(targetCompany.equals(companies.get(i))) {
           doSuggestion = true;
         }
       }

       if(doSuggestion) {

         // DECIDING WHETHER to suggest news
         float newsPriority =  targetCompany.getNewsPriority();

         AbstractMap.SimpleEntry<AIIntent,Float> topIntentData = targetCompany.getTopIntent(notToSuggestIntent);
         AIIntent topIntent = topIntentData.getKey();
         Float topIntentPriority = topIntentData.getValue();

         if(topIntentPriority > newsPriority || doNotSuggestNews) {
           lastSuggestion = suggestIntent(targetCompany,topIntent);
         } else {
           System.out.println("Suggesting news for " + targetCompany.getCode());
           lastSuggestion = suggestNews(targetCompany);
         }
         return lastSuggestion;
         // return Group to Core
       } else {
           System.out.println("Decided not to make a suggestion");//DEBUG
         return null;
       }
     }
   }

   /**
    * Gets the updated companies and groups data. Also calls @detectedImportantChange
    * to verify whether some companies had a significant change. If so, it returns a sugegstion
    * array to core.
    * @param  Float threshold
    * @return       an array of suggestions
    */
   public Suggestion[] onUpdatedDatabase(Float threshold) {
     companies = db.getAICompanies();
     groups = db.getAIGroups();
     // DEBUG
     if(companies == null) {
         System.out.println("No companies to update.");
         return null;
     }
    //  Collections.sort(companies);
     Collections.sort(companies, Collections.reverseOrder());
     if(groups == null) {
       // DEBUG
       System.out.println("GROUPS ARE NULL");
       return null;
     }
     //Collections.sort(groups);
     Collections.sort(groups, Collections.reverseOrder());

     ArrayList<Company> changed = detectedImportantChange(threshold);
     if((changed == null ) || (changed.size() == 0)) return null;

     ArrayList<Suggestion> res = new ArrayList<>();

     for(Company c: changed) {

       System.out.println("Company " + c.getCode() + "has had a significant change ");
       res.add(new Suggestion("Detected important change", c, false, new ParseResult(Intent.PERCENT_CHANGE,"Significant change!",c.getCode(),false,TimeSpecifier.TODAY)));
     }

     return res.toArray(new Suggestion[res.size()]);
   }
   /**
    * Fetches data from the database at startup of the program
    * and sort companies and groups by priority
    */
   public void onStartUp() {
     // Fetch from database
     companies = db.getAICompanies();
     groups = db.getAIGroups();
     if(companies != null){
         //Collections.sort(companies);
         Collections.sort(companies, Collections.reverseOrder());
     }
     if(groups  != null){
         //Collections.sort(groups);
         Collections.sort(groups, Collections.reverseOrder());
     }
   }

   /**
    * User has reported that a suggestion has not been relevant so
    * adjusts weights accordingly
    * Will decrement the priorities both locally and in the database by calling
    * @onSuggestionIrrelevant in DatabaseCore
    * @param  String companyOrGroup
    * @return
    */

   public void onSuggestionIrrelevant(Suggestion s) {
      AIIntent intent;
      boolean isNews = s.isNews();
      // company
      if(s.getCompany() != null) {

        Company c = s.getCompany();
        ParseResult pr = s.getParseResult();

        switch(pr.getIntent()) {
          case SPOT_PRICE : intent = AIIntent.SPOT_PRICE;
          break;
          case OPENING_PRICE : intent = AIIntent.OPENING_PRICE;
          break;
          case CLOSING_PRICE : intent = AIIntent.CLOSING_PRICE;
          break;
          case PERCENT_CHANGE : intent = AIIntent.PERCENT_CHANGE;
          break;
          case ABSOLUTE_CHANGE : intent = AIIntent.ABSOLUTE_CHANGE;
          break;
          case TREND : intent = AIIntent.TREND;
          break;
          case TRADING_VOLUME : intent = AIIntent.TRADING_VOLUME;
          break;
          default : intent = null;
        }
        System.out.println("Priority is "+ c.getPriority());
        c.decrementPriorityOfIntent(intent,isNews);
        db.onSuggestionIrrelevant(c, intent, isNews);
        System.out.println("Priority is now "+ c.getPriority());
      }

      return;

   }

   /**
    * Returns an array of the most important companies for the full summary
    * @return array of top companies
    */
   public Company[] onNewsTime() {
     // show report about 5 top companies
     // just returns the companies to core ?
     if(companies == null){
         return null;
     }
     Company[] result = new Company[TOP];
     for(int i = 0; i < TOP; i++) {
       result[i] = companies.get(i);
     }
	   return result;
   }

   //Unused in present version
   public Group[] onNewsTimeGroups() {
     Group[] result = new Group[TOP];
     for(int i = 0; i < TOP; i++) {
       result[i] = groups.get(i);
     }
    return result;
   }

   /**
    * Checks whether there is any company for which a significant percentage change occured.
    * If so, returns the list of such companies to Core.
    * @param  Float treshold      the threshold for the percentage change
    * @return       list of companies
    */
   private ArrayList<Company> detectedImportantChange(Float treshold) {
     ArrayList<String> names = db.detectedImportantChange(treshold);
     if((names == null)||(names.size() == 0)) return null;

     ArrayList<Company> winningCompanies = new ArrayList<>();

     for(String s: names) {
       for(Company c: companies) {
         if(s.equals(c.getCode())) {
           winningCompanies.add(c);
         }
       }
     }

     return winningCompanies;
   }

   /**
    * Utility method to create the suggestion object in case the suggestion is for an intent
    * (os opposed to news )
    * @param  Company company       The company for which the suggestion will be made
    * @return the suggestion object for core
    */
   private Suggestion suggestIntent(Company company ,AIIntent topIntent) {
     String reason = "Company is in top 5";
     // String description = "Suggesting ";

     // Create IParseResult
     TimeSpecifier tm = footsiebot.nlp.TimeSpecifier.TODAY;
     if(topIntent == AIIntent.CLOSING_PRICE) {
       tm = footsiebot.nlp.TimeSpecifier.YESTERDAY;
     }

     Intent i = null;

     switch(topIntent) {
       case SPOT_PRICE : i = footsiebot.nlp.Intent.SPOT_PRICE;
       break;
       case OPENING_PRICE : i = footsiebot.nlp.Intent.OPENING_PRICE;
       break;
       case CLOSING_PRICE : i = footsiebot.nlp.Intent.CLOSING_PRICE;
       break;
       case PERCENT_CHANGE : i = footsiebot.nlp.Intent.PERCENT_CHANGE;
       break;
       case ABSOLUTE_CHANGE : i = footsiebot.nlp.Intent.ABSOLUTE_CHANGE;
       break;
       case TREND : i = footsiebot.nlp.Intent.TREND;
       break;
       case TRADING_VOLUME : i = footsiebot.nlp.Intent.TRADING_VOLUME;
       break;
     }

     if(i == null) return null;

     ParseResult pr = new ParseResult(i, "", company.getCode(), false, tm);
     reason = "You have asked for the "+i.toString().toLowerCase().replace("_"," ");
     reason += " of this company quite a lot recently.";
     // false == suggestion is not news
     Suggestion result = new Suggestion(reason, company, false, pr);
     return result;
   }
   /**
    * Utility method to create suggestion in case is news
    * @param  Company company  the company for which the suggestion has to be made
    * @return         suggestion object for news
    */
   private Suggestion suggestNews(Company company) {
     String reason = "You have asked for news on this company quite a lot recently.";
     ParseResult pr = new ParseResult(footsiebot.nlp.Intent.NEWS, "", company.getCode(), false, footsiebot.nlp.TimeSpecifier.TODAY);
     Suggestion result = new Suggestion(reason, company, true, pr);
     return result;
   }


}
