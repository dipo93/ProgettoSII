package main_package;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.Comment;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.Video;
import com.google.api.client.util.DateTime;
import com.swabunga.spell.engine.GenericSpellDictionary;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;

//import org.joda.time.*;




public class FeatureProcessing {
	
	/* stores in a csv format all the feature of the comment passed
	 * IMPORTANT: to make a csv file , or ", or \n must not be included in
	 * any field
	 * */
	public static void storeFeatures(Comment comment, Video video, CommentThread cThread, BufferedWriter cp, YouTube youtube) throws IOException {
    	
		//isReply
		if(cThread != null)
    		cp.write("false,");
    	else
    		cp.write("true,");
		
		//verifica se il commento contiene il nome dell'autore del video
		//authorNameInComment
		cp.write(commentContainsVideoAuthorName(comment, video)+ ",");
	
		//verifica se il commento contiene il nome del suo autore
		//commenterNameInComment
		cp.write(searchAuthorNameInComment(comment) + ",");
		
		//verifica se il commento è stato scritto dall'autore del video
		//isCommentFromAuthor
		cp.write(isCommentFromTheVideoAuthor(comment, video) +",");

		//conta il numero massimo di numeri consecutivi nel nome dell'autore del commento
		//numbersInCommenterName
		cp.write(numbersInAuthorName(comment) + ",");
		
		//conta il numero di likes del commento
		//likes
		cp.write(getNumberOfLikes(comment) +",");
		
		//conta il numero di risposte del commento
		//replies
		if (cThread != null)
			cp.write(getNumberOfReplies(cThread) +",");
		else
    		cp.write("null,");
		
		//tempo trascorso tra la pubblicazione del video e del commento
		//daysInterval
		cp.write(intervalBetweenVideoAndCommentPublish(comment, video) + ",");
		
		//conta il massimo numero di parole ripetute consecutivamente
		//repeatedWords
		cp.write(numberRepeatedWords(comment) + ",");

		//conta numero di mention tags e di hashtags
		//mentionTags
		//hashtags
		int[] ss = new int[2];
		ss = numberMentionAndHashtags(comment);
		cp.write(ss[0] + ",");
		cp.write(ss[1] + ",");

		//conta il numero di iscritti al canale dell'autore del commento
		//commenterSubscribers
		String authorId = comment.getSnippet().getAuthorChannelId().getValue();
		cp.write(getChannelSubscribers(youtube, authorId) + ",");

		//verifica se c'è un riferimento a un momento del video
		//videoMomentReference
		cp.write(videoMomentReference(comment) + ",");
		
		//verifica se c'è un link alla home page di qualche sito
		//homePageLink
		cp.write(thereIsHomePageLink(comment) + ",");
		
		//verifica se c'è un link che redireziona a un altro sito
		//redirectingLink
		cp.write(thereIsRedirectingUrl(comment) + ",");
		
		//verifica se c'è un indirizzo IP nel commento
		//IP
		cp.write(isPresentIP(comment) + ",");
		
		//calcola la percentuale di parole corrispondenti al titolo del video rispetto al totale
		//percentageTitleWordsInComment
		cp.write(titleWordsPercentageInComment(comment, video) + ",");
		
		//calcola la percentuale di parole corrispondenti a tag rispetto al totale
		//percentageTagsInComment
		cp.write(videoTagPercentageInComment(comment, video) + ",");
		
		//calcola la percentuale di lettere in caps lock nel commento
		//percentageCapsLock
		cp.write(percentageCapsLock(comment) + ",");
		
		//calcola la percentuale di parole sbagliate rispetto al totale
		//percentageSpellingErrors
		cp.write(spellingErrorPercentage(comment) + ",");
		
		//calcola la percentuale di black words nel commento
		//blackWords
		cp.write(blackWords(comment) + ",");
		
		//textDisplay
		String textElaborated = comment.getSnippet().getTextDisplay().replaceAll(",", " ");
    	textElaborated = textElaborated.replaceAll("\"", " ");
    	textElaborated = textElaborated.replaceAll("\n", " ");
    	cp.write(textElaborated);
		
		cp.write("\n");
		
	}
	



	/*returns true if the comment text contains the name of the author of the video 
	 * IMPORTANT this method can be used for comments and also replies */
	private static boolean commentContainsVideoAuthorName(Comment videoComment, Video video) {
		String text = videoComment.getSnippet().getTextDisplay();
		String author = video.getSnippet().getChannelTitle();
		return text.contains(author);
	}
	
	
	/*returns the like count of the comment 
	 * IMPORTANT this method can be used for comments and also replies */
	private static Long getNumberOfLikes(Comment videoComment) {
		return videoComment.getSnippet().getLikeCount();
	}
	
	
	/*returns the reply count of the comment 
	 * IMPORTANT this method can be used only for comments, because replies don't have
	 * replies*/
	private static Long getNumberOfReplies(CommentThread comment) {
		return comment.getSnippet().getTotalReplyCount();
	}
	
	
	/*returns true if the comment is made from the video author */
	private static boolean isCommentFromTheVideoAuthor(Comment videoComment, Video video) {
		return ((videoComment.getSnippet().getAuthorChannelId().getValue()).equals(video.getSnippet().getChannelId()));
	}
	
	
	/**
	 * calcola l'intervallo di tempo tra la pubblicazione del video e quella del commento
	 * 
	 * @param videoComment il commento
	 * @param video il video
	 * @return una stringa che indica la differenza in giorni, ore, minuti e secondi
	 */
	private static double intervalBetweenVideoAndCommentPublish(Comment videoComment, Video video) {
		DateTime videoDate = video.getSnippet().getPublishedAt();
		DateTime commentDate = videoComment.getSnippet().getPublishedAt();

//		Period period = new Period(videoDate, commentDate);
		double diff = commentDate.getValue() - videoDate.getValue();
		
		double diffDays = diff / (60 * 60 * 1000 * 24);
		
        return diffDays;
	}
	

	/**
	 * conta il numero di mentiontags e hashtags nel commento
	 * 
	 * @param videoComment il commento
	 * @return un array di interi di dimensione 2, 
	 * 		il primo indice è per il mentiontags, il secondo per gli hashtags
	 */
	private static int[] numberMentionAndHashtags(Comment videoComment) {
    	String text = videoComment.getSnippet().getTextDisplay();
    	int tags[] = {0, 0};	//[0] is mention tags (+) and [1] is hashtags (#)
    	
    	Pattern mentions = Pattern.compile("\\+[a-zA-Z0-9_]*");
    	Matcher matcherM = mentions.matcher(text);
    	while(matcherM.find())
    		tags[0]++;
    	
    	Pattern hashtags = Pattern.compile("#[a-zA-Z0-9_-]*");
    	Matcher matcherH = hashtags.matcher(text);
    	while(matcherH.find())
    		tags[1]++;
    	
		return tags;
	}
    
	
    /**
     * With the channelId, returns a String with the subscribers' number
     * 
     * @param youtube l'oggetto youtube, serve a ottenere i dati di un canale
     * @param channeId l'id del canale youtube
     * @return una stringa che indica il numero di iscritti
     */
	public static String getChannelSubscribers(YouTube youtube, String channeId) throws IOException {
		// questo metodo deve essere pubblico perchè serve anche in CommentHandling
    	// Call the YouTube Data API's channels.list method to retrieve channels.
    	ChannelListResponse channelListResponse = youtube.channels().list("statistics")
    			.setId(channeId).execute();
    	List<Channel> channelList = channelListResponse.getItems();

    	if (channelList.isEmpty())
    		return ("Can't find a channel with ID: " + channeId);

    	Channel channel = channelList.get(0);
    	BigInteger subscribers = channel.getStatistics().getSubscriberCount();
    	return subscribers.toString();

    }
    
    
	/**
	 * verifica se nel testo del commento ci sono riferimenti ad un istante del video
	 * 
	 * @param videoComment il commento
	 * @return true se trova un riferimento, false altrimenti
	 */
	private static boolean videoMomentReference(Comment videoComment) {
		String text = videoComment.getSnippet().getTextDisplay();
		
		Pattern pattern = Pattern.compile("([0-9]{1,2}:)?[0-9]{1,2}:[0-9]{2}");
    	Matcher matcher = pattern.matcher(text);
    	boolean res = matcher.find();
		
		return res;
	}
	
	
	/** 
	 * Verca, se c'è, un link nel testo
	 * 
	 * @param text il testo da analizzare
	 * @return un stringa contenente il link trovato, se c'è, altrimenti la stringa vuota
	 */
	private static String findLinkInText(String text) throws IOException {
		Pattern pattern1 = Pattern.compile("https?://[a-zA-Z0-9./-_]*");
		Matcher matcher1 = pattern1.matcher(text);
		
		//web_domains.txt contiene la lista di domini web top-level, il matcher2 cerca link del tipo 'google.com'
		BufferedReader br = new BufferedReader(new FileReader("web_domains.txt"));
		String line = null;
		boolean found = false;
		String url = "";	//l'url trovato nella stringa
		
		//ricerca finchè non trova qualcosa o non finisce l'elenco di domini web top-level
		while ((line = br.readLine()) != null && !found) {
			Pattern pattern2 = Pattern.compile("[a-zA-Z0-9.-_]+" + line + "\\s");
			Matcher matcher2 = pattern2.matcher(text);
			
			if(matcher1.find()){
				url = matcher1.group();
				found = true;
				//l'url matchato viene tolto per evitare che sia ripreso (anche dall'altro if)
				text = matcher1.replaceFirst("");
				//rimette il file da capo
				br.close();
				br = new BufferedReader(new FileReader("web_domains.txt"));
			}
			else if(matcher2.find()){
				url = matcher2.group();
				found = true;
				//se l'url è del tipo 'google.it' o 'www.google.it' gli viene aggiunta la stringa del protocollo davanti
				url = parseUrl(url);
				//l'url matchato viene tolto per evitare che sia ripreso (anche dall'altro if)
				text = matcher2.replaceFirst("");
				//rimette il file da capo
				br.close();
				br = new BufferedReader(new FileReader("web_domains.txt"));
			}
		}
		br.close();
		return url;
	}

	
	/**
	 * Verifica se nel commento c'è un link a una home page di un sito
	 * 
	 * @param videoComment il commento in cui cercare
	 * @return true se esiste un link a una homepage, false altrimenti
	 */
	private static boolean thereIsHomePageLink(Comment videoComment) throws IOException {
		String text = videoComment.getSnippet().getTextDisplay();
		String url = findLinkInText(text);
		boolean homePage = false;
		
		//vado avanti finchè non trovo un home page o finchè non ci sono più url
		while(!homePage && !url.equals("")){
			String newUrl = "";
			
			//se l'url redireziona su un altro sito allora lo espando
			if(isRedirected(url))
				newUrl = expandUrl(url);
			else
				newUrl = url;

			//verifico se il link trovato corrisponde a una homepage
			homePage = isHomePage(newUrl);
			
			//tolgo l'url trovato dal testo e ne cerco un altro
			text = text.replaceFirst(url, "");
			url = findLinkInText(text);
		}
		return homePage;
	}
	
	
	/**
	 * È invocato solo se l'url è del tipo google.it o simili per motivi di parsing successivo
	 * Fa in modo che l'url inizi con 'https://www.'
	 * 
	 * @param l'url da correggere
	 * @return l'url corretto
	 */
	private static String parseUrl(String url) {
		Pattern pattern = Pattern.compile("^www.");
		Matcher matcher = pattern.matcher(url);
		if(matcher.find())
			return "https://" + url;
		return "https://www." + url;
	}
	
	
	/**
	 * Espande un url shortened (ma viene invocato su ogni url trovato)
	 * 
	 * @param shortenedUrl l'url abbreviato da analizzare
	 * @return l'url a cui lo short url redireziona
	 */
	private static String expandUrl(String shortenedUrl) throws IOException {
		//goo.gl  tinyurl.com  ow.ly  bit.ly  u.to  x.co  tr.im		
		//controlla youtu.be
		String expandedURL = null;
		
		try {
			URL url = new URL(shortenedUrl);    
			// open connection
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY); 

			// stop following browser redirect
			httpURLConnection.setInstanceFollowRedirects(false);
			
			httpURLConnection.connect();

			// extract location header containing the actual destination URL
			expandedURL = httpURLConnection.getHeaderField("Location");

			httpURLConnection.getInputStream().close();

		} catch (MalformedURLException e) {
//			e.printStackTrace();
			if(expandedURL == null)
				return shortenedUrl;
		} catch (IOException e) {
//			e.printStackTrace();
			if(expandedURL == null)
				return shortenedUrl;
		}
		
		if(expandedURL == null)
			return shortenedUrl;
		return expandedURL;
	}

	
	/**
	 * Verifica se un url porta sulla homepage di unn sito
	 * 
	 * @param url l'url da analizzare
	 * @return true se l'url è relativo a una homepage, false altrimenti
	 */
	private static boolean isHomePage(String url) {
		// se c'è uno / dopo l'url è probabile non sia una homepage
		Pattern pattern1 = Pattern.compile("https?://[a-zA-Z0-9.-_]*/\\w+");
		Matcher matcher1 = pattern1.matcher(url);
		
		// se finisce con /index.html o simili è una homepage
		Pattern pattern2 = Pattern.compile("(https?://)?.*/index.[a-z]{2,5}");
		Matcher matcher2 = pattern2.matcher(url);

		return !matcher1.find() || matcher2.find();
	}
	
	
	/**
	 * Verifica se nel commento c'è un url con redirezione
	 * 
	 * @param videoComment il commento 
	 * @return true se trova un link con redirezione, false altrimenti
	 */
	private static boolean thereIsRedirectingUrl(Comment videoComment) throws IOException {
		//cerca un link nel commento
		String link = findLinkInText(videoComment.getSnippet().getTextDisplay());
		
		//se non c'è nessun link ritorno subito false
		if(link.equals(""))
			return false;
		
		//se il link c'è faccio il controllo
		boolean isRedirected = isRedirected(link);
		
		return isRedirected;
	}
	

	/**
	 * Verifica se un link redireziona su un altro sito
	 * 
	 * @param link il link da analizzare
	 * @return true se il link è redirezionato, false altrimenti
	 */
	private static boolean isRedirected(String link) throws IOException {
		URL url = new URL(link); 
		
		// open connection
		HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY); 

		// stop following browser redirect
		httpURLConnection.setInstanceFollowRedirects(false);

		try{
			httpURLConnection.connect();
		} catch (IOException e) {
            e.printStackTrace();
            return false;
        }

		// extract response code
		int respCode = httpURLConnection.getResponseCode();
		boolean isRedirected = false;

		if(respCode == 301 || respCode == 302 || respCode == 307)
			isRedirected = true;

		httpURLConnection.disconnect();
		
		return isRedirected;
	}


	/**
	 * Conta il numero di parole del titolo che compaiono anche nel commento
	 * 
	 * @param videoComment il commento
	 * @param video il video da cui estrarre il titolo
	 * @return il numero di parole in comune
	 */
	private static long titleWordsNumberInComment(Comment videoComment, Video video) {
		String title = video.getSnippet().getTitle();
		String text = videoComment.getSnippet().getTextDisplay();
		//tutto ciò che non è una parola viene trasformato in uno spazio
		text = text.replaceAll("\\W+", " ");
		//trasformo il titolo in un array di parole, il divisore è lo spazio
		String[] words = title.split(" ");
		int matches = 0;
		for (String word : words)
			if (text.contains(word))
				matches++;
		return matches;
	}
	
	
	/**
	 * Calcola la percentuale di parole del commento che corrispondono a parole del titolo
	 * 
	 * @param videoComment il commento
	 * @param video il video da cui estrarre il titolo
	 * @return la percentuale
	 */
	private static double titleWordsPercentageInComment(Comment videoComment, Video video) {
		double wordNum = countWords(videoComment.getSnippet().getTextDisplay());
		double matchNum = titleWordsNumberInComment(videoComment, video);
		return (matchNum/wordNum)*100;
	}

	
	/**
	 * Conta il numero di parole del tag del video che compaiono anche nel commento
	 * 
	 * @param videoComment il commento
	 * @param video il video da cui prendere i tag
	 * @return il numero di parole in comune
	 */
	private static long videoTagNumberInComment(Comment videoComment, Video video) {
		String text = videoComment.getSnippet().getTextDisplay();
		//tutto ciò che non è una parola viene trasformato in uno spazio
		text = text.replaceAll("\\W+", " ");
		List<String> tags = video.getSnippet().getTags();
		long tagNum = 0;
		if(tags != null)
			for (String tag : tags)
				if (text.contains(tag))
					tagNum++;
		return tagNum;
	}
	
	
	/**
	 * Calcola la percentuale di parole del commento che corrispondono a tag del video
	 * 
	 * @param videoComment il commento
	 * @param video il video da cui estrarre i tag
	 * @return la percentuale
	 */
	private static double videoTagPercentageInComment(Comment videoComment, Video video) {
		double wordNum = countWords(videoComment.getSnippet().getTextDisplay());
		double tagNum = videoTagNumberInComment(videoComment, video);
		return (tagNum/wordNum)*100;
	}
	
	
	/**
	 * Calcola il numero di errori di grammatica presenti nel commento
	 * 
	 * @param videoComment il commento
	 * @return il numero di errori
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private static double spellingErrorNumber(Comment videoComment) throws FileNotFoundException, IOException {
		String text = videoComment.getSnippet().getTextDisplay();
		File dictionary = new File("dictionarySmall.txt");
		GenericSpellDictionary dict = new GenericSpellDictionary(dictionary);
		SpellChecker sp = new SpellChecker(dict);
		StringWordTokenizer swt = new StringWordTokenizer(text);
		double errors = sp.checkSpelling(swt);
		if(errors > 0)
			return errors;
		else
			return 0;
	}


	/**
	 * Calcola la percentuale di parole del commento con errori grammaticali
	 * 
	 * @param videoComment il commento
	 * @return la percentuale
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private static double spellingErrorPercentage(Comment videoComment) throws FileNotFoundException, IOException {
		double wordNum = countWords(videoComment.getSnippet().getTextDisplay());
		double errNum = spellingErrorNumber(videoComment);
		return (errNum/wordNum)*100;
	}
	

	/**
	 * Conta il numero di parole di un testo
	 * 
	 * @param text il testo
	 * @return il numero di parole
	 */
	private static double countWords(String text) {
		text = text.replaceAll("^\\W+", "");
		text = text.replaceAll("\\W+$", "");
		double words = 1;
		Pattern pattern = Pattern.compile("\\W+");
		Matcher matcher = pattern.matcher(text);
		while(matcher.find())
    		words++;
		return words;
	}
	

	/* returns the number of black words (from a .txt) contained in the comment text
	 * the .txt contains the words separated by a \n (each word is on a single line)*/
	public static double blackWords(Comment comment) throws IOException {
		String text = comment.getSnippet().getTextDisplay();
		List<String> bw = createBlackWordsList("black-words.txt");

		double numberBW = 0;
//		double totalWords = countWords(text);

		text = text.replaceAll("^\\W+", "");
		text = text.replaceAll("\\W+$", "");

		for (String b : bw) {

			Pattern pattern = Pattern.compile("[a-zA-Z0-9]*");
			Matcher matcher = pattern.matcher(text);

			while (matcher.find()) {
				if (matcher.group().toLowerCase().equals(b.toLowerCase())) {
					double numberBlackWordsInLine = countWords(b);
					numberBW = numberBW + numberBlackWordsInLine;
				}
			}
		}
//		return (numberBW * 100) / totalWords;
		return numberBW;
	}


	/* retrieves black words from a list in a .txt and creates a String list
	 * */
	public static List<String> createBlackWordsList(String textPath) throws IOException {
		Scanner s = new Scanner(new File(textPath));
		ArrayList<String> blackWords = new ArrayList<String>();
		while (s.hasNextLine()){
			blackWords.add(s.nextLine());
		}
		s.close();
		return blackWords;
	}

	
	/* return the max lenght of number sequence in the author's comment name
	 */
	public static int numbersInAuthorName(Comment comment) {
		String name = comment.getSnippet().getAuthorDisplayName();
		int i = 0;

		Pattern pattern = Pattern.compile("\\d+");
		Matcher matcher = pattern.matcher(name);

		while(matcher.find()) {
			if (matcher.group().length() > i)
				i = matcher.group().length();
		}
		return i;
	}

	
	/*
	 * returns the max number of repeated words consecutively in the comment
	 */
	public static int numberRepeatedWords(Comment comment) {
		int n;
		int max;
		String text = comment.getSnippet().getTextDisplay();

		text = text.replaceAll("^\\W+", "");
		text = text.replaceAll("\\W+$", "");

		Pattern pattern = Pattern.compile("\\w+");
		Matcher matcher = pattern.matcher(text);

		//there's at least a word in the text
		if (matcher.find()) {
			max=1;
			n=1;
			String currentWord = matcher.group();

			while(matcher.find()) {
				if (matcher.group().equals(currentWord))
					n++;
				else {
					if (n>max)
						max = n;
					n=1;
				}
				currentWord = matcher.group();
			}
		}

		//there are no words in the text
		else
			return 0;

		if (n>max)
			max = n;
		return max;
	}


	/*
	 * returns the percentual of capitalized letters in the text comment
	 */
	public static double percentageCapsLock(Comment comment) {
		String text = comment.getSnippet().getTextDisplay();
		Pattern pattern = Pattern.compile("\\w");
		int totalChar=0;
		int capitalizedChar=0;
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			totalChar++;
			String letter = matcher.group();
			if (letter.toUpperCase().equals(letter))
				capitalizedChar++;
		}
		if(totalChar == 0)
			return 0;
		return (capitalizedChar * 100) / totalChar;
	}


	public static boolean isPresentIP(Comment comment) {
		String text = comment.getSnippet().getTextDisplay();

		Pattern pattern = Pattern.compile("\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}/\\d{1,2}");
		Matcher matcher1 = pattern.matcher(text);

		pattern = Pattern.compile("https?://\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}");
		Matcher matcher2 = pattern.matcher(text);

		return (matcher1.find() || matcher2.find());
	}


	/* process comment data to create a json in the text with all the features needed
	 */
	public static boolean searchAuthorNameInComment(Comment videoComment) {
		String author = videoComment.getSnippet().getAuthorDisplayName();
		String text = videoComment.getSnippet().getTextDisplay();
		author = author.replaceAll("\\*", "");

		Pattern pattern = Pattern.compile(author);
		Matcher matcher = pattern.matcher(text);
		boolean res = matcher.find();

		return res;
	}

	
	
}







