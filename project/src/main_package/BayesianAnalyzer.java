package main_package;


/*public class FeatureProcessing {
}

*/

/****************************************************************
* Licensed to the Apache Software Foundation (ASF) under one   *
* or more contributor license agreements.  See the NOTICE file *
* distributed with this work for additional information        *
* regarding copyright ownership.  The ASF licenses this file   *
* to you under the Apache License, Version 2.0 (the            *
* "License"); you may not use this file except in compliance   *
* with the License.  You may obtain a copy of the License at   *
*                                                              *
*   http://www.apache.org/licenses/LICENSE-2.0                 *
*                                                              *
* Unless required by applicable law or agreed to in writing,   *
* software distributed under the License is distributed on an  *
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
* KIND, either express or implied.  See the License for the    *
* specific language governing permissions and limitations      *
* under the License.                                           *
****************************************************************/

//Revised from apache james

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;

import java.io.Reader;
//import java.io.StreamTokenizer;
//import java.io.StringReader;
//
//import main_package.BayesianAnalyzer.TokenProbabilityStrength;

/**
* <P>Determines probability that text contains Spam.</P>
*
* <P>Based upon Paul Grahams' <a href="http://www.paulgraham.com/spam.html">A Plan for Spam</a>.
* Extended to Paul Grahams' <a href="http://paulgraham.com/better.html">Better Bayesian Filtering</a>.</P>
*
* <P>Sample method usage:</P>
*
* <P>Use:
*   void addHam(Reader)
*   and
*   void addSpam(Reader)
*
*   methods to build up the Maps of ham & spam tokens/occurrences.
*   Both addHam and addSpam assume they're reading one message at a time,
*   if you feed more than one message per call, be sure to adjust the
*   appropriate message counter:  hamMessageCount or spamMessageCount.
*
*   Then...</P>
*
* <P>Use:
*   void buildCorpus()
*
*   to build the final token/probabilities Map.
*
*   Use your own methods for persistent storage of either the individual
*   ham/spam corpus & message counts, and/or the final corpus.
*
*   Then you can...</P>
*
* <P>Use:
*   double computeSpamProbability(Reader)
*
*   to determine the probability that a particular text contains spam.
*   A returned result of 0.9 or above is an indicator that the text was
*   spam.</P>
*
* <P>If you use persistent storage, use:
*   void setCorpus(Map)
*
* before calling computeSpamProbability.</P>
*
* @version CVS $Revision: $ $Date: $
* @since 2.3.0
*/

public class BayesianAnalyzer {
 
 /**
  * Number of "interesting" tokens to use to compute overall
  * spamminess probability.
  */
 private final static int MAX_INTERESTING_TOKENS = 15;
 
 /**
  * Minimum probability distance from 0.5 to consider a token "interesting" to use to compute overall
  * spamminess probability.
  */
 private final static double INTERESTINGNESS_THRESHOLD = 0.46;
 
 /**
  * Default token probability to use when a token has not been
  * encountered before.
  */
 private final static double DEFAULT_TOKEN_PROBABILITY = 0.4;
 
 /**
  * Map of ham tokens and their occurrences.
  *
  * String key
  * Integer value
  */
 private Map<String, Integer> hamTokenCounts = new HashMap<String, Integer>();
 
 /**
  * Map of spam tokens and their occurrences.
  *
  * String key
  * Integer value
  */
 private Map<String, Integer> spamTokenCounts = new HashMap<String, Integer>();
 
 /**
  * Number of ham messages analyzed.
  */
 private int hamMessageCount = 0;
 
 /**
  * Number of spam messages analyzed.
  */
 private int spamMessageCount = 0;
 
 /**
  * Final token/probability corpus.
  *
  * String key
  * Double value
  */
 private Map<String, Double> corpus = new HashMap<String, Double>();
 
 /**
  * Inner class for managing Token Probability Strengths during the
  * computeSpamProbability phase.
  *
  * By probability <i>strength</i> we mean the absolute distance of a
  * probability from the middle value 0.5.
  *
  * It implements Comparable so that it's sorting is automatic.
  */
 public class TokenProbabilityStrength
 implements Comparable<Object> {
     /**
      * Message token.
      */
     String token = null;
     
     /**
      * Token's computed probability strength.
      */
     double strength = Math.abs(0.5 - DEFAULT_TOKEN_PROBABILITY);
     
     /**
      * Force the natural sort order for this object to be high-to-low.
      *
      * @param anotherTokenProbabilityStrength A TokenProbabilityStrength instance to compare
      *                                this instance with.
      *
      * @return The result of the comparison (before, equal, after).
      */
     public final int compareTo(Object anotherTokenProbabilityStrength) {
         int result = (int) ((((TokenProbabilityStrength) anotherTokenProbabilityStrength).strength - strength) * 1000000);
         if (result == 0) {
             return this.token.compareTo(((TokenProbabilityStrength) anotherTokenProbabilityStrength).token);
         } else {
             return result;
         }
     }
     
     /**
      * Simple toString () implementation mostly for debugging purposes.
      *
      * @return String representation of this object.
      */
     public String toString() {
         StringBuffer sb = new StringBuffer(30);
         
         sb.append(token)
         .append("=")
         .append(strength);
         
         return sb.toString();
     }
 }
 
 /**
  * Basic class constructor.
  */
 public BayesianAnalyzer() {
 }
 
 /**
  * Public setter for the hamTokenCounts Map.
  *
  * @param hamTokenCounts The new ham Token counts Map.
  */
 public void setHamTokenCounts(Map<String, Integer> hamTokenCounts) {
     this.hamTokenCounts = hamTokenCounts;
 }
 
 /**
  * Public getter for the hamTokenCounts Map.
  */
 public Map<String, Integer> getHamTokenCounts() {
     return this.hamTokenCounts;
 }
 
 /**
  * Public setter for the spamTokenCounts Map.
  *
  * @param spamTokenCounts The new spam Token counts Map.
  */
 public void setSpamTokenCounts(Map<String, Integer> spamTokenCounts) {
     this.spamTokenCounts = spamTokenCounts;
 }
 
 /**
  * Public getter for the spamTokenCounts Map.
  */
 public Map<String, Integer> getSpamTokenCounts() {
     return this.spamTokenCounts;
 }
 
 /**
  * Public setter for spamMessageCount.
  *
  * @param spamMessageCount The new spam message count.
  */
 public void setSpamMessageCount(int spamMessageCount) {
     this.spamMessageCount = spamMessageCount;
 }
 
 /**
  * Public getter for spamMessageCount.
  */
 public int getSpamMessageCount() {
     return this.spamMessageCount;
 }
 
 /**
  * Public setter for hamMessageCount.
  *
  * @param hamMessageCount The new ham message count.
  */
 public void setHamMessageCount(int hamMessageCount) {
     this.hamMessageCount = hamMessageCount;
 }
 
 /**
  * Public getter for hamMessageCount.
  */
 public int getHamMessageCount() {
     return this.hamMessageCount;
 }
 
 /**
  * Clears all analysis repositories and counters.
  */
 public void clear() {
     corpus.clear();
     
     tokenCountsClear();
     
     hamMessageCount = 0;
     spamMessageCount = 0;
 }
 
 /**
  * Clears token counters.
  */
 public void tokenCountsClear() {
     hamTokenCounts.clear();
     spamTokenCounts.clear();
 }
 
 /**
  * Public setter for corpus.
  *
  * @param corpus The new corpus.
  */
 public void setCorpus(Map<String, Double> corpus) {
     this.corpus = corpus;
 }
 
 /**
  * Public getter for corpus.
  */
 public Map<String, Double> getCorpus() {
     return this.corpus;
 }
 
 /**
  * Builds the corpus from the existing ham & spam counts.
  */
 public void buildCorpus() {
     //Combine the known ham & spam tokens.
     Set<String> set = new HashSet<String>(hamTokenCounts.size() + spamTokenCounts.size());
     set.addAll(hamTokenCounts.keySet());
     set.addAll(spamTokenCounts.keySet());
     Map<String, Double> tempCorpus = new HashMap<String, Double>(set.size());
     
     //Iterate through all the tokens and compute their new
     //individual probabilities.
     Iterator<String> i = set.iterator();
     while (i.hasNext()) {
         String token = (String) i.next();
         tempCorpus.put(token, new Double(computeProbability(token)));
     }
     setCorpus(tempCorpus);
 }
 
 /**
  * Adds a message to the ham list.
  * @param stream A reader stream on the ham message to analyze
  * @throws IOException If any error occurs
  */
 public void addHam(Reader stream)
 throws java.io.IOException {
     addTokenOccurrences(stream, hamTokenCounts);
     hamMessageCount++;
 }
 
 /**
  * Adds a message to the spam list.
  * @param stream A reader stream on the spam message to analyze
  * @throws IOException If any error occurs
  */
 public void addSpam(Reader stream)
 throws java.io.IOException {
     addTokenOccurrences(stream, spamTokenCounts);
     spamMessageCount++;
 }
 
 /**
  * Computes the probability that the stream contains SPAM.
  * @param stream The text to be analyzed for Spamminess.
  * @return A 0.0 - 1.0 probability
  * @throws IOException If any error occurs
  */
 public double computeSpamProbability(Reader stream)
 throws java.io.IOException {
     //Build a set of the tokens in the Stream.
     Set<String> tokens = parse(stream);
     
     // Get the corpus to use in this run
     // A new corpus may be being built in the meantime
     Map<String, Double> workCorpus = getCorpus();
     
     //Assign their probabilities from the Corpus (using an additional
     //calculation to determine spamminess).
     SortedSet<TokenProbabilityStrength> tokenProbabilityStrengths = getTokenProbabilityStrengths(tokens, workCorpus);
     
     //Compute and return the overall probability that the
     //stream is SPAM.
     return computeOverallProbability(tokenProbabilityStrengths, workCorpus);
 }
 
 /**
  * Parses a stream into tokens, and updates the target Map
  * with the token/counts.
  *
  * @param stream
  * @param target
  */
 private void addTokenOccurrences(Reader stream, Map<String, Integer> target)
 throws java.io.IOException {
     String token;
     String header = "";
     
     //Update target with the tokens/count encountered.
     while ((token = nextToken(stream)) != null) {
         boolean endingLine = false;
         if (token.length() > 0 && token.charAt(token.length() - 1) == '\n') {
             endingLine = true;
             token = token.substring(0, token.length() - 1);
         }
         
         if (token.length() > 0 && header.length() + token.length() < 90 && !allDigits(token)) {
             if (token.equals("From:")
             || token.equals("Return-Path:")
             || token.equals("Subject:")
             || token.equals("To:")
             ) {
                 header = token;
                 if (!endingLine) {
                     continue;
                 }
             }
             
             token = header + token;
             
             Integer value = null;
             
             if (target.containsKey(token)) {
                 value = new Integer(((Integer) target.get(token)).intValue() + 1);
             } else {
                 value = new Integer(1);
             }
             
             target.put(token, value);
         }
         
         if (endingLine) {
             header = "";
         }
     }
 }
 
 /**
  * Parses a stream into tokens, and returns a Set of
  * the unique tokens encountered.
  *
  * @param stream
  * @return Set
  */
 private Set<String> parse(Reader stream)
 throws java.io.IOException {
     Set<String> tokens = new HashSet<String>();
     String token;
     String header = "";
     
     //Build a Map of tokens encountered.
     while ((token = nextToken(stream)) != null) {
         boolean endingLine = false;
         if (token.length() > 0 && token.charAt(token.length() - 1) == '\n') {
             endingLine = true;
             token = token.substring(0, token.length() - 1);
         }
         
         if (token.length() > 0 && header.length() + token.length() < 90 && !allDigits(token)) {
             if (token.equals("From:")
             || token.equals("Return-Path:")
             || token.equals("Subject:")
             || token.equals("To:")
             ) {
                 header = token;
                 if (!endingLine) {
                     continue;
                 }
             }
             
             token = header + token;
             
             tokens.add(token);
         }
         
         if (endingLine) {
             header = "";
         }
     }
     
     //Return the unique set of tokens encountered.
     return tokens;
 }
 
 private String nextToken(Reader reader) throws java.io.IOException {
     StringBuffer token = new StringBuffer();
     int i;
     char ch, ch2;
     boolean previousWasDigit = false;
     boolean tokenCharFound = false;
     
     if (!reader.ready()) {
         return null;
     }
     
     while ((i = reader.read()) != -1) {
         
         ch = (char) i;
         
         if (ch == ':') {
             String tokenString = token.toString() + ':';
             if (tokenString.equals("From:")
             || tokenString.equals("Return-Path:")
             || tokenString.equals("Subject:")
             || tokenString.equals("To:")
             ) {
                 return tokenString;
             }
         }
         
         if (Character.isLetter(ch)
         || ch == '-'
         || ch == '$'
         || ch == '\u20AC' // the EURO symbol
         || ch == '!'
         || ch == '\''
         ) {
             tokenCharFound = true;
             previousWasDigit = false;
             token.append(ch);
         } else if (Character.isDigit(ch)) {
             tokenCharFound = true;
             previousWasDigit = true;
             token.append(ch);
         } else if (previousWasDigit && (ch == '.' || ch == ',')) {
             reader.mark(1);
             previousWasDigit = false;
             i = reader.read();
             if (i == -1) {
                 break;
             }
             ch2 = (char) i;
             if (Character.isDigit(ch2)) {
                 tokenCharFound = true;
                 previousWasDigit = true;
                 token.append(ch);
                 token.append(ch2);
             } else {
                 reader.reset();
                 break;
             }
         } else if (ch == '\r') {
             // cr found, ignore
         } else if (ch == '\n') {
             // eol found
             tokenCharFound = true;
             previousWasDigit = false;
             token.append(ch);
             break;
         } else if (tokenCharFound) {
             break;
         }
     }
     
     if (tokenCharFound) {
         //          System.out.println("Token read: " + token);
         return token.toString();
     } else {
         return null;
     }
 }
 
 /**
  * Compute the probability that "token" is SPAM.
  *
  * @param token
  * @return  The probability that the token occurs within spam.
  */
 private double computeProbability(String token) {
     double hamFactor  = 0;
     double spamFactor = 0;
     
     boolean foundInHam = false;
     boolean foundInSpam = false;
     
     double minThreshold = 0.01;
     double maxThreshold = 0.99;
     
     if (hamTokenCounts.containsKey(token)) {
         foundInHam = true;
     }
     
     if (spamTokenCounts.containsKey(token)) {
         foundInSpam = true;
     }
     
     if (foundInHam) {
         hamFactor = 2 *((Integer) hamTokenCounts.get(token)).doubleValue();
         if (!foundInSpam) {
             minThreshold = (hamFactor > 20) ? 0.0001 : 0.0002;
         }
     }
     
     if (foundInSpam) {
         spamFactor = ((Integer) spamTokenCounts.get(token)).doubleValue();
         if (!foundInHam) {
             maxThreshold = (spamFactor > 10) ? 0.9999 : 0.9998;
         }
     }
     
     if ((hamFactor + spamFactor) < 5) {
         //This token hasn't been seen enough.
         return 0.4;
     }
     
     double spamFreq = Math.min(1.0, spamFactor / spamMessageCount);
     double hamFreq = Math.min(1.0, hamFactor / hamMessageCount);
     
     return Math.max(minThreshold, Math.min(maxThreshold, (spamFreq / (hamFreq + spamFreq))));
 }
 
 /**
  * Returns a SortedSet of TokenProbabilityStrength built from the
  * Corpus and the tokens passed in the "tokens" Set.
  * The ordering is from the highest strength to the lowest strength.
  *
  * @param tokens
  * @param workCorpus
  * @return  SortedSet of TokenProbabilityStrength objects.
  */
 private SortedSet<TokenProbabilityStrength> getTokenProbabilityStrengths(Set<String> tokens, Map<String, Double> workCorpus) {
     //Convert to a SortedSet of token probability strengths.
     SortedSet<TokenProbabilityStrength> tokenProbabilityStrengths = new TreeSet<TokenProbabilityStrength>();
     
     Iterator<String> i = tokens.iterator();
     while (i.hasNext()) {
         TokenProbabilityStrength tps = new TokenProbabilityStrength();
         
         tps.token = (String) i.next();
         
         if (workCorpus.containsKey(tps.token)) {
             tps.strength = Math.abs(0.5 - ((Double) workCorpus.get(tps.token)).doubleValue());
         }
         else {
             //This token has never been seen before,
             //we'll give it initially the default probability.
             Double corpusProbability = new Double(DEFAULT_TOKEN_PROBABILITY);
             tps.strength = Math.abs(0.5 - DEFAULT_TOKEN_PROBABILITY);
             boolean isTokenDegeneratedFound = false;
             
             Collection<String> degeneratedTokens = buildDegenerated(tps.token);
             Iterator<String> iDegenerated = degeneratedTokens.iterator();
             String tokenDegenerated = null;
             double strengthDegenerated;
             while (iDegenerated.hasNext()) {
                 tokenDegenerated = (String) iDegenerated.next();
                 if (workCorpus.containsKey(tokenDegenerated)) {
                     Double probabilityTemp = (Double) workCorpus.get(tokenDegenerated);
                     strengthDegenerated = Math.abs(0.5 - probabilityTemp.doubleValue());
                     if (strengthDegenerated > tps.strength) {
                         isTokenDegeneratedFound = true;
                         tps.strength = strengthDegenerated;
                         corpusProbability = probabilityTemp;
                     }
                 }
             }
             // to reduce memory usage, put in the corpus only if the probability is different from (stronger than) the default
             if (isTokenDegeneratedFound) {
                 synchronized(workCorpus) {
                     workCorpus.put(tps.token, corpusProbability);
                 }
             }
         }
         
         tokenProbabilityStrengths.add(tps);
     }
     
     return tokenProbabilityStrengths;
 }
 
 private Collection<String> buildDegenerated(String fullToken) {
     ArrayList<String> tokens = new ArrayList<String>();
     String header;
     String token;
     
     // look for a header string termination
     int headerEnd = fullToken.indexOf(':');
     if (headerEnd >= 0) {
         header = fullToken.substring(0, headerEnd);
         token = fullToken.substring(headerEnd);
     } else {
         header = "";
         token = fullToken;
     }
     
     int end = token.length();
     do {
         if (!token.substring(0, end).equals(token.substring(0, end).toLowerCase())) {
             tokens.add(header + token.substring(0, end).toLowerCase());
             if (header.length() > 0) {
                 tokens.add(token.substring(0, end).toLowerCase());
             }
         }
         if (end > 1 && token.charAt(0) >= 'A' && token.charAt(0) <= 'Z') {
             tokens.add(header + token.charAt(0) + token.substring(1, end).toLowerCase());
             if (header.length() > 0) {
                 tokens.add(token.charAt(0) + token.substring(1, end).toLowerCase());
             }
         }
         
         if (token.charAt(end - 1) != '!') {
             break;
         }
         
         end--;
         
         tokens.add(header + token.substring(0, end));
         if (header.length() > 0) {
             tokens.add(token.substring(0, end));
         }
     } while (end > 0);
     
     return tokens;
 }
 
 /**
  * Compute the spamminess probability of the interesting tokens in
  * the tokenProbabilities SortedSet.
  *
  * @param tokenProbabilities
  * @param workCorpus
  * @return  Computed spamminess.
  */
 private double computeOverallProbability(SortedSet<TokenProbabilityStrength> tokenProbabilityStrengths, Map<String, Double> workCorpus) {
     double p = 1.0;
     double np = 1.0;
     double tempStrength = 0.5;
     int count = MAX_INTERESTING_TOKENS;
     Iterator<TokenProbabilityStrength> iterator = tokenProbabilityStrengths.iterator();
     while ((iterator.hasNext()) && (count-- > 0 || tempStrength >= INTERESTINGNESS_THRESHOLD)) {
         TokenProbabilityStrength tps = (TokenProbabilityStrength) iterator.next();
         tempStrength = tps.strength;
         
         //      System.out.println(tps);
         
         double theDoubleValue = DEFAULT_TOKEN_PROBABILITY; // initialize it to the default
         Double theDoubleObject = (Double) workCorpus.get(tps.token);
         // if either the original token or a degeneration was found use the double value, otherwise use the default
         if (theDoubleObject != null) {
             theDoubleValue = theDoubleObject.doubleValue();
         }
         p *= theDoubleValue;
         np *= (1.0 - theDoubleValue);
         // System.out.println("Token:" + tps.token + ", p=" + theDoubleValue + ", overall p=" + p / (p + np));
     }
     
     return (p / (p + np));
 }
 
 @SuppressWarnings("unused")
private boolean allSameChar(String s) {
     if (s.length() < 2) {
         return false;
     }
     
     char c = s.charAt(0);
     
     for (int i = 1; i < s.length(); i++) {
         if (s.charAt(i) != c) {
             return false;
         }
     }
     return true;
 }
 
 private boolean allDigits(String s) {
     for (int i = 0; i < s.length(); i++) {
         if (!Character.isDigit(s.charAt(i))) {
             return false;
         }
     }
     return true;
 }
}
