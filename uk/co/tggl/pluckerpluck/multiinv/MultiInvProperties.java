package uk.co.tggl.pluckerpluck.multiinv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class MultiInvProperties {

    public static String loadFromProperties(String file, String key) {
        File FileP = new File(file);
        return loadFromProperties(FileP, key, null);
    }

    public static String loadFromProperties(String file, String key, String defaultValue) {
        File FileP = new File(file);
        return loadFromProperties(FileP, key, defaultValue);
    }

    public static String loadFromProperties(File file, String key) {
        return loadFromProperties(file, key, null);
    }

    public static String loadFromProperties(File file, String key, String defaultValue) {
        Properties prop = new Properties();
        String value = defaultValue;
        File dir = new File(file.getParent());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileInputStream in = new FileInputStream(file);
            prop.load(in);
            if (prop.containsKey(key)) {
                value = prop.getProperty(key);
                in.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return value;
    }

    public static Set<String> getAllKeys(String file) {
        File FileP = new File(file);
        return getAllKeys(FileP);
    }

    public static Set<String> getAllKeys(File file) {
        Properties prop = new Properties();
        HashSet<String> set = new HashSet<String>();
        File dir = new File(file.getParent());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileInputStream in = new FileInputStream(file);
            prop.load(in);
            for (Object o : prop.keySet()) {
                set.add((String) o);
            }
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return set;
    }

    public static void saveToProperties(String file, String key, String value) {
        saveToProperties(file, key, value, "No Comment");
    }

    public static void saveToProperties(String file, String key, String value, String comment) {
        File FileP = new File(file);
        Properties prop = new Properties();
        File dir = new File(FileP.getParent());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!FileP.exists()) {
            try {
                FileP.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileInputStream in = new FileInputStream(FileP);
            prop.load(in);
            prop.put(key, value);
            prop.store(new FileOutputStream(FileP), comment);
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void removeProperty(String file, String key, String comment) {
        File FileP = new File(file);
        Properties prop = new Properties();
        File dir = new File(FileP.getParent());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!FileP.exists()) {
            try {
                FileP.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileInputStream in = new FileInputStream(FileP);
            prop.load(in);
            prop.remove(key);
            prop.store(new FileOutputStream(FileP), comment);
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
