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
import org.bukkit.potion.PotionEffectType;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.books.MIBook;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 */
public class MIItemStack {
    
    private int itemID = 0;
    private int quantity = 0;
    private short durability = 0;
    private Map<Enchantment,Integer> enchantments = new HashMap<Enchantment,Integer>();
    private MIBook book = null;
    String nbttags = null;
    
    public MIItemStack(ItemStack itemStack) {
        if(itemStack != null) {
            itemID = itemStack.getTypeId();
            quantity = itemStack.getAmount();
            durability = itemStack.getDurability();
            enchantments = itemStack.getEnchantments();
            if(itemID == 386 || itemID == 387) {
                BookMeta meta = (BookMeta) itemStack.getItemMeta();
                // Make sure we don't use this on an empty book!
                if(meta.getPages() != null) {
                    book = new MIBook(meta.getAuthor(), meta.getTitle(), meta.getPages());
                }
            } else if(itemStack.hasItemMeta()) {
                nbttags = getItemMetaSerialized(itemStack.getItemMeta());
            }
        }
    }
    
    // Constructor to create an MIItemStack from a string containing its data
    public MIItemStack(String dataString) {
        String[] data = dataString.split(",");
        if(data.length >= 4) {
            itemID = Integer.parseInt(data[0]);
            quantity = Integer.parseInt(data[1]);
            durability = Short.parseShort(data[2]);
            getEnchantments(data[3]);
            if(data.length > 4) {
                // New format starts with a #
                if(data[4].startsWith("#")) {
                    // Chop off the beginning "#" sign...
                    nbttags = data[4].substring(1);
                }
            }
        }
    }
    
    public MIItemStack() {
        
    }
    
    public ItemStack getItemStack() {
        ItemStack itemStack = null;
        if(itemID != 0 && quantity != 0) {
            itemStack = new ItemStack(itemID, quantity, durability);
            itemStack.addUnsafeEnchantments(enchantments);
            if((itemID == 386 || itemID == 387) && book != null) {
                BookMeta bi = (BookMeta) itemStack.getItemMeta();
                bi.setAuthor(book.getAuthor());
                bi.setTitle(book.getTitle());
                bi.setPages(book.getPages());
                return itemStack;
            } else if(nbttags != null) {
                return addItemMeta(itemStack, nbttags);
            }
        }
        return itemStack;
    }
    
    public String toString() {
        if(nbttags != null) {
            return itemID + "," + quantity + "," + durability + "," + getEnchantmentString() + "," + "#" + nbttags;
        } else {
            return itemID + "," + quantity + "," + durability + "," + getEnchantmentString();
        }
    }
    
    private String getEnchantmentString() {
        if(book != null) {
            return "book_" + book.getHashcode();
        } else {
            String string = "";
            for(Enchantment enchantment : enchantments.keySet()) {
                string = string + enchantment.getId() + "-" + enchantments.get(enchantment) + "#";
            }
            if("".equals(string)) {
                string = "0";
            } else {
                string = string.substring(0, string.length() - 1);
            }
            return string;
        }
    }
    
    private void getEnchantments(String enchantmentString) {
        // if books ever have enchantments, that will be the end of me...
        // Hijack this function to import book data...
        if(enchantmentString.startsWith("book_")) {
            if(MIYamlFiles.config.getBoolean("useSQL")) {
                book = MIYamlFiles.con.getBook(enchantmentString, true);
            } else {
                book = new MIBook(new File(Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder() + File.separator +
                        "books" + File.separator + enchantmentString + ".yml"));
            }
        } else if(!"0".equals(enchantmentString)) {
            String[] enchantments = enchantmentString.split("#");
            for(String enchantment : enchantments) {
                String[] parts = enchantment.split("-");
                int ID = Integer.parseInt(parts[0]);
                int level = Integer.parseInt(parts[1]);
                Enchantment e = Enchantment.getById(ID);
                this.enchantments.put(e, level);
            }
        }
    }
    
    private ItemStack addItemMeta(ItemStack is, String meta) {
        ItemMeta ismeta = is.getItemMeta();
        String[] msplit = meta.split("#");
        int repairableindex = 2;
        if(msplit.length > 1) {
            if(!msplit[0].equals("")) {
                ismeta.setDisplayName(base64Decode(msplit[0]));
            }
            if(!msplit[1].equals("")) {
                ismeta.setLore(decodeLore(msplit[1]));
            }
            if(ismeta instanceof SkullMeta) {
                if(!msplit[2].isEmpty()) {
                    ((SkullMeta) ismeta).setOwner(msplit[2]);
                }
                repairableindex = 3;
            } else if(ismeta instanceof LeatherArmorMeta) {
                if(!msplit[2].equals("")) {
                    int color = Integer.parseInt(msplit[2]);
                    ((LeatherArmorMeta) ismeta).setColor(Color.fromRGB(color));
                }
                repairableindex = 3;
            } else if(ismeta instanceof PotionMeta) {
                if(msplit.length > repairableindex) {
                    boolean ispotion = true;
                    if(msplit[repairableindex].contains("+")) {
                        PotionMeta pmeta = (PotionMeta) ismeta;
                        for(; repairableindex < msplit.length && ispotion; repairableindex++) {
                            if(msplit[repairableindex].contains("+")) {
                                String[] potion = msplit[repairableindex].split("\\+");
                                PotionEffectType type = PotionEffectType.getByName(potion[0]);
                                int amplifier = Integer.parseInt(potion[1]);
                                int duration = Integer.parseInt(potion[2]);
                                PotionEffect pe = new PotionEffect(type, duration, amplifier);
                                pmeta.addCustomEffect(pe, true);
                            } else {
                                ispotion = false;
                                repairableindex--;
                            }
                        }
                    }
                }
            }
            if(ismeta instanceof Repairable) {
                if(msplit.length > repairableindex) {
                    Repairable rmeta = (Repairable) ismeta;
                    rmeta.setRepairCost(Integer.parseInt(msplit[repairableindex]));
                }
            }
        }
        is.setItemMeta(ismeta);
        return is;
    }
    
    private String getItemMetaSerialized(ItemMeta meta) {
        StringBuilder smeta = new StringBuilder();
        smeta.append(base64Encode(meta.getDisplayName()) + "#");
        smeta.append(encodeLore(meta.getLore()) + "#");
        if(meta instanceof SkullMeta) {
            SkullMeta skullmeta = (SkullMeta) meta;
            if(((SkullMeta) meta).hasOwner()) {
                smeta.append(skullmeta.getOwner() + "#");
            }
        } else if(meta instanceof LeatherArmorMeta) {
            Color color = ((LeatherArmorMeta) meta).getColor();
            smeta.append(String.valueOf(color.asRGB()) + "#");
        } else if(meta instanceof PotionMeta) {
            if(((PotionMeta) meta).hasCustomEffects()) {
                List<PotionEffect> effects = ((PotionMeta) meta).getCustomEffects();
                for(PotionEffect effect : effects) {
                    smeta.append(effect.getType().getName() + "+" + effect.getAmplifier() + "+" + effect.getDuration() + "#");
                }
            }
        }
        if(meta instanceof Repairable) {
            Repairable rmeta = (Repairable) meta;
            smeta.append(rmeta.getRepairCost());
        }
        return smeta.toString();
    }
    
    private String base64Encode(String encode) {
        if(encode == null) {
            return "";
        }
        return javax.xml.bind.DatatypeConverter.printBase64Binary(encode.getBytes());
    }
    
    private String base64Decode(String encoded) {
        if(encoded.isEmpty()) {
            return "";
        }
        byte[] bytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(encoded);
        return new String(bytes);
    }
    
    private String encodeLore(List<String> lore) {
        StringBuilder slore = new StringBuilder();
        if(lore == null) {
            return "";
        }
        for(int i = 0; i < lore.size(); i++) {
            if(i > 0) {
                slore.append("-");
            }
            slore.append(base64Encode(lore.get(i)));
        }
        return slore.toString();
    }
    
    private LinkedList<String> decodeLore(String lore) {
        String[] slore = lore.split("-");
        LinkedList<String> lstring = new LinkedList<String>();
        for(int i = 0; i < slore.length; i++) {
            lstring.add(base64Decode(slore[i]));
        }
        return lstring;
    }
}
