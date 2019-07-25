package com.tima.top.friends.storage.mongo.model;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@ToString
@Document(collection = "customer")
public class Vay1hCustomer {

    @Id
    private String id;

    private List<Map<String, Object>> address;
    
    private List<Map<String, Object>> bank;

    @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
    private Date birthday;

    @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
    private Date created;
    
    private String fullname;

    @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
    private Date lastUpdate;

    private String nationalid;
    
    private String nationalidPlace;
    
    private String nationalinfoAddress;
    
    private String nationalinfoBirthday;
    
    private String nationalinfoId;
    
    private String nationalinfoName;
    
    private String nationalinfoPeople;
    
    private String nationalinfoProvince;
    
    private String nationalinfoSexual;
    
    private String nationalinfoSign;

    @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
    private Date nationalinfoDate;

    private String phone;
    
    private List<Map<String, Object>> refer;

    private String sex;
    
    private List<Map<String, Object>> work;

    private List<Map<String, Object>> listcontact;

    public List<Map<String, Object>> getListcontact() {
        return listcontact;
    }

    public void setListcontact(List<Map<String, Object>> listcontact) {
        this.listcontact = listcontact;
    }
}
