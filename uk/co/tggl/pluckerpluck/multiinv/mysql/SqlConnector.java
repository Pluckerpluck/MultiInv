package uk.co.tggl.pluckerpluck.multiinv.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.GameMode;

import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventory;

public class SqlConnector {
	
	private Connection con;
	
	public SqlConnector(Connection con) {
		this.con = con;
	}
	
	public boolean tableExists(String table) {
		Statement st;
		try {
			st = con.createStatement();
	        ResultSet rs = st.executeQuery("show tables like \"" + table + "\"");
	        if(rs.next()) {
	        	return true;
	        }else {
	        	return false;
	        }
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean createTable(String table) {
		Statement st;
		try {
			st = con.createStatement();
	        st.executeUpdate("CREATE TABLE pet (player TEXT,survival TEXT, creative TEXT, health INT, gamemode TEXT, hunger INT, saturation DOUBLE, experience INT, level INT, exp DOUBLE)");
	        return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public MIInventory getInventory(String player, String group, String inventoryName) {
        // Get stored string from configuration file
        MIInventory inventory = null;
        try {
        	Statement st = con.createStatement();
	        ResultSet rs = st.executeQuery("SELECT * FROM " + group + " WHERE player=" + player);
	        if(rs.next()) {
	        	String inventoryString = rs.getString(inventoryName.toLowerCase());
	            inventory = new MIInventory(inventoryString);
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return inventory;
    }
	


    public void saveInventory(String player, String group, MIInventory inventory, String inventoryName){
        String inventoryString = inventory.toString();
        //Call this just to make sure the player record has been created.
        createRecord(player, group);
        try {
        	Statement st = con.createStatement();
	        st.executeUpdate("UPDATE " + group + " SET " + inventoryName.toLowerCase() + "='" + inventoryString + "' WHERE player='"+ player + "'");
	    } catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    public void createRecord(String player, String group) {
    	try {
        	Statement st = con.createStatement();
	        ResultSet rs = st.executeQuery("SELECT * FROM " + group + " WHERE player='" + player + "'");
	        if(!rs.next()) {
	        	st.executeUpdate("INSERT INTO " + group + " (player) VALUES('" + player + "')");
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    public int getHealth(String player, String group){
        int health = 20;
        try {
        	Statement st = con.createStatement();
	        ResultSet rs = st.executeQuery("SELECT * FROM " + group + " WHERE player=" + player);
	        if(rs.next()) {
	        	health = rs.getInt("health");
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
        if (health <= 0 || health > 20) {
            health = 20;
        }
        return health;
    }

    public void saveHealth(String player, String group, int health){
        //Call this just to make sure the player record has been created.
    	createRecord(player, group);
        try {
        	Statement st = con.createStatement();
	        st.executeUpdate("UPDATE " + group + " SET health='" + health + "' WHERE player='"+ player + "'");
	    } catch (SQLException e) {
			e.printStackTrace();
		}
    }

    public GameMode getGameMode(String player, String group){
        String gameModeString = null;
        try {
        	Statement st = con.createStatement();
	        ResultSet rs = st.executeQuery("SELECT * FROM " + group + " WHERE player=" + player);
	        if(rs.next()) {
	        	gameModeString = rs.getString("gamemode");
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
        GameMode gameMode = null;
        if ("CREATIVE".equalsIgnoreCase(gameModeString)){
            gameMode = GameMode.CREATIVE;
        }else if ("SURVIVAL".equalsIgnoreCase(gameModeString)){
            gameMode = GameMode.SURVIVAL;
        }
        return gameMode;
    }

    public void saveGameMode(String player, String group, GameMode gameMode){
    	//Call this just to make sure the player record has been created.
    	createRecord(player, group);
        try {
        	Statement st = con.createStatement();
	        st.executeUpdate("UPDATE " + group + " SET gamemode='" + gameMode.toString() + "' WHERE player='"+ player + "'");
	    } catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    public int getHunger(String player, String group){
        int hunger = 20;
        try {
        	Statement st = con.createStatement();
	        ResultSet rs = st.executeQuery("SELECT * FROM " + group + " WHERE player=" + player);
	        if(rs.next()) {
	        	hunger = rs.getInt("hunger");
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
        if (hunger <= 0 || hunger > 20) {
            hunger = 20;
        }
        return hunger;
    }

    public float getSaturation(String player, String group){
        double saturationDouble = 0;
        try {
        	Statement st = con.createStatement();
	        ResultSet rs = st.executeQuery("SELECT * FROM " + group + " WHERE player=" + player);
	        if(rs.next()) {
	        	saturationDouble = rs.getDouble("saturation");
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
        float saturation = (float)saturationDouble;
        return saturation;
    }

    public void saveSaturation(String player, String group, float saturation){
    	//Call this just to make sure the player record has been created.
    	createRecord(player, group);
        try {
        	Statement st = con.createStatement();
	        st.executeUpdate("UPDATE " + group + " SET saturation='" + saturation + "' WHERE player='"+ player + "'");
	    } catch (SQLException e) {
			e.printStackTrace();
		}
    }

    public int getTotalExperience(String player, String group){
    	int experience = 0;
    	try {
        	Statement st = con.createStatement();
	        ResultSet rs = st.executeQuery("SELECT * FROM " + group + " WHERE player=" + player);
	        if(rs.next()) {
	        	experience = rs.getInt("experience");
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
        return experience;
    }

    public int getLevel(String player, String group){
    	int level = 0;
    	try {
        	Statement st = con.createStatement();
	        ResultSet rs = st.executeQuery("SELECT * FROM " + group + " WHERE player=" + player);
	        if(rs.next()) {
	        	level = rs.getInt("level");
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
        return level;
    }

    public float getExperience(String player, String group){
        double expDouble = 0;
        try {
        	Statement st = con.createStatement();
	        ResultSet rs = st.executeQuery("SELECT * FROM " + group + " WHERE player=" + player);
	        if(rs.next()) {
	        	expDouble = rs.getDouble("exp");
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
        float exp = (float)expDouble;
        return exp;
    }

    public void saveExperience(String player, String group, int experience, int level, float exp){
    	//Call this just to make sure the player record has been created.
    	createRecord(player, group);
        try {
        	Statement st = con.createStatement();
	        st.executeUpdate("UPDATE " + group + " SET experience='" + experience + "', level='" + level + "', exp='" + exp + "' WHERE player='"+ player + "'");
	    } catch (SQLException e) {
			e.printStackTrace();
		}
    }

    public void saveHunger(String player, String group, int hunger){
    	createRecord(player, group);
        try {
        	Statement st = con.createStatement();
	        st.executeUpdate("UPDATE " + group + " SET hunger='" + hunger + "' WHERE player='"+ player + "'");
	    } catch (SQLException e) {
			e.printStackTrace();
		}
    }

}
