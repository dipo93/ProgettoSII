package main_package;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.Comment;
import com.google.api.services.youtube.model.CommentThread;
//import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.Video;


public class FeatureProcessing {
	
	/* stores in a csv format all the feature of the comment passed
	 * IMPORTANT: to make a csv file , or " must not be included in
	 * any field
	 * */
	public static void storeFeatures(Comment comment, Video video, CommentThread cThread, BufferedWriter cp, YouTube youtube) throws IOException {
		//elenco nomi campi separati da virgole SOLO UNA VOLTA, VEDI DOVE METTERLO
		
		//campi separati da virgole, RISPETTA L'ORDINE.i campi inoltre devono essere privi di , o "
		//PROVA
		cp.write("commentContainsVideoAuthorName: "+ commentContainsVideoAuthorName(comment, video)+ "\n");
		cp.write("likes: "+ getNumberOfLikes(comment) +"\n");
		if (cThread != null) {
			cp.write("replies: " + getNumberOfReplies(cThread) +"\n");
		}
		cp.write("isCommentFromTheVideoAuthor: "+ isCommentFromTheVideoAuthor(comment, video) +"\n");

		cp.write("authorNameInComment: " + searchAuthorNameInComment(comment) + "\n");

		cp.write("intervalBetweenVideoAndComment: " + intervalBetweenVideoAndCommentPublish(comment, video) + "\n");

		int[] ss = new int[2];
		ss = numberMentionAndHashtags(comment);
		cp.write("mentionTagsNumber: " + ss[0] + "\n");
		cp.write("hashtagsNumber: " + ss[1] + "\n");

		String authorId = comment.getSnippet().getAuthorChannelId().getValue();
		cp.write("authorChannelSubscribersCount: " + getChannelSubscribers(youtube, authorId) + "\n");

		cp.write("videoMomentReference: " + videoMomentReference(comment) + ",\n");
		
		cp.write("\n");
		//a fine metodo vai a capo
		
	   

	}
	
	
	/*returns true if the comment text contains the name of the author of the video 
	 * IMPORTANT this method can be used for comments and also replies */
	public static boolean commentContainsVideoAuthorName(Comment comment, Video video) {
		String text = comment.getSnippet().getTextDisplay();
		String author = video.getSnippet().getChannelTitle();
		return text.contains(author);
	}
	
	/*returns the like count of the comment 
	 * IMPORTANT this method can be used for comments and also replies */
	public static Long getNumberOfLikes(Comment comment) {
		return comment.getSnippet().getLikeCount();
	}
	
	/*returns the reply count of the comment 
	 * IMPORTANT this method can be used only for comments, because replies don't have
	 * replies*/
	public static Long getNumberOfReplies(CommentThread comment) {
		return comment.getSnippet().getTotalReplyCount();
	}
	
	/*returns true if the comment is made from the video author */
	public static boolean isCommentFromTheVideoAuthor(Comment comment, Video video) {
		return ((comment.getSnippet().getAuthorChannelId().getValue()).equals(video.getSnippet().getChannelId()));
	}
	
	/* returns suspicious keywords
	 * this method must be used on the comment text and also on the author name*/
	public static String keywords(String s) {
		return "";
		
	}
	/* process comment data to create a json in the text with all the features needed
     */
    
	
	
	public static String searchAuthorNameInComment(Comment videoComment) {
    	String author = videoComment.getSnippet().getAuthorDisplayName();
    	String text = videoComment.getSnippet().getTextDisplay();
    	
    	Pattern pattern = Pattern.compile(author);
    	Matcher matcher = pattern.matcher(text);
    	boolean res = matcher.find();
		
    	return res==true ? "true" : "false";
	}
	

	public static String intervalBetweenVideoAndCommentPublish(Comment videoComment, Video video) {
		DateTime videoDate = video.getSnippet().getPublishedAt();
		DateTime commentDate = videoComment.getSnippet().getPublishedAt();
		
		long diff = commentDate.getValue() - videoDate.getValue();
		long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000);
        int diffDays = (int) (diff) / (1000 * 60 * 60 * 24);
        
        String res = diffDays + " days, " + diffHours + " hours, " + diffMinutes + " minutes, " + diffSeconds + " seconds";
		
        return res;
	}
	

	public static int[] numberMentionAndHashtags(Comment videoComment) {
    	String text = videoComment.getSnippet().getTextDisplay();
    	int tags[] = {0, 0};	//[0] is mention tags (+) and [1] is hashtags (#)
    	
    	Pattern mentions = Pattern.compile("+[a-zA-Z_0-9]*");
    	Matcher matcherM = mentions.matcher(text);
    	while(matcherM.find())
    		tags[0]++;
    	
    	Pattern hashtags = Pattern.compile("#[a-zA-Z_-0-9]*");
    	Matcher matcherH = hashtags.matcher(text);
    	while(matcherH.find())
    		tags[1]++;
    	
		return tags;
	}
    
    /**
     * With the channelId, returns a String with the subscribers' number
     */
    public static String getChannelSubscribers(YouTube youtube, String channeId) {
        try {
            // Call the YouTube Data API's channels.list method to
            // retrieve channels.
            ChannelListResponse channelListResponse = youtube.channels().list("statistics")
            		.setId(channeId).execute();
            List<Channel> channelList = channelListResponse.getItems();
            
            if (channelList.isEmpty())
            	return ("Can't find a channel with ID: " + channeId);
            
            Channel channel = channelList.get(0);
            BigInteger subscribers = channel.getStatistics().getSubscriberCount();
            return subscribers.toString();
        
        } catch (GoogleJsonResponseException e) {
           System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode()
                    + " : " + e.getDetails().getMessage());
            e.printStackTrace();
            return ("GoogleJsonResponseException code: " + e.getDetails().getCode()
                    + " : " + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
            return ("IOException: " + e.getMessage());
        } catch (Throwable t) {
            System.err.println("Throwable: " + t.getMessage());
            t.printStackTrace();
            return ("Throwable: " + t.getMessage());
        }
    }
    
    
	public static String videoMomentReference(Comment videoComment) {
		String text = videoComment.getSnippet().getTextDisplay();
		
		Pattern pattern = Pattern.compile("([0-9]{1,2}:)?[0-9]{1,2}:[0-9]{2}");
    	Matcher matcher = pattern.matcher(text);
    	boolean res = matcher.find();
		
		return res==true ? "true" : "false";
	}
    
	
	
}
