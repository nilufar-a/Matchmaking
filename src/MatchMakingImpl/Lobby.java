package MatchMakingImpl;



import java.util.ArrayList;

public class Lobby  {

    private String map_ID="";
    private double timeToUpdate;
    public boolean disappear=false;
    private int num_of_players=4;
    private int num_of_ai1=1;
    private int num_of_ai2=1;
    //public ArrayList<User> human_players=new ArrayList<>();
    public ArrayList<User> allPlayers=new ArrayList<>();
    public Lobby(){

    }
    public Lobby(String map_ID,int num_of_players,int num_of_ai1,int num_of_ai2){
        this.map_ID=map_ID;
        
        this.num_of_players=num_of_players;
        this.num_of_ai1=num_of_ai1;
        this.num_of_ai2=num_of_ai2;
    }
    public String getMap_ID(){
        return map_ID;
    }
   
    public int getNum_of_players(){
        return num_of_players;
    }
    public int getNum_of_ai1(){
        return num_of_ai1;
    }
    public int getNum_of_ai2(){
        return num_of_ai2;
    }
    public double getTimeToUpdate() {
		return timeToUpdate;
	}
    
    public void setMap_ID(String map_ID) {
        this.map_ID = map_ID;
    }

    public void setNum_of_players(int num_of_players) {
        this.num_of_players = num_of_players;
    }
    public void setNum_of_ai1(int num_of_ai1){
        this.num_of_ai1=num_of_ai1;
    }
    public void setNum_of_ai2(int num_of_ai2){
        this.num_of_ai2=num_of_ai2;
    }
    public void setTimeToUpdate(double timeToUpdate) {
		this.timeToUpdate = timeToUpdate;
	}
    public void display_lobby(){
        System.out.println(map_ID);
        
        System.out.println(num_of_players);
        System.out.println(num_of_ai1);
        System.out.println(num_of_ai2);
    }
	
	
}
