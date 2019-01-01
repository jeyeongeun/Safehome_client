package org.khucd.cdapp;

/**
 * Created by Eunji on 2016-05-08.
 */

public class Friends {

    private String name;
    private String phoneNumber; // int는 사이즈가 안맞음
    private boolean help;   // 도움을 요청할 것인지

    public Friends() {
        name = new String("");
        phoneNumber = new String("");
        help = false;
    }
    public Friends(String name, String phoneNumber, boolean flag) {
        setName(name);
        setPhoneNumber(phoneNumber);
        help = flag;
    }

    public void setName(String _name) {
        name=_name;
    }
    public void setPhoneNumber(String phone) {
        phoneNumber = phone;
    }
    public void setHelp(boolean _help) {
        help = _help;
    }

    public String getName() {
        return name;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public boolean getHelp() {
        return help;
    }
}
