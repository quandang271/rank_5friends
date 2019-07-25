package com.tima.top.friends.service;

import com.tima.top.friends.storage.mongo.model.Vay1hCustomer;
import com.tima.top.friends.storage.mongo.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class DataMongoService {

    @Autowired
    CustomerRepository customerRepository;

    private static String[] familyKeyArr = new String[]{
            "chong",
            "vo",
            "chồng",
            "vợ",
            "bố",
            "mẹ",
            "me",
            "papa",
            "mama",
            "má",
            "mother",
            "father",
            "mom",
            "mami",
            "dad",
            "dady",
            "daddy",
            "ck",
            "vk",
            "wife",
            "husband"
    };

    //lay danh sach so dien thoai duoc luu trong danh ba cua customer
    public Map<String, String> genPhoneNumberListOfCustomer(List<Map<String, Object>> listContact) {
        Map<String, String> savePhoneNumbers = new HashMap<>();
        for (Map<String, Object> map : listContact) {
            if (map.containsKey("phoneNumbers")) {
                //doi voi cau truc du lieu kieu 1
                addPhoneNumberListWithDataType1(map, savePhoneNumbers);
            } else {
                //doi voi cau truc du lieu kieu 2
                addPhoneNumberListWithDataType2(map, savePhoneNumbers);
            }
        }
        return savePhoneNumbers;
    }

    public void addPhoneNumberListWithDataType2(Map<String, Object> map, Map<String, String> savePhoneNumbers) {
        if (map.containsKey("phone")) {
            String phones = (String) map.get("phone");
            String fullName = (String) map.get("fullName");
            Map<String, String> phoneNumbers = breakPhonesString(phones, fullName);
            if (phoneNumbers != null && !phoneNumbers.isEmpty())
                savePhoneNumbers.putAll(phoneNumbers);
        }
    }

    public Map<String, String> breakPhonesString(String phones, String fullName) {
        if (phones == null || "".equals(phones)) return null;
        Map<String, String> phoneNumbers = new HashMap<>();
        //tach thanh cac chuoi string gom label va sdt
        String[] phoneWithLabel = phones.split(Pattern.quote(","));
        if (phoneWithLabel == null || phoneWithLabel.length == 0) return null;
        for (String p : phoneWithLabel) {
            //tach label va sdt tu chuoi string
            String[] phoneAndLabel = p.split(":");
            if (phoneAndLabel != null && phoneAndLabel.length > 1) {
                phoneNumbers.put(phoneAndLabel[1], fullName);
            }
        }
        return phoneNumbers;
    }

    public void addPhoneNumberListWithDataType1(Map<String, Object> map, Map<String, String> savePhoneNumbers) {
        List<Map<String, Object>> phoneList = (List<Map<String, Object>>) map.get("phoneNumbers");
        if (phoneList != null && !phoneList.isEmpty()) {
            for (Map<String, Object> phone : phoneList) {
                savePhoneNumbers.put((String) phone.get("number"), (String) phone.get("label"));
            }
        }
    }

    public Map<String, String> getFamilyMemberPhone(String uid) {
        Map<String, String> familyPhones = new HashMap<>();
        Vay1hCustomer obj = customerRepository.findById(uid).orElse(null);
        if (obj == null) return null;
        Map<String, String> phones = genPhoneNumberListOfCustomer(obj.getListcontact());
        phones.forEach((key, value) -> {
            List<String> valueSplit = Arrays.asList(value.split(" "));
            for (int i = 0; i < familyKeyArr.length; i++) {
                if (valueSplit.contains(familyKeyArr[i])) {
                    familyPhones.put(key, value);
                    continue;
                }
            }
        });

        return familyPhones;
    }

}
