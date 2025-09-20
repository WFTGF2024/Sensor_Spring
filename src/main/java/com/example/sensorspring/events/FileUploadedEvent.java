package com.example.sensorspring.events;
public class FileUploadedEvent { private Long fileId; private Long ownerId; private String fileName; private String hash; private Long size; private String permission;
    public FileUploadedEvent(){} public FileUploadedEvent(Long f,Long o,String n,String h,Long s,String p){fileId=f;ownerId=o;fileName=n;hash=h;size=s;permission=p;}
    public Long getFileId(){return fileId;} public void setFileId(Long v){fileId=v;}
    public Long getOwnerId(){return ownerId;} public void setOwnerId(Long v){ownerId=v;}
    public String getFileName(){return fileName;} public void setFileName(String v){fileName=v;}
    public String getHash(){return hash;} public void setHash(String v){hash=v;}
    public Long getSize(){return size;} public void setSize(Long v){size=v;}
    public String getPermission(){return permission;} public void setPermission(String v){permission=v;}
}
