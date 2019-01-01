package org.khucd.cdapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by Eunji on 2016-05-08.
 */
public class MainApplication extends Thread {

    private int port;
    private String address;
    private Socket sock;
    private DataOutputStream dos;
    private DataInputStream dis;
    private StringBuffer buff = new StringBuffer(4096);;
    private Thread thread;

    private User user;
    private String current_user = "";
    private boolean safetyModeOn;
    private boolean alertMode;
    Handler handle;
    LoginActivity log = new LoginActivity();

    public static final int HANDLER_LOGINRESULT = 0;
    public static final int HANDLER_ADDFRIEND = 1;
    public static final int HANDLER_POLICEGPSRESULT = 2;
    public static final int HANDLER_FRIENDSGPSRESULT = 3;

    public void setUser(String _user){
        //LoginActivity log = new LoginActivity();
        current_user = _user;
    }
    public String getUser(){
        return current_user;
    }
    public void run() {
        try {
            Thread th = Thread.currentThread();
            while(th == thread) {
                String recvData = dis.readUTF();
                Log.d("서버에서 받은 메시지 :", recvData);
                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("result", recvData);
                msg.setData(bundle);
                msg.what = 0;
                StringTokenizer st = new StringTokenizer(recvData, "|");
                String member = st.nextToken();
                Log.d("현재 메인 사용자 :",current_user);
                if(member.equals(current_user)){
                    String info = st.nextToken();
                    if(info.equals("ConfirmLogIn")) {
                        // 로그인이 되었는지를 확인한다
                        // ConfirmLogIn|성공or실패
                        // 이미 user에 정보를 넣었으므로 성공이면 그대로 두고 실패이면 user에서 정보를 빼야 된다.
                        // 로그인 성공 시 친구 목록을 받아와야 한다.
                        String data = st.nextToken();
                        if(data.equals("성공")) {
                            bundle.putString("LoginResult", "성공");
                            msg.setData(bundle);
                            msg.what = HANDLER_LOGINRESULT;
                            handle.sendMessage(msg);
                            sendMessage("FriendInfo|"+user.getPhoneNumber());
                        } else {
                            bundle.putString("LoginResult", "실패");
                            msg.setData(bundle);
                            msg.what = HANDLER_LOGINRESULT;
                            handle.sendMessage(msg);
                            user.setPhoneNumber("");
                            user.setName("");
                        }
                    } else if(info.equals("FriendList")) {
                        // 친구 목록을 받음
                        // FriendList|친구번호|친구이름|help|...
                        while(st.hasMoreTokens()) {
                            String data = st.nextToken();   // 번호
                            String name = st.nextToken();   // 이름
                            String help = st.nextToken();   // help
                            boolean flag = true;
                            if(help.equals("0")) {
                                flag = false;
                            }
                            Friends newF = new Friends(name, data, flag);
                        }
                    } else if(info.equals("GPSList")) {
                        // 가까이에 있는 친구들의 GPS 위치 정보를 받음
                        // GPSList|친구번호|위도|경도|...
                        /*buff.setLength(0);
                        while(st.hasMoreTokens()) {
                            String phone = st.nextToken();
                            Double latitude = Double.parseDouble( st.nextToken() );
                            Double longitude = Double.parseDouble( st.nextToken() );
                            buff.append(phone);
                            buff.append("|");
                            buff.append(latitude);
                            buff.append("|");
                            buff.append(longitude);
                            buff.append("|");*/
                            bundle.putString("FriendsGPS", recvData);
                            msg.setData(bundle);
                            msg.what = HANDLER_FRIENDSGPSRESULT;
                            handle.sendMessage(msg);
                      //  }
                    } else if(info.equals("ConfirmAddFriend")) {
                        // 친구추가하기 위해서 입력한 번호에 맞는 친구 이름을 받아서 실제로 추가함
                        // ConfirmAddFriend|친구번호|친구이름|HELP
                        String phone = st.nextToken();
                        String name = st.nextToken();
                        String help = st.nextToken();
                        boolean flag = true;
                        if(help.equals("0")) {
                            flag = false;
                        }
                        addFriend(name, phone, flag);
                        bundle.putString("phone", phone);
                        bundle.putString("name", name);
                        msg.setData(bundle);
                        msg.what = HANDLER_ADDFRIEND;
                        handle.sendMessage(msg);
                    } else if(info.equals("PoliceList")) {
                        // 가까이에 있는 경찰서의 GPS 위치 정보를 받음
                        // PoliceList|이름|위도|경도|...
                      /*  while (st.hasMoreTokens()) {
                            String name = st.nextToken();
                            Double latitude = Double.parseDouble(st.nextToken());
                            Double longitude = Double.parseDouble(st.nextToken()); buff.append(phone);
                            buff.append("|");
                            buff.append(latitude);
                            buff.append("|");
                            buff.append(longitude);
                            buff.append("|");*/
                            bundle.putString("PoliceGPS", recvData);
                            msg.setData(bundle);
                            msg.what = HANDLER_POLICEGPSRESULT;
                            handle.sendMessage(msg);
                        //}
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // 서버에 메시지 전송
    public void sendMessage(String message) {
        try {
            Log.d("서버로 보낸 메시지", message);
            dos.writeUTF(message);
            dos.flush();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // 생성자
    public MainApplication (Handler handler) {
        handle = handler;   // ui로 정보를 넘겨주기 위한 핸들러

        // 아래는 서버와 통신을 위한 것
        port = 5555;    // 포트는 5555로 임의로 결정함
        try {
            address = "172.16.206.91";        // 서버의 ip주소로 설정해야한다.
            sock = new Socket(address, port);
            dos = new DataOutputStream(sock.getOutputStream());
            dis = new DataInputStream(sock.getInputStream());
            thread = this;
            thread.start();
        } catch (Exception except) {
            except.printStackTrace();
        }

        user = new User();
        safetyModeOn = false;
    }

    // 서버에 로그인 요청
    // LogIn|폰번호|이름
    // ConfrimLogIn|성공or실패 를 답으로 받음
    public void login(String name, String phone) {
        buff.setLength(0);
        buff.append("LogIn");
        buff.append("|");
        buff.append(phone);
        buff.append("|");
        buff.append(name);
        sendMessage(buff.toString());

        user.setName(name);
        user.setPhoneNumber(phone);
    }

    public void requestPolice(double latitude, double longitude) {
        buff.setLength(0);
        buff.append("PoliceofficeInfo");
        buff.append("|");
        buff.append(getUserPhoone());
        buff.append("|");
        buff.append(latitude);
        buff.append("|");
        buff.append(longitude);
        sendMessage(buff.toString());
    }
    // getter
    public String getUserName() {
        return user.getName();
    }
    public String getUserPhoone() {
        return user.getPhoneNumber();
    }
    // 친구목록 출력하는 ui를 위해 list를 만들어서 return
    public List<String> getFriendsList() {
        List<Friends> friends = user.getFriendsList();
        List<String> info = new ArrayList<String> ();

        for(int i=0; i<friends.size(); i++) {
            info.add ( friends.get(i).getName() + ":" + friends.get(i).getPhoneNumber() );
        }

        return info;
    }
    // help가 true인 친구들 번호만 리스트로 만들어서 리턴
    public List<String> getFriendsInfo() {
        List<Friends> friends = user.getFriendsList();
        List<String> info = new ArrayList<String> ();

        for(int i=0; i<friends.size(); i++) {
            if(friends.get(i).getHelp() == true)
                info.add(friends.get(i).getPhoneNumber());
        }
        return info;
    }
    public int getStartTime() {
        return user.getStartTime();
    }
    public int getEndTime() {
        return user.getEndTime();
    }
    public boolean getMovingDetect() {
        return user.getMovingDetect();
    }
    public boolean getCollisionDetect() {
        return user.getCollisionDetect();
    }
    public boolean getSafetyMode() {
        return safetyModeOn;
    }
    public boolean getAlertMode() {
        return alertMode;
    }

    // setter
    public void setSafetyMode(boolean setting) { // 안심귀가모드 켰을 때
        safetyModeOn = setting;
        /* 추가할 내용
         *  - 1, 2를 따로 실시함
         *  - 즉, MainApplication의 safetyModeOn을 계속 체크해서 true이면 1, 2를 각각 계속 검사하는 것이다.
         * 1. 위치 이동을 계속 감지해서 몇 분동안 이동이 없으면 setAlertMode(true)로 신고를 실시
         * 2. 센서를 계속 감지해서 큰 충격이 가해지는 경우에 setAlertMode(true)로 신고를 실시함
         */
    }
    public void setSafetySetting(int start, int end, boolean move, boolean collision) {
        user.setTime(start, end);
        user.setMovingDetect(move);
        user.setCollisionDetect(collision);
    }
    public void setAlertMode(boolean setting) { // 신고모드
        alertMode = setting;
        /* 추가할 내용
         * 1 ~ 4 까지 차례로 수행한다
         * 1. 경보음을 울림
         * 2. 내 위치를 신고한다고 설정한 친구에게 보내서 신고함 (경찰에는 당연히! 하면 안됌!!)
         * 3. 서버에 가까이에 있는 친구목록을 요청함
         * 4. 서버에서 받아서 한번 더 신고함
         */
    }

    // 서버에 회원가입 요청
    // Register|폰번호|이름
    public void join(String name, String phone) {
        buff.setLength(0);
        buff.append("Register");
        buff.append("|");
        buff.append(phone);
        buff.append("|");
        buff.append(name);
        sendMessage(buff.toString());
    }

    // 서버에서 ConfrimAddFriend를 받아서 user의 친구 목록에 친구 추가함
    public void addFriend(String name, String phone, boolean flag) {
        user.addFriend(name, phone, flag);
    }

    // 서버에 친구 추가 요청
    // addFriend|내폰번호|친구폰번호|help
    // 답으로 ConfirmAddFriend|친구번호|친구이름|help 를 받음
    public void addFriend(String phone, boolean flag) {
        buff.setLength(0);
        buff.append("addFriend");
        buff.append("|");
        buff.append(user.getPhoneNumber());
        buff.append("|");
        buff.append(phone);
        buff.append("|");
        buff.append(flag);
        sendMessage(buff.toString());
    }

    public void requestFriendsGPS() {
        buff.setLength(0);
        buff.append("GPSInfo");
        buff.append("|");
        buff.append(user.getPhoneNumber());
        sendMessage(buff.toString());
    }
    public void updateGPS(double latitude, double longitude) {
        buff.setLength(0);
        buff.append("UpdateGPS");
        buff.append("|");
        buff.append(user.getPhoneNumber());
        buff.append("|");
        buff.append(latitude);
        buff.append("|");
        buff.append(longitude);
        sendMessage(buff.toString());
    }
    // 친구의 help상태를 바꿈 (help가 true인 친구만 전해줘서 새로 다시 설정함)
    // UpdateFriend|내폰번호|친구번호|친구번호|..
    public void changeFriendStatus(List<String> info) {
        buff.setLength(0);
        buff.append("UpdateFriend");
        buff.append("|");
        buff.append(user.getPhoneNumber());

        for(int i=0; i<info.size(); i++) {
            buff.append("|");
            buff.append(info.get(i));
        }
        sendMessage(buff.toString());

        user.changeFriendStatus(info);
    }

 /*   class ConnectThread extends Thread {

        private int port = 5555;
        private String address;
        private Socket sock;
        private DataOutputStream dos;
        private DataInputStream dis;
        private StringBuffer buff;
        private Thread thread;

        public ConnectThread() {
            try {
                address = "127.0.0.1";
                sock = new Socket(address, port);
                dos = new DataOutputStream(sock.getOutputStream());
                dis = new DataInputStream(sock.getInputStream());
                buff = new StringBuffer(4096);
                thread = this;
            }  catch (Exception except) {
                except.printStackTrace();
            }
        }


        public ConnectThread(String addr) {
            try {
                address = addr;
                sock = new Socket(address, port);
                dos = new DataOutputStream(sock.getOutputStream());
                dis = new DataInputStream(sock.getInputStream());
                buff = new StringBuffer(4096);
                thread = this;
            } catch (Exception except) {
                except.printStackTrace();
            }
        }
        public void run() {
            try {
                Thread th = Thread.currentThread();
                while(th == thread) {
                    String recvData = dis.readUTF();

                    Log.d("server connect 테스트", recvData);
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("result", recvData);
                    msg.setData(bundle);
                    msg.what = 0;

                    handle.sendMessage(msg);
                    StringTokenizer st = new StringTokenizer(recvData, "|");
                }
            } catch(Exception e) {

            }
        }

        public void sendMessage(String message) {
            try {
                dos.writeUTF(message);
                dos.flush();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }*/
}
