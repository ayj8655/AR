package com.example.ar;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.EditText;
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
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

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
    Double latitude;
    Double longitude;
    EditText editText;

    double destinationX; // longitude
    double destinationY; // latitude

    Polyline mPolyline;

//288

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        latitude= intent.getExtras().getDouble("위도");
        longitude= intent.getExtras().getDouble("경도");

        editText =(EditText)findViewById(R.id.editText11);

        // 맵박스 사용하기 위한 접근 토큰 지정
        Mapbox.getInstance(this, getString(R.string.access_token));
        // 아래 함수로 통해 목적지 주소값을 위도 경도 값으로 변경
   //     getPointFromGeoCoder("null");
        // 사용자 현재 gps 위치
        final Point origin = Point.fromLngLat(longitude, latitude);
        // 도착지 gps 위치
   //     final Point destination = Point.fromLngLat(destinationX, destinationY);
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
                        new LatLng(latitude, longitude), 12));
//                // Add origin and destination to the map
//                mapboxMap.addMarker(new MarkerOptions()
//                        .position(new LatLng(origin.latitude(), origin.longitude()))
//                        // 타이틀은 상호명 건물명, snippet은 설명 그에 대한 설명이다
//                        // 출발지
//                        //.title("소우")
//                        .snippet("현재 위치"));
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
        map.addPolyline(new PolylineOptions().add(points).color(Color.parseColor("#009688")).width(5));
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

    public void newmap(View view) {
        // 카메라 위치 고정(내 gps 위치로 임의지정)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                // 카메라는 반대의 값으로 적어줄 것
                // 뒤에 숫자 15은 카메라 확대 배수이다( 15가 적당 )
                new LatLng(latitude, longitude), 12));
    }

    public void map_search(View view) {
        Toast.makeText(getApplicationContext(),editText.getText().toString(), Toast.LENGTH_LONG).show();
        getPointFromGeoCoder(editText.getText().toString());
        final Point origin = Point.fromLngLat(longitude, latitude);
        final Point destination = Point.fromLngLat(destinationX, destinationY);

        map.clear();//마커 및 폴리라인 모두 지우기

        getRoute(origin,destination);//폴리라인 그리기
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(//카메라 위치 지정
                // 카메라는 반대의 값으로 적어줄 것
                // 뒤에 숫자 15은 카메라 확대 배수이다( 15가 적당 )
                new LatLng(destination.latitude(), destination.longitude()), 12));
        map.addMarker(new MarkerOptions()//마커 추가
                        .position(new LatLng(origin.latitude(), origin.longitude()))
                        //.title("소우")
                        .snippet("현재 위치"));
        map.addMarker(new MarkerOptions()//마커 추가
                .position(new LatLng(destination.latitude(), destination.longitude()))
                //.title("소우")
                .snippet("도착지"));


    }
}