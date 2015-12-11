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
import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.common.collect.Lists;

/**
 * This sample creates and manages comments by:
 *
 * 1. Retrieving the top-level comments for a video via "commentThreads.list" method.
 * 2. Replying to a comment thread via "comments.insert" method.
 * 3. Retrieving comment replies via "comments.list" method.
 * 4. Updating an existing comment via "comments.update" method.
 * 5. Sets moderation status of an existing comment via "comments.setModerationStatus" method.
 * 6. Marking a comment as spam via "comments.markAsSpam" method.
 * 7. Deleting an existing comment via "comments.delete" method.
 *
 * @author Ibrahim Ulukaya
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
            
            
            
            
            


            // All the available methods are used in sequence just for the sake
            // of an example.

            // Call the YouTube Data API's commentThreads.list method to
            // retrieve video comment threads.
            CommentThreadListResponse videoCommentsListResponse = youtube.commentThreads()
                    .list("snippet, replies").setVideoId(videoId).setTextFormat("plainText")
                    .setMaxResults(100L).setOrder("relevance").execute();
            
            String nextpage = videoCommentsListResponse.getNextPageToken();
            
            List<CommentThread> videoComments = videoCommentsListResponse.getItems();
            
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
            	FileWriter w = new FileWriter("comments.txt", true);
            	BufferedWriter b = new BufferedWriter(w);
            	int i = 0;
            	
                // Print information from the API response.
                System.out.println("\n================== Returned Video Comments ==================\n");
                for (CommentThread videoComment : videoComments) {
                    CommentSnippet snippet = videoComment.getSnippet().getTopLevelComment().getSnippet();
//                    System.out.println("  - Author: " + snippet.getAuthorDisplayName());
//                    System.out.println("  - Comment: " + snippet.getTextDisplay());
                    System.out.println(snippet.toPrettyString());
                    System.out.println("\n-------------------------------------------------------------\n");
                    i++;
                    
                    
                    //questa cosa va usata durante la stampa dei commenti, il parametro è l'id del canale
//                    String subscribers = getChannelSubscribers("a");
                    
                    
                    
                    b.write(snippet.toPrettyString() + ",\n\n");
                    
                    // Prints the replies
                    if(videoComment.getReplies() != null){
                    	List<Comment> videoReplies = videoComment.getReplies().getComments();
                    	
                    	for(Comment videoReply : videoReplies){
                    		CommentSnippet snippet_r = videoReply.getSnippet();
//                    		  System.out.println("  - Author: " + snippet_r.getAuthorDisplayName());
//                            System.out.println("  - Reply: " + snippet_r.getTextDisplay());
                    		System.out.println(snippet_r.toPrettyString());
                            System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
                            i++;
                            
                            b.write(snippet_r.toPrettyString() + ",\n\n");
                    	}
                    	
                    } // end if
                    
                } //end for
                
                System.out.println(i + " comments and replies retrieved");
                b.close();
                
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
            
            return ("This channel has " + subscribers + " subscribers");
        
        
        } catch (GoogleJsonResponseException e) {
//            System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode()
//                    + " : " + e.getDetails().getMessage());
            e.printStackTrace();
            return ("GoogleJsonResponseException code: " + e.getDetails().getCode()
                    + " : " + e.getDetails().getMessage());
        } catch (IOException e) {
//            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
            return ("IOException: " + e.getMessage());
        } catch (Throwable t) {
//            System.err.println("Throwable: " + t.getMessage());
            t.printStackTrace();
            return ("Throwable: " + t.getMessage());
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
     * Prompt the user to enter text for a comment. Then return the text.
     */
/*    private static String getText() throws IOException {

        String text = "";

        System.out.print("Please enter a comment text: ");
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        text = bReader.readLine();

        if (text.length() < 1) {
            // If nothing is entered, defaults to "YouTube For Developers."
            text = "YouTube For Developers.";
        }
        return text;
    }*/
    
    
    /*
    
    // Will use this thread as parent to new reply.
    String parentId = firstComment.getId();

    // Create a comment snippet with text.
    CommentSnippet commentSnippet = new CommentSnippet();
    commentSnippet.setTextOriginal(text);
    commentSnippet.setParentId(parentId);

    // Create a comment with snippet.
    Comment comment = new Comment();
    comment.setSnippet(commentSnippet);
    
    // Call the YouTube Data API's comments.insert method to reply
    // to a comment.
    // (If the intention is to create a new top-level comment,
    // commentThreads.insert
    // method should be used instead.)
    Comment commentInsertResponse = youtube.comments().insert("snippet", comment)
            .execute();

    // Print information from the API response.
    System.out
            .println("\n================== Created Comment Reply ==================\n");
    CommentSnippet snippet = commentInsertResponse.getSnippet();
    System.out.println("  - Author: " + snippet.getAuthorDisplayName());
    System.out.println("  - Comment: " + snippet.getTextDisplay());
    System.out
            .println("\n-------------------------------------------------------------\n");

    // Call the YouTube Data API's comments.list method to retrieve
    // existing comment replies.
    CommentListResponse commentsListResponse = youtube.comments().list("snippet")
            .setParentId(parentId).setTextFormat("plainText").execute();
    List<Comment> comments = commentsListResponse.getItems();

    if (comments.isEmpty()) {
        System.out.println("Can't get comment replies.");
    } else {
        // Print information from the API response.
        System.out
                .println("\n================== Returned Comment Replies ==================\n");
        for (Comment commentReply : comments) {
            snippet = commentReply.getSnippet();
            System.out.println("  - Author: " + snippet.getAuthorDisplayName());
            System.out.println("  - Comment: " + snippet.getTextDisplay());
            System.out
                    .println("\n-------------------------------------------------------------\n");
        }
        Comment firstCommentReply = comments.get(0);
        firstCommentReply.getSnippet().setTextOriginal("updated");
        Comment commentUpdateResponse = youtube.comments()
                .update("snippet", firstCommentReply).execute();
        // Print information from the API response.
        System.out
                .println("\n================== Updated Video Comment ==================\n");
        snippet = commentUpdateResponse.getSnippet();
        System.out.println("  - Author: " + snippet.getAuthorDisplayName());
        System.out.println("  - Comment: " + snippet.getTextDisplay());
        System.out
                .println("\n-------------------------------------------------------------\n");

        // Call the YouTube Data API's comments.setModerationStatus
        // method to set moderation
        // status of an existing comment.
        youtube.comments().setModerationStatus(firstCommentReply.getId(), "published");
        System.out.println("  -  Changed comment status to published: "
                + firstCommentReply.getId());

        // Call the YouTube Data API's comments.markAsSpam method to
        // mark an existing comment as spam.
        youtube.comments().markAsSpam(firstCommentReply.getId());
        System.out.println("  -  Marked comment as spam: " + firstCommentReply.getId());

        // Call the YouTube Data API's comments.delete method to
        // delete an existing comment.
        youtube.comments().delete(firstCommentReply.getId());
        System.out
                .println("  -  Deleted comment as spam: " + firstCommentReply.getId());
    }*/
    
}
