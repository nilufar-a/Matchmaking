package TronPackage;

import java.util.ArrayList;

import javax.inject.Singleton;
	@Singleton
	public class Game{
	    private int game_ID;
	    private String game_state;
	    private User host;
	    private Lobby l;
	    
	    public ArrayList<User> allPlayers=new ArrayList<>();
	    public Game() {
	    	
	    }
	    public Game(int game_ID,String game_state,User host,Lobby l){
	        this.host=host;
	        this.setLobby(l);
	        this.game_ID=game_ID;
	        this.game_state=game_state;

	    }
	    public User getHost(){return host;}
	    public void setHost(User host){ this.host=host; }
	    public int getGame_ID(){
	        return game_ID;
	    }
	    public String getGame_state(){
	        return  game_state;
	    }
	    public void setGame_ID(int game_ID) {
	        this.game_ID = game_ID;
	    }
	    public void setGame_state(String game_state){
	        this.game_state=game_state;
	    }
	    public void dislay_game(){
	        System.out.println(this.game_ID);
	        System.out.println(this.game_state);
	    }
		public Lobby getLobby() {
			return l;
		}
		public void setLobby(Lobby l) {
			this.l = l;
		}
		
	   
	

}
