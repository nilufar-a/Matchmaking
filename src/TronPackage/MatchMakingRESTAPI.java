package TronPackage;


import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import TronPackage.MapPackage.Example;
import TronPackage.MapPackage.Map;


@Path("/MatchMaking")
@Singleton
public class MatchMakingRESTAPI {

    static int unique_ID = 0;
    static int AIunique_ID = 0;

    ExistingGames exG = new ExistingGames();

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/PutCreateGame")
    public String create_new_game(CreateGameInput c) throws IOException {
        User U = new User(c.userID);
        int gameID = unique_ID++;
        Lobby l = new Lobby("hfkf", 6, 3, 3);
        Game g = new Game(gameID, "Created", U, l);
        URL mapEDitorURL = new URL("https://mapeditor-dot-trainingprojectlab2019.appspot.com/getMapsWithXPlayers?NumberOfPlayers=" + g.getLobby().getNum_of_players());
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
        g.getLobby().setMap_ID(listOfMaps.get(0).getName());
        g.getLobby().human_players.add(U);
        g.setHost(U);


        exG.games.add(g);

        return "200 OK";
    }



    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/JoinGame")
    public String joinGame(PostJoinGameInput p) throws IOException {
        //#todo A user can join only one game, can be present in only one game's lobby
        User U = new User(p.userID);
        for (int i = 0; i < exG.games.size(); i++) {
            if (exG.games.get(i).getGame_ID() == p.gameID) {
                exG.games.get(i).getLobby().human_players.add(U);
                exG.games.get(i).allPlayers.add(U);
            }
        }
        int cnt = 0;
        for (int i = 0; i < exG.games.size(); i++) {
            if (exG.games.get(i).getGame_ID() == p.gameID) {
                for (int k = 0; k < exG.games.get(i).allPlayers.size(); k++) {
                    if (exG.games.get(i).allPlayers.get(k).getUser_state().equals("Ready")) {
                        cnt++;
                    }
                }
                if (exG.games.get(i).getLobby().getNum_of_players() == cnt) {
                    URL gameEngineURL = new URL("https://game-engine-devs-dot-trainingprojectlab2019.appspot.com/PostCreateGame?GameID=" + exG.games.get(i).getGame_ID());
                    HttpURLConnection gameEngineConnection = (HttpURLConnection) gameEngineURL.openConnection();
                    gameEngineConnection.setRequestMethod("GET");
                    gameEngineConnection.connect();
                    int gameEngineURLResponse = gameEngineConnection.getResponseCode();
                    if (gameEngineURLResponse != 200) {
                        throw new RuntimeException("HTTPResponseCode:" + gameEngineURLResponse);
                    }


                    for (int c = 0; c < exG.games.get(i).getLobby().getNum_of_ai1(); c++) {
                        URL userAuthURL = new URL("https://userauth-dot-trainingprojectlab2019.appspot.com/registerai");
                        HttpURLConnection userAuthConnection = (HttpURLConnection) userAuthURL.openConnection();
                        userAuthConnection.setRequestMethod("POST");
                        userAuthConnection.connect();
                        int userAuthURLResponse = userAuthConnection.getResponseCode();
                        if (userAuthURLResponse != 200) {
                            throw new RuntimeException("HTTPResponseCode:" + userAuthURLResponse);
                        }
                        BufferedReader userAuthJson = new BufferedReader(new InputStreamReader(userAuthConnection.getInputStream()));
                        AIbot bot = new Gson().fromJson(userAuthJson, AIbot.class);

                        AIInput ai = new AIInput();
                        ai.user_ID = bot.userID;
                        ai.game_ID = exG.games.get(i).getGame_ID();
                        ai.token = bot.token;
                        /*String postAI1Url="https://ai1-dot-trainingprojectlab2019.appspot.com";
                        Gson gson=new Gson();
                        HttpClient httpClient=HttpClientBuilder.create().build();
                        HttpPost post  = new HttpPost(postAI1Url);
                        StringEntity postingString = new StringEntity(gson.toJson(ai));//gson.tojson() converts your pojo to json
                        post.setEntity(postingString);
                        post.setHeader("Content-type", "application/json");
                        HttpResponse  response = httpClient.execute(post);
                        if (response.toString() != "200") {
                            throw new RuntimeException("HTTPResponseCode:" + response);}*/
                        URL AI1URL = new URL("https://ai1-dot-trainingprojectlab2019.appspot.com/ai-bot?userID=" + ai.user_ID + "gameID=" + ai.game_ID + "token=" + ai.token);
                        HttpURLConnection AI1Connection = (HttpURLConnection) AI1URL.openConnection();
                        AI1Connection.setRequestMethod("POST");
                        AI1Connection.connect();
                        int AI1URLResponse = AI1Connection.getResponseCode();
                        if (AI1URLResponse != 200) {
                            throw new RuntimeException("HTTPResponseCode:" + AI1URLResponse);
                        }


                    }
                    for (int c = 0; c < exG.games.get(i).getLobby().getNum_of_ai2(); c++) {
                        URL userAuthURL = new URL("https://userauth-dot-trainingprojectlab2019.appspot.com/registerai");
                        HttpURLConnection userAuthConnection = (HttpURLConnection) userAuthURL.openConnection();
                        userAuthConnection.setRequestMethod("POST");
                        userAuthConnection.connect();
                        int userAuthURLResponse = userAuthConnection.getResponseCode();
                        if (userAuthURLResponse != 200) {
                            throw new RuntimeException("HTTPResponseCode:" + userAuthURLResponse);
                        }
                        BufferedReader userAuthJson = new BufferedReader(new InputStreamReader(userAuthConnection.getInputStream()));
                        AIbot bot = new Gson().fromJson(userAuthJson, AIbot.class);

                        AIInput ai = new AIInput();
                        ai.user_ID = bot.userID;
                        ai.game_ID = exG.games.get(i).getGame_ID();
                        ai.token = bot.token;
                        /* String postAI2Url="https://ai2-dot-trainingprojectlab2019.appspot.com";
                         Gson gson=new Gson();
                         HttpClient httpClient=HttpClientBuilder.create().build();
                         HttpPost post  = new HttpPost(postAI2Url);
                         StringEntity postingString = new StringEntity(gson.toJson(ai));//gson.tojson() converts your pojo to json
                         post.setEntity(postingString);
                         post.setHeader("Content-type", "application/json");
                         HttpResponse  response = httpClient.execute(post);
                         if (response.toString() != "200") {
                             throw new RuntimeException("HTTPResponseCode:" + response);}*/
                        URL AI2URL = new URL("https://ai2-dot-trainingprojectlab2019.appspot.com/ai-bot?userID=" + ai.user_ID + "gameID=" + ai.game_ID + "token=" + ai.token);
                        HttpURLConnection AI2Connection = (HttpURLConnection) AI2URL.openConnection();
                        AI2Connection.setRequestMethod("POST");
                        AI2Connection.connect();
                        int AI2URLResponse = AI2Connection.getResponseCode();
                        if (AI2URLResponse != 200) {
                            throw new RuntimeException("HTTPResponseCode:" + AI2URLResponse);
                        }


                    }
                }
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

            for (int j = 0; j < exG.games.get(i).allPlayers.size(); j++) {
                if (exG.games.get(i).allPlayers.get(j).getUser_ID().equals(k.userID)) {
                    exG.games.get(i).allPlayers.get(j).setUser_state("Not ready");
                    exG.games.get(i).allPlayers.remove(j);
                }
            }
        }
        //Game g=new Game();

        for (int i = 0; i < exG.games.size(); i++) {

            for (int j = 0; j < exG.games.get(i).getLobby().human_players.size(); j++) {
                if (exG.games.get(i).getLobby().human_players.get(j).getUser_ID().equals(k.userID)) {
                    exG.games.get(i).getLobby().human_players.get(j).setUser_state("Not ready");
                    exG.games.get(i).getLobby().human_players.remove(j);
                    //g=exG.games.get(i);

                }
            }
        }


        return "200 OK";
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/PostChangeColor")
    public String changeColor(@QueryParam("color") String color, @QueryParam("UserID") String userID) {
        for (int i = 0; i < exG.games.size(); i++) {
            for (int j = 0; j < exG.games.get(i).allPlayers.size(); j++) {
                if (exG.games.get(i).allPlayers.get(j).getUser_ID().equals(userID) && !exG.games.get(i).allPlayers.get(j).getColor().getName().equals(color)) {
                    exG.games.get(i).allPlayers.get(j).setColor(exG.games.get(i).allPlayers.get(j).searchForColor(color));
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
            for (int j = 0; j < exG.games.get(i).allPlayers.size(); j++) {
                if (exG.games.get(i).allPlayers.get(j).getUser_ID() == userID) {
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
            for (int j = 0; j < exG.games.get(i).allPlayers.size(); j++) {
                if (exG.games.get(i).allPlayers.get(j).getUser_ID() == userID) {
                    exG.games.get(i).getLobby().setNum_of_players(numOfPlayers);
                }
            }
        }

        return "200 OK";
    }
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/PostIAmReady")
    public String postIAmReady(@QueryParam("UserID") String userID) {
        for (int i = 0; i < exG.games.size(); i++) {
            for (int j = 0; j < exG.games.get(i).allPlayers.size(); j++) {
                if (exG.games.get(i).allPlayers.get(j).getUser_ID() == userID) {
                    exG.games.get(i).allPlayers.get(j).setUser_state("Ready");
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
            for (int j = 0; j < exG.games.get(i).allPlayers.size(); j++) {
                if (exG.games.get(i).allPlayers.get(j).getUser_ID() == userID) {
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
            for (int j = 0; j < exG.games.get(i).allPlayers.size(); j++) {
                if (exG.games.get(i).allPlayers.get(j).getUser_ID() == userID) {
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
        for (int i = 0; i < exG.games.size(); i++) {
            if (exG.games.get(i).getGame_ID() == gameID) {
                exG.games.get(i).allPlayers.add(AI1bot);

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
    public String addAI2(@QueryParam("GameID") int gameID) throws IOException {
        String AIusername = "AI2;" + (AIunique_ID++);
        User AI2bot = new User(AIusername, "AI2", "Ready");
        for (int i = 0; i < exG.games.size(); i++) {
            if (exG.games.get(i).getGame_ID() == gameID) {
                exG.games.get(i).allPlayers.add(AI2bot);
            }
        }
        /*URL userAuthURL = new URL("https://userauth-dot-trainingprojectlab2019.appspot.com/RegisterAI?GameID=" + gameID);
		HttpURLConnection userAuthConnection = (HttpURLConnection) userAuthURL.openConnection();
		userAuthConnection.setRequestMethod("POST");
		userAuthConnection.connect();
        int userAuthURLResponse = userAuthConnection.getResponseCode();
        if (userAuthURLResponse != 200) {
            throw new RuntimeException("HTTPResponseCode:" + userAuthURLResponse);}
        BufferedReader userAuthJson = new BufferedReader(new InputStreamReader(userAuthConnection.getInputStream()));
        AIbot bot = new Gson().fromJson(userAuthJson , AIbot.class);
        
        AIInput ai=new AIInput();
        ai.user_ID=bot.userID;
        ai.game_ID=gameID;
        ai.token=bot.token;
        String postAI2Url="https://ai2-dot-trainingprojectlab2019.appspot.com";
        Gson gson=new Gson();
        HttpClient httpClient=HttpClientBuilder.create().build();
        HttpPost post  = new HttpPost(postAI2Url);
        StringEntity postingString = new StringEntity(gson.toJson(ai));//gson.tojson() converts your pojo to json
        post.setEntity(postingString);
        post.setHeader("Content-type", "application/json");
        HttpResponse  response = httpClient.execute(post);
        if (response.toString() != "200") {
            throw new RuntimeException("HTTPResponseCode:" + response);}*/
        /*HttpURLConnection AI2Connection = (HttpURLConnection) AI2URL.openConnection();
        AI2Connection.setRequestMethod("POST");
        AI2Connection.connect();
        int AI2URLResponse = AI2Connection.getResponseCode();
        if (AI2URLResponse != 200) {
            throw new RuntimeException("HTTPResponseCode:" + AI2URLResponse);}*/

        return "200 OK";
    }
    @GET
    @Path("/GetLobbyState")
    @Produces(MediaType.APPLICATION_JSON)
    public String getLobbyState(@QueryParam("GameID") int gameID) {
        Game g = null;
        String lobbyStateJSON;
        for (Game game: exG.games) {
            if (game.getGame_ID() == gameID) {
                g = game;
                break;
            }
        }
        if (g != null) {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
            lobbyStateJSON = gson.toJson(g.getLobby());
            return lobbyStateJSON;
        }
        return null;
    }
    @GET
    @Path("/GetListOfGames")
    @Produces(MediaType.APPLICATION_JSON)
    public String getListOfGames() {

        String listOfGamesJSON;


        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
        listOfGamesJSON = gson.toJson(exG.games);
        return listOfGamesJSON;

    }



}