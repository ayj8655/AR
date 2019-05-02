package com.example.ar;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.core.constants.Constants.PRECISION_6;


//현재 지도


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener,
        PermissionsListener, MapboxMap.OnMapClickListener {
    // private MapView mapView;
    private MapView mapView;
    private MapboxMap map;
    private Button startButton;
    private DirectionsRoute currentRoute;
    private MapboxDirections client;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation;
    private Point orginPosition;
    private Point destinationPosition;
    private Marker destinationMarker;
    private NavigationMapRoute navigationMapRoute;
    private  static final String TAG = "MainActivity";


    EditText editText;

    double destinationX; // longitude
    double destinationY; // latitude

    Polyline mPolyline;

//288

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 맵박스 사용하기 위한 접근 토큰 지정
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);
        editText =(EditText)findViewById(R.id.txtDestination);
        startButton = findViewById(R.id.button3);
        // 아래 함수로 통해 목적지 주소값을 위도 경도 값으로 변경
        // getPointFromGeoCoder("null");
        // 사용자 현재 gps 위치
     //  Point origin = Point.fromLngLat(longitude, latitude);
        // 도착지 gps 위치
   //     final Point destination = Point.fromLngLat(destinationX, destinationY);
        // Setup the MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //네이게이션  ui 실행
                Log.e(TAG,"네비 실행");
                NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                        .origin(orginPosition).destination(destinationPosition)
                        .shouldSimulateRoute(true)
                        .build();
                NavigationLauncher.startNavigation(MainActivity.this,options);
            }
        });
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected() {
        Log.e(TAG,"onConnected 실행");
        locationEngine.requestLocationUpdates();
    }



    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG,"onLocationChanged 실행");
        if(location != null){
            originLocation = location;
            setCameraPostion(location);
        }
        locationLayerPlugin = new LocationLayerPlugin(mapView,map,locationEngine);
        locationLayerPlugin.setCameraMode(CameraMode.TRACKING_COMPASS);
        locationLayerPlugin.setRenderMode(RenderMode.COMPASS);
}

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }


    @SuppressWarnings("MissingPermission")
    private  void initializeLocationEngine() {
        Log.e(TAG,"initializeLocationEngine 실행");
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            originLocation = lastLocation;
            setCameraPostion(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }
    @SuppressWarnings("MissingPermission")
    private void  initializeLocationLayer() {
        Log.e(TAG,"initializeLocationLayer 실행");
        locationLayerPlugin = new LocationLayerPlugin(mapView,map,locationEngine);
        locationLayerPlugin.setLocationLayerEnabled(true);
        locationLayerPlugin.setCameraMode(CameraMode.TRACKING_COMPASS);
        locationLayerPlugin.setRenderMode(RenderMode.COMPASS);
    }


    private void setCameraPostion(Location location) {
        Log.e(TAG,"setCameraPostion 실행");
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),17.0));
    }

    public void onMapClick(LatLng point) {
        Log.e(TAG,"onMapClick 실행");
        if (destinationMarker != null) {
            map.removeMarker(destinationMarker);
        }
        destinationMarker = map.addMarker(new MarkerOptions().position(point));
        destinationPosition = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        orginPosition = Point.fromLngLat(originLocation.getLongitude(), originLocation.getLatitude());

        getRoute2(orginPosition, destinationPosition);
        startButton.setEnabled(true);
        startButton.setBackgroundResource(R.color.mapbox_blue);
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


    private void getRoute2(Point origin, Point destination) {
        NavigationRoute.builder().accessToken(Mapbox.getAccessToken()).origin(origin).destination(destination)
                .build().getRoute(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                if (response.body() == null) {
                    Log.e(TAG, "no Routes found, check right user and access token");
                    return;
                } else if (response.body().routes().size() ==0) {
                    Log.e(TAG, "no Routes found");
                }

                DirectionsRoute currentRoute = response.body().routes().get(0);

                if (navigationMapRoute != null) {
                    navigationMapRoute.removeRoute();
                } else {
                    navigationMapRoute = new NavigationMapRoute(null, mapView, map);

                }
                    navigationMapRoute.addRoute(currentRoute);

            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {

                Log.e(TAG, "eError" + t.getMessage());
            }
        });
    }



    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        Log.e(TAG,"onMapReady 실행");

        map = mapboxMap;
        map.addOnMapClickListener(this);
        enableLocation();



    }



    private void enableLocation() {
        Log.e(TAG,"enableLocation 실행");
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            initializeLocationEngine();
            initializeLocationLayer();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }





    // 목적지 주소값을 통해 목적지 위도 경도를 얻어오는 구문
    public void getPointFromGeoCoder(String addr) {
        Log.e(TAG,"지오코더 실행");
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
        Log.e(TAG,"drawRoute 실행");


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
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
        }
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStart();
        }
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
        }
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStop();
        }


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
        if (locationEngine != null) {
            locationEngine.deactivate();
        }

        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
//
//    public void newmap(View view) {
//        // 카메라 위치 고정(내 gps 위치로 임의지정)
//        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                // 카메라는 반대의 값으로 적어줄 것
//                // 뒤에 숫자 15은 카메라 확대 배수이다( 15가 적당 )
//                new LatLng(latitude, longitude), 8));
//    }

    public void map_search(View view) {
        Log.e(TAG,"map_search 실행");
        //Intent intent = new Intent();
        //intent.setClassName("com.google.ar.core.examples.java.helloar", "com.google.ar.core.examples.java.helloar.HelloArActivity");
        //startActivity(intent);

        Toast.makeText(getApplicationContext(),editText.getText().toString(), Toast.LENGTH_LONG).show();
        getPointFromGeoCoder(editText.getText().toString());
        final Point origin = Point.fromLngLat(originLocation.getLongitude(), originLocation.getLatitude());
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