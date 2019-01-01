package org.khucd.cdapp;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.StringTokenizer;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private SensorManager mSensorManager;
    private boolean mCompassEnabled;
    private String friends = "";
    private String police = "";
    Double latitude;
    Double longitude;

    @Override
    protected void onResume() {
        super.onResume();

        try{
            //내 위치 자동 표시 disable
            mMap.setMyLocationEnabled(true);
        }catch(SecurityException e){
            e.printStackTrace();
        }

        if(mCompassEnabled){
            mSensorManager.unregisterListener(mListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        try{
            //내 위치 자동 표시 enable
            mMap.setMyLocationEnabled(false);
        }catch(SecurityException e){
            e.printStackTrace();
        }

        if(mCompassEnabled){
            mSensorManager.registerListener(mListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     //   setContentView(R.layout.activity_maps);
      //  SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
       //         .findFragmentById(R.id.maps);
       // mapFragment.getMapAsync(this);
        //mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps)).getMap();

        Window win = getWindow();
        win.setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maps);
        mapFragment.getMapAsync(this);
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps)).getMap();

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout linear = (LinearLayout) inflater.inflate(R.layout.map_button, null);
        LinearLayout.LayoutParams paramlinear = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT);
        win.addContentView(linear, paramlinear);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mCompassEnabled = true;
        startLocationService();

        Intent intent = getIntent();
        friends = intent.getExtras().getString("GPSFriends");
        police = intent.getExtras().getString("GPSPolice");
        Toast.makeText(this,friends,Toast.LENGTH_LONG).show();
        Toast.makeText(this,police,Toast.LENGTH_LONG).show();

        Button btn1 = (Button) findViewById(R.id.btn1);
        Button btn2 = (Button) findViewById(R.id.btn2);
        btn1.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Log.i("내 위치 선택!", "선택 완료");
                //여기에 각자 출력 함수 넣으면 됨.
                StringTokenizer t = new StringTokenizer(friends, "|");
                String number = t.nextToken();
                String protocol = t.nextToken();
                while(t.hasMoreTokens()){
                    number = t.nextToken();
                    double lat = Double.parseDouble(t.nextToken());
                    double lon = Double.parseDouble(t.nextToken());

                    MarkerOptions marker = new MarkerOptions();
                    marker.position(new LatLng(lat, lon));
                    marker.title(number);
                    marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker));
                    mMap.addMarker(marker);
                }
            }
        });
        btn2.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Log.i("경찰서 위치 선택!", "선택 완료");
                //여기에 각자 출력 함수 넣으면 됨.
                StringTokenizer t = new StringTokenizer(police, "|");
                String number = t.nextToken();
                String protocol = t.nextToken();
                while(t.hasMoreTokens()){
                    number = t.nextToken();
                    double lat = Double.parseDouble(t.nextToken());
                    double lon = Double.parseDouble(t.nextToken());

                    MarkerOptions marker = new MarkerOptions();
                    marker.position(new LatLng(lat, lon));
                    marker.title(number);
                    marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker));
                    mMap.addMarker(marker);
                }
            }
        });
        //센서 관리자 객체 참조
    }

    public void getLocationInfo() {
     //   mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
     //   mCompassEnabled = true;
        startLocationService();
    }

    private void startLocationService() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 리스너 객체 생성
        GPSListener gpsListener = new GPSListener();
        long minTime = 10000;
        float minDistance = 0;

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

        Toast.makeText(getApplicationContext(), "위치 확인 시작함. 로그를 확인하세요.", Toast.LENGTH_SHORT).show();
    }

    private class GPSListener implements LocationListener {
        /**
         * 위치 정보가 확인되었을 때 호출되는 메소드
         */
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            String msg = "Latitude : " + latitude + "\nLongitude:" + longitude;
            Log.i("GPSLocationService", msg);

            // 현재 위치의 지도를 보여주기 위해 정의한 메소드 호출
            showCurrentLocation(latitude, longitude);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }

    private void showCurrentLocation(Double latitude, Double longitude) {
        // 현재 위치를 이용해 LatLon 객체 생성
        LatLng curPoint = new LatLng(latitude, longitude);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 15));

        // 지도 유형 설정. 지형도인 경우에는 GoogleMap.MAP_TYPE_TERRAIN, 위성 지도인 경우에는 GoogleMap.MAP_TYPE_SATELLITE
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    /**
     * 센서의 정보를 받기 위한 리스너 객체 생성
     */
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


    };

    public double getlatitudeInfo() {return latitude;}
    public double getlongitudeInfo(){return longitude;}
}
