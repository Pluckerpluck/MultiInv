package uk.co.tggl.pluckerpluck.multiinv.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.Serializable;

public class MIEnderchestInventory implements Serializable {
    
    /**
	 * 
	 */
    private static final long serialVersionUID = -8407863073614592570L;
    protected MIItemStack[] MIInventoryContents = new MIItemStack[27];
    
    // Create an MIInventory from a PlayerInventory
    public MIEnderchestInventory(Inventory inventory) {
        // Iterate and store inventory contents
        ItemStack[] inventoryContents = inventory.getContents();
        MIInventoryContents = new MIItemStack[inventoryContents.length];
        for(int i = 0; i < inventoryContents.length; i++) {
            MIInventoryContents[i] = new MIItemStack(inventoryContents[i]);
        }
    }
    
    // Create an MIInventory from an arry of ItemStacks
    public MIEnderchestInventory(ItemStack[] inventory) {
        // Iterate and store inventory contents
        MIInventoryContents = new MIItemStack[inventory.length];
        for(int i = 0; i < inventory.length; i++) {
            MIInventoryContents[i] = new MIItemStack(inventory[i]);
        }
    }
    
    // Create an MIInventory from a string containing inventory data
    public MIEnderchestInventory(String inventoryString) {
        if(inventoryString != null && !inventoryString.equals("")) {
			if(inventoryString.startsWith("{")) {
				//New JSON format
			    JSONParser parser = new JSONParser();
				JSONObject jsonInventory = null;
		        try {
					jsonInventory = (JSONObject)parser.parse(inventoryString);
					@SuppressWarnings("unchecked")
					int invLength = ((Number)jsonInventory.getOrDefault("size", 0)).intValue();
					MIInventoryContents = new MIItemStack[invLength]; 
					JSONObject items = (JSONObject)jsonInventory.get("items");
					for(int i = 0; i < invLength; i++) {
						if(items.containsKey(String.valueOf(i))) {
							MIInventoryContents[i] = new MIItemStack((JSONObject)items.get(String.valueOf(i)));
						}else {
							MIInventoryContents[i] = new MIItemStack();
						}
					}
				}catch(Exception e) {
					e.printStackTrace();
				}
			}else {
	            // Fill MIInventoryContents
	            String[] inventoryData = inventoryString.split(";");
	            MIInventoryContents = new MIItemStack[inventoryData.length];
	            for(int i = 0; i < inventoryData.length; i++) {
	                MIInventoryContents[i] = new MIItemStack(inventoryData[i]);
	            }
			}
        }
    }
    
    public MIEnderchestInventory(Player player) {
        this(player.getEnderChest());
    }
    
    public MIEnderchestInventory() {
    }
    
    public void loadIntoInventory(Inventory inventory) {
        // Iterate and get inventory contents
        ItemStack[] inventoryContents = new ItemStack[MIInventoryContents.length];
        for(int i = 0; i < inventoryContents.length; i++) {
            if(MIInventoryContents[i] != null) {
                inventoryContents[i] = MIInventoryContents[i].getItemStack();
            } else {
                inventoryContents[i] = null;
            }
            inventory.setContents(inventoryContents);
        }
    }
    
    public static MIItemStack[] getInventoryContents(String inventoryString) {
        if(inventoryString != null && !inventoryString.equals("")) {
            
            // Fill MIInventoryContents
            String[] inventoryData = inventoryString.split(";");
            MIItemStack[] micontents = new MIItemStack[inventoryData.length];
            for(int i = 0; i < inventoryData.length; i++) {
                micontents[i] = new MIItemStack(inventoryData[i]);
            }
            return micontents;
        }
        return null;
    }
    
    public MIItemStack[] getInventoryContents() {
        return MIInventoryContents;
    }
    
    @SuppressWarnings("unchecked")
	public String toString() {
		JSONObject inventoryContents = new JSONObject();
		inventoryContents.put("size", MIInventoryContents.length);
		JSONObject items = new JSONObject();
		for(int i = 0; i < MIInventoryContents.length; i++) {
			JSONObject item = MIInventoryContents[i].getJSONItem();
			if(item != null) {
				items.put(String.valueOf(i), item);
			}
		}
		inventoryContents.put("items", items);
		return inventoryContents.toJSONString();
    	/*
        // Initial capacity = (20 + 4) * 7 - 1
        StringBuilder inventoryString = new StringBuilder(167);
        
        // Add MIInventoryContents
        for(MIItemStack itemStack : MIInventoryContents) {
        	if(itemStack == null) {
        		itemStack = new MIItemStack();
        	}
            inventoryString.append(itemStack.toString());
            inventoryString.append(";");
        }
        
        // Remove final ";"
        inventoryString.deleteCharAt(inventoryString.length() - 1);
        
        return inventoryString.toString();*/
    }
}
