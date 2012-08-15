package uk.co.tggl.pluckerpluck.multiinv.inventory;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Pluckerpluck
 * Date: 17/12/11
 * Time: 12:32
 * To change this template use File | Settings | File Templates.
 */
public class MIInventory implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 8695822035863770397L;
	protected MIItemStack[] MIInventoryContents = new MIItemStack[36];
    protected MIItemStack[] MIArmourContents = new MIItemStack[4];

    // Create an MIInventory from a PlayerInventory
    public MIInventory (PlayerInventory inventory){
        // Iterate and store inventory contents
        ItemStack[] inventoryContents = inventory.getContents();
        for (int i = 0; i < inventoryContents.length; i++) {
            MIInventoryContents[i] = new MIItemStack(inventoryContents[i]);
        }

        // Iterate and store armour contents
        ItemStack[] armourContents = inventory.getArmorContents();

        for (int i = 0; i < armourContents.length; i++) {
            MIArmourContents[i] = new MIItemStack(armourContents[i]);
        }
    }
    
    public MIInventory (ItemStack[] inventory, ItemStack[] armor) {
    	if(inventory != null) {
        	for(int i = 0; i < inventory.length && i < 36; i++) {
        		MIInventoryContents[i] = new MIItemStack(inventory[i]);
        	}
    	}else {
    		for(int i = 0; i < MIInventoryContents.length; i++) {
        		MIInventoryContents[i] = new MIItemStack("");
        	}
    	}
    	if(armor != null) {
        	for(int i = 0; i < armor.length && i < 4; i++) {
        		MIArmourContents[i] = new MIItemStack(armor[i]);
        	}
    	}else {
    		for(int i = 0; i < MIArmourContents.length; i++) {
        		MIArmourContents[i] = new MIItemStack("");
        	}
    	}
    }

    // Create an MIInventory from a string containing inventory data
    public MIInventory (String inventoryString){
        if (inventoryString != null && !inventoryString.equals("")) {
            // data[0] = inventoryContents
            // data[1] = armourContents
            String[] data = inventoryString.split(":");

            // Fill MIInventoryContents
            String[] inventoryData = data[0].split(";");
            for (int i = 0; i < inventoryData.length; i++) {
                MIInventoryContents[i] = new MIItemStack(inventoryData[i]);
            }

            // Fill MIArmourContents
            if(data.length > 1) {
                String[] armourData = data[1].split(";");
                for (int i = 0; i < armourData.length; i++) {
                    MIArmourContents[i] = new MIItemStack(armourData[i]);
                }
            }else {
            	for (int i = 0; i < 4; i++) {
                    MIArmourContents[i] = new MIItemStack("");
                }
            }
        }
    }

    public MIInventory() {
    }

    public void loadIntoInventory(PlayerInventory inventory){
        // Iterate and get inventory contents
        ItemStack[] inventoryContents = new ItemStack[MIInventoryContents.length];
        for (int i = 0; i < inventoryContents.length; i++) {
            if (MIInventoryContents[i]  != null) {
                inventoryContents[i] = MIInventoryContents[i].getItemStack();
            }else{
                inventoryContents[i] = null;
            }
            inventory.setContents(inventoryContents);
        }

        // Iterate and get armour contents
        ItemStack[] armourContents = new ItemStack[MIArmourContents.length];
        for (int i = 0; i < armourContents.length; i++) {
            if (MIArmourContents[i]  != null) {
                armourContents[i] = MIArmourContents[i].getItemStack();
            }else{
                armourContents[i] = null;
            }
            inventory.setArmorContents(armourContents);
        }
    }
    
    public MIItemStack[] getInventoryContents() {
    	return MIInventoryContents;
    }
    
    public MIItemStack[] getArmorContents() {
    	return MIArmourContents;
    }


    public String toString(){
        // Initial capacity = (20 + 4) * 7 - 1
        StringBuilder inventoryString = new StringBuilder(167);

        // Add MIInventoryContents
        for (MIItemStack itemStack : MIInventoryContents){
            inventoryString.append(itemStack.toString());
            inventoryString.append(";");
        }

        // Replace last ";" with ":" (makes string look nicer)
        inventoryString.deleteCharAt(inventoryString.length() - 1);
        inventoryString.append(":");

        // Add MIArmourContents
        for (MIItemStack itemStack : MIArmourContents){
            inventoryString.append(itemStack.toString());
            inventoryString.append(";");
        }

        // Remove final ";"
        inventoryString.deleteCharAt(inventoryString.length() - 1);

        return inventoryString.toString();
    }
}
