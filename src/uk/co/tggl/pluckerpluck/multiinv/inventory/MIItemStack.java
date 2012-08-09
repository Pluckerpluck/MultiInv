package uk.co.tggl.pluckerpluck.multiinv.inventory;

import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.books.MIBook;

import java.awt.print.Book;
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
    private String bookid = "";
    private MIBook book = null;

    public MIItemStack(ItemStack itemStack){
        if (itemStack != null){
            itemID = itemStack.getTypeId();
            quantity = itemStack.getAmount();
            durability = itemStack.getDurability();
            enchantments = itemStack.getEnchantments();
            if(itemID == 386 || itemID == 387) {
            	if(itemStack instanceof CraftItemStack) {
            		net.minecraft.server.ItemStack stack = ((CraftItemStack)itemStack).getHandle();
            		NBTTagCompound tags = stack.getTag();
            		if(tags == null) {
            			return;
            		}
            		NBTTagList pages = tags.getList("pages");
            		String[] pagestrings = new String[pages.size()];
            		for(int i = 0; i < pages.size(); i++) {
            			pagestrings[i] = pages.get(i).toString();
            		}
            		String author = tags.getString("author");
            		String title = tags.getString("title");
            		if(author == null) {
            			author = "";
            		}
            		if(title == null) {
            			title = "";
            		}
            		book = new MIBook(author, title, pagestrings);
            	}
            }
        }
    }

    // Constructor to create an MIItemStack from a string containing its data
    public MIItemStack(String dataString){
        String[] data = dataString.split(",");
        if (data.length == 4) {
            itemID = Integer.parseInt(data[0]);
            quantity = Integer.parseInt(data[1]);
            durability = Short.parseShort(data[2]);
            getEnchantments(data[3]);
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
            	MultiInv.log.debug("We've got a book in the inventory!");
            	CraftItemStack cs = new CraftItemStack(itemStack);
            	net.minecraft.server.ItemStack stack = cs.getHandle();
            	NBTTagCompound tags = stack.tag;
                if (tags == null) {
                    tags = stack.tag = new NBTTagCompound();
                }
            	NBTTagList pages = new NBTTagList("pages");
            	//we don't want to throw any errors if the book is blank!
            	if(book.getPages().length == 0) {
            		return cs;
            	}
            	for(int i = 0; i < book.getPages().length; i++) {
            		MultiInv.log.debug("Loading page " + i + ": " + book.getPages()[i]);
            		pages.add(new NBTTagString("" + i + "", book.getPages()[i]));
            	}
            	tags.set("pages", pages);
            	if(!book.getAuthor().equals("")) {
            		MultiInv.log.debug("Setting author to: " + book.getAuthor());
                	tags.setString("author", book.getAuthor());
            	}
            	if(!book.getTitle().equals("")) {
            		MultiInv.log.debug("Setting title to: " + book.getTitle());
                	tags.setString("title", book.getTitle());
            	}
            	MultiInv.log.debug("Returning craftitemstack.");
            	return cs;
            }
        }
        return itemStack;
    }

    public String toString() {
        // TODO: Add compatibility with old inventories
        // return itemID + "," + quantity + "," + data + "," + durability;
        return itemID + "," + quantity + "," + durability + "," + getEnchantmentString();
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
