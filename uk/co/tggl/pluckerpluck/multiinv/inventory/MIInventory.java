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

    private final MIItemStack[] MIInventoryContents = new MIItemStack[20]; // TODO: Find playerInventory size
    private final MIItemStack[] MIArmourContents = new MIItemStack[4];

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

    // Create an MIInventory from a string containing inventory data
    public MIInventory (String inventoryString){
        // data[0] = inventoryContents
        // data[1] = armourContents
        String[] data = inventoryString.split(":");

        // Fill MIInventoryContents
        String[] inventoryData = data[0].split(";");
        for (int i = 0; i < inventoryData.length; i++) {
            MIInventoryContents[i] = new MIItemStack(inventoryData[i]);
        }

        // Fill MIArmourContents
        String[] armourData = data[1].split(";");
        for (int i = 0; i < armourData.length; i++) {
            MIArmourContents[i] = new MIItemStack(armourData[i]);
        }
    }

    // DEV: UNSURE IF CHANGING getContents() CHANGES THE PLAYERS INVENTORY OR IF IT IS CLONED
    public void loadIntoInventory(PlayerInventory inventory){
        // Iterate and get inventory contents
        ItemStack[] inventoryContents = inventory.getContents();
        for (int i = 0; i < inventoryContents.length; i++) {
            inventoryContents[i] = MIInventoryContents[i].getItemStack();
        }

        // Iterate and get armour contents
        ItemStack[] armourContents = inventory.getArmorContents();
        for (int i = 0; i < armourContents.length; i++) {
            armourContents[i] = MIArmourContents[i].getItemStack();
        }
    }

    public String toString(){
        // Initial capacity = (20 + 4) * 7 - 1
        StringBuffer inventoryString = new StringBuffer(167);

        // Add MIInventoryContents
        for (MIItemStack itemStack : MIInventoryContents){
            inventoryString.append(itemStack.toString());
            inventoryString.append(";");
        }

        // Replace last ";" with ":"
        inventoryString.deleteCharAt(-1);
        inventoryString.append(":");

        // Add MIArmourContents
        for (MIItemStack itemStack : MIArmourContents){
            inventoryString.append(itemStack.toString());
            inventoryString.append(";");
        }

        // Remove final ";"
        inventoryString.deleteCharAt(-1);

        return inventoryString.toString();
    }
}
