package org.khucd.cdapp;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Double latitude;    // 현재 위도
    Double xLatitude;   // 전에 측정했던 위도
    Double longitude;   // 현재 경도
    Double xLongitude;  // 전에 측정했던 경도

    MainApplication app;
    MySensor sensor;        // 가속도 센서

    MediaPlayer player;     // 경보음 재생용

    // 정해놓은 시간에 자동 위험 감지 기능이 작동하도록 하기 위해서 알람매니저 사용함
    AlarmManager startAlarm;
    AlarmManager endAlarm;

    String main_user = "";

    // 인텐트 결과 받기 위한 것
    public static final int REQUEST_CODE_LOGIN = 1001;
    public static final int REQUEST_CODE_JOIN = 1002;
    public static final int REQUEST_CODE_SAFETYSETTING = 1003;
    public static final int REQUEST_CODE_FRIENDS = 1004;
    public static final int REQUEST_CODE_MAPS = 1005;

    // 핸들러 결과 받기 위한 것
    public static final int HANDLER_LOGINRESULT = 0;
    public static final int HANDLER_ADDFRIEND = 1;
    public static final int HANDLER_POLICEGPSRESULT = 2;
    public static final int HANDLER_FRIENDSGPSRESULT = 3;

    boolean getGPSPolice = false;
    boolean getGPSFriends = false;
    String policeData;
    String friendsData = "null";

    // 서버에서 전송받은 메시지를 받아오는 것
    /**
     * 서버에서 전송받은 메시지를 MainApplication이 받는다.
     * MainApplication은 받은 메시지를 Handler를 통해 MainActivity로 전송한다.
     * 밑의 핸들러는 MainApplication에서 전송받은 데이터를 받아서 처리하는 역할을 한다.
     */
     Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if(msg.what == HANDLER_LOGINRESULT) {   // 로그인의 결과를 받아올 때
                // 로그인이 성공했는지 실패했는지 알려주고
                // 로그인에 성공한 경우에는 나의 현재 위치를 간격으로 측정하여 주기적으로 서버에 전송해준다.
                String data = msg.getData().getString("LoginResult");
                Toast toast = Toast.makeText(getBaseContext(), "로그인에 " + data + "했습니다.", Toast.LENGTH_LONG);
                toast.show();
                if( data.equals("성공") ){
                    setTimer(); // 나의 위치를 주기적으로 전송해주기 위한 Timer를 setting한다.
                }
            } else if(msg.what == HANDLER_ADDFRIEND) {  // 친구추가 요청의 결과를 받아올 때
                String phone = msg.getData().getString("phone");    // 친구의 휴대폰 번호
                String name = msg.getData().getString("name");      // 친구의 이름
                Toast toast = Toast.makeText(getBaseContext(), name + ":" + phone + "를 친구로 추가했습니다.", Toast.LENGTH_LONG);
                toast.show();
            } else if(msg.what == HANDLER_POLICEGPSRESULT) {    // MapActivity를 선택하여서 자동으로 요청한 경찰서 정보를 받은 경우
                policeData = msg.getData().getString("PoliceGPS");  // string으로 저장된 경찰서 위치 정보
                getGPSPolice = true;        // 경찰서 위치 정보를 받았으므로 true로 표시한다.
                if(getGPSFriends) { // 친구 위치 정보도 이미 받은 상태이면 mapactivity를 열 수 잇다.
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    intent.putExtra("GPSFriends", friendsData);
                    intent.putExtra("GPSPolice", policeData);
                    startActivityForResult(intent, REQUEST_CODE_MAPS);
                }
            } else if(msg.what == HANDLER_FRIENDSGPSRESULT) {   // MapActivity를 선택하여서 자동으로 요청한 친구 정보를 받은 경우
                friendsData = msg.getData().getString("FriendsGPS");    // String으로 저장된 친구의 위치 정보
                getGPSFriends = true;   // 친구의 위치정보를 받았으므로 true로 표시한다.
                if(getGPSPolice) {     // 경찰서의 위치정보를 이미 받은 상태이면 mapactivity를 열 수 있다.
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    intent.putExtra("GPSFriends", friendsData);
                    intent.putExtra("GPSPolice", policeData);
                    startActivityForResult(intent, REQUEST_CODE_MAPS);
                }
            }
        }
    };

    Button login;
    Button join;
    ToggleButton alertButton; // 경보음 버튼
    ToggleButton safetyModeButton;  // 위험 자동 감지 버튼
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1234;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        app = new MainApplication(handler);

        login = (Button) findViewById(R.id.btn_login);
        join = (Button) findViewById(R.id.btn_join);
        alertButton = (ToggleButton)findViewById(R.id.imageToggleBtn);
        safetyModeButton = (ToggleButton)findViewById(R.id.safetymodeToggleBtn);

        // 마시멜로 버전 때문에 권한 받아오는 부분
        boolean hasPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        if(!hasPermission)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 111);
        hasPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED);
        if(!hasPermission)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 121);
        hasPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED);
        if(!hasPermission)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE);

        // 경보음 추가
        player = MediaPlayer.create(this, R.raw.alertsound);

        // startTimer가 작동한 경우의 receiver
        registerReceiver( new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                app.setSafetyMode(false);   // 이전의 설정상태를 모르므로 SafetyMode(false)
                safetyModeButton.performClick();    // 자동으로 safetyMode를 시작함
                Log.d("로그 : 위험 감지 모드", "위험 감지 모드 ON");
            }
        }, new IntentFilter("startTime"));

        // endTimer가 작동한 경우의 receiver
        registerReceiver( new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                app.setSafetyMode(true);    // 이전의 상태를 알수 없으므로 true로 셋팅
                safetyModeButton.performClick();    // 자동으로 safetyMode를 종료
                Log.d("로그 : 위험 감지 모드", "위험 감지 모드 OFF");

            }
        }, new IntentFilter("endTime"));

    //    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //   mSensorManager.registerListener(mListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
        startLocationService();
    }
    // private SensorManager mSensorManager;

    /**
     * 일정 시간 간격으로 GPS를 보내기위한 Timer를 SETTING 하는 함수
     * 서버로 GPS 정보를 보내는 것은 로그인한 후에 해야 한다.
     */
    public void setTimer() {
        // Timer가 작동되었을 때 수행해야하는 일을 run() 안에 적어준다.
        TimerTask timerTask = new TimerTask() {
            public void run() {
                if(!app.getUserName().equals("")) { // 로그인 상태를 한번 더 확인하고
                    if( xLatitude != latitude || xLongitude != longitude)   // 위치에 변화가 있는 경우만 서버로 위치를 전송한다.
                        app.updateGPS(latitude, longitude);
                }
            }
        };
        Timer timer = new Timer();
        // schedule(task, firstTime, period) : firstTime부터 period 간격으로 task를 수행한다.
        // 1000이면 1초
        timer.schedule(timerTask, 15000, 30000);
        Log.d("로그 : 위치 전송용 타이머", "타이머 ON");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            intent.putExtra("startTime", app.getStartTime());
            intent.putExtra("endTime", app.getEndTime());
            intent.putExtra("moving", app.getMovingDetect());
            intent.putExtra("collision", app.getCollisionDetect());
            startActivityForResult(intent, REQUEST_CODE_SAFETYSETTING);
            /*
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);*/
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id==R.id.nav_safety_mode) {  // 위험감지 모드 작동
            safetyModeButton.performClick();
        } else if (id == R.id.nav_safety_setting)  {    // 위험감지 모드 설정
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            intent.putExtra("startTime", app.getStartTime());
            intent.putExtra("endTime", app.getEndTime());
            intent.putExtra("moving", app.getMovingDetect());
            intent.putExtra("collision", app.getCollisionDetect());
            startActivityForResult(intent, REQUEST_CODE_SAFETYSETTING);
            // Handle the camera action
        } else if (id == R.id.nav_map) {    // 지도
            // 지도를 작동시킨 경우 경찰서 위치정보와 친구 위치정보를 요청한다
            // 서버에서 정보를 받으면 Handler에서 activity를 연다.
            app.requestPolice(latitude, longitude);
            app.requestFriendsGPS();
            /* 이 부분은 필요없음!
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            intent.putExtra("GPSFriends", friendsData);
            intent.putExtra("GPSPolice", policeData);
            startActivityForResult(intent, REQUEST_CODE_MAPS);
            */
        } else if (id == R.id.nav_friends) {    // 친구 정보
            Intent intent = new Intent(getApplicationContext(), FriendsActivity.class);
            ArrayList<String> friend = new ArrayList<String> (app.getFriendsList());
            intent.putExtra("list", friend);
            ArrayList<String> help = new ArrayList<String> (app.getFriendsInfo());
            intent.putExtra("help", help);
            startActivityForResult(intent, REQUEST_CODE_FRIENDS);
        } else if (id == R.id.nav_statistics) {
            Intent intent = new Intent(getApplicationContext(), StatisticActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onLoginBtnClicked(View v) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivityForResult(intent, REQUEST_CODE_LOGIN);
        //String user = intent.getExtras().getString("user_number");
    }

    public void setUserMain(String _user){
        main_user = _user;
        app.setUser(main_user);
    }

    public String getUserMain(){
        return main_user;
    }

    public void onJoinBtnClicked(View v) {
        Intent intent = new Intent(getApplicationContext(), JoinActivity.class);
        startActivityForResult(intent, REQUEST_CODE_JOIN);
    }

    /**
     * Activity를 전환할 때 startActivityForResult() 를 사용했는데 그 결과를 받아오기 위함
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_LOGIN) {
                String sName = intent.getExtras().getString("name");
                String sPhone = intent.getExtras().getString("phone");
                String sUser = intent.getExtras().getString("user_number");
                app.login(sName, sPhone);
                app.setUser(sUser);
            } else if (requestCode == REQUEST_CODE_JOIN) {
                String sName = intent.getExtras().getString("name");
                String sPhone = intent.getExtras().getString("phone");
                app.join(sName, sPhone);
            } else if (requestCode == REQUEST_CODE_FRIENDS) {
                // Friends Activity는 받아오는 결과의 케이스가 두가지로 나뉜다.
                // 1. 친구 추가 : sPhone에 추가할 폰 번호, help에 추가할 친구의 help 정보
                // 2. 친구의 help 정보 변경 : 친구 중에 help가 true인 친구의 phone 번호 리스트
                String sPhone = intent.getExtras().getString("phone");
                boolean help = intent.getExtras().getBoolean("statusFlag");
                ArrayList<String> status = intent.getExtras().getStringArrayList("status");
                if(!sPhone.equals("")) {    // sPhone에 값이 있으면 친구 추가인 경우
                    app.addFriend(sPhone, help);
                } else {    // 아니면 친구의 help 정보를 변경한 경우
                    app.changeFriendStatus(status);
                }
            } else if(requestCode == REQUEST_CODE_SAFETYSETTING) {
                int start = intent.getExtras().getInt("startTime");
                int end = intent.getExtras().getInt("endTime");
                Boolean move = intent.getExtras().getBoolean("movingCheck");
                Boolean collision = intent.getExtras().getBoolean("collisionDetectCheck");

                app.setSafetySetting(start, end, move, collision);
                alarmSet(start, end);      // 알람 작동할 시간을 설정한 후에 자동감지 기능을 작동시키게 한다.
            }
        }
    }

    /**
     * 설정한 시간에 자동감지기능이 작동할 수 있도록 alarm을 설정함
     * 설정 창에서 시간을 설정한 후에 alarm이 작동하도록 구현해놓았음!
     */
    public void alarmSet(int start, int end) {
        Intent startIntent = new Intent("startTime");   // 특정시간이 되면 startIntent가 작동한다.
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, startIntent, 0);
        Calendar cal = Calendar.getInstance();      // 일단 현재의 시간을 받아온다.
        cal.set(Calendar.HOUR_OF_DAY, start);   // HOUR_OF_DAY로 해야 24시간 단위의 시간이 됨  // 설정한 시간에 알람이 작동하게 현재시간에서 hour의 정보만 바꿔준다.
        startAlarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        startAlarm.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
        Log.d("로그 : 위험 자동감지 start시간", String.valueOf(cal.get(Calendar.HOUR_OF_DAY)));

        Intent endIntent = new Intent("endTime");   // 특정시간이 되면 endIntent가 작동한다.
        PendingIntent sender2 = PendingIntent.getBroadcast(this, 0, endIntent, 0);
        Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.HOUR_OF_DAY, end);
        endAlarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        endAlarm.set(AlarmManager.RTC_WAKEUP, cal2.getTimeInMillis(), sender2);
        Log.d("로그 : 위험 자동감지 end시간", String.valueOf(cal2.get(Calendar.HOUR_OF_DAY)));

    }

    // 자동감지모드를 수동으로 키거나 끈 경우
    public void onSafetymodeBtnClicked(View v) {

        if(app.getSafetyMode() == true) { // 수동으로 끈 경우
            app.setSafetyMode(false);
            Log.d("로그 : 위험 자동 감지 모드", "자동감지 모드 OFF");
            if(app.getCollisionDetect()) {
                sensor.unregister();
            }
        } else { // 수동으로 켠 경우
            app.setSafetyMode(true);
            Log.d("로그 : 위험 자동 감지 모드", "자동감지 모드 ON");
            if(app.getCollisionDetect()) {  // 충격감지 기능을 사용하는 경우
                sensor = new MySensor();
                sensor.register();
            } if(app.getMovingDetect()) { // 위치감지 기능을 사용하는 경우
                /* 채워줘야하는 부분 */
            }
        }
    }

    // 신고 기능을 키거나 끈 경우
    public void onImageBtnClicked(View v) {
        if(app.getAlertMode() == true) {    // 신고 기능을 끈 경우
            app.setAlertMode(false);
            player.pause();
        } else {    // 신고 기능을 켠 경우
            app.setAlertMode(true);
            makeAlarm();    // 알람 켜고
            sendEmergencySMS(); // 비상 메시지 전송
        }
    }

    // 경보음을 켜는 코드
    public void makeAlarm() {
        player.seekTo(0);
        player.start();
        // 경보음을 계속해서 재생하기 위해서 꺼지면 다시 재생시킴.
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                player.start();
            }
        });
    }

    public void sendEmergencySMS() {
        String msg = app.getUserName() + "님께서 " + "보낸 도움요청 신호입니다.";
        String url =  "http://maps.google.com/maps?q=" + latitude +"," + longitude+ "&hl=ko&z=17";

        // 메시지 전송한다고 check한 모든 친구에게 메시지를 전송
        SmsManager sms = SmsManager.getDefault();
        List<String> phone = app.getFriendsInfo();
        for( int i =0; i<phone.size(); i++) {
            Toast.makeText(this, phone.get(i) + "로 메시지를 전송합니다.", Toast.LENGTH_SHORT).show();
            // msg와 url을 함께보내는 경우 메시지전송이 되지 않음
            sms.sendTextMessage(phone.get(i), null, msg, null, null);
            sms.sendTextMessage(phone.get(i), null, url, null, null);
        }
    }

    class MySensor implements SensorEventListener {

        private SensorManager manager;
        private Sensor accelerormeterSensor;

        private long lastTime;
        private float speed, lastX, lastY, lastZ;
        private float x, y, z;

        private static final int SHAKE_THRESHOLD = 900;
        private static final int DATA_X = SensorManager.DATA_X;
        private static final int DATA_Y = SensorManager.DATA_Y;
        private static final int DATA_Z = SensorManager.DATA_Z;

        private int checkCnt;
        public MySensor() {
            manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            accelerormeterSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        public void register() {
            checkCnt = 0;
            manager.registerListener(this, accelerormeterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        public void unregister() {
            manager.unregisterListener(this);
        }
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                long currentTime = System.currentTimeMillis();
                long gabOfTime = (currentTime - lastTime);
                if(gabOfTime > 100) {
                    lastTime = currentTime;
                    x = event.values[SensorManager.DATA_X];
                    y = event.values[SensorManager.DATA_Y];
                    z = event.values[SensorManager.DATA_Z];

                    speed = Math.abs(x+y+z-lastX-lastY-lastZ) / gabOfTime * 10000;

                    if(speed > SHAKE_THRESHOLD) {
                        checkCnt ++;
                        if(checkCnt > 10) {
                            alertButton.performClick();
                            checkCnt = 0;
                        }
                    }
                    lastX = event.values[DATA_X];
                    lastY = event.values[DATA_Y];
                    lastZ = event.values[DATA_Z];
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    private  LocationManager manager;
    private GPSListener gpsListener;
    private void startLocationService() {
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 리스너 객체 생성
        gpsListener = new GPSListener();
        long minTime = 10000;
        float minDistance = 1;

        try {
            // GPS 기반 위치 요청
            manager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTime,
                    minDistance,
                    gpsListener);

            // 네트워크 기반 위치 요청
            manager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    minTime,
                    minDistance,
                    gpsListener);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }

        Toast.makeText(getApplicationContext(), "위치감지 시작, 로그 확인", Toast.LENGTH_SHORT).show();
    }

    private class GPSListener implements LocationListener {
        public void onLocationChanged(Location location) {
            xLatitude = latitude;
            xLongitude = longitude;
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            Log.i("로그 : 위치 변화", "Latitude : " + latitude + " / Longitude:" + longitude);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

    }

    /*
    private final SensorEventListener mListener = new SensorEventListener() {
        private int iOrientation = -1;

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        //센서의 값을 받을 수 있도록 호출되는 메소드

        public void onSensorChanged(SensorEvent event) {
            if (iOrientation < 0) {
                iOrientation = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            }
        }


    }; */
}