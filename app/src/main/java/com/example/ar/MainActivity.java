package com.example.ar;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.core.constants.Constants.PRECISION_6;


//현재 지도


public class MainActivity extends AppCompatActivity {
    // private MapView mapView;
    private static final String TAG = "DirectionsActivity";
    private MapView mapView;
    private MapboxMap map;
    private DirectionsRoute currentRoute;
    private MapboxDirections client;
    double destinationX; // longitude
    double destinationY; // latitude


    private Button btnShowLocation;
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;
    // GPSTracker class
    private GpsInfo gps;

//288

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnShowLocation = (Button) findViewById(R.id.btn_start);


        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // 권한 요청을 해야 함
                if (!isPermission) {
                    callPermission();
                    return;
                }
                gps = new GpsInfo(MainActivity.this);
                // GPS 사용유무 가져오기
                if (gps.isGetLocation()) {
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    Toast.makeText(
                            getApplicationContext(),
                            "당신의 위치 - \n위도: " + latitude + "\n경도: " + longitude,
                            Toast.LENGTH_LONG).show();
                } else {
                    // GPS 를 사용할수 없으므로
                    gps.showSettingsAlert();
                }
            }
        });

        callPermission();  // 권한 요청을 해야 함


        // 맵박스 사용하기 위한 접근 토큰 지정
        Mapbox.getInstance(this, getString(R.string.access_token));
        // 아래 함수로 통해 목적지 주소값을 위도 경도 값으로 변경
        getPointFromGeoCoder("충청남도 천안시 동남구 단대로 119");
        // 사용자 현재 gps 위치
        final Point origin = Point.fromLngLat(127.17937, 36.833038);
        // 도착지 gps 위치
        final Point destination = Point.fromLngLat(destinationX, destinationY);
        // Setup the MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
                // 카메라 위치 고정(내 gps 위치로 임의지정)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        // 카메라는 반대의 값으로 적어줄 것
                        // 뒤에 숫자 15은 카메라 확대 배수이다( 15가 적당 )
                        new LatLng(36.833038, 127.17937), 12));
                // Add origin and destination to the map
                mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(origin.latitude(), origin.longitude()))
                        // 타이틀은 상호명 건물명, snippet은 설명 그에 대한 설명이다
                        // 출발지
                        //.title("소우")
                        .snippet("출발지"));
                mapboxMap.addMarker(new MarkerOptions()
                        // 목적지
                        .position(new LatLng(destination.latitude(), destination.longitude()))
                        //.title("가비")
                        .snippet("도착지"));
                // Get route from API
                getRoute(origin, destination);
            }
        });
    }







    private void getRoute(Point origin, Point destination) {

        client = MapboxDirections.builder()
                .origin(origin)
                .destination(destination)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_CYCLING)
                .accessToken(getString(R.string.access_token))
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                System.out.println(call.request().url().toString());

                // You can get the generic HTTP info about the response
                Log.d(TAG, "Response code: " + response.code());
                if (response.body() == null) {
                    Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().routes().size() < 1) {
                    Log.e(TAG, "No routes found");
                    return;
                }

                // Print some info about the route
                currentRoute = response.body().routes().get(0);
                Log.d(TAG, "Distance: " + currentRoute.distance());

                // Draw the route on the map
                drawRoute(currentRoute);
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Log.e(TAG, "Error: " + throwable.getMessage());
                Toast.makeText(MainActivity.this, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawRoute(DirectionsRoute route) {
        // Convert LineString coordinates into LatLng[]
        LineString lineString = LineString.fromPolyline(route.geometry(), PRECISION_6);
        List<Point> coordinates = lineString.coordinates();
        LatLng[] points = new LatLng[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = new LatLng(
                    coordinates.get(i).latitude(),
                    coordinates.get(i).longitude());
        }

        // Draw Points on MapView
        map.addPolyline(new PolylineOptions()
                .add(points)
                .color(Color.parseColor("#009688"))
                .width(5));
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel the directions API request
        if (client != null) {
            client.cancelCall();
        }
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    // 목적지 주소값을 통해 목적지 위도 경도를 얻어오는 구문
    public void getPointFromGeoCoder(String addr) {
        String destinationAddr = addr;

        Geocoder geocoder = new Geocoder(this);
        List<Address> listAddress = null;
        try {
            listAddress = geocoder.getFromLocationName(destinationAddr, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        destinationX = listAddress.get(0).getLongitude();
        destinationY = listAddress.get(0).getLatitude();

        System.out.println( addr + "'s Destination x, y = " + destinationX + ", " + destinationY);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessFineLocation = true;

        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            isAccessCoarseLocation = true;
        }

        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
    }

    // 전화번호 권한 요청
    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }


}