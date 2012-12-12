package uk.co.tggl.pluckerpluck.multiinv.inventory;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import Tux2.TuxTwoLib.BookItem;
import Tux2.TuxTwoLib.TuxTwoNBTData;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.books.MIBook;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 */
public class MIItemStack {

    private int itemID = 0;
    private int quantity = 0;
    private short durability = 0;
    private Map<Enchantment, Integer>  enchantments = new HashMap<Enchantment, Integer>();
    private MIBook book = null;
    String nbttags = null;

    public MIItemStack(ItemStack itemStack){
        if (itemStack != null){
            itemID = itemStack.getTypeId();
            quantity = itemStack.getAmount();
            durability = itemStack.getDurability();
            enchantments = itemStack.getEnchantments();
            if(itemID == 386 || itemID == 387) {
            	BookItem bi = new BookItem(itemStack);
            	//Make sure we don't use this on an empty book!
            	if(bi.getPages() != null) {
            		book = new MIBook(bi.getAuthor(), bi.getTitle(), bi.getPages());
            	}
            }else {
            	nbttags = TuxTwoNBTData.readOutNBTString(itemStack);
            }
        }
    }

    // Constructor to create an MIItemStack from a string containing its data
    public MIItemStack(String dataString){
        String[] data = dataString.split(",");
        if (data.length >= 4) {
            itemID = Integer.parseInt(data[0]);
            quantity = Integer.parseInt(data[1]);
            durability = Short.parseShort(data[2]);
            getEnchantments(data[3]);
            if(data.length > 4) {
            	String unserialized = new String(javax.xml.bind.DatatypeConverter.parseBase64Binary(data[4]));
            	//Don't read in the old format.
            	if(!unserialized.startsWith("<?")) {
            		nbttags = data[4];
            	}
            }
        }
    }

    public MIItemStack(){

    }

    public ItemStack getItemStack(){
        ItemStack itemStack = null;
        if (itemID != 0 && quantity != 0){
            itemStack = new ItemStack(itemID, quantity, durability);
            itemStack.addUnsafeEnchantments(enchantments);
            if((itemID == 386 || itemID == 387) && book != null) {
            	BookItem bi = new BookItem(itemStack);
            	bi.setAuthor(book.getAuthor());
            	bi.setTitle(book.getTitle());
            	bi.setPages(book.getPages());
            	return bi.getItemStack();
            }else if(nbttags != null) {
            	return TuxTwoNBTData.readInNBTString(itemStack, nbttags);
            }
        }
        return itemStack;
    }

    public String toString() {
    	if(nbttags != null) {
            return itemID + "," + quantity + "," + durability + "," + getEnchantmentString() + "," + nbttags;
    	}else {
            return itemID + "," + quantity + "," + durability + "," + getEnchantmentString();
    	}
    }

	private String getEnchantmentString(){
    	if(book != null) {
    		return "book_" + book.getHashcode();
    	}else {
            String string = "";
            for (Enchantment enchantment : enchantments.keySet()){
                string = string + enchantment.getId() + "-" + enchantments.get(enchantment) + "#";
            }
            if ("".equals(string)){
                string = "0";
            }else{
                string = string.substring(0, string.length() - 1);
            }
            return string;
    	}
    }

    private void getEnchantments(String enchantmentString){
    	//if books ever have enchantments, that will be the end of me...
    	//Hijack this function to import book data...
    	if(enchantmentString.startsWith("book_")) {
    		if (MIYamlFiles.config.getBoolean("useSQL")) {
    			book = MIYamlFiles.con.getBook(enchantmentString, true);
    		}else {
        		book = new MIBook(new File(Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder() + File.separator + 
        				"books" + File.separator + enchantmentString + ".yml"));
    		}
    	}else if (!"0".equals(enchantmentString)){
            String[] enchantments = enchantmentString.split("#");
            for (String enchantment : enchantments){
                String[] parts = enchantment.split("-");
                int ID = Integer.parseInt(parts[0]);
                int level = Integer.parseInt(parts[1]);
                Enchantment e = Enchantment.getById(ID);
                this.enchantments.put(e, level);
            }
        }
    }

}
