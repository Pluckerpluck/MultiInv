package uk.co.tggl.pluckerpluck.multiinv;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class MultiInvItem implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2433424709013450693L;
    private int itemID = 0;
    private int quanitity = 0;
    private byte data = 0;
    private short durability = 0;
    private HashMap<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
    

    public void setId(Integer id) {
        itemID = id;
    }

    public void setQuanitity(Integer q) {
        quanitity = q;
    }

    public void setData(Byte d) {
        data = d;
    }

    public void setDurability(Short damage) {
        durability = damage;
    }
    
    public void addEnchantment(Enchantment id, int level) {
    	enchantments.put(id, new Integer(level));
    }

    public int getId() {
        return itemID;
    }

    public int getQuanitity() {
        return quanitity;
    }

    public byte getData() {
        return data;
    }

    public short getDurability() {
        return durability;
    }
    
    public HashMap<Enchantment, Integer> getEnchantments() {
    	return enchantments;
    }

    public String toString() {
    	String senchantments = "";
    	Set<Enchantment> setenchantments = enchantments.keySet();
    	boolean first = true;
    	for(Enchantment tench : setenchantments) {
    		if(!first) {
    			senchantments = senchantments + ",";
    		}else {
    			first = false;
    		}
    		senchantments = senchantments + tench.getId() + "," + enchantments.get(tench).intValue();
    	}
    	if(enchantments.size() > 0) {
            return itemID + "," + quanitity + "," + data + "," + durability + "," + senchantments;
    	}else {
            return itemID + "," + quanitity + "," + data + "," + durability;
    	}
    }

    public void fromString(String string) {
        String[] data = string.split(",");
        if (data.length == 4) {
            setId(Integer.parseInt(data[0]));
            setQuanitity(Integer.parseInt(data[1]));
            setData(Byte.parseByte(data[2]));
            setDurability(Short.parseShort(data[3]));
        }else if (data.length >= 6 && (data.length%2 == 0)) {
            setId(Integer.parseInt(data[0]));
            setQuanitity(Integer.parseInt(data[1]));
            setData(Byte.parseByte(data[2]));
            setDurability(Short.parseShort(data[3]));
            for(int i = 4; i < data.length; i++) {
            	addEnchantment(Enchantment.getById(Integer.parseInt(data[i])), Integer.parseInt(data[++i]));
            }
        }
    }
}
