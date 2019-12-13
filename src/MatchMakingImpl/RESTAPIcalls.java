package MatchMakingImpl;





import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;


import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/*import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;*/

import com.google.gson.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import MatchMakingImpl.User.Color;
import MatchMakingImpl.MapParsing.Map;

/*import TronPackage.MapPackage.Example;
import TronPackage.MapPackage.Map;*/


@Path("/MatchMaking")
@Singleton
public class RESTAPIcalls {

    static int unique_ID = 0;
    static int AIunique_ID = 0;

    ExistingGames exG = new ExistingGames();

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/PutCreateGame")
    public String create_new_game(CreateGameInput c) {
        User U = new User(c.userID);
        //Color a;
        U.setColor(Color.RED);
        int gameID = unique_ID++;
        Lobby l = new Lobby("", 4, 1, 1);
        Game g = new Game(gameID, "Created", U, l);
        try {
            URL mapEDitorURL = new URL("https://mapeditor-dot-trainingprojectlab2019.appspot.com/getMapsWithXPlayers?NumberOfPlayers=" + g.getLobby().getNum_of_players());
            HttpURLConnection mapEditorConnection = (HttpURLConnection) mapEDitorURL.openConnection();
            mapEditorConnection.setRequestMethod("GET");
            mapEditorConnection.connect();
            int mapEditorURLResponse = mapEditorConnection.getResponseCode();
            BufferedReader mapEditorJson = new BufferedReader(new InputStreamReader(mapEditorConnection.getInputStream()));
            List < Map > listOfMaps = new ArrayList();

            Type listType = new TypeToken < List < Map >> () {}.getType();
            listOfMaps = new Gson().fromJson(mapEditorJson, listType);
            g.getLobby().setMap_ID(listOfMaps.get(0).getName());
        }
        /*if (mapEditorURLResponse != 200) {
            throw new RuntimeException("HTTPResponseCode:" + mapEditorURLResponse);
        }*/
        catch (IOException re) {
            System.out.println("Response not OK!");
        }


        //g.getLobby().human_players.add(U);
        g.getLobby().allPlayers.add(U);
        g.setHost(U);
        String s = "";

        exG.games.add(g);

        return ""+gameID+"";
    }



    @POST
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     @Path("/JoinGame")
     public String joinGame(PostJoinGameInput p) throws IOException {
         //#todo A user can join only one game, can be present in only one game's lobby
         User U = new User(p.userID);
         for (int i = 0; i < exG.games.size(); i++) {
             if (exG.games.get(i).getGame_ID() == p.gameID&&exG.games.get(i).getLobby().allPlayers.size()<exG.games.get(i).getLobby().getNum_of_players()) {
                // exG.games.get(i).getLobby().human_players.add(U);
            	 
                 exG.games.get(i).getLobby().allPlayers.add(U);
             }else {
            	 System.out.println("No empty slot!");
             }
         }
         


         return "200 OK";
     }
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/PostKickPlayer")
    public String removePlayer(KickPlayerInput k) {

        for (int i = 0; i < exG.games.size(); i++) {

            for (int j = 0; j < exG.games.get(i).getLobby().allPlayers.size(); j++) {
                if (exG.games.get(i).getLobby().allPlayers.get(j).getUser_ID().equals(k.userID)) {
                    exG.games.get(i).getLobby().allPlayers.get(j).setUser_state("Not ready");
                    exG.games.get(i).getLobby().allPlayers.remove(j);
                }
            }
        }
        //Game g=new Game();

        /*for (int i = 0; i < exG.games.size(); i++) {

            for (int j = 0; j < exG.games.get(i).getLobby().human_players.size(); j++) {
                if (exG.games.get(i).getLobby().human_players.get(j).getUser_ID().equals(k.userID)) {
                    exG.games.get(i).getLobby().human_players.get(j).setUser_state("Not ready");
                    exG.games.get(i).getLobby().human_players.remove(j);
                    //g=exG.games.get(i);

                }
            }
        }*/


        return "200 OK";
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/PostChangeColor")
    public String changeColor(@QueryParam("color") String color, @QueryParam("UserID") String userID) {
        for (int i = 0; i < exG.games.size(); i++) {
            for (int j = 0; j < exG.games.get(i).getLobby().allPlayers.size(); j++) {
                if (exG.games.get(i).getLobby().allPlayers.get(j).getUser_ID().equals(userID) && !exG.games.get(i).getLobby().allPlayers.get(j).getColor().getName().equals(color)) {
                    exG.games.get(i).getLobby().allPlayers.get(j).setColor(exG.games.get(i).getLobby().allPlayers.get(j).searchForColor(color));
                }
            }
        }
        return "200 OK";

    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/PostChangeMap")
    public String changeMap(@QueryParam("MapID") String mapID, @QueryParam("UserID") String userID) throws IOException {

        for (int i = 0; i < exG.games.size(); i++) {
            for (int j = 0; j < exG.games.get(i).getLobby().allPlayers.size(); j++) {
                if (exG.games.get(i).getLobby().allPlayers.get(j).getUser_ID().equals(userID)) {
                    URL mapEDitorURL = new URL("https://mapeditor-dot-trainingprojectlab2019.appspot.com/getMapsWithXPlayers?NumberOfPlayers=" + exG.games.get(i).getLobby().getNum_of_players());
                    HttpURLConnection mapEditorConnection = (HttpURLConnection) mapEDitorURL.openConnection();
                    mapEditorConnection.setRequestMethod("GET");
                    mapEditorConnection.connect();
                    int mapEditorURLResponse = mapEditorConnection.getResponseCode();
                    if (mapEditorURLResponse != 200) {
                        throw new RuntimeException("HTTPResponseCode:" + mapEditorURLResponse);
                    }
                    BufferedReader mapEditorJson = new BufferedReader(new InputStreamReader(mapEditorConnection.getInputStream()));
                    List < Map > listOfMaps = new ArrayList();

                    Type listType = new TypeToken < List < Map >> () {}.getType();
                    listOfMaps = new Gson().fromJson(mapEditorJson, listType);
                    boolean unavailableMap = true;
                    for (int k = 0; k < listOfMaps.size(); k++) {
                        if (listOfMaps.get(k).getName().equals(mapID)) {
                            exG.games.get(i).getLobby().setMap_ID(mapID);
                            unavailableMap = false;
                            break;
                        }

                    }
                    if (unavailableMap) {
                        throw new RuntimeException("The requested map is unavailable!");
                    }
                    //#todo add exception if map is not available


                }
            }
        }
        return "200 OK";
    }
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/PostChangeNumOfPlayers")
    public String changeNumOfPlayers(@QueryParam("NumofPlayersID") int numOfPlayers, @QueryParam("UserID") String userID) {
        for (int i = 0; i < exG.games.size(); i++) {
            for (int j = 0; j < exG.games.get(i).getLobby().allPlayers.size(); j++) {
                if (exG.games.get(i).getLobby().allPlayers.get(j).getUser_ID().equals(userID)) {
                    exG.games.get(i).getLobby().setNum_of_players(numOfPlayers);
                }
            }
        }

        return "200 OK";
    }
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/PostIAmReady")
    public String postIAmReady(@QueryParam("UserID") String userID) throws IOException {
        for (int i = 0; i < exG.games.size(); i++) {
            for (int j = 0; j < exG.games.get(i).getLobby().allPlayers.size(); j++) {
                if (exG.games.get(i).getLobby().allPlayers.get(j).getUser_ID().equals(userID)) {
                    exG.games.get(i).getLobby().allPlayers.get(j).setUser_state("Ready");
                }
            }}
            int cnt = 0;
            for (int l = 0; l < exG.games.size(); l++) {
                    for (int k = 0; k < exG.games.get(l).getLobby().allPlayers.size(); k++) {
                        if (exG.games.get(l).getLobby().allPlayers.get(k).getUser_ID().equals(userID)) {
                        	if(exG.games.get(l).getLobby().allPlayers.get(k).getUser_state().equals("Ready")) {
                        		cnt++;
                        	}}
                        }
                    
                    if (exG.games.get(l).getLobby().getNum_of_players() == cnt) {
                    	URL gameEngineURL = new URL("https://game-engine-devs-dot-trainingprojectlab2019.appspot.com/PostCreateGame?GameID=" + exG.games.get(l).getGame_ID());
                        HttpURLConnection gameEngineConnection = (HttpURLConnection) gameEngineURL.openConnection();
                        gameEngineConnection.setRequestMethod("POST");
                        gameEngineConnection.setFixedLengthStreamingMode(0);
                        //gameEngineConnection.setRequestProperty("Content-length", "82");
                        gameEngineConnection.setDoOutput(true);
                        gameEngineConnection.connect();
                        int gameEngineURLResponse = gameEngineConnection.getResponseCode();
                        String gameEngineResponseMessage=gameEngineConnection.getResponseMessage();
                        System.out.println("Game engine: "+gameEngineResponseMessage);
                        System.out.println("Game engine: "+gameEngineURLResponse);
                        /*catch(IOException io){
                       	 System.out.println("Game engine problem!");
                        }*/
                    	AIbot bot=new AIbot();
                    	AIInput ai= new AIInput();
                        for (int c = 0; c < exG.games.get(l).getLobby().getNum_of_ai1(); c++) {
                        	try { URL userAuthURL = new URL("https://userauth-dot-trainingprojectlab2019.appspot.com/registerai");
                            HttpURLConnection userAuthConnection = (HttpURLConnection) userAuthURL.openConnection();
                            userAuthConnection.setRequestMethod("POST");
                            //userAuthConnection.setRequestProperty("Content-length", "100");
                            userAuthConnection.setFixedLengthStreamingMode(0);
                            userAuthConnection.setDoOutput(true);
                            userAuthConnection.connect();
                            int userAuthURLResponse = userAuthConnection.getResponseCode();
                            //userAuthConnection.getResponseMessage();
                            System.out.println("User Auth: "+userAuthURLResponse);
                            BufferedReader userAuthJson = new BufferedReader(new InputStreamReader(userAuthConnection.getInputStream()));
                            bot = new Gson().fromJson(userAuthJson, AIbot.class);
                        	}
                        	catch(IOException e){
                             	 System.out.println("User auth problem!");
                              }
                            //ai.user_ID = bot.username;
                            System.out.println(bot.username);
                            //ai.game_ID = gameID;
                            System.out.println(bot.token);
                            //ai.token = bot.token;
                            for(int m=0;m<exG.games.get(l).getLobby().allPlayers.size();m++) {
                            	if(exG.games.get(l).getLobby().allPlayers.get(m).getUser_type().equals("AI1")) {
                            		exG.games.get(l).getLobby().allPlayers.get(m).setUser_ID(bot.username);
                            		break;
                            	}
                            	//break;
                            }
                           
                        	URL aiPostUrl;
                        	try {
    							aiPostUrl=new URL("https://trainingprojectlab2019.appspot.com/ai-bot-1");
    							HttpURLConnection AIcon = (HttpURLConnection)aiPostUrl.openConnection();
    							AIcon.setRequestMethod("POST");
    							AIcon.setRequestProperty("Content-Type", "application/json; utf-8");
    							AIcon.setRequestProperty("Accept", "application/json");
    							AIcon.setDoOutput(true);
    							String jsonInputString = "{'userID':"+bot.username+ " , 'gameID':"+exG.games.get(l).getGame_ID()+"'token':"+bot.token+"}";
    							try(OutputStream os = AIcon.getOutputStream()) {
    							    byte[] input = jsonInputString.getBytes("utf-8");
    							    os.write(input, 0, input.length);           
    							}/*try(BufferedReader br = new BufferedReader(
    									  new InputStreamReader(AIcon.getInputStream(), "utf-8"))) {
    							    StringBuilder response = new StringBuilder();
    							    String responseLine = null;
    							    while ((responseLine = br.readLine()) != null) {
    							        response.append(responseLine.trim());
    							    }
    							    System.out.println(response.toString());
    							}*/
    						} catch (MalformedURLException e) {
    							// TODO Auto-generated catch block
    							e.printStackTrace();
    						}
                        	catch (IOException e) {
    							// TODO Auto-generated catch block
    							e.printStackTrace();
    						}
                            
                        }
                        for (int c = 0; c < exG.games.get(l).getLobby().getNum_of_ai2(); c++) {
                        	 try{URL userAuthURL = new URL("https://userauth-dot-trainingprojectlab2019.appspot.com/registerai");
                             HttpURLConnection userAuthConnection = (HttpURLConnection) userAuthURL.openConnection();
                             userAuthConnection.setRequestMethod("POST");
                             //userAuthConnection.setRequestProperty("Content-length", "100");
                             userAuthConnection.setFixedLengthStreamingMode(0);
                             userAuthConnection.setDoOutput(true);
                             userAuthConnection.connect();
                             int userAuthURLResponse = userAuthConnection.getResponseCode();
                             System.out.println("User Auth: "+userAuthURLResponse);
                             BufferedReader userAuthJson = new BufferedReader(new InputStreamReader(userAuthConnection.getInputStream()));
                             bot = new Gson().fromJson(userAuthJson, AIbot.class);
                             ai = new AIInput();
                             //ai.user_ID = bot.username;
                             //ai.game_ID = gameID;
                             //ai.token = bot.token;
                             for(int m=0;m<exG.games.get(l).getLobby().allPlayers.size();m++) {
                             	if(exG.games.get(l).getLobby().allPlayers.get(m).getUser_type().equals("AI2")) {
                             		exG.games.get(l).getLobby().allPlayers.get(m).setUser_ID(bot.username);
                             		break;
                             	}
                             	
                             }}
                             catch(IOException e){
                               	 System.out.println("User auth problem!");
                                }
                        	URL aiPostUrl;
                         	try {
    								aiPostUrl=new URL("https://trainingprojectlab2019.appspot.com/ai-bot-2");
    								HttpURLConnection AIcon = (HttpURLConnection)aiPostUrl.openConnection();
    								AIcon.setRequestMethod("POST");
    								AIcon.setRequestProperty("Content-Type", "application/json; utf-8");
    								AIcon.setRequestProperty("Accept", "application/json");
    								AIcon.setDoOutput(true);
    								String jsonInputString = "{'userID':"+bot.username+ " , 'gameID':"+exG.games.get(l).getGame_ID()+"'token':"+bot.token+"}";
    								try(OutputStream os = AIcon.getOutputStream()) {
    								    byte[] input = jsonInputString.getBytes("utf-8");
    								    os.write(input, 0, input.length);           
    								}/*try(BufferedReader br = new BufferedReader(
    										  new InputStreamReader(AIcon.getInputStream(), "utf-8"))) {
    								    StringBuilder response = new StringBuilder();
    								    
    								    String responseLine = null;
    								    while ((responseLine = br.readLine()) != null) {
    								        response.append(responseLine.trim());
    								    }
    								    System.out.println(response.toString());
    								}*/
    							} catch (MalformedURLException e) {
    								// TODO Auto-generated catch block
    								e.printStackTrace();
    							}
                         	catch (IOException e) {
    								// TODO Auto-generated catch block
    								e.printStackTrace();
    							}
                             
                            /*String postAI2Url="https://ai2-dot-trainingprojectlab2019.appspot.com";
                             Gson gson=new Gson();
                             HttpClient httpClient=HttpClientBuilder.create().build();
                             HttpPost post  = new HttpPost(postAI2Url);
                             StringEntity postingString = new StringEntity(gson.toJson(ai));//gson.tojson() converts your pojo to json
                             post.setEntity(postingString);
                             post.setHeader("Content-type", "application/json");
                             HttpResponse  response = httpClient.execute(post);
                             if (response.toString() != "200") {
                                 throw new RuntimeException("HTTPResponseCode:" + response);}
                            URL AI2URL = new URL("https://ai2-dot-trainingprojectlab2019.appspot.com/ai-bot?userID=" + ai.user_ID + "gameID=" + ai.game_ID + "token=" + ai.token);
                            HttpURLConnection AI2Connection = (HttpURLConnection) AI2URL.openConnection();
                            AI2Connection.setRequestMethod("POST");
                            AI2Connection.connect();
                            int AI2URLResponse = AI2Connection.getResponseCode();
                            if (AI2URLResponse != 200) {
                                throw new RuntimeException("HTTPResponseCode:" + AI2URLResponse);
                            }*/


                        }
                    }
            }

        
        

        return "200 OK";
    }
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/PostChangeTurnInterval")
    public String postChangeTurnInterval(@QueryParam("UserID") String userID, @QueryParam("turnInterval") double turnInterval) {
        for (int i = 0; i < exG.games.size(); i++) {
            for (int j = 0; j < exG.games.get(i).getLobby().allPlayers.size(); j++) {
                if (exG.games.get(i).getLobby().allPlayers.get(j).getUser_ID().equals(userID)) {
                    exG.games.get(i).getLobby().setTimeToUpdate(turnInterval);
                }
            }
        }

        return "200 OK";
    }
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/PostChangeCycleBehaviour")
    public String postChangeCycleBehaviour(@QueryParam("UserID") String userID, @QueryParam("disappear") boolean disappear) {
        for (int i = 0; i < exG.games.size(); i++) {
            for (int j = 0; j < exG.games.get(i).getLobby().allPlayers.size(); j++) {
                if (exG.games.get(i).getLobby().allPlayers.get(j).getUser_ID().equals(userID)) {
                    exG.games.get(i).getLobby().disappear = true;
                }
            }
        }

        return "200 OK";
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/PostAddAI1")
    public String addAI1(@QueryParam("GameID") int gameID) throws IOException {
        String AIusername = "AI1;" + (AIunique_ID++);
        User AI1bot = new User(AIusername, "AI1", "Ready");
        AI1bot.setColor(Color.RED);
        for (int i = 0; i < exG.games.size(); i++) {
            if (exG.games.get(i).getGame_ID() == gameID) {
                exG.games.get(i).getLobby().allPlayers.add(AI1bot);

            }
        }

        int cnt = 0;
        for (int l = 0; l < exG.games.size(); l++) {
                
                    if (exG.games.get(l).getGame_ID()==gameID) {
                    	for (int k = 0; k < exG.games.get(l).getLobby().allPlayers.size(); k++) {
                    	if(exG.games.get(l).getLobby().allPlayers.get(k).getUser_state().equals("Ready")) {
                    		cnt++;
                    	}}
                    }
                
                if (exG.games.get(l).getLobby().getNum_of_players() == cnt) {
                	URL gameEngineURL = new URL("https://game-engine-devs-dot-trainingprojectlab2019.appspot.com/PostCreateGame?GameID=" + gameID);
                    HttpURLConnection gameEngineConnection = (HttpURLConnection) gameEngineURL.openConnection();
                    gameEngineConnection.setRequestMethod("POST");
                    gameEngineConnection.setFixedLengthStreamingMode(0);
                    //gameEngineConnection.setRequestProperty("Content-length", "82");
                    gameEngineConnection.setDoOutput(true);
                    gameEngineConnection.connect();
                    int gameEngineURLResponse = gameEngineConnection.getResponseCode();
                    String gameEngineResponseMessage=gameEngineConnection.getResponseMessage();
                    System.out.println("Game engine: "+gameEngineResponseMessage);
                    System.out.println("Game engine: "+gameEngineURLResponse);
                    /*catch(IOException io){
                   	 System.out.println("Game engine problem!");
                    }*/
                	AIbot bot=new AIbot();
                	AIInput ai= new AIInput();
                    for (int c = 0; c < exG.games.get(l).getLobby().getNum_of_ai1(); c++) {
                    	try { URL userAuthURL = new URL("https://userauth-dot-trainingprojectlab2019.appspot.com/registerai");
                        HttpURLConnection userAuthConnection = (HttpURLConnection) userAuthURL.openConnection();
                        userAuthConnection.setRequestMethod("POST");
                        //userAuthConnection.setRequestProperty("Content-length", "100");
                        userAuthConnection.setFixedLengthStreamingMode(0);
                        userAuthConnection.setDoOutput(true);
                        userAuthConnection.connect();
                        int userAuthURLResponse = userAuthConnection.getResponseCode();
                        //userAuthConnection.getResponseMessage();
                        System.out.println("User Auth: "+userAuthURLResponse);
                        BufferedReader userAuthJson = new BufferedReader(new InputStreamReader(userAuthConnection.getInputStream()));
                        bot = new Gson().fromJson(userAuthJson, AIbot.class);
                    	}
                    	catch(IOException e){
                         	 System.out.println("User auth problem!");
                          }
                        //ai.user_ID = bot.username;
                        System.out.println(bot.username);
                        //ai.game_ID = gameID;
                        System.out.println(bot.token);
                        //ai.token = bot.token;
                        for(int m=0;m<exG.games.get(l).getLobby().allPlayers.size();m++) {
                        	if(exG.games.get(l).getLobby().allPlayers.get(m).getUser_type().equals("AI1")) {
                        		exG.games.get(l).getLobby().allPlayers.get(m).setUser_ID(bot.username);
                        		break;
                        	}
                        	//break;
                        }
                       
                    	URL aiPostUrl;
                    	try {
							aiPostUrl=new URL("https://trainingprojectlab2019.appspot.com/ai-bot-1");
							HttpURLConnection AIcon = (HttpURLConnection)aiPostUrl.openConnection();
							AIcon.setRequestMethod("POST");
							AIcon.setRequestProperty("Content-Type", "application/json; utf-8");
							AIcon.setRequestProperty("Accept", "application/json");
							AIcon.setDoOutput(true);
							String jsonInputString = "{'userID':"+bot.username+ " , 'gameID':"+gameID+"'token':"+bot.token+"}";
							try(OutputStream os = AIcon.getOutputStream()) {
							    byte[] input = jsonInputString.getBytes("utf-8");
							    os.write(input, 0, input.length);           
							}/*try(BufferedReader br = new BufferedReader(
									  new InputStreamReader(AIcon.getInputStream(), "utf-8"))) {
							    StringBuilder response = new StringBuilder();
							    String responseLine = null;
							    while ((responseLine = br.readLine()) != null) {
							        response.append(responseLine.trim());
							    }
							    System.out.println(response.toString());
							}*/
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    	catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        
                    }
                    for (int c = 0; c < exG.games.get(l).getLobby().getNum_of_ai2(); c++) {
                    	 try{URL userAuthURL = new URL("https://userauth-dot-trainingprojectlab2019.appspot.com/registerai");
                         HttpURLConnection userAuthConnection = (HttpURLConnection) userAuthURL.openConnection();
                         userAuthConnection.setRequestMethod("POST");
                         //userAuthConnection.setRequestProperty("Content-length", "100");
                         userAuthConnection.setFixedLengthStreamingMode(0);
                         userAuthConnection.setDoOutput(true);
                         userAuthConnection.connect();
                         int userAuthURLResponse = userAuthConnection.getResponseCode();
                         System.out.println("User Auth: "+userAuthURLResponse);
                         BufferedReader userAuthJson = new BufferedReader(new InputStreamReader(userAuthConnection.getInputStream()));
                         bot = new Gson().fromJson(userAuthJson, AIbot.class);
                         ai = new AIInput();
                         //ai.user_ID = bot.username;
                         //ai.game_ID = gameID;
                         //ai.token = bot.token;
                         for(int m=0;m<exG.games.get(l).getLobby().allPlayers.size();m++) {
                         	if(exG.games.get(l).getLobby().allPlayers.get(m).getUser_type().equals("AI2")) {
                         		exG.games.get(l).getLobby().allPlayers.get(m).setUser_ID(bot.username);
                         		break;
                         	}
                         	
                         }}
                         catch(IOException e){
                           	 System.out.println("User auth problem!");
                            }
                    	URL aiPostUrl;
                     	try {
								aiPostUrl=new URL("https://trainingprojectlab2019.appspot.com/ai-bot-2");
								HttpURLConnection AIcon = (HttpURLConnection)aiPostUrl.openConnection();
								AIcon.setRequestMethod("POST");
								AIcon.setRequestProperty("Content-Type", "application/json; utf-8");
								AIcon.setRequestProperty("Accept", "application/json");
								AIcon.setDoOutput(true);
								String jsonInputString = "{'userID':"+bot.username+ " , 'gameID':"+gameID+"'token':"+bot.token+"}";
								try(OutputStream os = AIcon.getOutputStream()) {
								    byte[] input = jsonInputString.getBytes("utf-8");
								    os.write(input, 0, input.length);           
								}/*try(BufferedReader br = new BufferedReader(
										  new InputStreamReader(AIcon.getInputStream(), "utf-8"))) {
								    StringBuilder response = new StringBuilder();
								    
								    String responseLine = null;
								    while ((responseLine = br.readLine()) != null) {
								        response.append(responseLine.trim());
								    }
								    System.out.println(response.toString());
								}*/
							} catch (MalformedURLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                     	catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                         
                        /*String postAI2Url="https://ai2-dot-trainingprojectlab2019.appspot.com";
                         Gson gson=new Gson();
                         HttpClient httpClient=HttpClientBuilder.create().build();
                         HttpPost post  = new HttpPost(postAI2Url);
                         StringEntity postingString = new StringEntity(gson.toJson(ai));//gson.tojson() converts your pojo to json
                         post.setEntity(postingString);
                         post.setHeader("Content-type", "application/json");
                         HttpResponse  response = httpClient.execute(post);
                         if (response.toString() != "200") {
                             throw new RuntimeException("HTTPResponseCode:" + response);}
                        URL AI2URL = new URL("https://ai2-dot-trainingprojectlab2019.appspot.com/ai-bot?userID=" + ai.user_ID + "gameID=" + ai.game_ID + "token=" + ai.token);
                        HttpURLConnection AI2Connection = (HttpURLConnection) AI2URL.openConnection();
                        AI2Connection.setRequestMethod("POST");
                        AI2Connection.connect();
                        int AI2URLResponse = AI2Connection.getResponseCode();
                        if (AI2URLResponse != 200) {
                            throw new RuntimeException("HTTPResponseCode:" + AI2URLResponse);
                        }*/


                    }
                }
            }

        //AddAIs only when the game starts
        //Two new requests POSTChangeTurnTime(int) and POSTChangeCyclesDissapper(boolean)
        //Change the JSON LobbyState
        //MapEditor JSON
        //The above code will be in the start_game
        //addAI will add an AI that I created
        //color should have name and HEXcode in enum
        //Change the LOBBystate json add color code and name

        return "200 OK";
    }
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/PostAddAI2")
    public String addAI2(@QueryParam("GameID") int gameID) throws IOException  {
        String AIusername = "AI2;" + (AIunique_ID++);
        User AI2bot = new User(AIusername, "AI2", "Ready");
        AI2bot.setColor(Color.RED);
        for (int i = 0; i < exG.games.size(); i++) {
            if (exG.games.get(i).getGame_ID() == gameID) {
                exG.games.get(i).getLobby().allPlayers.add(AI2bot);
            }
        }
        int cnt = 0;
        for (int l = 0; l < exG.games.size(); l++) {
                
                    if (exG.games.get(l).getGame_ID()==gameID) {
                    	for (int k = 0; k < exG.games.get(l).getLobby().allPlayers.size(); k++) {
                    	if(exG.games.get(l).getLobby().allPlayers.get(k).getUser_state().equals("Ready")) {
                    		cnt++;
                    	}}
                    }
                
                if (exG.games.get(l).getLobby().getNum_of_players() == cnt) {
                	URL gameEngineURL = new URL("https://game-engine-devs-dot-trainingprojectlab2019.appspot.com/PostCreateGame?GameID=" + gameID);
                    HttpURLConnection gameEngineConnection = (HttpURLConnection) gameEngineURL.openConnection();
                    gameEngineConnection.setRequestMethod("POST");
                    gameEngineConnection.setFixedLengthStreamingMode(0);
                    //gameEngineConnection.setRequestProperty("Content-length", "82");
                    gameEngineConnection.setDoOutput(true);
                    gameEngineConnection.connect();
                    int gameEngineURLResponse = gameEngineConnection.getResponseCode();
                    String gameEngineResponseMessage=gameEngineConnection.getResponseMessage();
                    System.out.println("Game engine: "+gameEngineResponseMessage);
                    System.out.println("Game engine: "+gameEngineURLResponse);
                    /*catch(IOException io){
                   	 System.out.println("Game engine problem!");
                    }*/
                	AIbot bot=new AIbot();
                	AIInput ai= new AIInput();
                    for (int c = 0; c < exG.games.get(l).getLobby().getNum_of_ai1(); c++) {
                    	try { URL userAuthURL = new URL("https://userauth-dot-trainingprojectlab2019.appspot.com/registerai");
                        HttpURLConnection userAuthConnection = (HttpURLConnection) userAuthURL.openConnection();
                        userAuthConnection.setRequestMethod("POST");
                        //userAuthConnection.setRequestProperty("Content-length", "100");
                        userAuthConnection.setFixedLengthStreamingMode(0);
                        userAuthConnection.setDoOutput(true);
                        userAuthConnection.connect();
                        int userAuthURLResponse = userAuthConnection.getResponseCode();
                        //userAuthConnection.getResponseMessage();
                        System.out.println("User Auth: "+userAuthURLResponse);
                        BufferedReader userAuthJson = new BufferedReader(new InputStreamReader(userAuthConnection.getInputStream()));
                        bot = new Gson().fromJson(userAuthJson, AIbot.class);
                    	}
                    	catch(IOException e){
                         	 System.out.println("User auth problem!");
                          }
                        //ai.user_ID = bot.username;
                        System.out.println(bot.username);
                        //ai.game_ID = gameID;
                        System.out.println(bot.token);
                        //ai.token = bot.token;
                        for(int m=0;m<exG.games.get(l).getLobby().allPlayers.size();m++) {
                        	if(exG.games.get(l).getLobby().allPlayers.get(m).getUser_type().equals("AI1")) {
                        		exG.games.get(l).getLobby().allPlayers.get(m).setUser_ID(bot.username);
                        		break;
                        	}
                        	//break;
                        }
                       
                    	URL aiPostUrl;
                    	try {
							aiPostUrl=new URL("https://trainingprojectlab2019.appspot.com/ai-bot-1");
							HttpURLConnection AIcon = (HttpURLConnection)aiPostUrl.openConnection();
							AIcon.setRequestMethod("POST");
							AIcon.setRequestProperty("Content-Type", "application/json; utf-8");
							AIcon.setRequestProperty("Accept", "application/json");
							AIcon.setDoOutput(true);
							String jsonInputString = "{'userID':"+bot.username+ " , 'gameID':"+gameID+"'token':"+bot.token+"}";
							try(OutputStream os = AIcon.getOutputStream()) {
							    byte[] input = jsonInputString.getBytes("utf-8");
							    os.write(input, 0, input.length);           
							}try(BufferedReader br = new BufferedReader(
									  new InputStreamReader(AIcon.getInputStream(), "utf-8"))) {
							    StringBuilder response = new StringBuilder();
							    String responseLine = null;
							    while ((responseLine = br.readLine()) != null) {
							        response.append(responseLine.trim());
							    }
							    System.out.println(response.toString());
							}
							System.out.println("AI2 response: "+AIcon.getResponseCode());
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    	catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        
                    }
                    for (int c = 0; c < exG.games.get(l).getLobby().getNum_of_ai2(); c++) {
                    	 try{URL userAuthURL = new URL("https://userauth-dot-trainingprojectlab2019.appspot.com/registerai");
                         HttpURLConnection userAuthConnection = (HttpURLConnection) userAuthURL.openConnection();
                         userAuthConnection.setRequestMethod("POST");
                         //userAuthConnection.setRequestProperty("Content-length", "100");
                         userAuthConnection.setFixedLengthStreamingMode(0);
                         userAuthConnection.setDoOutput(true);
                         userAuthConnection.connect();
                         int userAuthURLResponse = userAuthConnection.getResponseCode();
                         System.out.println("User Auth: "+userAuthURLResponse);
                         BufferedReader userAuthJson = new BufferedReader(new InputStreamReader(userAuthConnection.getInputStream()));
                         bot = new Gson().fromJson(userAuthJson, AIbot.class);
                         ai = new AIInput();
                         //ai.user_ID = bot.username;
                         //ai.game_ID = gameID;
                         //ai.token = bot.token;
                         for(int m=0;m<exG.games.get(l).getLobby().allPlayers.size();m++) {
                         	if(exG.games.get(l).getLobby().allPlayers.get(m).getUser_type().equals("AI2")) {
                         		exG.games.get(l).getLobby().allPlayers.get(m).setUser_ID(bot.username);
                         		break;
                         	}
                         	
                         }}
                         catch(IOException e){
                           	 System.out.println("User auth problem!");
                            }
                    	URL aiPostUrl;
                     	try {
								aiPostUrl=new URL("https://trainingprojectlab2019.appspot.com/ai-bot-2");
								HttpURLConnection AIcon = (HttpURLConnection)aiPostUrl.openConnection();
								AIcon.setRequestMethod("POST");
								AIcon.setRequestProperty("Content-Type", "application/json; utf-8");
								AIcon.setRequestProperty("Accept", "application/json");
								AIcon.setDoOutput(true);
								String jsonInputString = "{'userID':"+bot.username+ " , 'gameID':"+gameID+"'token':"+bot.token+"}";
								try(OutputStream os = AIcon.getOutputStream()) {
								    byte[] input = jsonInputString.getBytes("utf-8");
								    os.write(input, 0, input.length);           
								}try(BufferedReader br = new BufferedReader(
										  new InputStreamReader(AIcon.getInputStream(),"utf-8"))) {
								    StringBuilder response = new StringBuilder();
								    
								    String responseLine = null;
							
								    while ((responseLine = br.readLine()) != null) {
								        response.append(responseLine.trim());
								    }
								    System.out.println(response.toString());
								}
								System.out.println("AI2 response: "+AIcon.getResponseCode());
							} catch (MalformedURLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                     	catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                         
                        /*String postAI2Url="https://ai2-dot-trainingprojectlab2019.appspot.com";
                         Gson gson=new Gson();
                         HttpClient httpClient=HttpClientBuilder.create().build();
                         HttpPost post  = new HttpPost(postAI2Url);
                         StringEntity postingString = new StringEntity(gson.toJson(ai));//gson.tojson() converts your pojo to json
                         post.setEntity(postingString);
                         post.setHeader("Content-type", "application/json");
                         HttpResponse  response = httpClient.execute(post);
                         if (response.toString() != "200") {
                             throw new RuntimeException("HTTPResponseCode:" + response);}
                        URL AI2URL = new URL("https://ai2-dot-trainingprojectlab2019.appspot.com/ai-bot?userID=" + ai.user_ID + "gameID=" + ai.game_ID + "token=" + ai.token);
                        HttpURLConnection AI2Connection = (HttpURLConnection) AI2URL.openConnection();
                        AI2Connection.setRequestMethod("POST");
                        AI2Connection.connect();
                        int AI2URLResponse = AI2Connection.getResponseCode();
                        if (AI2URLResponse != 200) {
                            throw new RuntimeException("HTTPResponseCode:" + AI2URLResponse);
                        }*/


                    }
                }
            }
        return "200 OK";
    }
    @GET
    @Path("/GetLobbyState")
    @Produces(MediaType.APPLICATION_JSON)
    public String getLobbyState(@QueryParam("GameID") int gameID) {
        Game g = null;
        Lobby l=new Lobby("map1",6,2,2);
        String lobbyStateJSON;
        for (Game game: exG.games) {
            if (game.getGame_ID() == gameID) {
                g = game;
                l=game.getLobby();
                break;
            }
        }
        if (l != null) {
            Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
            lobbyStateJSON = gson.toJson(l);
            System.out.println(lobbyStateJSON);
            return lobbyStateJSON;
        }
        return null;
    }
    @GET
    @Path("/GetListOfGames")
    @Produces(MediaType.APPLICATION_JSON)
    public String getListOfGames() {

        String listOfGamesJSON;


        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();


        listOfGamesJSON = gson.toJson(exG.games);
        
        return listOfGamesJSON;
        
    }
    @GET
    @Path("/GetTestJSON")
    @Produces(MediaType.APPLICATION_JSON)
    public String getTestJSON() {

        String testJSON;
        Lobby l=new Lobby();
        l.setMap_ID("map1");
        l.setNum_of_players(6);
        l.setNum_of_ai1(2);

        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();


        testJSON = gson.toJson(l);
        
        return testJSON;
        
    }



}