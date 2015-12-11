/*
 * Copyright (c) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Comment;
import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.common.collect.Lists;

public class CommentHandling {

    /**
     * Define a global instance of a YouTube object, which will be used to make
     * YouTube Data API requests. 
     */
    private static YouTube youtube;

    /* With this main we will retrieve youtube comments from a video that the user
     * will specify on the console */
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

            // Prompt the user for the ID of a video to retrieve comments from.
            String videoId = getVideoId();
            System.out.println("You will retrieve comments from " + videoId + ".");
            
            //retrieves the first 100 comments from the video and stores in a list
            CommentThreadListResponse videoCommentsListResponse = youtube.commentThreads()
                    .list("snippet, replies").setVideoId(videoId).setTextFormat("plainText")
                    .setOrder("relevance").setMaxResults(100L).execute();
            
            //gets the next page token, to retrieve more comments
            String nextpage = videoCommentsListResponse.getNextPageToken();
            
            //videoComments will be the final list with all the comments. for now
            //it will store the first 100 comments
            List<CommentThread> videoComments = videoCommentsListResponse.getItems();
            
            //videoCommentsAdd is a support-list
            List<CommentThread> videoCommentsAdd = null;
            
            //with this while we will store all the video comments in the list videoComments
            while(nextpage != null){
            	videoCommentsListResponse = youtube.commentThreads().list("snippet, replies")
                        .setVideoId(videoId).setTextFormat("plainText").setMaxResults(100L)
                        .setOrder("relevance").setPageToken(nextpage).execute();
            	
            	videoCommentsAdd = videoCommentsListResponse.getItems();
            	videoComments.addAll(videoCommentsAdd);
            	//go to the next page
                nextpage = videoCommentsListResponse.getNextPageToken();
            }
            
            //now we will render the comments retrieved on the console
            //and store them in a json format in a .txt
            if (videoComments.isEmpty()) {
            	
                System.out.println("There are no comments.");
                
            } else {
            	
                FileWriter w = new FileWriter("comments.txt", true);
                
                //the following line is a .txt used for tests, instead of using the comments.txt
                //FileWriter w = new FileWriter("test.txt", true);
                
                BufferedWriter b = new BufferedWriter (w);
                
                //with the variable i we will count the number of comments retrieved
            	int i = 0;
            	
            	//prints and stores comments
                for (CommentThread videoComment : videoComments) {
                	
                    CommentSnippet snippet = videoComment.getSnippet().getTopLevelComment()
                            .getSnippet();
                    
                    System.out.println(snippet.toPrettyString());
                    
                    b.write(snippet.toPrettyString() + ",\n\n");
                    	
                    	//prints and stores replies of the current comment, if there are any.
                    	if (videoComment.getReplies() != null) {
                    	
                    	System.out
                        .println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
                    	
                    	List<Comment> videoReplies = videoComment.getReplies().getComments();
                    	
                    	for(Comment videoReply : videoReplies) {
                    		CommentSnippet snippet_r = videoReply.getSnippet();
                    		System.out.println(snippet_r.toPrettyString());
                    		b.write(snippet_r.toPrettyString() + ",\n\n");
                    		i++;	//we will count the reply retrieved as a comment retrieved
                    		System.out.println("");
                    	}
                    		System.out
                            .println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
                    	
                    }
                    else {
                    	System.out
                        .println("\n-------------------------------------------------------------\n");
                    }
                    i++;	//i increases because of the comment retrieved
                }
                System.out.println(i + " comments retrieved.");
                b.close();
             
            }					
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
}
