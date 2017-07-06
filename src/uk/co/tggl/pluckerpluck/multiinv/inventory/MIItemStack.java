package uk.co.tggl.pluckerpluck.multiinv.inventory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import Tux2.TuxTwoLib.NMSHeadData;
import Tux2.TuxTwoLib.TuxTwoPlayerHead;
import Tux2.TuxTwoLib.attributes.Attribute;
import Tux2.TuxTwoLib.attributes.Attributes;
import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.books.MIBook;

/**
 * Created by IntelliJ IDEA.
 */
public class MIItemStack {
    
    //private int itemID = 0;
    private Material item = Material.AIR;
    private int quantity = 0;
    private short durability = 0;
    private Map<Enchantment,Integer> enchantments = new HashMap<Enchantment,Integer>();
    private ArrayList<Attribute> attributes = new ArrayList<Attribute>();
    private MIBook book = null;
    private ItemStack is = null;
    private NMSHeadData headdata = null;
    String nbttags = null;
	protected MIItemStack[] subInventory = null;
    
    public MIItemStack(ItemStack itemStack) {
        if(itemStack != null) {
            item = itemStack.getType();
            quantity = itemStack.getAmount();
            durability = itemStack.getDurability();
            enchantments = itemStack.getEnchantments();
            attributes = Attributes.fromStack(itemStack);
            if(itemStack.getItemMeta() instanceof BookMeta) {
                BookMeta meta = (BookMeta) itemStack.getItemMeta();
                // Make sure we don't use this on an empty book!
                if(meta.getPages() != null) {
                    book = new MIBook(meta.getAuthor(), meta.getTitle(), meta.getPages());
                }
            } else if(itemStack.hasItemMeta()) {
                nbttags = getItemMetaSerialized(itemStack);
            }
            is = itemStack.clone();
        }
    }
    
    // Constructor to create an MIItemStack from a string containing its data
    @SuppressWarnings("deprecation")
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
                if(data[4].startsWith("#NM")) {
                    // Chop off the beginning "#" sign...
                    nbttags = data[4].substring(1);
                }else if(data[4].startsWith("#AD")) {
            		parseAttributes(data[4].substring(1));
                }
            }if(data.length > 5) {
            	if(data[5].startsWith("#AD")) {
            		parseAttributes(data[5].substring(1));
            	}
            }
        }
        is = getItemStack();
    }
    
    @SuppressWarnings("unchecked")
	public MIItemStack(JSONObject jsonData) {
    	item = Material.getMaterial(jsonData.get("item").toString());
    	quantity = ((Number)jsonData.getOrDefault("quantity", 1)).intValue();
    	durability = ((Number)jsonData.getOrDefault("durability", 0)).shortValue();
    	if(jsonData.containsKey("enchantments")) {
    		getEnchantments((JSONObject)jsonData.get("enchantments"));
    	}
    	if(jsonData.containsKey("attributes")) {
    		parseAttributes((JSONArray)jsonData.get("attributes"));
    	}
    	if(jsonData.containsKey("nbttags") && jsonData.get("nbttags") != null) {
    		nbttags = jsonData.get("nbttags").toString();
    	}
    	if(jsonData.containsKey("subitems")) {
    		JSONObject subItems = (JSONObject) jsonData.get("subitems");
    		int size = ((Number)subItems.get("length")).intValue();
    		subInventory = new MIItemStack[size];
    		JSONObject items = (JSONObject)subItems.get("items");
			for(int i = 0; i < size; i++) {
				if(items.containsKey(String.valueOf(i))) {
					subInventory[i] = new MIItemStack((JSONObject)items.get(String.valueOf(i)));
				}else {
					subInventory[i] = new MIItemStack();
				}
			}
    	}
    }
    
    private String getAttributeString() {
    	StringBuilder sb = new StringBuilder();
    	for(Attribute attribute : attributes) {
    		if(attribute == null) {
    			continue;
    		}
    		if(sb.length() == 0) {
    			sb.append("#AD#");
    		}else {
    			sb.append("#");
    		}
    		sb.append(attribute.getType());
    		sb.append("|");
    		sb.append(attribute.getAmount());
    		sb.append("|");
    		sb.append(attribute.getOperation());
    		sb.append("|");
    		sb.append(attribute.getUUID().toString());
    	}
    	return sb.toString();
    }
    
    private void parseAttributes(String sattributes) {
    	//Attributes!
    	String[] attribs = sattributes.split("#");
    	for(String attrib : attribs) {
    		//Let's skip the Attribute Data identifier
    		if(attrib.equals("AD")) continue;
    		String[] a = attrib.split("\\|");
    		if(a.length > 1) {
        		String type = a[0];
        		try {
        			double amount = Double.parseDouble(a[1]);
        			int operation = Integer.parseInt(a[2]);
        			UUID uuid = UUID.fromString(a[3]);
        			Attribute at = new Attribute(type, operation, amount, uuid);
        			attributes.add(at);
        		}catch (NumberFormatException e) {
        			e.printStackTrace();
        		}
    		}
    	}
    }
    
    private void parseAttributes(JSONArray sattributes) {
    	for(Object attrib : sattributes) {
    		if(attrib instanceof JSONObject) {
    			JSONObject attribute = (JSONObject) attrib;
    			String type = attribute.get("type").toString();
    			double amount = ((Number)attribute.get("amount")).doubleValue();
    			int operation = ((Number)attribute.get("operation")).intValue();
    			UUID uuid = UUID.fromString(attribute.get("uuid").toString());
    			Attribute at = new Attribute(type, operation, amount, uuid);
    			attributes.add(at);
    		}
    	}
    }
    
    public MIItemStack() {
        
    }
    
    public ItemStack getItemStack() {
    	if(is != null) {
    		return is.clone();
    	}
        ItemStack itemStack = null;
        if(item != null && item != Material.AIR && quantity != 0) {
            itemStack = new ItemStack(item, quantity, durability);
            //Only apply attributes if they exist! Otherwise items don't stack.
            if(attributes.size() > 0) {
                itemStack = Attributes.apply(itemStack, attributes, true);
            }
            itemStack.addUnsafeEnchantments(enchantments);
            if((item == Material.BOOK_AND_QUILL || item == Material.WRITTEN_BOOK) && book != null) {
                BookMeta bi = (BookMeta) itemStack.getItemMeta();
                bi.setAuthor(book.getAuthor());
                bi.setTitle(book.getTitle());
                bi.setPages(book.getPages());
                itemStack.setItemMeta(bi);
                return itemStack;
            } else if(nbttags != null) {
                itemStack = addItemMeta(itemStack, nbttags);
            }
            if(subInventory != null && subInventory.length > 0 && itemStack.getItemMeta() instanceof BlockStateMeta) {
            	BlockStateMeta im = (BlockStateMeta)itemStack.getItemMeta();
            	if(im.getBlockState() instanceof ShulkerBox) {
            		ShulkerBox shulker = (ShulkerBox) im.getBlockState();
            		Inventory inv = shulker.getInventory();
            		inv.clear();
            		for(int i = 0; i < subInventory.length && i < inv.getSize(); i++) {
            			inv.setItem(i, subInventory[i].getItemStack());
            		}
            		im.setBlockState(shulker);
            		itemStack.setItemMeta(im);
            	}
            }
        }
        return itemStack;
    }
    
    public String toString() {
    	StringBuilder is = new StringBuilder();
    	is.append(item.name() + "," + quantity + "," + durability + "," + getEnchantmentString());
        if(nbttags != null) {
        	is.append("," + "#" + nbttags);
        }
        if(attributes.size() > 0) {
        	is.append("," + getAttributeString());
        }
        return is.toString();
    }
    
    @SuppressWarnings("unchecked")
	public JSONObject getJSONItem() {
    	if(item == null || item == Material.AIR) {
    		return null;
    	}
    	JSONObject jo = new JSONObject();
    	jo.put("item", item.name());
    	jo.put("quantity", quantity);
    	jo.put("durability", durability);
		JSONObject enchantObject = new JSONObject();
    	if(enchantments.size() > 0) {
    		JSONArray enchants = new JSONArray();
    		Set<Entry<Enchantment, Integer>> ec = enchantments.entrySet();
    		for(Entry<Enchantment, Integer> enchant : ec) {
    			JSONObject ench = new JSONObject();
    			ench.put("id", enchant.getKey().getName());
    			ench.put("level", enchant.getValue());
    			enchants.add(ench);
    		}
    		enchantObject.put("enchants", enchants);
    	}
    	if(book != null) {
    		enchantObject.put("book", "book_" + book.getHashcode());
    	}
		jo.put("enchantments", enchantObject);
		jo.put("nbttags", nbttags);
		if(attributes.size() > 0) {
			JSONArray jsonAttributes = new JSONArray();
			for(Attribute att : attributes) {
				JSONObject attribute = new JSONObject();
				attribute.put("type", att.getType());
				attribute.put("amount", att.getAmount());
				attribute.put("operation", att.getOperation());
				attribute.put("uuid", att.getUUID().toString());
				jsonAttributes.add(attribute);
			}
			jo.put("attributes", jsonAttributes);
		}
		if(subInventory != null && subInventory.length > 0) {
			JSONObject subItems = new JSONObject();
			subItems.put("length", subInventory.length);
			JSONObject items = new JSONObject();
			for(int i = 0; i < subInventory.length; i++) {
				JSONObject item = subInventory[i].getJSONItem();
				if(item != null) {
					items.put(String.valueOf(i), item);
				}
			}
			subItems.put("items", items);
			jo.put("subitems", subItems);
		}
		return jo;
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
    
    @SuppressWarnings("deprecation")
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
    
    private void getEnchantments(JSONObject enchantmentJson) {
    	if(enchantmentJson.containsKey("book")) {
    		if(MIYamlFiles.usesql) {
                book = MIYamlFiles.con.getBook(enchantmentJson.get("book").toString(), true);
            } else {
                book = new MIBook(new File(Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder() + File.separator +
                        "books" + File.separator + enchantmentJson.get("book").toString() + ".yml"));
            }
    	}
    	//Well, books can now handle enchantments if you ever really want to...
    	//Yep, this will no longer be the death of me. :D
    	if(enchantmentJson.containsKey("enchants")) {
    		JSONArray enchantments = (JSONArray) enchantmentJson.get("enchants");
    		for(Object object : enchantments) {
    			if(object instanceof JSONObject) {
        			JSONObject enchant = (JSONObject) object;
        			Enchantment e = Enchantment.getByName(enchant.get("id").toString());
        			int level = ((Number)enchant.get("level")).intValue();
        			if(e != null) {
        				this.enchantments.put(e, level);
        			}
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
                data = itemdata.get("I");
                if(data != null) {
                	try {
                    	String texture = itemdata.get("T");
                    	headdata = new NMSHeadData(UUID.fromString(data), texture);
                    	is = TuxTwoPlayerHead.getHead(is, headdata);
                    	ismeta = is.getItemMeta();
                	}catch(IllegalArgumentException e) {
                		
                	}
                }
                data = itemdata.get("I");
                if(data != null) {
                	try {
                    	String texture = itemdata.get("T");
                    	headdata = new NMSHeadData(UUID.fromString(data), texture);
                    	is = TuxTwoPlayerHead.getHead(is, headdata);
                    	ismeta = is.getItemMeta();
                	}catch(IllegalArgumentException e) {
                		
                	}
                }
            } else if(ismeta instanceof LeatherArmorMeta) {
                data = itemdata.get("C");
                if(data != null) {
                    int color = Integer.parseInt(data);
                    ((LeatherArmorMeta) ismeta).setColor(Color.fromRGB(color));
                }
            } else if(ismeta instanceof PotionMeta) {
                PotionMeta pmeta = (PotionMeta) ismeta;
                data = itemdata.get("A");
                if(data != null) {
                    String[] potion = data.split("\\+");
                    PotionType type = PotionType.valueOf(potion[0]);
                    boolean extended = Boolean.valueOf(potion[1]);
                    boolean upgraded = Boolean.valueOf(potion[2]);
                    PotionData pdata = new PotionData(type, extended, upgraded);
                    pmeta.setBasePotionData(pdata);
                }
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
            }else if(ismeta instanceof BannerMeta) {
                data = itemdata.get("B");
                if(data != null) {
                	BannerMeta bmeta = (BannerMeta)ismeta;
                    String[] fedata = data.split("\\+");
                	DyeColor basecolor = DyeColor.valueOf(fedata[0]);
                	if(basecolor != null) {
                    	List<Pattern> patterns = new ArrayList<Pattern>();
                		for(int i = 1; i < fedata.length; i++) {
                			try {
                                String[] spattern = fedata[i].split("-");
                                DyeColor patterncolor = DyeColor.valueOf(spattern[0]);
                                PatternType patterntype = PatternType.getByIdentifier(spattern[1]);
                                Pattern pattern = new Pattern(patterncolor, patterntype);
                    			patterns.add(pattern);
                			}catch (Exception e) {
                				//Don't want to crash item creation here!
                			}
                		}
                		bmeta.setBaseColor(basecolor);
                		bmeta.setPatterns(patterns);
                	}
                }
            }else if(ismeta instanceof BlockStateMeta) {
        		BlockStateMeta bmeta = (BlockStateMeta) ismeta;
        		if(bmeta.getBlockState() instanceof Banner) {
                    data = itemdata.get("B");
                    if(data != null) {
                		Banner banner = (Banner) bmeta.getBlockState();
                        String[] fedata = data.split("\\+");
                    	DyeColor basecolor = DyeColor.valueOf(fedata[0]);
                    	if(basecolor != null) {
                        	List<Pattern> patterns = new ArrayList<Pattern>();
                    		for(int i = 1; i < fedata.length; i++) {
                    			try {
                                    String[] spattern = fedata[i].split("-");
                                    DyeColor patterncolor = DyeColor.valueOf(spattern[0]);
                                    PatternType patterntype = PatternType.getByIdentifier(spattern[1]);
                                    Pattern pattern = new Pattern(patterncolor, patterntype);
                        			patterns.add(pattern);
                    			}catch (Exception e) {
                    				//Don't want to crash item creation here!
                    			}
                    		}
                    		banner.setBaseColor(basecolor);
                    		banner.setPatterns(patterns);
                    		banner.update();
                    		bmeta.setBlockState(banner);
                    	}
                    }
        		}
            }else if (ismeta instanceof SpawnEggMeta) {
            	data = itemdata.get("S");
            	if (data != null) {
            		SpawnEggMeta bmeta = (SpawnEggMeta) ismeta;
            		bmeta.setSpawnedType(EntityType.valueOf(data));
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
    
    private String getItemMetaSerialized(ItemStack is) {
    	ItemMeta meta = is.getItemMeta();
        StringBuilder smeta = new StringBuilder();
        smeta.append("NM#");
        smeta.append("N" + base64Encode(meta.getDisplayName()) + "#");
        smeta.append("L" + encodeLore(meta.getLore()) + "#");
        if(meta instanceof SkullMeta) {
            SkullMeta skullmeta = (SkullMeta) meta;
            if(((SkullMeta) meta).hasOwner()) {
                smeta.append("O" + skullmeta.getOwner() + "#");
            }
            headdata = TuxTwoPlayerHead.getHeadData(is);
            if(headdata != null) {
                smeta.append("I" + headdata.getId().toString() + "#");
                smeta.append("T" + headdata.getTexture() + "#");
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
            if(((PotionMeta) meta).getBasePotionData() != null) {
            	PotionData pdata = ((PotionMeta) meta).getBasePotionData();
                smeta.append("A" + pdata.getType().name() + "+" + pdata.isExtended() + "+" + pdata.isUpgraded() + "#");
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
        }else if(meta instanceof BannerMeta) {
        	BannerMeta bmeta = (BannerMeta)meta;
        	DyeColor basecolor = bmeta.getBaseColor();
        	//Bukkit has a habait of setting the base color for black to null...
        	//Let's just automagically fill this in
        	if(basecolor == null) {
        		basecolor = DyeColor.BLACK;
        	}
        	List<Pattern> patterns = bmeta.getPatterns();
        	StringBuilder colorstring = new StringBuilder();
            if(patterns != null) {
            	try {
                    for(Pattern pattern : patterns) {
                        if(colorstring.length() > 0) {
                            colorstring.append("+");
                        }
                        colorstring.append(pattern.getColor().name() + "-" + pattern.getPattern().getIdentifier());
                    }
                    smeta.append("B" + basecolor.name() + "+" + colorstring.toString() + "#");
            	}catch(NullPointerException e) {
            		//Hmm... the banner data isn't quite in the right format...
            		//Let's just blank the banner instead of throwing an NPE.
            	}
            }
        }else if(meta instanceof BlockStateMeta) {
    		BlockStateMeta bmeta = (BlockStateMeta) meta;
    		if(bmeta.getBlockState() instanceof Banner) {
        		Banner banner = (Banner) bmeta.getBlockState();
            	DyeColor basecolor = banner.getBaseColor();
            	//Bukkit has a habait of setting the base color for black to null...
            	//Let's just automagically fill this in
            	if(basecolor == null) {
            		basecolor = DyeColor.BLACK;
            	}
            	List<Pattern> patterns = banner.getPatterns();
            	StringBuilder colorstring = new StringBuilder();
                if(patterns != null) {
                	try {
                        for(Pattern pattern : patterns) {
                            if(colorstring.length() > 0) {
                                colorstring.append("+");
                            }
                            colorstring.append(pattern.getColor().name() + "-" + pattern.getPattern().getIdentifier());
                        }
                        smeta.append("B" + basecolor.name() + "+" + colorstring.toString() + "#");
                	}catch(NullPointerException e) {
                		//Hmm... the banner data isn't quite in the right format...
                		//Let's just blank the banner instead of throwing an NPE.
                	}
                }
    		}else if(bmeta.getBlockState() instanceof ShulkerBox) {
        		ShulkerBox shulker = (ShulkerBox) bmeta.getBlockState();
        		ItemStack[] inv = shulker.getInventory().getContents();
        		subInventory = new MIItemStack[inv.length];
        		for(int i = 0; i < inv.length; i++) {
        			subInventory[i] = new MIItemStack(inv[i]);
        		}
        	}
        }else if (meta instanceof SpawnEggMeta) {
        	SpawnEggMeta semeta = (SpawnEggMeta)meta;
        	smeta.append("S" + semeta.getSpawnedType().toString() + "#");
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
