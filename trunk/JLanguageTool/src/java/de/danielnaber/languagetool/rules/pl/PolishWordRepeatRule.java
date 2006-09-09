/**
 * 
 */
package de.danielnaber.languagetool.rules.pl;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.rules.Category;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * @author Marcin Miłkowski
 * 
 * Rule for detecting same words in the sentence
 * but not just in a row 
 *
 */
public class PolishWordRepeatRule extends PolishRule {

  public PolishWordRepeatRule(ResourceBundle messages) {
    if (messages != null)
      super.setCategory(new Category(messages.getString("category_misc")));
  }
  
	/* (non-Javadoc)
	 * @see de.danielnaber.languagetool.rules.Rule#getId()
	 */
	@Override
	public String getId() {
		return "PL_WORD_REPEAT";
	}

	/* (non-Javadoc)
	 * @see de.danielnaber.languagetool.rules.Rule#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Powtórzenia wyrazów w zdaniu (monotonia stylistyczna)";
	}


	/* (non-Javadoc)
	 * @see de.danielnaber.languagetool.rules.Rule#match(de.danielnaber.languagetool.AnalyzedSentence)
	 */
	@Override
	public RuleMatch[] match(AnalyzedSentence text) {
	    List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
	    AnalyzedTokenReadings[] tokens = text.getTokens();
	    boolean repetition = false;
	    //boolean hasLemma = true;
	    List<String> inflectedWords = new ArrayList<String>();
	    String prevLemma, curLemma;
	    int pos = 0;
	    for (int i = 0; i < tokens.length; i++) {
	      String token = tokens[i].getToken();
	      if (token.trim().equals("")) {
	        // ignore
	      } else {
	        // avoid "..." etc. to be matched:
	        boolean isWord = true;
	        boolean hasLemma = true;
	        if (token.length() == 1) {
	          char c = token.charAt(0);
		  // Polish '\u0347' is not classified as letter by isLetter
	          if (!Character.isLetter(c)) {
	            isWord = false;
	          }
	        }
	        for (int k = 0; k < tokens[i].getReadingsLength(); k++) {
	        	String posTag = tokens[i].getAnalyzedToken(k).getPOSTag();
	        	if (posTag != null) {
	        	if (posTag.equals("")) {
	        		isWord = false;
	        		break;
	        	}
           //too many false alarms here:     
                String lemma = tokens[i].getAnalyzedToken(k).getLemma();
                if (Pattern.matches("to|siebie|być|ani|albo|lub|czy|bądź|jako|zł|coraz|bardzo|ten|jak|mln|tys|swój|mój|twój|nasz|wasz|i", lemma)) {
                    isWord = false;
                    break;
                 }
                
	        	if (Pattern.matches("prep:.*|ppron.*", posTag)) {
	        		isWord = false;
	        		break;
	        	 }
                } else {
                    hasLemma = false;
                }
                                       		    
	        }
	        // Roman numbers regexp added
	        if (Pattern.matches("nie|&quot|&gt|&lt|&amp|[0-9].*|M*(D?C{0,3}|C[DM])(L?X{0,3}|X[LC])(V?I{0,3}|I[VX])$", tokens[i].getToken())) {
	        	isWord = false;
	        }
	        
	        prevLemma = "";
	        if (isWord) {
	           for (int j=0; j <tokens[i].getReadingsLength(); j++) {
	        	   if (hasLemma) {
	        	   curLemma = tokens[i].getAnalyzedToken(j).getLemma();
	        	   if (!prevLemma.equals(curLemma)) {
	        	   if (inflectedWords.contains(curLemma)){
	        		   repetition = true;
      	       	   } else {	        			   	           
      	       		   inflectedWords.add(tokens[i].getAnalyzedToken(j).getLemma());
      	       	   }
	        	   }
	        	   prevLemma = curLemma;
	        	   } else {
	        		   if (inflectedWords.contains(tokens[i].getToken())) {
	        			   repetition = true;
	        		   } else {
	        			   inflectedWords.add(tokens[i].getToken());
	        		   }
	        	   }
	        	   
	           }
	        }
	        
	         if (repetition) {
	          String msg = "Powtórzony wyraz w zdaniu";
	          RuleMatch ruleMatch = new RuleMatch(this, pos, pos+token.length(), msg);
	          ruleMatch.setSuggestedReplacement(tokens[i].getToken());
	          ruleMatches.add(ruleMatch);
	          repetition = false;
	        }
	      }
	      pos += token.length();
	    }
	    return toRuleMatchArray(ruleMatches);
	  }
	

	/* (non-Javadoc)
	 * @see de.danielnaber.languagetool.rules.Rule#reset()
	 */
	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

}