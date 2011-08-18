package uk.co.tggl.pluckerpluck.multiinv;

import java.io.Serializable;


import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class MultiInvInventory implements Serializable {

    private static final long serialVersionUID = -9100540910611570679L;
    /*
     * Inventory object
     * inventory[0] = main inventory's contents
     * inventory[1] = armour slots contents (null for non-player inventories)
     */
    private MultiInvItem[][] storedInventory = new MultiInvItem[2][];
    private String name = null;
    private String regionName = null;
    private String worldName = null;
    private String pluginName = null;

    public MultiInvInventory() {
    }

    public MultiInvInventory(Inventory inventory, String pluginName) {
        if (inventory != null) {
            setInventory(inventory);
        }
        this.pluginName = pluginName;
    }

    public MultiInvInventory(Player player, String pluginName) {
        PlayerInventory inventory = player.getInventory();
        if (inventory != null) {
            setInventory(inventory);
        }
        this.pluginName = pluginName;
    }

    public MultiInvInventory(Inventory inventory, String name, String pluginName) {
        if (inventory != null) {
            setInventory(inventory);
        }
        this.name = name;
        this.pluginName = pluginName;
    }

    public MultiInvInventory(Player player, String name, String pluginName) {
        PlayerInventory inventory = player.getInventory();
        if (inventory != null) {
            setInventory(inventory);
        }
        this.name = name;
        this.pluginName = pluginName;
    }

    /**
     * Store an inventory in the MultiInvInventory
     *
     * @param inventory
     **/
    public void setInventory(Inventory inventory) {
        setContents(inventory.getContents());
        if (inventory instanceof PlayerInventory) {
            setArmourContents(((PlayerInventory) inventory).getArmorContents());
        }
    }

    /**
     * Gets the MultiInvInventory and stores it in a player
     *
     * @param player in which to store the inventory
     * @throws IllegalArgumentException if incorrect ItemStack length is stored
     **/
    public void getInventory(Player player) {
        PlayerInventory inventory = player.getInventory();

        if (getContents() != null) {
            inventory.setContents(getContents());
        }
        ItemStack[] armourS = getArmourContents();
        if (armourS != null) {
            inventory.setHelmet(armourS[3]);
            inventory.setChestplate(armourS[2]);
            inventory.setLeggings(armourS[1]);
            inventory.setBoots(armourS[0]);
        }
    }

    /**
     * Gets the MultiInvInventory and stores it in a block
     *
     * @param block in which to store the inventory
     * @return true if the inventory was set
     * @throws IllegalArgumentException if incorrect ItemStack length is stored
     * 
     **/
    public boolean getInventory(Block block) {
        if (block instanceof ContainerBlock) {
            ContainerBlock container = (ContainerBlock) block;
            container.getInventory().setContents(getContents());
            return true;
        }
        return false;
    }

    /**
     * Retrieves the name of the inventory
     *
     * @return name of the inventory
     **/
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the inventory
     *
     * @param name of the inventory
     **/
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the region name of the inventory
     *
     * @param name of the region
     **/
    public void setRegionName(String name) {
        this.regionName = name;
    }

    /**
     * Retrieves the region name of the inventory
     *
     * @return name of the region
     **/
    public String getRegionName() {
        return regionName;
    }

    /**
     * Sets the region name of the inventory
     *
     * @param name of the region
     **/
    public void setWorldName(String name) {
        this.worldName = name;
    }

    /**
     * Retrieves the region name of the inventory
     *
     * @return name of the region
     **/
    public String getWorldName() {
        return worldName;
    }

    /**
     * Retrieves the plugin that created the inventory
     *
     * @return name of the plugin
     **/
    public String getPluginName() {
        return pluginName;
    }

    private void setContents(ItemStack[] itemstacks) {
        storedInventory[0] = itemStackToObject(itemstacks);
    }

    private ItemStack[] getContents() {
        if (storedInventory[0] != null) {
            return objectToItemStack(storedInventory[0]);
        }
        return null;
    }

    private void setArmourContents(ItemStack[] itemstacks) {
        storedInventory[1] = itemStackToObject(itemstacks);
    }

    private ItemStack[] getArmourContents() {
        if (storedInventory[1] != null) {
            return objectToItemStack(storedInventory[1]);
        }
        return null;
    }

    private MultiInvItem[] itemStackToObject(ItemStack[] stacks) {
        MultiInvItem[] items = new MultiInvItem[stacks.length];
        int i = 0;
        for (ItemStack stack : stacks) {
            if (stack == null || stack.getAmount() == 0) {
                items[i] = null;
                i++;
                continue;
            }
            MultiInvItem item = new MultiInvItem();
            item.setId(stack.getTypeId());
            item.setQuanitity(stack.getAmount());
            item.setDurability(stack.getDurability());
            items[i] = item;
            i++;
        }
        return items;
    }

    private ItemStack[] objectToItemStack(MultiInvItem[] itemArray) {
        ItemStack[] items = new ItemStack[itemArray.length];
        int i = 0;
        for (MultiInvItem item : itemArray) {
            if (item == null || item.getQuanitity() == 0) {
                items[i] = null;
                i++;
                continue;
            }
            int id = item.getId();
            int amount = item.getQuanitity();
            short damage = item.getDurability();
            ItemStack stack = new ItemStack(id, amount, damage);
            items[i] = stack;
            i++;
        }
        return items;
    }

    @Override
    public String toString() {
        String string = "";
        if (storedInventory[0] != null) {
            for (MultiInvItem object : storedInventory[0]) {
                if (object == null) {
                    string = string + "!";
                } else {
                    string = string + object.toString();
                }
                string = string + ";";
            }
        } else {
            string = string + "!!!";
        }
        string = string + "-;";
        if (storedInventory[1] != null) {
            for (MultiInvItem object : storedInventory[1]) {
                if (object == null) {
                    string = string + "!";
                } else {
                    string = string + object.toString();
                }
                string = string + ";";
            }
        } else {
            string = string + "!!!";
        }
        string = string + "-;";

        if (name == null) {
            string = string + "!";
        } else {
            string = string + name;
        }

        if (regionName == null) {
            string = string + ";!";
        } else {
            string = string + ";" + regionName;
        }

        if (worldName == null) {
            string = string + ";!";
        } else {
            string = string + ";" + worldName;
        }

        if (pluginName == null) {
            string = string + ";!";
        } else {
            string = string + ";" + pluginName;
        }
        return string;
    }

    public void fromString(String string) {
        String[] data = string.split(";-;");
        if (data.length == 3) {
            if (!data[0].equals("!!!")) {
                String[] items1 = data[0].split(";");
                MultiInvItem[] itemArray0 = new MultiInvItem[items1.length];
                int i = 0;
                for (String itemString : items1) {
                    MultiInvItem item = new MultiInvItem();
                    if (itemString.equals("!")) {
                        item = null;
                    } else {
                        item.fromString(itemString);
                    }
                    itemArray0[i] = item;
                    i++;
                }
                storedInventory[0] = itemArray0;
            }

            if (!data[1].equals("!!!")) {
                String[] items2 = data[1].split(";");
                MultiInvItem[] itemArray1 = new MultiInvItem[items2.length];
                int j = 0;
                for (String itemString : items2) {
                    MultiInvItem item = new MultiInvItem();
                    if (itemString.equals("!")) {
                        item = null;
                    } else {
                        item.fromString(itemString);
                    }
                    itemArray1[j] = item;
                    j++;
                }
                storedInventory[1] = itemArray1;
            }
            String[] items3 = data[2].split(";");
            if (items3.length == 4) {
                if (items3[0].equals("!")) {
                    name = null;
                } else {
                    name = items3[0];
                }

                if (items3[1].equals("!")) {
                    regionName = null;
                } else {
                    regionName = items3[0];
                }

                if (items3[2].equals("!")) {
                    worldName = null;
                } else {
                    worldName = items3[0];
                }

                if (items3[3].equals("!")) {
                    pluginName = null;
                } else {
                    pluginName = items3[0];
                }
            }
        }
    }
}
