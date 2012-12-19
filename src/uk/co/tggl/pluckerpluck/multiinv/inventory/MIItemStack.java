package uk.co.tggl.pluckerpluck.multiinv.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.books.MIBook;

import java.io.File;
import java.util.HashMap;
import java.util.List;
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
            	BookMeta meta = (BookMeta)itemStack.getItemMeta();
            	//Make sure we don't use this on an empty book!
            	if(meta.getPages() != null) {
            		book = new MIBook(meta.getAuthor(), meta.getTitle(), meta.getPages());
            	}
            }else if(itemStack.hasItemMeta()) {
            	nbttags = getItemMetaSerialized(itemStack.getItemMeta());
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
            	//New format starts with a #
            	if(unserialized.startsWith("#")) {
            		//Chop off the beginning "#" sign...
            		nbttags = data[4].substring(1);
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
            	BookMeta bi = (BookMeta)itemStack.getItemMeta();
            	bi.setAuthor(book.getAuthor());
            	bi.setTitle(book.getTitle());
            	bi.setPages(book.getPages());
            	return itemStack;
            }else if(nbttags != null) {
            	//TODO: add item meta
            	//return TuxTwoNBTData.readInNBTString(itemStack, nbttags);
            }
        }
        return itemStack;
    }

    public String toString() {
    	if(nbttags != null) {
            return itemID + "," + quantity + "," + durability + "," + getEnchantmentString() + "," + "#" + nbttags;
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
    
    private String getItemMetaSerialized(ItemMeta meta) {
    	StringBuilder smeta = new StringBuilder();
    	smeta.append(base64Encode(meta.getDisplayName()) + "#");
    	smeta.append(base64Encode(encodeLore(meta.getLore())) + "#");
    	if(meta instanceof SkullMeta) {
    		SkullMeta skullmeta = (SkullMeta)meta;
    		if(((SkullMeta) meta).hasOwner()) {
        		smeta.append(skullmeta.getOwner());
    		}
    	}else if(meta instanceof LeatherArmorMeta) {
    		Color color = ((LeatherArmorMeta)meta).getColor();
    		smeta.append(String.valueOf(color.asRGB()) + "#");
    	}else if(meta instanceof PotionMeta) {
    		if(((PotionMeta)meta).hasCustomEffects()) {
    			List<PotionEffect> effects = ((PotionMeta)meta).getCustomEffects();
    			for(PotionEffect effect : effects) {
    				smeta.append(effect.getType().getName() + "-" + effect.getAmplifier() + "-" + effect.getDuration() + "#");
    			}
    		}
    	}
    	if(meta instanceof Repairable) {
    		Repairable rmeta = (Repairable)meta;
    		smeta.append(rmeta.getRepairCost());
    	}
    	return smeta.toString();
    }

    private String base64Encode(String encode) {
    	return javax.xml.bind.DatatypeConverter.printBase64Binary(encode.getBytes());
    }
    
    private String base64Decode(String encoded) {
    	byte[] bytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(encoded);
    	return new String(bytes);
    }
    
    private String encodeLore(List<String> lore) {
    	StringBuilder slore = new StringBuilder();
    	for(int i = 0; i < lore.size(); i++) {
    		if(i > 0) {
    			slore.append("-");
    		}
    		slore.append(base64Encode(lore.get(i)));
    	}
    	return slore.toString();
    }
    
    private String[] decodeLore(String lore) {
    	String[] slore = lore.split("-");
    	for(int i = 0; i < slore.length; i++) {
    		slore[i] = base64Decode(slore[i]);
    	}
    	return slore;
    }
}
