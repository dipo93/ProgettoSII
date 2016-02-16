package main_package;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.math.BigInteger;
import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

import main_package.Auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
//import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
//import com.google.api.services.youtube.model.Channel;
//import com.google.api.services.youtube.model.ChannelListResponse;
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
            CommentThreadListResponse videoCommentsListResponseRelevance = youtube.commentThreads()
            		.list("snippet, replies").setVideoId(videoId).setTextFormat("plainText")
                    .setMaxResults(20L).setOrder("relevance").execute();
            
            CommentThreadListResponse videoCommentsListResponseTime = youtube.commentThreads()
            		.list("snippet, replies").setVideoId(videoId).setTextFormat("plainText")
                    .setMaxResults(30L).setOrder("time").execute();
            
//            String nextPageRel = videoCommentsListResponseRelevance.getNextPageToken();
//            String nextPageTime = videoCommentsListResponseTime.getNextPageToken();
            
            List<CommentThread> videoCommentsRel = videoCommentsListResponseRelevance.getItems();
            List<CommentThread> videoCommentsTime = videoCommentsListResponseTime.getItems();
            
            //getting all the comments in relevance order
//            while(nextPageRel != null){
//            	videoCommentsListResponseRelevance = youtube.commentThreads().list("snippet, replies")
//                        .setVideoId(videoId).setTextFormat("plainText").setMaxResults(20L).
//                        setPageToken(nextPageRel).setOrder("relevance").execute();
//            	
//            	videoCommentsRel.addAll(videoCommentsListResponseRelevance.getItems());
//                nextPageRel = videoCommentsListResponseRelevance.getNextPageToken();
//            }
//            
//          //getting all the comments in time order
//            while(nextPageTime != null){
//            	videoCommentsListResponseTime = youtube.commentThreads().list("snippet, replies")
//                        .setVideoId(videoId).setTextFormat("plainText").setMaxResults(20L).
//                        setPageToken(nextPageTime).setOrder("time").execute();
//            	
//            	videoCommentsTime.addAll(videoCommentsListResponseTime.getItems());
//            	nextPageTime = videoCommentsListResponseTime.getNextPageToken();
//            }
            
            
        	//commentsTxt.txt is a file where we will store the comments in a JSON like format
        	FileWriter commentsTxt = new FileWriter("comments.txt", true);
        	//commentsCSV.csv is a file used for storing the features
        	FileWriter commentsCSV = new FileWriter("commentsCSV.csv", true);
        	BufferedWriter c = new BufferedWriter(commentsTxt);
        	BufferedWriter csv = new BufferedWriter(commentsCSV);
            
            
            if (videoCommentsRel.isEmpty()) {
                System.out.println("Can't get video comments.");
            } else {
            	//feature headers
            	csv.write("isReply,authorNameInComment,isCommentFromAuthor,commenterNameInComment," +
            			"numbersInCommenterName,likes,replies,daysInterval," +
            			"repeatedWords,mentionTags,hashtags,commenterSubscribers," +
            			"videoMomentReference,homePageLink,redirectingLink,IP,"+
            			"percentageTitleWordsInComment,percentageTagsInComment,percentageCapsLock," +
            			"percentageSpellingErrors,blackWords," +
            			"textDisplay\n");
            	
            	//comment retrieved counter
            	int i = 0;
            	
                // Print and store information from the API response.
                System.out.println("\n================== Returned Video Comments ==================\n");
                
                for (CommentThread videoComment : videoCommentsRel) {
                	
                    printVideoComment(videoComment.getSnippet().getTopLevelComment(), video, videoComment);
                    
                    storeVideoComment(videoComment.getSnippet().getTopLevelComment(), video, videoComment, c);
                    
                    FeatureProcessing.storeFeatures(videoComment.getSnippet().getTopLevelComment(), video, videoComment, csv, youtube);
                    
                    System.out.println("\n-------------------------------------------------------------\n");
                    i++;
                    
                    // Prints and stores replies
                    if(videoComment.getReplies() != null){
                    	
                    	List<Comment> videoReplies = videoComment.getReplies().getComments();
                    	
                    	for(Comment videoReply : videoReplies){
                    		
                    		printVideoComment(videoReply, video, null);
                    		
                    		storeVideoComment(videoReply, video, null, c);
                    		
                    		FeatureProcessing.storeFeatures(videoReply, video, null, csv, youtube);
                            
                    		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
                            i++;
                    	}
                    } // end if
                } //end for
                System.out.println(i + " comments and replies in relevance retrieved");
            } //end else
            
            
            
            if (videoCommentsTime.isEmpty()) {
                System.out.println("Can't get video comments.");
            } else {
            	//comment retrieved counter
            	int i = 0;
            	
                // Print and store information from the API response.
                System.out.println("\n================== Returned Video Comments ==================\n");
                
                for (CommentThread videoComment : videoCommentsTime) {
                	
                    printVideoComment(videoComment.getSnippet().getTopLevelComment(), video, videoComment);
                    
                    storeVideoComment(videoComment.getSnippet().getTopLevelComment(), video, videoComment, c);
                    
                    FeatureProcessing.storeFeatures(videoComment.getSnippet().getTopLevelComment(), video, videoComment, csv, youtube);
                    
                    System.out.println("\n-------------------------------------------------------------\n");
                    i++;
                    
                    // Prints and stores replies
                    if(videoComment.getReplies() != null){
                    	
                    	List<Comment> videoReplies = videoComment.getReplies().getComments();
                    	
                    	for(Comment videoReply : videoReplies){
                    		
                    		printVideoComment(videoReply, video, null);
                    		
                    		storeVideoComment(videoReply, video, null, c);
                    		
                    		FeatureProcessing.storeFeatures(videoReply, video, null, csv, youtube);
                            
                    		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
                            i++;
                    	}
                    } // end if
                } //end for
                System.out.println(i + " comments and replies in time retrieved");
                c.close();
                csv.close();
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
    private static void printVideoComment(Comment videoComment, Video video, CommentThread cThread) throws IOException {
    	System.out.println("Author Channel ID: "+videoComment.getSnippet().getAuthorChannelId().getValue());
    	System.out.println("Author Display Name: "+videoComment.getSnippet().getAuthorDisplayName());
    	System.out.println("Author Channel URL: "+videoComment.getSnippet().getAuthorChannelUrl());
    	System.out.println("Channel Subscribers Count: "+ FeatureProcessing.getChannelSubscribers(youtube, videoComment.getSnippet().getAuthorChannelId().getValue()));
    	System.out.println("Video ID: "+videoComment.getSnippet().getVideoId());
    	System.out.println("Parent ID: "+videoComment.getSnippet().getParentId());
    	System.out.println("Text Display: "+videoComment.getSnippet().getTextDisplay());
    	System.out.println("Moderation Status: "+videoComment.getSnippet().getModerationStatus());
    	System.out.println("Like Count: "+videoComment.getSnippet().getLikeCount());
    	if(cThread != null)
    		System.out.println("Reply Count: "+cThread.getSnippet().getTotalReplyCount());
    	System.out.println("Published At: "+videoComment.getSnippet().getPublishedAt());
    	System.out.println("Updated At: "+videoComment.getSnippet().getUpdatedAt());
        System.out.println("Video Title: " + video.getSnippet().getTitle());
        System.out.println("Video Author Display Name: " + video.getSnippet().getChannelTitle());
        System.out.println("Video Author Channel ID: " + video.getSnippet().getChannelId());
        System.out.println("Video Description: " + video.getSnippet().getDescription());
        System.out.println("Video Published At: " + video.getSnippet().getPublishedAt());
        System.out.print("Video Tags: ");
        if(video.getSnippet().getTags() == null)
        	System.out.println("");
        else for (String tag : video.getSnippet().getTags()) {
        	System.out.print(tag + " ");
        }
        System.out.println();
        }
    
    /*
     * Stores with BufferedWriter b all the informations about the comment in the .txt
     * */
    private static void storeVideoComment(Comment videoComment, Video video, CommentThread cThread, BufferedWriter b) throws IOException {
    	b.write("{\n");
    	if(cThread != null)
    		b.write("\"isReply\" : \"false\",\n");
    	else
    		b.write("\"isReply\" : \"true\",\n");
    	b.write("\"moderationStatus\" : \""+videoComment.getSnippet().getModerationStatus()+"\",\n");
    	b.write("\"authorChannelId\" : \""+videoComment.getSnippet().getAuthorChannelId().getValue()+"\",\n");
    	b.write("\"authorDisplayName\" : \""+videoComment.getSnippet().getAuthorDisplayName()+"\",\n");
    	b.write("\"authorChannelUrl\" : \""+videoComment.getSnippet().getAuthorChannelUrl()+"\",\n");
    	b.write("\"channelSubscribersCount\" : \""+ FeatureProcessing.getChannelSubscribers(youtube, videoComment.getSnippet().getAuthorChannelId().getValue())+"\",\n");
    	b.write("\"videoId\" : \""+videoComment.getSnippet().getVideoId()+"\",\n");
    	b.write("\"parentId\" : \""+videoComment.getSnippet().getParentId()+"\",\n");
    	b.write("\"textDisplay\" : \""+videoComment.getSnippet().getTextDisplay()+"\",\n");
    	b.write("\"likeCount\" : \""+videoComment.getSnippet().getLikeCount()+"\",\n");
    	if(cThread != null)
    		b.write("\"replyCount\" : \""+cThread.getSnippet().getTotalReplyCount()+"\",\n");
    	else
    		b.write("\"replyCount\" : \"null\",\n");
    	b.write("\"publishedAt\" : \""+videoComment.getSnippet().getPublishedAt()+"\",\n");
    	b.write("\"updatedAt\" : \""+videoComment.getSnippet().getUpdatedAt()+"\",\n");
    	b.write("\"videoTitle\" : \"" + video.getSnippet().getTitle()+"\",\n");
    	b.write("\"videoAuthorDisplayName\" : \"" + video.getSnippet().getChannelTitle()+"\",\n");
    	b.write("\"videoAuthorChannelId\" : \"" + video.getSnippet().getChannelId()+"\",\n");
    	b.write("\"videoDescription\" : \"" + video.getSnippet().getDescription()+"\",\n");
    	b.write("\"videoPublishedAt\" : \"" + video.getSnippet().getPublishedAt()+"\",\n");
    	b.write("\"videoTags\" : \"");
    	if(video.getSnippet().getTags() == null)
        	System.out.println("");
        else for (String tag : video.getSnippet().getTags()) {
        	b.write(tag + " ");
        }
        b.write("\",\n");
    	b.write("},\n\n");
    }    

}
