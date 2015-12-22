package main_package;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.List;

import main_package.Auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.Comment;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.common.collect.Lists;

/**
 * This sample creates and manages comments
 */
public class CommentHandling {

    /**
     * Define a global instance of a YouTube object, which will be used to make
     * YouTube Data API requests.
     */
    private static YouTube youtube;

    /**
     * List, reply to comment threads; list, update, moderate, mark and delete
     * replies.
     *
     * @param args command line args (not used).
     */
    public static void main(String[] args) {

        // This OAuth 2.0 access scope allows for full read/write access to the
        // authenticated user's account and requires requests to use an SSL connection.
        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.force-ssl");

        try {
            // Authorize the request.
            Credential credential = Auth.authorize(scopes, "commentthreads");

            // This object is used to make YouTube Data API requests.
            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
                    .setApplicationName("youtube-cmdline-commentthreads-sample").build();

            // Prompt the user for the ID of a video to comment on.
            // Retrieve the video ID that the user is commenting to.
            String videoId = getVideoId();
            System.out.println("You chose " + videoId + " to subscribe.");
            
            //gets the resource Video, to retrieve more informations
            VideoListResponse videoListResponse = youtube.videos().
                    list("snippet").setId(videoId).execute();
            Video video = videoListResponse.getItems().get(0);

            // Call the YouTube Data API's commentThreads.list method to
            // retrieve video comment threads.
            CommentThreadListResponse videoCommentsListResponse = youtube.commentThreads()
            		.list("snippet, replies").setVideoId(videoId).setTextFormat("plainText")
                    .setMaxResults(100L).setOrder("relevance").execute();
            
            String nextpage = videoCommentsListResponse.getNextPageToken();
            
            List<CommentThread> videoComments = videoCommentsListResponse.getItems();
            
            //getting all the comments
            while(nextpage != null){
            	videoCommentsListResponse = youtube.commentThreads().list("snippet, replies")
                        .setVideoId(videoId).setTextFormat("plainText").setMaxResults(100L).
                        setPageToken(nextpage).setOrder("relevance").execute();
            	videoComments.addAll(videoCommentsListResponse.getItems());
                nextpage = videoCommentsListResponse.getNextPageToken();
            }
            
            if (videoComments.isEmpty()) {
                System.out.println("Can't get video comments.");
            } else {
            	//comments.txt is a file where we will store the comments in a JSON like format
            	FileWriter commentsTxt = new FileWriter("comments.txt", true);
            	FileWriter commentsProcessedTxt = new FileWriter("commentsProcessed.txt", true);
            	BufferedWriter c = new BufferedWriter(commentsTxt);
            	BufferedWriter cp = new BufferedWriter(commentsProcessedTxt);
            	
            	//comment retrieved counter
            	int i = 0;
            	
                // Print and store information from the API response.
                System.out.println("\n================== Returned Video Comments ==================\n");
                
                for (CommentThread videoComment : videoComments) {
                	
                    printVideoComment(videoComment, video);
                    
                    storeVideoComment(videoComment, video, c);
                    
                    processVideoComment(videoComment, video, cp);
                    
                    System.out.println("\n-------------------------------------------------------------\n");
                    i++;
                    
                    // Prints and stores replies
                    if(videoComment.getReplies() != null){
                    	
                    	List<Comment> videoReplies = videoComment.getReplies().getComments();
                    	
                    	for(Comment videoReply : videoReplies){
                    		
                    		printVideoCommentReply(videoReply, video);
                    		
                    		storeVideoCommentReply(videoReply, video, c);
                    		
                    		processVideoCommentReply(videoReply, video, cp);
                            
                    		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
                            i++;
                            
                    	}
                    	
                    } // end if
                    
                } //end for
                
                System.out.println(i + " comments and replies retrieved");
                c.close();
                cp.close();
                
            } //end else
            
        } catch (GoogleJsonResponseException e) {
            System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode()
                    + " : " + e.getDetails().getMessage());
            e.printStackTrace();

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (Throwable t) {
            System.err.println("Throwable: " + t.getMessage());
            t.printStackTrace();
        }
    }

    /*
     * Prompt the user to enter a video ID. Then return the ID.
     */
    private static String getVideoId() throws IOException {

        String videoId = "";

        System.out.print("Please enter a video id: ");
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        videoId = bReader.readLine();

        return videoId;
    }
    
    /*
     * Prints on the console all the informations about the comment
     * */
    private static void printVideoComment(CommentThread videoComment, Video video) throws IOException {
    	System.out.println("Author Channel ID: "+videoComment.getSnippet().getTopLevelComment().getSnippet().getAuthorChannelId().getValue());
    	System.out.println("Author Display Name: "+videoComment.getSnippet().getTopLevelComment().getSnippet().getAuthorDisplayName());
    	System.out.println("Author Channel URL: "+videoComment.getSnippet().getTopLevelComment().getSnippet().getAuthorChannelUrl());
    	System.out.println("Channel Subscribers Count: "+ getChannelSubscribers(videoComment.getSnippet().getTopLevelComment().getSnippet().getAuthorChannelId().getValue()));
    	System.out.println("Video ID: "+videoComment.getSnippet().getVideoId());
    	System.out.println("Text Display: "+videoComment.getSnippet().getTopLevelComment().getSnippet().getTextDisplay());
    	System.out.println("Moderation Status: "+videoComment.getSnippet().getTopLevelComment().getSnippet().getModerationStatus());
    	System.out.println("Like Count: "+videoComment.getSnippet().getTopLevelComment().getSnippet().getLikeCount());
    	System.out.println("Reply Count: "+videoComment.getSnippet().getTotalReplyCount());
    	System.out.println("Published At: "+videoComment.getSnippet().getTopLevelComment().getSnippet().getPublishedAt());
    	System.out.println("Updated At: "+videoComment.getSnippet().getTopLevelComment().getSnippet().getUpdatedAt());
        System.out.println("Video Title: " + video.getSnippet().getTitle());
        System.out.println("Video Author Display Name: " + video.getSnippet().getChannelTitle());
        System.out.println("Video Author Channel ID: " + video.getSnippet().getChannelId());
        System.out.println("Video Description: " + video.getSnippet().getDescription());
        System.out.println("Video Published At: " + video.getSnippet().getPublishedAt());
        System.out.print("Video Tags: ");
        for (String tag : video.getSnippet().getTags()) {
        	System.out.print(tag + " ");
        }
        System.out.println();
        }
    
    /*
     * Stores with BufferedWriter b all the informations about the comment in the .txt
     * */
    private static void storeVideoComment(CommentThread videoComment, Video video, BufferedWriter b) throws IOException {
    	b.write("{\n");
    	b.write("\"moderationStatus\" : \""+videoComment.getSnippet().getTopLevelComment().getSnippet().getModerationStatus()+"\",\n");
    	b.write("\"isReply\" : \"false\",\n");
    	b.write("\"authorChannelId\" : \""+videoComment.getSnippet().getTopLevelComment().getSnippet().getAuthorChannelId().getValue()+"\",\n");
    	b.write("\"authorDisplayName\" : \""+videoComment.getSnippet().getTopLevelComment().getSnippet().getAuthorDisplayName()+"\",\n");
    	b.write("\"authorChannelUrl\" : \""+videoComment.getSnippet().getTopLevelComment().getSnippet().getAuthorChannelUrl()+"\",\n");
    	b.write("\"channelSubscribersCount\" : \""+ getChannelSubscribers(videoComment.getSnippet().getTopLevelComment().getSnippet().getAuthorChannelId().getValue())+"\",\n");
    	b.write("\"videoId\" : \""+videoComment.getSnippet().getVideoId()+"\",\n");
    	b.write("\"textDisplay\" : \""+videoComment.getSnippet().getTopLevelComment().getSnippet().getTextDisplay()+"\",\n");
    	b.write("\"likeCount\" : \""+videoComment.getSnippet().getTopLevelComment().getSnippet().getLikeCount()+"\",\n");
    	b.write("\"replyCount\" : \""+videoComment.getSnippet().getTotalReplyCount()+"\",\n");
    	b.write("\"publishedAt\" : \""+videoComment.getSnippet().getTopLevelComment().getSnippet().getPublishedAt()+"\",\n");
    	b.write("\"updatedAt\" : \""+videoComment.getSnippet().getTopLevelComment().getSnippet().getUpdatedAt()+"\",\n");
    	b.write("\"videoTitle\" : \"" + video.getSnippet().getTitle()+"\",\n");
    	b.write("\"videoAuthorDisplayName\" : \"" + video.getSnippet().getChannelTitle()+"\",\n");
    	b.write("\"videoAuthorChannelId\" : \"" + video.getSnippet().getChannelId()+"\",\n");
    	b.write("\"videoDescription\" : \"" + video.getSnippet().getDescription()+"\",\n");
    	b.write("\"videoPublishedAt\" : \"" + video.getSnippet().getPublishedAt()+"\",\n");
    	b.write("\"videoTags\" : \"");
        for (String tag : video.getSnippet().getTags()) {
        	b.write(tag + " ");
        }
        b.write("\",\n");
    	b.write("},\n\n");
    }
    
    /* process comment data to create a json in the text with all the features needed
     */
    private static void processVideoComment(CommentThread videoComment, Video video, BufferedWriter b) throws IOException {
    	
    }
    
    /*
     * Prints on the console all the informations about the comment reply
     * */
    private static void printVideoCommentReply(Comment videoCommentReply, Video video) throws IOException {
    	System.out.println("Author Channel ID: "+videoCommentReply.getSnippet().getAuthorChannelId().getValue());
    	System.out.println("Author Display Name: "+videoCommentReply.getSnippet().getAuthorDisplayName());
    	System.out.println("Author Channel URL: "+videoCommentReply.getSnippet().getAuthorChannelUrl());
    	System.out.println("Channel Subscribers Count: "+ getChannelSubscribers(videoCommentReply.getSnippet().getAuthorChannelId().getValue()));
    	System.out.println("Video ID: "+videoCommentReply.getSnippet().getVideoId());
    	System.out.println("Parent ID: "+videoCommentReply.getSnippet().getParentId());
    	System.out.println("Text Display: "+videoCommentReply.getSnippet().getTextDisplay());
    	System.out.println("Moderation Status: "+videoCommentReply.getSnippet().getModerationStatus());
    	System.out.println("Like Count: "+videoCommentReply.getSnippet().getLikeCount());
    	System.out.println("Published At: "+videoCommentReply.getSnippet().getPublishedAt());
    	System.out.println("Updated At: "+videoCommentReply.getSnippet().getUpdatedAt());
        System.out.println("Video Title: " + video.getSnippet().getTitle());
        System.out.println("Video Author Display Name: " + video.getSnippet().getChannelTitle());
        System.out.println("Video Author Channel ID: " + video.getSnippet().getChannelId());
        System.out.println("Video Description: " + video.getSnippet().getDescription());
        System.out.println("Video Published At: " + video.getSnippet().getPublishedAt());
        System.out.print("Video Tags: ");
        for (String tag : video.getSnippet().getTags()) {
        	System.out.print(tag + " ");
        }
        System.out.println();
        }
    
    /*
     * Stores with BufferedWriter b all the informations about the comment reply in the .txt
     * */
    private static void storeVideoCommentReply(Comment videoCommentReply, Video video, BufferedWriter b) throws IOException {
    	b.write("{\n");
    	b.write("\"isReply\" : \"true\",\n");
    	b.write("\"moderationStatus\" : \""+videoCommentReply.getSnippet().getModerationStatus()+"\",\n");
    	b.write("\"authorChannelId\" : \""+videoCommentReply.getSnippet().getAuthorChannelId().getValue()+"\",\n");
    	b.write("\"authorDisplayName\" : \""+videoCommentReply.getSnippet().getAuthorDisplayName()+"\",\n");
    	b.write("\"authorChannelUrl\" : \""+videoCommentReply.getSnippet().getAuthorChannelUrl()+"\",\n");
    	b.write("\"channelSubscribersCount\" : \""+ getChannelSubscribers(videoCommentReply.getSnippet().getAuthorChannelId().getValue())+"\",\n");
    	b.write("\"videoId\" : \""+videoCommentReply.getSnippet().getVideoId()+"\",\n");
    	b.write("\"parentId\" : \""+videoCommentReply.getSnippet().getParentId()+"\",\n");
    	b.write("\"textDisplay\" : \""+videoCommentReply.getSnippet().getTextDisplay()+"\",\n");
    	b.write("\"likeCount\" : \""+videoCommentReply.getSnippet().getLikeCount()+"\",\n");
    	b.write("\"publishedAt\" : \""+videoCommentReply.getSnippet().getPublishedAt()+"\",\n");
    	b.write("\"updatedAt\" : \""+videoCommentReply.getSnippet().getUpdatedAt()+"\",\n");
    	b.write("\"videoTitle\" : \"" + video.getSnippet().getTitle()+"\",\n");
    	b.write("\"videoAuthorDisplayName\" : \"" + video.getSnippet().getChannelTitle()+"\",\n");
    	b.write("\"videoAuthorChannelId\" : \"" + video.getSnippet().getChannelId()+"\",\n");
    	b.write("\"videoDescription\" : \"" + video.getSnippet().getDescription()+"\",\n");
    	b.write("\"videoPublishedAt\" : \"" + video.getSnippet().getPublishedAt()+"\",\n");
    	b.write("\"videoTags\" : \"");
        for (String tag : video.getSnippet().getTags()) {
        	b.write(tag + " ");
        }
        b.write("\",\n");
    	b.write("},\n\n");
    }
    
    /*
     * process reply data to create a json in the text with all the features needed
     * */
    
    private static void processVideoCommentReply(Comment videoCommentReply, Video video, BufferedWriter b) throws IOException {
    	
    }
    
    
    /**
     * With the channelId, returns a String with the subscribers' number
     */
    private static String getChannelSubscribers(String channeId) {
    
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
