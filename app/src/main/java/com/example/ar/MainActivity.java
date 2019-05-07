package com.example.ar;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
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
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.core.constants.Constants.PRECISION_6;


//현재 지도


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        PermissionsListener, MapboxMap.OnMapClickListener {

    // Variables needed to initialize a map
    private MapboxMap mapboxMap;
    private MapView mapView;
    // Variables needed to handle location permissions
    private PermissionsManager permissionsManager;
    // Variables needed to add the location engine
    private LocationEngine locationEngine;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private LocationEngineResult result;
    // Variables needed to listen to location updates
    private MainActivityLocationCallback callback = new MainActivityLocationCallback(this);
    private static final String Tag = "MainActivity";


    private MapboxMap map;
    private Button startButton;
    private DirectionsRoute currentRoute;
    private MapboxDirections client;
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
    double La;
    double Lo;

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

        Button search_Button= findViewById(R.id.btnStartLoc);
        search_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), String.format("내 위치 : " + La + Lo), Toast.LENGTH_SHORT).show();
            }
        });
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            if (mapboxMap.getStyle() != null) {
                enableLocationComponent(mapboxMap.getStyle());
            }
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    class MainActivityLocationCallback implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<MainActivity> activityWeakReference;

        MainActivityLocationCallback(MainActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        @Override
        public void onSuccess(LocationEngineResult result) {
            Log.e(TAG,"onSuccess 실행");
            MainActivity activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();

                if (location == null) {
                    return;
                }

// Create a Toast which displays the new location's coordinates
                La = result.getLastLocation().getLatitude();
                Lo = result.getLastLocation().getLongitude();

//                Toast.makeText(activity, String.format("새로운 위치 : " + La + Lo), Toast.LENGTH_SHORT).show();

//                Toast.makeText(activity, String.format(activity.getString(R.string.new_location),
//                        String.format(result.getLastLocation().getLatitude()), String.format(result.getLastLocation().getLongitude())),
                //                      Toast.LENGTH_SHORT).show();

// Pass the new location to the Maps SDK's LocationComponent
                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can not be captured
         *
         * @param exception the exception message
         */
        @Override
        public void onFailure(@NonNull Exception exception) {
            Log.d("LocationChangeActivity", exception.getLocalizedMessage());
            MainActivity activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setCameraPostion(Location location) {
        Log.e(TAG,"setCameraPostion 실행");
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),15.0));
    }

    private void getRoute(Point origin, Point destination) {
        Log.e(TAG,"getRoute 실행");
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
                Log.e(TAG,"onResponse 실행");
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
//    private void getRoute2(Point origin, Point destination) {
//        NavigationRoute.builder().accessToken(Mapbox.getAccessToken()).origin(origin).destination(destination)
//                .build().getRoute(new Callback<DirectionsResponse>() {
//            @Override
//            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
//                if (response.body() == null) {
//                    Log.e(TAG, "no Routes found, check right user and access token");
//                    return;
//                } else if (response.body().routes().size() ==0) {
//                    Log.e(TAG, "no Routes found");
//                }
//                DirectionsRoute currentRoute = response.body().routes().get(0);
//                if (navigationMapRoute != null) {
//                    navigationMapRoute.removeRoute();
//                } else {
//                    navigationMapRoute = new NavigationMapRoute(null, mapView, map);
//                }
//                    navigationMapRoute.addRoute(currentRoute);
//            }
//            @Override
//            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
//
//                Log.e(TAG, "eError" + t.getMessage());
//            }
//        });
//    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        Log.e(Tag, "onMapReady");
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.TRAFFIC_NIGHT,
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                    }
                });
    }
    /**
     * Initialize the Maps SDK's LocationComponent
     */
    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        Log.e(TAG,"enableLocationComponent 실행");
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
// Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
// Set the LocationComponent activation options
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .useDefaultLocationEngine(false)
                            .build();
// Activate with the LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(locationComponentActivationOptions);
// Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);
// Set the component's camera mode
            locationComponent.setCameraMode(com.mapbox.mapboxsdk.location.modes.CameraMode.TRACKING);
// Set the component's render mode
            locationComponent.setRenderMode(com.mapbox.mapboxsdk.location.modes.RenderMode.COMPASS);
            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */
    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        Log.e(TAG,"initLocationEngine 실행");
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
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
        //될때도있고 안될때도 있음 ???원래 안되는게 정상
        mapboxMap.clear();
        mapboxMap.addPolyline(new PolylineOptions()
                .add(points)
                .color(Color.parseColor("#3bb2d0"))
                .width(5));
    }


    public void map_search(View view) {
        Log.e(TAG,"map_search 실행");
        //Intent intent = new Intent();
        //intent.setClassName("com.google.ar.core.examples.java.helloar", "com.google.ar.core.examples.java.helloar.HelloArActivity");
        //startActivity(intent);



        Toast.makeText(getApplicationContext(),editText.getText().toString(), Toast.LENGTH_LONG).show();
        getPointFromGeoCoder(editText.getText().toString());
        Point origin = Point.fromLngLat(Lo,La);
        Point destination = Point.fromLngLat(destinationX, destinationY);
        getRoute(origin,destination);//폴리라인 그리기
//        map.animateCamera(CameraUpdateFactory.newLatLngZoom(//카메라 위치 지정
//                // 카메라는 반대의 값으로 적어줄 것
//                // 뒤에 숫자 15은 카메라 확대 배수이다( 15가 적당 )
//                new LatLng(destination.latitude(), destination.longitude()), 12));
//        map.addMarker(new MarkerOptions()//마커 추가
//                .position(new LatLng(origin.latitude(), origin.longitude()))
//                //.title("소우")
//                .snippet("현재 위치"));
//        map.addMarker(new MarkerOptions()//마커 추가
//                .position(new LatLng(destination.latitude(), destination.longitude()))
//                //.title("소우")
//                .snippet("도착지"));
    }

    // 목적지 주소값을 통해 목적지 위도 경도를 얻어오는 구문
    public void getPointFromGeoCoder(String destinationxy) {
        Log.e(TAG,"지오코더 실행");
        Geocoder geocoder = new Geocoder(this);
        List<Address> listAddress = null;
        try {
            listAddress = geocoder.getFromLocationName(destinationxy, 1);
            destinationX = listAddress.get(0).getLongitude();
            destinationY = listAddress.get(0).getLatitude();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
//            locationEngine.requestLocationUpdates();
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
 //           locationEngine.requestLocationUpdates();
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
// Prevent leaks
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
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


    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        return false;
    }
}