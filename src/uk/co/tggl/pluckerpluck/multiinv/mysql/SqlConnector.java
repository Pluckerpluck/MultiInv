package uk.co.tggl.pluckerpluck.multiinv.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.GameMode;

import uk.co.tggl.pluckerpluck.multiinv.inventory.MIEnderchestInventory;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventory;

public class SqlConnector {
	
	private Connection con;
	private String prefix = "multiinv_";
	
	public SqlConnector(Connection con, String prefix) {
		this.con = con;
		this.prefix = prefix;
	}
	
	public boolean tableExists() {
		Statement st;
		try {
			st = con.createStatement();
	        ResultSet rs = st.executeQuery("show tables like '" + prefix + "multiinv'");
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
	
	public boolean chestTableExists() {
		Statement st;
		try {
			st = con.createStatement();
	        ResultSet rs = st.executeQuery("show tables like '" + prefix + "enderchestinv'");
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
	
	public boolean createTable() {
		Statement st;
		try {
			st = con.createStatement();
	        st.executeUpdate("CREATE TABLE `" + prefix + "multiinv` (" +
	        		"`inv_id` INT( 11 ) NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
	        		"`inv_group` VARCHAR( 50 ) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL COMMENT 'Inventory group.', " +
	        		"`inv_player` VARCHAR( 16 ) CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL COMMENT 'Minecraft player name.', " +
	        		"`inv_gamemode` ENUM('CREATIVE','SURVIVAL') CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL COMMENT 'CREATIVE or SURVIVAL game mode.', " +
	        		"`inv_health` TINYINT( 4 ) NOT NULL COMMENT 'Valid values are 0 to 20.', " +
	        		"`inv_hunger` TINYINT( 4 ) NOT NULL COMMENT 'Valid values are 0 to 20.', " +
	        		"`inv_saturation` DOUBLE NOT NULL COMMENT 'Valid values are 0.0 to 20.0.', " +
	        		"`inv_level` SMALLINT( 6 ) NOT NULL, " +
	        		"`inv_experience` INT( 11 ) NOT NULL, " +
	        		"`inv_survival` text NOT NULL, " +
	        		"`inv_creative` text NOT NULL, " +
	        		"UNIQUE KEY `unique_player_group` ( `inv_player` , `inv_group` ) ) ENGINE=InnoDB DEFAULT CHARSET=latin1");
	        return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean createChestTable() {
		Statement st;
		try {
			st = con.createStatement();
	        st.executeUpdate("CREATE TABLE `" + prefix + "enderchestinv` (" +
	        		"`inv_id` INT( 11 ) NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
	        		"`inv_group` VARCHAR( 50 ) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL COMMENT 'Inventory group.', " +
	        		"`chest_player` VARCHAR( 16 ) CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL COMMENT 'Minecraft player name.', " +
	        		"`chest_survival` text NOT NULL, " +
	        		"`chest_creative` text NOT NULL, " +
	        		"`chest_adventure` text NOT NULL, " +
	        		"UNIQUE KEY `unique_player_group` ( `chest_player` , `inv_group` ) ) ENGINE=InnoDB DEFAULT CHARSET=latin1");
	        return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	

	
	public MIEnderchestInventory getEnderchestInventory(String player, String group, String inventoryName) {
        // Get stored string from configuration file
        MIEnderchestInventory inventory = new MIEnderchestInventory((String)null);
        try {
        	Statement st = con.createStatement();
	        ResultSet rs = st.executeQuery("SELECT * FROM " + prefix + "enderchestinv WHERE chest_player='" + player + "' AND inv_group='" + group + "'");
	        if(rs.next()) {
	        	String inventoryString = rs.getString("chest_" + inventoryName.toLowerCase());
	            inventory = new MIEnderchestInventory(inventoryString);
	        }
		} catch (SQLException e) {
			//e.printStackTrace();
		}
		return inventory;
    }
	
	public MIInventory getInventory(String player, String group, String inventoryName) {
        // Get stored string from configuration file
        MIInventory inventory = new MIInventory((String)null);
        try {
        	Statement st = con.createStatement();
	        ResultSet rs = st.executeQuery("SELECT * FROM " + prefix + "multiinv WHERE inv_player='" + player + "' AND inv_group='" + group + "'");
	        if(rs.next()) {
	        	String inventoryString = rs.getString("inv_" + inventoryName.toLowerCase());
	            inventory = new MIInventory(inventoryString);
	        }
		} catch (SQLException e) {
			//e.printStackTrace();
		}
		return inventory;
    }

    public void saveInventory(String player, String group, MIInventory inventory, String inventoryName){
        String inventoryString = inventory.toString();
        //Call this just to make sure the player record has been created.
        createRecord(player, group);
        try {
        	Statement st = con.createStatement();
	        st.executeUpdate("UPDATE " + prefix + "multiinv SET inv_" + inventoryName.toLowerCase() + "='" + inventoryString + "' WHERE inv_player='"+ player + "' AND inv_group='" + group + "'");
	    } catch (SQLException e) {
			e.printStackTrace();
		}
    }

    public void saveEnderchestInventory(String player, String group, MIEnderchestInventory inventory, String inventoryName){
        String inventoryString = inventory.toString();
        //Call this just to make sure the player record has been created.
        createRecord(player, group);
        try {
        	Statement st = con.createStatement();
	        st.executeUpdate("UPDATE " + prefix + "enderchestinv SET inv_" + inventoryName.toLowerCase() + "='" + inventoryString + "' WHERE chest_player='"+ player + "' AND inv_group='" + group + "'");
	    } catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    public void createRecord(String player, String group) {
    	if(!tableExists()) {
    		createTable();
    	}
    	try {
        	Statement st = con.createStatement();
	        ResultSet rs = st.executeQuery("SELECT * FROM " + prefix + "multiinv WHERE inv_player='" + player + "' AND inv_group='" + group + "'");
	        if(!rs.next()) {
	        	st.executeUpdate("INSERT INTO " + prefix + "multiinv (inv_player, inv_group, inv_gamemode, inv_health, inv_hunger, inv_saturation, inv_level, inv_experience, inv_survival, inv_creative) " +
	        			"VALUES('" + player + "', '" + group + "', 'SURVIVAL', 20, 20, 5, 0, 0, '', '')");
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    public void createChestRecord(String player, String group) {
    	if(!chestTableExists()) {
    		createChestTable();
    	}
    	try {
        	Statement st = con.createStatement();
	        ResultSet rs = st.executeQuery("SELECT * FROM " + prefix + "enderchestinv WHERE chest_player='" + player + "' AND inv_group='" + group + "'");
	        if(!rs.next()) {
	        	st.executeUpdate("INSERT INTO " + prefix + "enderchestinv (chest_player, inv_group, chest_survival, chest_creative) " +
	        			"VALUES('" + player + "', '" + group + "', '', '')");
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    public int getHealth(String player, String group){
        int health = 20;
        try {
        	Statement st = con.createStatement();
	        ResultSet rs = st.executeQuery("SELECT * FROM " + prefix + "multiinv WHERE inv_player='" + player + "' AND inv_group='" + group + "'");
	        if(rs.next()) {
	        	health = rs.getInt("inv_health");
	        }
		} catch (SQLException e) {
			//e.printStackTrace();
		}
        if (health > 20) {
            health = 20;
        }
        return health;
    }

    public void saveHealth(String player, String group, int health){
        //Call this just to make sure the player record has been created.
    	createRecord(player, group);
        try {
        	Statement st = con.createStatement();
	        st.executeUpdate("UPDATE " + prefix + "multiinv SET inv_health='" + health + "' WHERE inv_player='"+ player + "' AND inv_group='" + group + "'");
	    } catch (SQLException e) {
			e.printStackTrace();
		}
    }

    public GameMode getGameMode(String player, String group){
        String gameModeString = null;
        try {
        	Statement st = con.createStatement();
	        ResultSet rs = st.executeQuery("SELECT * FROM " + prefix + "multiinv WHERE inv_player='" + player + "' AND inv_group='" + group + "'");
	        if(rs.next()) {
	        	gameModeString = rs.getString("inv_gamemode");
	        }
		} catch (SQLException e) {
			//e.printStackTrace();
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
	        st.executeUpdate("UPDATE " + prefix + "multiinv SET inv_gamemode='" + gameMode.toString() + "' WHERE inv_player='"+ player + "' AND inv_group='" + group + "'");
	    } catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    public int getHunger(String player, String group){
        int hunger = 20;
        try {
        	Statement st = con.createStatement();
	        ResultSet rs = st.executeQuery("SELECT * FROM " + prefix + "multiinv WHERE inv_player='" + player + "' AND inv_group='" + group + "'");
	        if(rs.next()) {
	        	hunger = rs.getInt("inv_hunger");
	        }
		} catch (SQLException e) {
			//e.printStackTrace();
		}
        if (hunger > 20) {
            hunger = 20;
        }
        return hunger;
    }

    public float getSaturation(String player, String group){
        double saturationDouble = 5;
        try {
        	Statement st = con.createStatement();
	        ResultSet rs = st.executeQuery("SELECT * FROM " + prefix + "multiinv WHERE inv_player='" + player + "' AND inv_group='" + group + "'");
	        if(rs.next()) {
	        	saturationDouble = rs.getDouble("inv_saturation");
	        }
		} catch (SQLException e) {
			//e.printStackTrace();
		}
        float saturation = (float)saturationDouble;
        return saturation;
    }

    public void saveSaturation(String player, String group, float saturation){
    	//Call this just to make sure the player record has been created.
    	createRecord(player, group);
        try {
        	Statement st = con.createStatement();
	        st.executeUpdate("UPDATE " + prefix + "multiinv SET inv_saturation='" + saturation + "' WHERE inv_player='"+ player + "' AND inv_group='" + group + "'");
	    } catch (SQLException e) {
			e.printStackTrace();
		}
    }

    public int getTotalExperience(String player, String group){
    	int experience = 0;
    	try {
        	Statement st = con.createStatement();
	        ResultSet rs = st.executeQuery("SELECT * FROM " + prefix + "multiinv WHERE inv_player='" + player + "' AND inv_group='" + group + "'");
	        if(rs.next()) {
	        	experience = rs.getInt("inv_experience");
	        }
		} catch (SQLException e) {
			//e.printStackTrace();
		}
        return experience;
    }

    public void saveExperience(String player, String group, int experience){
    	//Call this just to make sure the player record has been created.
    	createRecord(player, group);
        try {
        	Statement st = con.createStatement();
	        st.executeUpdate("UPDATE " + prefix + "multiinv SET inv_experience='" + experience + "' WHERE inv_player='"+ player + "' AND inv_group='" + group + "'");
	    } catch (SQLException e) {
			e.printStackTrace();
		}
    }

    public void saveHunger(String player, String group, int hunger){
    	createRecord(player, group);
        try {
        	Statement st = con.createStatement();
	        st.executeUpdate("UPDATE " + prefix + "multiinv SET inv_hunger='" + hunger + "' WHERE inv_player='"+ player + "' AND inv_group='" + group + "'");
	    } catch (SQLException e) {
			e.printStackTrace();
		}
    }

}
