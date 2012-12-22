package uk.co.tggl.pluckerpluck.multiinv.inventory;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * User: Pluckerpluck Date: 23/12/11
 */
public class MIInventoryOld extends MIInventory {
    
    /**
	 * 
	 */
    private static final long serialVersionUID = -9035556692410143450L;
    
    public MIInventoryOld(Player inventory) {
        super(inventory);
    }
    
    public MIInventoryOld(String inventoryString) {
        if(inventoryString != null) {
            String[] data = inventoryString.split(";-;");
            if(data.length == 3) {
                if(!data[0].equals("!!!")) {
                    String[] items1 = data[0].split(";");
                    int i = 0;
                    for(String itemString : items1) {
                        MIItemStack itemStack;
                        if(itemString.equals("!")) {
                            itemStack = new MIItemStack();
                        } else {
                            itemStack = stringToItem(itemString);
                        }
                        MIInventoryContents[i] = itemStack;
                        i++;
                    }
                }
                
                if(!data[1].equals("!!!")) {
                    String[] items2 = data[1].split(";");
                    int j = 0;
                    for(String itemString : items2) {
                        MIItemStack itemStack;
                        if(itemString.equals("!")) {
                            itemStack = new MIItemStack();
                        } else {
                            itemStack = stringToItem(itemString);
                        }
                        MIArmourContents[j] = itemStack;
                        j++;
                    }
                }
            }
        }
    }
    
    private MIItemStack stringToItem(String string) {
        String[] data = string.split(",");
        int ID = Integer.parseInt(data[0]);
        int amount = Integer.parseInt(data[1]);
        short dur = Short.parseShort(data[3]);
        ItemStack itemStack = new ItemStack(ID, amount, dur);
        if(data.length >= 6 && (data.length % 2 == 0)) {
            for(int i = 4; i < data.length; i++) {
                itemStack.addUnsafeEnchantment(Enchantment.getById(Integer.parseInt(data[i])), Integer.parseInt(data[++i]));
            }
        }
        return new MIItemStack(itemStack);
    }
    
}
