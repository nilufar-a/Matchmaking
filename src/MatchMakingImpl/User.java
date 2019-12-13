package MatchMakingImpl;


public class User {



    private String user_ID;
    private String user_state="Not ready";
    private String user_type="Human";
    private Color color;
    enum  Color {
        RED("red", "#FF0000"),
        YELLOW("yellow", "#FFFF00"),
        WHITE("white", "#FFFFFF"),
        BLACK("black", "#000000"),
        PURPLE("purple", "#800080"),
        GREEN("green", "#008000"),
        BLUE("blue", "#0000FF"),
        ORANGE("orange", "#FFA500"),
        BROWN("brown", "#654321"),
        GREY("grey", "#808080");
        private final String name;
        private final String hex_code;


        private Color(String name, String hex_code) {
            this.name = name;
            this.hex_code = hex_code;

        }

        public String getName() {
            return name;
        }

        

        public String getHex_code() {
            return hex_code;
        }
    };
    public Color searchForColor(String nameOfColor) {
    	Color foundColor=null;
        for(Color c: Color.values()) {
            if(c.getName().equals(nameOfColor)) { 
            	foundColor=c;
            }
        }
       return foundColor;
    }
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public User(){ }
    public User(String user_ID){
        this.user_ID=user_ID;
    }
    public User(String user_ID,String user_type,String user_state){
        this.user_ID=user_ID;
        this.user_type=user_type;
        this.user_state=user_state;
    }
    public String getUser_ID(){
        return user_ID;
    }

    public void setUser_ID(String user_ID) {
        this.user_ID = user_ID;
    }
    public String getUser_state(){
        return user_state;
    }

    public void setUser_state(String user_state) {
        this.user_state = user_state;
    }
	public String getUser_type() {
		return user_type;
	}
	public void setUser_type(String user_type) {
		this.user_type = user_type;
	}



}


