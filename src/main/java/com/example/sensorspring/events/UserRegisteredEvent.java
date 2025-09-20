package com.example.sensorspring.events;
public class UserRegisteredEvent { private Long userId; private String username; private String email;
    public UserRegisteredEvent(){} public UserRegisteredEvent(Long i,String u,String e){userId=i;username=u;email=e;}
    public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;}
    public String getUsername(){return username;} public void setUsername(String v){username=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
}
