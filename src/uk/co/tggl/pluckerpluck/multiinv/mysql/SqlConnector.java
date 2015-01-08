package uk.co.tggl.pluckerpluck.multiinv.mysql;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.books.MIBook;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIEnderchestInventory;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventory;

public class SqlConnector implements Runnable {
    
    private Connection con;
    private String prefix = "multiinv_";
    private String url;
    private String username;
    private String password;
    private ConcurrentLinkedQueue<MISqlStatement> sqlstatements = new ConcurrentLinkedQueue<MISqlStatement>();
    
    public SqlConnector(Connection con, String prefix, String url, String username, String password) {
        this.con = con;
        this.prefix = prefix;
        this.url = url;
        this.username = username;
        this.password = password;
        if(!tableExists()) {
            createTable();
        }
        if(!inventoryColumnExists("ADVENTURE")) {
            addInventoryColumn("ADVENTURE");
        }
        if(!inventoryColumnExists("SPECTATOR")) {
            addInventoryColumn("SPECTATOR");
        }
        if(chestColumnExists("chest_chest_SPECTATOR")) {
            try {
                Statement st = con.createStatement();
                st.executeUpdate("ALTER TABLE `" + prefix + "enderchestinv` CHANGE `chest_chest_spectator` `chest_spectator` TEXT CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL");
            } catch(SQLException e) {
                e.printStackTrace();
				/*try {
	                Statement st = con.createStatement();
	                st.executeUpdate("ALTER TABLE `" + prefix + "enderchestinv` DROP `chest_chest_spectator`");
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}*/
            }
        }
        if(!chestColumnExists("chest_SPECTATOR")) {
            addChestColumn("spectator");
        }
        if(!inventoryColumnExists("uuid")) {
            convertToUUID();
        }
        if(!chestTableExists()) {
            createChestTable();
        }
        if(!chestColumnExists("inv_uuid")) {
        	convertChestToUUID();
        }
        if(!bookTableExists()) {
            createBookTable();
        }
        if(columnPrecision("multiinv", "inv_player") < 30) {
        	setColumnPrecision("multiinv", "inv_player", "VARCHAR", 30);
        }
        if(columnPrecision("enderchestinv", "chest_player") < 30) {
        	setColumnPrecision("enderchestinv", "chest_player", "VARCHAR", 30);
        }
        if(!getColumnType("multiinv", "inv_health").equalsIgnoreCase("DOUBLE")) {
        	setColumnType("multiinv", "inv_health", "DOUBLE");
        }
        
    }
    
    public String getColumnType(String table, String column) {
    	Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT '" + column + "' FROM `" + prefix + table + "`;");
            if(rs.next()) {
            	ResultSetMetaData rsmd = rs.getMetaData();
            	return rsmd.getColumnTypeName(rs.findColumn(column));
            } else {
                return "";
            }
        } catch(SQLException e) {
            e.printStackTrace();
            return "";
        }
    }
    
    public boolean setColumnType(String table, String column, String type) {
    	Statement st;
        try {
            st = con.createStatement();
            st.executeUpdate("ALTER TABLE `" + prefix + table + "` modify `" + column + "` " + type.toUpperCase() + " NOT NULL");
            return true;
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean setColumnPrecision(String table, String column, String type, int precision) {
    	Statement st;
        try {
            st = con.createStatement();
            st.executeUpdate("ALTER TABLE `" + prefix + table + "` modify `" + column + "` " + type.toUpperCase() + "(" + String.valueOf(precision) + ") NOT NULL");
            return true;
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void refreshConnection() {
        try {
        	if(con.isValid(1)) {
        		return;
        	}
            con.close();
        } catch(SQLException e) {
            // We don't need to do anything if the connection isn't there...
        }
        try {
            con = DriverManager.getConnection(url, username, password);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
    
    public boolean tableExists() {
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("show tables like '" + prefix + "multiinv'");
            if(rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch(SQLException e) {
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
            } else {
                return false;
            }
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean bookTableExists() {
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("show tables like '" + prefix + "books'");
            if(rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean inventoryColumnExists(String gamemode) {
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SHOW COLUMNS FROM `" + prefix + "multiinv` LIKE 'inv_" + gamemode.toLowerCase() + "';");
            if(rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean chestColumnExists(String columnname) {
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SHOW COLUMNS FROM `" + prefix + "enderchestinv` LIKE '" + columnname.toLowerCase() + "';");
            if(rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Returns the column's precision.
     * @param table The table name, without the prefix.
     * @param column The column name you want to check precision on.
     * @return Precision of the column or -1 if there was an error.
     */
    public int columnPrecision(String table, String column) {
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT '" + column + "' FROM `" + prefix + table + "`;");
            if(rs.next()) {
            	ResultSetMetaData rsmd = rs.getMetaData();
            	return rsmd.getPrecision(rs.findColumn(column));
            } else {
                return -1;
            }
        } catch(SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    public boolean addInventoryColumn(String gamemode) {
        Statement st;
        try {
            st = con.createStatement();
            st.executeUpdate("ALTER TABLE `" + prefix + "multiinv` ADD `inv_" + gamemode.toLowerCase()
                    + "` TEXT CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL");
            return true;
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean addChestColumn(String gamemode) {
        Statement st;
        try {
            st = con.createStatement();
            st.executeUpdate("ALTER TABLE `" + prefix + "enderchestinv` ADD `chest_" + gamemode.toLowerCase()
                    + "` TEXT CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL");
            return true;
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void convertToUUID() {
    	//TODO: convert to UUID
    	addInventoryColumn("uuid");
        Statement st;
        try {
        	st = con.createStatement();
            st.executeUpdate("ALTER TABLE `" + prefix + "enderchestinv` ADD `inv_uuid` TEXT CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL");
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM `" + prefix + "multiinv" + "`;");
            while(rs.next()) {
            	String playername = rs.getString("inv_player");
            	String uuid = Bukkit.getServer().getOfflinePlayer(playername).getUniqueId().toString();
            	System.out.println("Setting " + playername + "'s uuid to: " + uuid);
                Statement st1 = con.createStatement();
                st1.executeUpdate("UPDATE " + prefix + "multiinv SET inv_uuid='" + uuid + "' WHERE inv_player='" + playername + "'");
                st1.executeUpdate("UPDATE " + prefix + "enderchestinv SET inv_uuid='" + uuid + "' WHERE chest_player='" + playername + "'");
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void convertChestToUUID() {
    	//TODO: convert to UUID
        Statement st;
        try {
        	st = con.createStatement();
            st.executeUpdate("ALTER TABLE `" + prefix + "enderchestinv` ADD `inv_uuid` TEXT CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL");
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM `" + prefix + "enderchestinv" + "`;");
            while(rs.next()) {
            	String playername = rs.getString("chest_player");
            	String uuid = Bukkit.getServer().getOfflinePlayer(playername).getUniqueId().toString();
            	System.out.println("Setting " + playername + "'s uuid to: " + uuid);
                Statement st1 = con.createStatement();
                st1.executeUpdate("UPDATE " + prefix + "enderchestinv SET inv_uuid='" + uuid + "' WHERE chest_player='" + playername + "'");
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
    
    public boolean createTable() {
        Statement st;
        try {
            st = con.createStatement();
            st.executeUpdate("CREATE TABLE `"
                    + prefix
                    + "multiinv` ("
                    +
                    "`inv_id` INT( 11 ) NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                    +
                    "`inv_group` VARCHAR( 50 ) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL COMMENT 'Inventory group.', "
                    +
                    "`inv_player` VARCHAR( 30 ) CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL COMMENT 'Minecraft player name.', "
                    +
                    "`inv_uuid` VARCHAR( 36 ) CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL COMMENT 'Minecraft player UUID.', "
                    +
                    "`inv_gamemode` ENUM('ADVENTURE','CREATIVE','SURVIVAL') CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL COMMENT 'ADVENTURE, CREATIVE or SURVIVAL game mode.', "
                    +
                    "`inv_health` DOUBLE NOT NULL COMMENT 'Valid values are 0 to 20.', " +
                    "`inv_hunger` TINYINT( 4 ) NOT NULL COMMENT 'Valid values are 0 to 20.', " +
                    "`inv_saturation` DOUBLE NOT NULL COMMENT 'Valid values are 0.0 to 20.0.', " +
                    "`inv_level` SMALLINT( 6 ) NOT NULL, " +
                    "`inv_experience` INT( 11 ) NOT NULL, " +
                    "`inv_survival` text NOT NULL, " +
                    "`inv_creative` text NOT NULL, " +
                    "`inv_adventure` text NOT NULL, " +
                    "`inv_spectator` text NOT NULL, " +
                    "UNIQUE KEY `unique_player_group` ( `inv_uuid` , `inv_group` ) ) ENGINE=InnoDB DEFAULT CHARSET=latin1");
            return true;
        } catch(SQLException e) {
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
                    "`chest_player` VARCHAR( 30 ) CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL COMMENT 'Minecraft player name.', " +
                    "`inv_uuid` VARCHAR( 36 ) CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL COMMENT 'Minecraft player UUID.', " +
                    "`chest_survival` text NOT NULL, " +
                    "`chest_creative` text NOT NULL, " +
                    "`chest_adventure` text NOT NULL, " +
                    "`chest_spectator` text NOT NULL, " +
                    "UNIQUE KEY `unique_player_group` ( `inv_uuid` , `inv_group` ) ) ENGINE=InnoDB DEFAULT CHARSET=latin1");
            return true;
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean createBookTable() {
        Statement st;
        try {
            st = con.createStatement();
            st.executeUpdate("CREATE TABLE `" + prefix + "books` (" +
                    "`book_hash` VARCHAR( 37 ) NOT NULL PRIMARY KEY, " +
                    "`book_author` VARCHAR( 35 ) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL COMMENT 'The book author', " +
                    "`book_title` VARCHAR( 35 ) CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL COMMENT 'Book title.', " +
                    "`book_contents` text NOT NULL ) ENGINE=InnoDB DEFAULT CHARSET=latin1");
            return true;
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void saveBook(MIBook book, boolean immediate) {
    	if(immediate) {
            try {
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM " + prefix + "books WHERE book_hash='book_" + book.getHashcode() + "'");
                if(!rs.next()) {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    XMLEncoder xml = new XMLEncoder(os);
                    xml.writeObject(book.getPages());
                    xml.close();
                    PreparedStatement addbook = null;
                    con.setAutoCommit(false);
                    addbook = con.prepareStatement("INSERT INTO " + prefix + "books (book_hash, book_author, book_title, book_contents) " +
                            "VALUES('book_" + book.getHashcode() + "', ?, ?, ?)");
                    //Don't save nulls
                    if(book.getAuthor() == null) {
                        addbook.setString(1, "");
                    }else {
                        addbook.setString(1, book.getAuthor());
                    }
                    if(book.getTitle() == null) {
                        addbook.setString(2, "");
                    }else {
                        addbook.setString(2, book.getTitle());
                    }
                    addbook.setString(3, new String(os.toByteArray()));
                    addbook.executeUpdate();
                    con.commit();
                    con.setAutoCommit(true);
                }
            } catch(SQLException e) {
                e.printStackTrace();
            }
    	}else {
    		MISqlStatement st = new MISqlStatement(book);
    	}
    }
    
    public MIBook getBook(String bookhash, boolean bookprefix) {
        if(!bookTableExists()) {
            createBookTable();
        }
        if(bookprefix == false) {
            bookhash = "book_" + bookhash;
        }
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM " + prefix + "books WHERE book_hash='" + bookhash + "'");
            if(rs.next()) {
                String bookcontentsxml = rs.getString("book_contents");
                ByteArrayInputStream os = new ByteArrayInputStream(bookcontentsxml.getBytes());
                XMLDecoder xml = new XMLDecoder(os);
                String[] pages = (String[]) xml.readObject();
                xml.close();
                String author = rs.getString("book_author");
                //reconvert to null
                if(author != null && author.equals("")) {
                    author = null;
                }
                String title = rs.getString("book_title");
                if(title != null && title.equals("")) {
                    title = null;
                }
                String hashcode = rs.getString("book_hash").substring(5);
                MIBook book = new MIBook(hashcode, author, title, pages);
                return book;
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public MIEnderchestInventory getEnderchestInventory(OfflinePlayer player, String group, String inventoryName) {
        // Get stored string from configuration file
        MIEnderchestInventory inventory = new MIEnderchestInventory();
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM " + prefix + "enderchestinv WHERE inv_uuid='" + player.getUniqueId().toString() + "' AND inv_group='" + group + "'");
            if(rs.next()) {
                String inventoryString = rs.getString("chest_" + inventoryName.toLowerCase());
                inventory = new MIEnderchestInventory(inventoryString);
            }
        } catch(SQLException e) {
            // e.printStackTrace();
        }
        return inventory;
    }
    
    public MIInventory getInventory(OfflinePlayer player, String group, String inventoryName) {
        // Get stored string from configuration file
        MIInventory inventory = new MIInventory((String) null);
        // Let's add inventory gamemode columns dynamically for newer versions of minecraft
        if(!inventoryColumnExists(inventoryName)) {
            addInventoryColumn(inventoryName);
        }
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM " + prefix + "multiinv WHERE inv_uuid='" + player.getUniqueId().toString() + "' AND inv_group='" + group + "'");
            if(rs.next()) {
                String inventoryString = rs.getString("inv_" + inventoryName.toLowerCase());
                inventory = new MIInventory(inventoryString);
            }
        } catch(SQLException e) {
            // e.printStackTrace();
        }
        return inventory;
    }
    
    public void saveAll(OfflinePlayer player, String group, MIInventory inventory, String inventoryName,
    		int experience, GameMode gameMode, double health, int hunger, float saturation) {
    	String inventoryString = inventory.toString();
        // Call this just to make sure the player record has been created.
        //createRecord(player, group);
        // Let's add inventory gamemode columns dynamically for newer versions of minecraft
        /*if(!inventoryColumnExists(inventoryName)) {
            addInventoryColumn(inventoryName);
        }*/
        String st = "UPDATE " + prefix + "multiinv SET inv_" + inventoryName.toLowerCase() + "='" + 
                inventoryString + "', inv_experience='" + experience + "', inv_gamemode='" + gameMode.toString() + 
                "', inv_health='" + health + "', inv_hunger='" + hunger + "', inv_saturation='" + saturation + 
                "' WHERE inv_uuid='" + player.getUniqueId().toString() + "' AND inv_group='" + group + "'";
    	MISqlStatement statement = new MISqlStatement(st, player, group);
        statement.setCheckPlayerInv(true);
        statement.setInventoryColumn(inventoryName);
        sqlstatements.add(statement);
        /*try {
            Statement st = con.createStatement();
            st.executeUpdate("UPDATE " + prefix + "multiinv SET inv_" + inventoryName.toLowerCase() + "='" + 
            inventoryString + "', inv_experience='" + experience + "', inv_gamemode='" + gameMode.toString() + 
            "', inv_health='" + health + "', inv_hunger='" + hunger + "', inv_saturation='" + saturation + 
            "' WHERE inv_uuid='" + player.getUniqueId().toString() + "' AND inv_group='" + group + "'");
        } catch(SQLException e) {
            e.printStackTrace();
        }*/
    }
    
    public void saveInventory(OfflinePlayer player, String group, MIInventory inventory, String inventoryName) {
    	if(player == null || group == null || inventory == null || inventoryName == null) {
    		MultiInv.log.warning("Unable to save inventory of player due to a null string...");
    		return;
    	}
        String inventoryString = inventory.toString();
        /*// Call this just to make sure the player record has been created.
        createRecord(player, group);
        // Let's add inventory gamemode columns dynamically for newer versions of minecraft
        if(!inventoryColumnExists(inventoryName)) {
            addInventoryColumn(inventoryName);
        }*/
        String st = "UPDATE " + prefix + "multiinv SET inv_" + inventoryName.toLowerCase() + "='" + inventoryString + "' WHERE inv_uuid='" + player.getUniqueId().toString()
                + "' AND inv_group='" + group + "'";
        MISqlStatement statement = new MISqlStatement(st, player, group);
        statement.setCheckPlayerInv(true);
        statement.setInventoryColumn(inventoryName);
        sqlstatements.add(statement);
        /*try {
            Statement st = con.createStatement();
            st.executeUpdate("UPDATE " + prefix + "multiinv SET inv_" + inventoryName.toLowerCase() + "='" + inventoryString + "' WHERE inv_uuid='" + player.getUniqueId().toString()
                    + "' AND inv_group='" + group + "'");
        } catch(SQLException e) {
            e.printStackTrace();
        }*/
    }
    
    public void saveEnderchestInventory(OfflinePlayer player, String group, MIEnderchestInventory inventory, String inventoryName) {
        String inventoryString = inventory.toString();
        // Call this just to make sure the player record has been created.
        String st = "UPDATE " + prefix + "enderchestinv SET chest_" + inventoryName.toLowerCase() + "='" + inventoryString + "' WHERE inv_uuid='"
                + player.getUniqueId().toString() + "' AND inv_group='" + group + "'";
        //createChestRecord(player, group);
        MISqlStatement statement = new MISqlStatement(st, player, group);
        statement.setCheckChestInv(true);
        sqlstatements.add(statement);
        /*try {
            Statement st = con.createStatement();
            st.executeUpdate("UPDATE " + prefix + "enderchestinv SET chest_" + inventoryName.toLowerCase() + "='" + inventoryString + "' WHERE inv_uuid='"
                    + player.getUniqueId().toString() + "' AND inv_group='" + group + "'");
        } catch(SQLException e) {
            e.printStackTrace();
        }*/
    }
    
    public void createRecord(OfflinePlayer player, String group) {
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM " + prefix + "multiinv WHERE inv_uuid='" + player.getUniqueId().toString() + "' AND inv_group='" + group + "'");
            if(!rs.next()) {
                st.executeUpdate("INSERT INTO "
                        + prefix
                        + "multiinv (inv_player, inv_uuid, inv_group, inv_gamemode, inv_health, inv_hunger, inv_saturation, inv_level, inv_experience, inv_survival, inv_creative, inv_ADVENTURE, inv_SPECTATOR) "
                        +
                        "VALUES('" + player.getName() + "', '" + player.getUniqueId().toString() + "', '" + group + "', 'SURVIVAL', 20, 20, 5, 0, 0, '', '', '', '')");
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void createChestRecord(OfflinePlayer player, String group) {
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM " + prefix + "enderchestinv WHERE inv_uuid='" + player.getUniqueId().toString() + "' AND inv_group='" + group + "'");
            if(!rs.next()) {
                st.executeUpdate("INSERT INTO " + prefix + "enderchestinv (chest_player, inv_uuid, inv_group, chest_survival, chest_creative, chest_adventure, chest_spectator) " +
                        "VALUES('" + player.getName() + "', '" + player.getUniqueId().toString() + "', '" + group + "', '', '', '', '')");
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
    
    public double getHealth(OfflinePlayer player, String group) {
        double health = 20;
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM " + prefix + "multiinv WHERE inv_uuid='" + player.getUniqueId().toString() + "' AND inv_group='" + group + "'");
            if(rs.next()) {
                health = rs.getDouble("inv_health");
            }
        } catch(SQLException e) {
            // e.printStackTrace();
        }
        if(health > 20) {
            health = 20;
        }
        return health;
    }
    
    public void saveHealth(OfflinePlayer player, String group, double health) {
        // Call this just to make sure the player record has been created.
        createRecord(player, group);
        try {
            Statement st = con.createStatement();
            st.executeUpdate("UPDATE " + prefix + "multiinv SET inv_health='" + health + "' WHERE inv_uuid='" + player.getUniqueId().toString() + "' AND inv_group='" + group + "'");
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
    
    public GameMode getGameMode(OfflinePlayer player, String group) {
        String gameModeString = null;
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM " + prefix + "multiinv WHERE inv_uuid='" + player.getUniqueId().toString() + "' AND inv_group='" + group + "'");
            if(rs.next()) {
                gameModeString = rs.getString("inv_gamemode");
            }
        } catch(SQLException e) {
            // e.printStackTrace();
        }
        GameMode gameMode = null;
        if("CREATIVE".equalsIgnoreCase(gameModeString)) {
            gameMode = GameMode.CREATIVE;
        } else if("SURVIVAL".equalsIgnoreCase(gameModeString)) {
            gameMode = GameMode.SURVIVAL;
        }
        return gameMode;
    }
    
    public void saveGameMode(OfflinePlayer player, String group, GameMode gameMode) {
        // Call this just to make sure the player record has been created.
        createRecord(player, group);
        try {
            Statement st = con.createStatement();
            st.executeUpdate("UPDATE " + prefix + "multiinv SET inv_gamemode='" + gameMode.toString() + "' WHERE inv_uuid='" + player.getUniqueId().toString() + "' AND inv_group='"
                    + group + "'");
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
    
    public int getHunger(OfflinePlayer player, String group) {
        int hunger = 20;
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM " + prefix + "multiinv WHERE inv_uuid='" + player.getUniqueId().toString() + "' AND inv_group='" + group + "'");
            if(rs.next()) {
                hunger = rs.getInt("inv_hunger");
            }
        } catch(SQLException e) {
            // e.printStackTrace();
        }
        if(hunger > 20) {
            hunger = 20;
        }
        return hunger;
    }
    
    public float getSaturation(OfflinePlayer player, String group) {
        double saturationDouble = 5;
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM " + prefix + "multiinv WHERE inv_uuid='" + player.getUniqueId().toString() + "' AND inv_group='" + group + "'");
            if(rs.next()) {
                saturationDouble = rs.getDouble("inv_saturation");
            }
        } catch(SQLException e) {
            // e.printStackTrace();
        }
        float saturation = (float) saturationDouble;
        return saturation;
    }
    
    public void saveSaturation(OfflinePlayer player, String group, float saturation) {
        // Call this just to make sure the player record has been created.
        //createRecord(player, group);
    	String st = "UPDATE " + prefix + "multiinv SET inv_saturation='" + saturation + "' WHERE inv_uuid='" + player.getUniqueId().toString() + "' AND inv_group='" + group
                + "'";
        MISqlStatement statement = new MISqlStatement(st, player, group);
        statement.setCheckPlayerInv(true);
        sqlstatements.add(statement);
        /*try {
            Statement st = con.createStatement();
            st.executeUpdate("UPDATE " + prefix + "multiinv SET inv_saturation='" + saturation + "' WHERE inv_uuid='" + player.getUniqueId().toString() + "' AND inv_group='" + group
                    + "'");
        } catch(SQLException e) {
            e.printStackTrace();
        }*/
    }
    
    public int getTotalExperience(OfflinePlayer player, String group) {
        int experience = 0;
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM " + prefix + "multiinv WHERE inv_uuid='" + player.getUniqueId().toString() + "' AND inv_group='" + group + "'");
            if(rs.next()) {
                experience = rs.getInt("inv_experience");
            }
        } catch(SQLException e) {
            // e.printStackTrace();
        }
        return experience;
    }
    
    public void saveExperience(OfflinePlayer player, String group, int experience) {
        // Call this just to make sure the player record has been created.
        //createRecord(player, group);
    	String st = "UPDATE " + prefix + "multiinv SET inv_experience='" + experience + "' WHERE inv_uuid='" + player.getUniqueId().toString() + "' AND inv_group='" + group
                + "'";
        MISqlStatement statement = new MISqlStatement(st, player, group);
        statement.setCheckPlayerInv(true);
        sqlstatements.add(statement);
        /*try {
            Statement st = con.createStatement();
            st.executeUpdate("UPDATE " + prefix + "multiinv SET inv_experience='" + experience + "' WHERE inv_uuid='" + player.getUniqueId().toString() + "' AND inv_group='" + group
                    + "'");
        } catch(SQLException e) {
            e.printStackTrace();
        }*/
    }
    
    public void saveHunger(OfflinePlayer player, String group, int hunger) {
        //createRecord(player, group);
        String st = "UPDATE " + prefix + "multiinv SET inv_hunger='" + hunger + "' WHERE inv_uuid='" + player.getUniqueId().toString() + "' AND inv_group='" + group + "'";
        MISqlStatement statement = new MISqlStatement(st, player, group);
        statement.setCheckPlayerInv(true);
        sqlstatements.add(statement);
        /*try {
            Statement st = con.createStatement();
            st.executeUpdate("UPDATE " + prefix + "multiinv SET inv_hunger='" + hunger + "' WHERE inv_uuid='" + player.getUniqueId().toString() + "' AND inv_group='" + group + "'");
        } catch(SQLException e) {
            e.printStackTrace();
        }*/
    }

	@Override
	public void run() {
		while(!sqlstatements.isEmpty()) {
			try {
				MISqlStatement statement = sqlstatements.poll();
				if(statement == null) {
					continue;
				}
				if(statement.getBook() != null) {
					saveBook(statement.getBook(), true);
					continue;
				}
				if(statement.checkPlayerInv()) {
			        createRecord(statement.getPlayer(), statement.getGroup());
				}
				if(statement.checkChestInv()) {
					createChestRecord(statement.getPlayer(), statement.getGroup());
				}
				if(statement.getInventoryColumn() != null) {
			        if(!inventoryColumnExists(statement.getInventoryColumn())) {
			            addInventoryColumn(statement.getInventoryColumn());
			        }
				}
	            Statement st = con.createStatement();
	            st.executeUpdate(statement.getStatement());
	        } catch(SQLException e) {
	            e.printStackTrace();
	        }
		}
	}
}
