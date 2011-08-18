package uk.co.tggl.pluckerpluck.multiinv;

import java.io.Serializable;

public class MultiInvItem implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2433424709013450693L;
    private int itemID = 0;
    private int quanitity = 0;
    private byte data = 0;
    private short durability = 0;

    public void setId(Integer id) {
        itemID = id;
    }

    public void setQuanitity(Integer q) {
        quanitity = q;
    }

    public void setData(Byte d) {
        data = d;
    }

    public void setDurability(Short damage) {
        durability = damage;
    }

    public int getId() {
        return itemID;
    }

    public int getQuanitity() {
        return quanitity;
    }

    public byte getData() {
        return data;
    }

    public short getDurability() {
        return durability;
    }

    public String toString() {
        return itemID + "," + quanitity + "," + data + "," + durability;
    }

    public void fromString(String string) {
        String[] data = string.split(",");
        if (data.length == 4) {
            setId(Integer.parseInt(data[0]));
            setQuanitity(Integer.parseInt(data[1]));
            setData(Byte.parseByte(data[2]));
            setDurability(Short.parseShort(data[3]));
        }
    }
}
