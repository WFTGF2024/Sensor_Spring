package com.example.sensorspring.util;
public class FilenameUtil {
    public static String sanitize(String input){ if(input==null)return "file"; String c=input.replace("\\","/"); c=c.substring(c.lastIndexOf('/')+1); c=c.replaceAll("[\\r\\n\\t]"," "); c=c.replaceAll("[^\\w\\-\\.\\s]","_"); c=c.replaceAll("\\s+","_"); return c.isBlank()?"file":c; }
}
