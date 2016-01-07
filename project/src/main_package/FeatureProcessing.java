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
	
	
	/* process comment data to create a json in the text with all the features needed
     */
    public static void storeFeatures(Comment videoComment, Video video, CommentThread cThread, BufferedWriter b, YouTube youtube) throws IOException {
    	b.write("{\n");
    	
    	String s = searchAuthorNameInComment(videoComment);
    	b.write("\"authorNameInComment\" : \"" + s + "\",\n");
    	
    	s = intervalBetweenVideoAndCommentPublish(videoComment, video);
    	b.write("\"intervalBetweenVideoAndComment\" : \"" + s + "\",\n");
    	
    	int[] ss = new int[2];
    	ss = numberMentionAndHashtags(videoComment);
    	b.write("\"mentionTagsNumber\" : \"" + ss[0] + "\",\n");
    	b.write("\"hashtagsNumber\" : \"" + ss[1] + "\",\n");
    	
    	String authorId = videoComment.getSnippet().getAuthorChannelId().getValue();
    	s = getChannelSubscribers(youtube, authorId);
    	b.write("\"authorChannelSubscribersCount\" : \"" + s + "\",\n");
    	
    	s = videoMomentReference(videoComment);
    	b.write("\"videoMomentReference\" : \"" + s + "\",\n");
    	
    	b.write("},\n\n");
    }
	
	
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
