package uk.co.tggl.pluckerpluck.multiinv.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;

public class MIEnderchestInventory implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8407863073614592570L;
	protected MIItemStack[] MIInventoryContents = new MIItemStack[36];

    // Create an MIInventory from a PlayerInventory
    public MIEnderchestInventory (Inventory inventory){
        // Iterate and store inventory contents
        ItemStack[] inventoryContents = inventory.getContents();
        MIInventoryContents = new MIItemStack[inventoryContents.length];
        for (int i = 0; i < inventoryContents.length; i++) {
            MIInventoryContents[i] = new MIItemStack(inventoryContents[i]);
        }
    }

    // Create an MIInventory from a string containing inventory data
    public MIEnderchestInventory (String inventoryString){
        if (inventoryString != null && !inventoryString.equals("")) {

            // Fill MIInventoryContents
            String[] inventoryData = inventoryString.split(";");
            MIInventoryContents = new MIItemStack[inventoryData.length];
            for (int i = 0; i < inventoryData.length; i++) {
                MIInventoryContents[i] = new MIItemStack(inventoryData[i]);
            }
        }
    }

    public MIEnderchestInventory() {
    }

    public void loadIntoInventory(Inventory inventory){
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
    }
    
    public static MIItemStack[] getInventoryContents(String inventoryString) {
    	if (inventoryString != null && !inventoryString.equals("")) {

            // Fill MIInventoryContents
            String[] inventoryData = inventoryString.split(";");
            MIItemStack[] micontents = new MIItemStack[inventoryData.length];
            for (int i = 0; i < inventoryData.length; i++) {
            	micontents[i] = new MIItemStack(inventoryData[i]);
            }
            return micontents;
        }
    	return null;
    }
    
    public MIItemStack[] getInventoryContents() {
    	return MIInventoryContents;
    }


    public String toString(){
        // Initial capacity = (20 + 4) * 7 - 1
        StringBuilder inventoryString = new StringBuilder(167);

        // Add MIInventoryContents
        for (MIItemStack itemStack : MIInventoryContents){
            inventoryString.append(itemStack.toString());
            inventoryString.append(";");
        }

        // Remove final ";"
        inventoryString.deleteCharAt(inventoryString.length() - 1);

        return inventoryString.toString();
    }
}
