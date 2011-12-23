package uk.co.tggl.pluckerpluck.multiinv.inventory;

import org.bukkit.inventory.ItemStack;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 */
public class MIItemStack implements Serializable{

    //private static final long serialVersionUID = 2433424709013450693L;
    private int itemID = 0;
    private int quantity = 0;
    private short durability = 0;

    public MIItemStack(ItemStack itemStack){
        if (itemStack != null){
            itemID = itemStack.getTypeId();
            quantity = itemStack.getAmount();
            durability = itemStack.getDurability();
        }
    }

    // Constructor to create an MIItemStack from a string containing its data
    public MIItemStack(String dataString){
        String[] data = dataString.split(",");
        if (data.length == 3) {
            itemID = Integer.parseInt(data[0]);
            quantity = Integer.parseInt(data[1]);
            durability = Short.parseShort(data[2]);
        }
    }

    public MIItemStack(){

    }

    public ItemStack getItemStack(){
        ItemStack itemStack = null;
        if (itemID != 0 && quantity != 0){
            itemStack = new ItemStack(itemID, quantity, durability);
        }
        return itemStack;
    }

    public String toString() {
        // TODO: Add compatibility with old inventories
        // return itemID + "," + quantity + "," + data + "," + durability;
        return itemID + "," + quantity + "," + durability;
    }

}
