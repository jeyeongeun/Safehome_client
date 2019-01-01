package org.khucd.cdapp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eunji on 2016-05-08.
 */
public class User {
    private String name;
    private String phoneNumber; // int는 사이즈가 안맞음
    private int startTime;
    private int endTime;
    private boolean movingDetect;
    private boolean collisionDetect;
    private List<Friends> friendsList;

    public User() {
        name = new String("");
        phoneNumber= new String("");
        friendsList = new ArrayList<Friends>();
        startTime = 22;
        endTime = 23;
        movingDetect = false;
        collisionDetect = false;
    }
    public void setName(String _name) {
        name = _name;
    }
    public void setPhoneNumber(String phone) {
        phoneNumber = phone;
    }
    public void setTime(int start, int end) {
        startTime = start;
        endTime = end;
    }
    public void setMovingDetect(boolean set) {
        movingDetect = set;
    }
    public void setCollisionDetect(boolean set) {
        collisionDetect  = set;
    }
    public void addFriend(String name, String phone, boolean flag) {
        Friends newFriend = new Friends(name, phone, flag);
        friendsList.add(newFriend);
    }

    public String getName() {
        return name;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public int getStartTime() {
        return startTime;
    }
    public int getEndTime() {
        return endTime;
    }
    public boolean getMovingDetect() {
        return movingDetect;
    }
    public boolean getCollisionDetect() {
        return collisionDetect;
    }
    public List<Friends> getFriendsList() {
        return friendsList;
    }
    public void changeFriendStatus(List<String> info) {
        int toFind = 0;
        Friends tmp;
        for(int i=0; info.size() != 0 && i<friendsList.size(); i++) {
            tmp = friendsList.get(i);
            if(tmp.getPhoneNumber().equals(info.get(toFind))) {
               tmp.setHelp(true);
                if(toFind+1 < info.size())
                    toFind++;
            } else {
                tmp.setHelp(false);
            }
        }
    }
}
