package uk.co.tggl.pluckerpluck.multiinv.inventory;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

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

    public MIItemStack(ItemStack itemStack){
        if (itemStack != null){
            itemID = itemStack.getTypeId();
            quantity = itemStack.getAmount();
            durability = itemStack.getDurability();
            enchantments = itemStack.getEnchantments();
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
        }
        return itemStack;
    }

    public String toString() {
        // TODO: Add compatibility with old inventories
        // return itemID + "," + quantity + "," + data + "," + durability;
        return itemID + "," + quantity + "," + durability + "," + getEnchantmentString();
    }

    private String getEnchantmentString(){
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

    private void getEnchantments(String enchantmentString){
        if (!"0".equals(enchantmentString)){
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
