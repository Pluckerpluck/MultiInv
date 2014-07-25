package uk.co.tggl.pluckerpluck.multiinv.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
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
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 */
public class MIItemStack {
    
    //private int itemID = 0;
    private Material item = Material.AIR;
    private int quantity = 0;
    private short durability = 0;
    private Map<Enchantment,Integer> enchantments = new HashMap<Enchantment,Integer>();
    private MIBook book = null;
    private ItemStack is = null;
    String nbttags = null;
    
    public MIItemStack(ItemStack itemStack) {
        if(itemStack != null) {
            item = itemStack.getType();
            quantity = itemStack.getAmount();
            durability = itemStack.getDurability();
            enchantments = itemStack.getEnchantments();
            if(itemStack.getItemMeta() instanceof BookMeta) {
                BookMeta meta = (BookMeta) itemStack.getItemMeta();
                // Make sure we don't use this on an empty book!
                if(meta.getPages() != null) {
                    book = new MIBook(meta.getAuthor(), meta.getTitle(), meta.getPages());
                }
            } else if(itemStack.hasItemMeta()) {
                nbttags = getItemMetaSerialized(itemStack.getItemMeta());
            }
            is = itemStack.clone();
        }
    }
    
    // Constructor to create an MIItemStack from a string containing its data
    public MIItemStack(String dataString) {
        String[] data = dataString.split(",");
        if(data.length >= 4) {
        	try {
                item = Material.getMaterial(Integer.parseInt(data[0]));
        	}catch(NumberFormatException e) {
        		item = Material.getMaterial(data[0]);
        	}
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
        is = getItemStack();
    }
    
    public MIItemStack() {
        
    }
    
    public ItemStack getItemStack() {
    	if(is != null) {
    		return is.clone();
    	}
        ItemStack itemStack = null;
        if(item != Material.AIR && quantity != 0) {
            itemStack = new ItemStack(item, quantity, durability);
            itemStack.addUnsafeEnchantments(enchantments);
            if((item == Material.BOOK_AND_QUILL || item == Material.WRITTEN_BOOK) && book != null) {
                BookMeta bi = (BookMeta) itemStack.getItemMeta();
                bi.setAuthor(book.getAuthor());
                bi.setTitle(book.getTitle());
                bi.setPages(book.getPages());
                itemStack.setItemMeta(bi);
                return itemStack;
            } else if(nbttags != null) {
                return addItemMeta(itemStack, nbttags);
            }
        }
        return itemStack;
    }
    
    public String toString() {
        if(nbttags != null) {
            return item.name() + "," + quantity + "," + durability + "," + getEnchantmentString() + "," + "#" + nbttags;
        } else {
            return item.name() + "," + quantity + "," + durability + "," + getEnchantmentString();
        }
    }
    
    private String getEnchantmentString() {
        if(book != null) {
            return "book_" + book.getHashcode();
        } else {
            String string = "";
            for(Enchantment enchantment : enchantments.keySet()) {
                string = string + enchantment.getName() + "-" + enchantments.get(enchantment) + "#";
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
            if(MIYamlFiles.usesql) {
                book = MIYamlFiles.con.getBook(enchantmentString, true);
            } else {
                book = new MIBook(new File(Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder() + File.separator +
                        "books" + File.separator + enchantmentString + ".yml"));
            }
        } else if(!"0".equals(enchantmentString)) {
            String[] enchantments = enchantmentString.split("#");
            for(String enchantment : enchantments) {
                String[] parts = enchantment.split("-");
                Enchantment e;
                try {
                	e = Enchantment.getById(Integer.parseInt(parts[0]));
                }catch(NumberFormatException ex) {
                	e = Enchantment.getByName(parts[0]);
                }
                int level = Integer.parseInt(parts[1]);
                if(e != null) {
                    this.enchantments.put(e, level);
                }
            }
        }
    }
    
    private ItemStack addItemMeta(ItemStack is, String meta) {
        ItemMeta ismeta = is.getItemMeta();
        String[] msplit = meta.split("#");
        //Let's see if we've got new meta or the old way of handling it.
        if(msplit[0].equals("NM")) {
            HashMap<String, String> itemdata = new HashMap<String,String>();
            int potionint = 0;
            int effectint = 0;
            int fireworkint = 0;
            for(String data : msplit) {
                if(data.equals("NM")) continue;
                if(data.length() > 1) {
                    String key = data.substring(0, 1);
                    //There can be multiple potion effects, let's add an identifier
                    if(key.equals("P")) {
                        key = "P" + String.valueOf(potionint);
                        potionint++;
                    }else if(key.equals("E")) {
                        key = "E" + String.valueOf(effectint);
                        effectint++;
                    }else if(key.equals("F")) {
                        key = "F" + String.valueOf(fireworkint);
                        fireworkint++;
                    }
                    String value = data.substring(1);
                    itemdata.put(key, value);
                }
            }
            String data = itemdata.get("N");
            if(data != null) {
                ismeta.setDisplayName(base64Decode(data));
            }
            data = itemdata.get("L");
            if(data != null) {
                ismeta.setLore(decodeLore(data));
            }
            if(ismeta instanceof SkullMeta) {
                data = itemdata.get("O");
                if(data != null) {
                    ((SkullMeta) ismeta).setOwner(data);
                }
            } else if(ismeta instanceof LeatherArmorMeta) {
                data = itemdata.get("C");
                if(data != null) {
                    int color = Integer.parseInt(data);
                    ((LeatherArmorMeta) ismeta).setColor(Color.fromRGB(color));
                }
            } else if(ismeta instanceof PotionMeta) {
                PotionMeta pmeta = (PotionMeta) ismeta;
                for(int i = 0; (data = itemdata.get("P" + String.valueOf(i))) != null; i++) {
                    String[] potion = data.split("\\+");
                    PotionEffectType type = PotionEffectType.getByName(potion[0]);
                    int amplifier = Integer.parseInt(potion[1]);
                    int duration = Integer.parseInt(potion[2]);
                    PotionEffect pe = new PotionEffect(type, duration, amplifier);
                    pmeta.addCustomEffect(pe, true);
                }
            }else if(ismeta instanceof EnchantmentStorageMeta) {
                EnchantmentStorageMeta emeta = (EnchantmentStorageMeta) ismeta;
                for(int i = 0; (data = itemdata.get("E" + String.valueOf(i))) != null; i++) {
                    String[] enchant = data.split("\\+");
                    Enchantment enchantenum = Enchantment.getByName(enchant[0]);
                    if(enchantenum != null) {
                        int value = Integer.parseInt(enchant[1]);
                        emeta.addStoredEnchant(enchantenum, value, true);
                    }
                }
            }else if(ismeta instanceof FireworkMeta) {
                FireworkMeta fmmeta = (FireworkMeta) ismeta;
                for(int i = 0; (data = itemdata.get("F" + String.valueOf(i))) != null; i++) {
                    String[] fedata = data.split("\\+");
                    Type effect = FireworkEffect.Type.valueOf(fedata[0]);
                    Builder ef = FireworkEffect.builder();
                    ef.with(effect);
                    if(effect != null) {
                        String[] colors = fedata[1].split("-");
                        for(String color : colors) {
                            try {
                                ef.withColor(Color.fromRGB(Integer.parseInt(color)));
                            }catch (Exception e) {
                                
                            }
                        }
                        String[] fadecolors = fedata[2].split("-");
                        for(String fadecolor : fadecolors) {
                            try {
                                ef.withFade(Color.fromRGB(Integer.parseInt(fadecolor)));
                            }catch (Exception e) {
                                
                            }
                        }
                        ef.flicker(Boolean.parseBoolean(fedata[3]));
                        ef.trail(Boolean.parseBoolean(fedata[4]));
                        fmmeta.addEffect(ef.build());
                    }
                }
                data = itemdata.get("G");
                try {
                    fmmeta.setPower(Integer.parseInt(data));
                }catch (Exception e) {
                    
                }
            }else if(ismeta instanceof FireworkEffectMeta) {
                data = itemdata.get("F0");
                if(data != null) {
                    String[] fedata = data.split("\\+");
                    Type effect = FireworkEffect.Type.valueOf(fedata[0]);
                    Builder ef = FireworkEffect.builder();
                    ef.with(effect);
                    if(effect != null) {
                        String[] colors = fedata[1].split("-");
                        for(String color : colors) {
                            try {
                                ef.withColor(Color.fromRGB(Integer.parseInt(color)));
                            }catch (Exception e) {
                                
                            }
                        }
                        String[] fadecolors = fedata[2].split("-");
                        for(String fadecolor : fadecolors) {
                            try {
                                ef.withFade(Color.fromRGB(Integer.parseInt(fadecolor)));
                            }catch (Exception e) {
                                
                            }
                        }
                        ef.flicker(Boolean.parseBoolean(fedata[3]));
                        ef.trail(Boolean.parseBoolean(fedata[4]));
                        ((FireworkEffectMeta) ismeta).setEffect(ef.build());
                    }
                }
            }
            if(ismeta instanceof Repairable) {
                data = itemdata.get("R");
                if(data != null) {
                    Repairable rmeta = (Repairable) ismeta;
                    rmeta.setRepairCost(Integer.parseInt(data));
                }
            }
        }else {
            //Old item meta here
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
        }
        is.setItemMeta(ismeta);
        return is;
    }
    
    private String getItemMetaSerialized(ItemMeta meta) {
        StringBuilder smeta = new StringBuilder();
        smeta.append("NM#");
        smeta.append("N" + base64Encode(meta.getDisplayName()) + "#");
        smeta.append("L" + encodeLore(meta.getLore()) + "#");
        if(meta instanceof SkullMeta) {
            SkullMeta skullmeta = (SkullMeta) meta;
            if(((SkullMeta) meta).hasOwner()) {
                smeta.append("O" + skullmeta.getOwner() + "#");
            }
        } else if(meta instanceof LeatherArmorMeta) {
            Color color = ((LeatherArmorMeta) meta).getColor();
            smeta.append("C" + String.valueOf(color.asRGB()) + "#");
        } else if(meta instanceof PotionMeta) {
            if(((PotionMeta) meta).hasCustomEffects()) {
                List<PotionEffect> effects = ((PotionMeta) meta).getCustomEffects();
                for(PotionEffect effect : effects) {
                    smeta.append("P" + effect.getType().getName() + "+" + effect.getAmplifier() + "+" + effect.getDuration() + "#");
                }
            }
        }else if(meta instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta emeta = (EnchantmentStorageMeta) meta;
            if(emeta.hasStoredEnchants()) {
                Set<Entry<Enchantment,Integer>> enchants = emeta.getStoredEnchants().entrySet();
                for(Entry<Enchantment,Integer> enchant : enchants) {
                    smeta.append("E" + enchant.getKey().getName() + "+" + enchant.getValue().toString() + "#");
                }
            }
        }else if(meta instanceof FireworkMeta) {
            List<FireworkEffect> effects = ((FireworkMeta) meta).getEffects();
            ((FireworkMeta) meta).getPower();
            for(FireworkEffect effect : effects) {
                List<Color> colors = effect.getColors();
                List<Color> fadecolors = effect.getFadeColors();
                StringBuilder colorstring = new StringBuilder();
                for(Color color : colors) {
                    if(colorstring.length() > 0) {
                        colorstring.append("-");
                    }
                    colorstring.append(Integer.toString(color.asRGB()));
                }
                StringBuilder fadecolorstring = new StringBuilder();
                for(Color color : fadecolors) {
                    if(fadecolorstring.length() > 0) {
                        fadecolorstring.append("-");
                    }
                    fadecolorstring.append(Integer.toString(color.asRGB()));
                }
                smeta.append("F" + effect.getType().name() + "+" + colorstring.toString() + "+" + fadecolorstring.toString() + "+" + 
                        effect.hasFlicker() + "+" + effect.hasTrail() + "#");
            }
            smeta.append("G" + Integer.toString(((FireworkMeta) meta).getPower()) + "#");
        }else if(meta instanceof FireworkEffectMeta) {
            FireworkEffect effect = ((FireworkEffectMeta) meta).getEffect();
            if(effect != null) {
                List<Color> colors = effect.getColors();
                List<Color> fadecolors = effect.getFadeColors();
                StringBuilder colorstring = new StringBuilder();
                for(Color color : colors) {
                    if(colorstring.length() > 0) {
                        colorstring.append("-");
                    }
                    colorstring.append(Integer.toString(color.asRGB()));
                }
                StringBuilder fadecolorstring = new StringBuilder();
                for(Color color : fadecolors) {
                    if(fadecolorstring.length() > 0) {
                        fadecolorstring.append("-");
                    }
                    fadecolorstring.append(Integer.toString(color.asRGB()));
                }
                smeta.append("F" + effect.getType().name() + "+" + colorstring.toString() + "+" + fadecolorstring.toString() + "+" + 
                        effect.hasFlicker() + "+" + effect.hasTrail() + "#");
            }
        }
        if(meta instanceof Repairable) {
            Repairable rmeta = (Repairable) meta;
            smeta.append("R" + rmeta.getRepairCost());
        }
        return smeta.toString();
    }
    
    private String base64Encode(String encode) {
        if(encode == null) {
            return "";
        }
        return javax.xml.bind.DatatypeConverter.printBase64Binary(encode.getBytes(Charset.forName("UTF-8")));
    }
    
    private String base64Decode(String encoded) {
        if(encoded.isEmpty()) {
            return "";
        }
        byte[] bytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(encoded);
        return new String(bytes, Charset.forName("UTF-8"));
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
