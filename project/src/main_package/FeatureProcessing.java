package main_package;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.Comment;
import com.google.api.services.youtube.model.CommentThread;
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
	
}
