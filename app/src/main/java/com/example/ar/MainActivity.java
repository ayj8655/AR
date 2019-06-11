package com.example.ar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
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
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.core.constants.Constants.PRECISION_6;

import java.util.Arrays;
import java.util.Locale;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.api.Places;

//현재 지도
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener,
        MapboxMap.OnMapClickListener, NavigationView.OnNavigationItemSelectedListener {

    LoginActivity loginActivity;
    BackgroundWorker backgroundWorker;

    private DrawerLayout mDrawerlayout;
    private ActionBarDrawerToggle mToggle;

    // Variables needed to initialize a map
    private MapboxMap mapboxMap;
    private MapView mapView;

    // Variables needed to handle location permissions
    private PermissionsManager permissionsManager;

    // Variables needed to add the location engine
    private LocationEngine locationEngine;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;

    // Variables needed to listen to location updates
    private MainActivityLocationCallback callback = new MainActivityLocationCallback(this);
    private static final String Tag = "MainActivity";

    //navigation
    private Location originLocation;
    private Point originPosition;
    private Point destinatonPosition;
    private Marker destinationMarker;
    private Button startButton;
    private NavigationMapRoute navigationMapRoute;
    private NavigationRoute navigationRoute;
    public static DirectionsRoute currentRoute;
    private MapboxDirections client;
    private  static final String TAG = "MainActivity";

    EditText editText;

    double destinationX; // longitude
    double destinationY; // latitude
    public static double La;          //latitude
    public static double Lo;          // longitude

    //String TAG = "placeautocomplete";
    TextView txtView;

    // FB add
    public TextView txtname, txtemail;
    static int facebook_login = 0;
    String f_userid, f_userpw, f_username;

    //bookMark
    BookMarkList bookMarkList;
    Button a_d;
    String user_id, place_mark, type, buttonState;

    //STT
    AutocompleteSupportFragment STT;

    //기본 위도 경도 36.8341039, 127.1792902
    //https://docs.mapbox.com/unity/maps/examples/world-scale-ar/ 월드스케일AR 예제 설명
    //36.834 , 127.179
    //36.833297 , 127.179541



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));// 맵박스 사용하기 위한 접근 토큰 지정
        setContentView(R.layout.activity_main);
        Log.e(TAG,"onCreate 실행");


        startButton = findViewById(R.id.startButton);
        editText =(EditText)findViewById(R.id.txtDestination);
        Button search_Button= findViewById(R.id.btnStartLoc);

        startButton.setOnClickListener(new View.OnClickListener() { //네비게이션 버튼 나타내기+ar 실행하기
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), UnityPlayerActivity.class);
                startActivity(intent);
                //유니티 플레이어 액티비티 실행

                NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                        .directionsRoute(currentRoute)
                        .build();
                // Call this method with Context from within an Activity
                NavigationLauncher.startNavigation(MainActivity.this, options);
                //네비게이션 실행 (MainActivity에서)
            }
        });

        // Setup the MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        //loginSession

        if(loginActivity.loginId == null && loginActivity.loginPwd == null && loginActivity.nonMember == 0){
            //로그인 액티비티의 값들을 가지고 세션이 있는지 없는지 확인.
            if(facebook_login == 1){
                if(backgroundWorker.user_info != null){
                    String str = backgroundWorker.user_info;
                    f_userid = str.split(":")[0];  //":"를 기준으로 문자열 자름
                    f_userpw = str.split(":")[1];
                    f_username = str.split(":")[2];
                    backgroundWorker.user_info = null;
                }
                LoginManager.getInstance().logOut();
                SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
                SharedPreferences.Editor autoLogin = auto.edit();
                autoLogin.putString("inputId", f_userid);
                autoLogin.putString("inputPwd", f_userpw );
                autoLogin.putString("inputName", f_username);
                autoLogin.putString("inputTeg", "facebook");

                autoLogin.commit();
                Toast.makeText(MainActivity.this, f_userid +"님 환영합니다.", Toast.LENGTH_SHORT).show();
            }

            else{

                Log.e(TAG,"loginSession else 실행");
                SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
                SharedPreferences.Editor autoLogin = auto.edit();
                if(loginActivity.UseridEt!=null) {
                    autoLogin.putString("inputId", loginActivity.UseridEt.getText().toString());
                    autoLogin.putString("inputPwd", loginActivity.PasswordEt.getText().toString());
                    autoLogin.putString("inputName", backgroundWorker.user_info);
                    autoLogin.putString("inputTeg", null);
                    autoLogin.commit();
                    Toast.makeText(MainActivity.this, loginActivity.UseridEt.getText().toString() + "님 환영합니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }

        //.loginSession

        search_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraPosition position = new CameraPosition.Builder()
                        .target(new LatLng(La, Lo)) // Sets the new camera position
                        .zoom(13) // Sets the zoom , 줌 정도 숫자가 클수록 더많이 줌함
                        .bearing(180) // Rotate the camera , 카메라 방향(북쪽이 0) 북쪽부터 시계방향으로 측정
                        .tilt(0) // Set the camera tilt , 각도
                        .build(); // Creates a CameraPosition from the builder

                //https://docs.mapbox.com/android/maps/overview/camera/

                //카메라 움직이기
                mapboxMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(position), 7000);

                Toast.makeText(getApplicationContext(), String.format("            내위치 \n위도 : " + La + "\n경도 : "+Lo), Toast.LENGTH_SHORT).show();
            }
        });

        mDrawerlayout = (DrawerLayout) findViewById(R.id.drawer);
        NavigationView navigationView = findViewById(R.id.navigationView2);
        navigationView.setNavigationItemSelectedListener(this);

        mToggle = new ActionBarDrawerToggle(this, mDrawerlayout, R.string.open, R.string.close);
        mDrawerlayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //네비게이션바에 로그인 정보
        View nav_header_view = navigationView.getHeaderView(0);

        txtname = nav_header_view.findViewById(R.id.txtName);
        txtemail = nav_header_view.findViewById(R.id.txtEmail);


        if(facebook_login == 1){
            if(loginActivity.loginName == null){
                //처음 로그인할때
                // txtname.setText(backgroundWorker.user_info);
                user_id = f_userid;
                txtname.setText(f_username);
                txtemail.setText(f_userpw);
            }else{
                //자동 로그인일때
                //txtname.setText(MainActivity.loginName);
                user_id = loginActivity.loginId;
                if(loginActivity.loginName != null) {
                    txtname.setText(loginActivity.loginName);
                    txtemail.setText(loginActivity.loginPwd);
                }
            }
        }else{
            if(loginActivity.loginName == null){
                //처음 로그인할때
                user_id = loginActivity.UseridEt.getText().toString();
                // txtname.setText(backgroundWorker.user_info);
                if(backgroundWorker.user_info != null) {
                    String str = backgroundWorker.user_info;
                    String str_name = str.split(":")[0];  //":"를 기준으로 문자열 자름
                    String str_email = str.split(":")[1];
                    txtname.setText(str_name);
                    txtemail.setText(str_email);
                    backgroundWorker.user_info=null;
                }
            }else{
                //자동 로그인일때
                //txtname.setText(MainActivity.loginName);
                if(loginActivity.loginName != null) {
                    String str = loginActivity.loginName;
                    String str_name = str.split(":")[0];
                    String str_email = str.split(":")[1];
                    txtname.setText(str_name);
                    txtemail.setText(str_email);
                }
                user_id = loginActivity.loginId;
            }
        }


        //.네비게이션 바에 로그인 정보

        //장소 자동완성
        Button search = (Button)findViewById(R.id.btnSearch);
        search.bringToFront();
        txtView = findViewById(R.id.txtDestination);

        // Initialize Places.
        Places.initialize(getApplicationContext(), "AIzaSyA1zuxMWkupxfZ7ePhoFlII-TlRs6-wFTw");
        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        STT = autocompleteFragment;
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                a_d.setText("즐겨찾기 등록");
                buttonState = "add";

                // TODO: Get info about the selected place.
                txtView.setText(place.getName());
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        //.장소 자동완성
        //Speak to Text 버튼

        Button sttButton = (Button)findViewById(R.id.btn_stt);
        sttButton.bringToFront();

        sttButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
                try{
                    startActivityForResult(intent,200);
                }catch (ActivityNotFoundException a){
                    Toast.makeText(getApplicationContext(),"Intent problem", Toast.LENGTH_SHORT).show();
                }

            }
        });

        //bookMark
        //userid = (TextView)findViewById(R.id.userid);
        //place = (EditText)findViewById(R.id.place);
        a_d = (Button)findViewById(R.id.btn_add_delete);

        bookMarkList.list.clear();

        if(bookMarkList.ListView == 1){
            autocompleteFragment.setText(bookMarkList.getListViewString);
            a_d.setText("즐겨찾기 삭제");
            buttonState = "delete";
            bookMarkList.ListView = 0;
        }else{
            a_d.setText("즐겨찾기 등록");
            buttonState = "add";
            bookMarkList.ListView = 0;
        }
        //.bookMark

    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 200){
            if(resultCode == RESULT_OK && data != null){
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                editText.setText(result.get(0));

                startButton.setEnabled(true);
                getPointFromGeoCoder(editText.getText().toString());
                Point origin = Point.fromLngLat(Lo,La);
                Point destination = Point.fromLngLat(destinationX, destinationY);
                getRoute_walking(origin,destination);//폴리라인 그리기
                getRoute_navi_walking(origin,destination);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }


    public void showDialog2(View _view) //검색시 다이얼로그 띄우기
    {
        final CharSequence[] oItems = {"도보", "자전거", "자동차"};

        AlertDialog.Builder oDialog = new AlertDialog.Builder(this,
                android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);

        oDialog.setTitle("방법을 선택하세요")
                .setItems(oItems, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                         if (which == 0 ) {
                             //도보 길찾기 진행
                             getPointFromGeoCoder(editText.getText().toString());
                             Point origin = Point.fromLngLat(Lo,La);
                             Point destination = Point.fromLngLat(destinationX, destinationY);
                             getRoute_walking(origin,destination);//예상 시간 및 위도 경도 출력
                             getRoute_navi_walking(origin,destination);//네비게이션 정보 저장

                             startButton.setEnabled(true);

                         } else if ( which == 1) {
                            //자전거 길찾기 진행
                             getPointFromGeoCoder(editText.getText().toString());
                             Point origin = Point.fromLngLat(Lo,La);
                             Point destination = Point.fromLngLat(destinationX, destinationY);
                             getRoute_CYCLING(origin,destination);//예상 시간 및 위도 경도 출력
                             getRoute_navi_CYCLING(origin,destination);//네비게이션 정보 저장

                             startButton.setEnabled(true);

                         } else if (which == 2) {
                             //자동차 길찾기 진행
                             getPointFromGeoCoder(editText.getText().toString());
                             Point origin = Point.fromLngLat(Lo,La);
                             Point destination = Point.fromLngLat(destinationX, destinationY);
                             getRoute_DRIVING(origin,destination);//예상 시간 및 위도 경도 출력
                             getRoute_navi_DRIVING(origin,destination);//네비게이션 정보 저장

                             startButton.setEnabled(true);

                         } else {
                             Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_LONG).show();
                         }


                        Toast.makeText(getApplicationContext(), oItems[which], Toast.LENGTH_LONG).show();
                    }
                })
                .setCancelable(false) //뒤로가기로 취소 막기
                .show();
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.BOOKMARK:
                type ="callList";
                BackgroundWorker backgroundWorker = new BackgroundWorker(this);
                backgroundWorker.execute(type, user_id);
                break;
            case R.id.GUIDE:
                Intent intent_guide = new Intent(MainActivity.this, menu_GUIDE.class);
                startActivity(intent_guide);
                break;
            case R.id.LOGOUT:
                LoginManager.getInstance().logOut();
                loginActivity.facebook_login = 0;
                SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = auto.edit();
                editor.clear();
                editor.commit();
                Intent intent_logout = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent_logout);
                Toast.makeText(MainActivity.this, "로그아웃.", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case R.id.INFO:
                Intent intent_info = new Intent(MainActivity.this, menu_VERSION.class);
                startActivity(intent_info);
                break;
            case R.id.EXIT:
                LoginManager.getInstance().logOut();
                moveTaskToBack(true);
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
                break;
            case R.id.STYLECHANGE:
                final CharSequence[] oItems = {"STREETS", "DARK", "LIGHT", "OUTDOORS", "SATELLITE", "SATELLITE_STREETS"};

                AlertDialog.Builder oDialog = new AlertDialog.Builder(this,
                        android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);

                oDialog.setTitle("스타일을 지정하세요")
                        .setItems(oItems, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if (which == 0 ) {
                                    mapboxMap.setStyle(Style.MAPBOX_STREETS);

                                } else if ( which == 1) {
                                    mapboxMap.setStyle(Style.DARK);

                                } else if (which == 2) {
                                    mapboxMap.setStyle(Style.LIGHT);

                                } else if (which == 3) {
                                    mapboxMap.setStyle(Style.OUTDOORS);

                                }else if (which == 4) {
                                    mapboxMap.setStyle(Style.SATELLITE);

                                }else if (which == 5) {
                                    mapboxMap.setStyle(Style.SATELLITE_STREETS);

                                }else {
                                    Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_LONG).show();
                                }
                                Toast.makeText(getApplicationContext(), oItems[which], Toast.LENGTH_LONG).show();
                            }
                        })
                        .setCancelable(false) //뒤로가기로 취소 막기
                        .show();

        }
        mDrawerlayout.closeDrawer(GravityCompat.START);
        return true;
    }

    //즐겨찾기 등록/삭제 버튼 클릭
    public void onMark(View view) {
        place_mark = txtView.getText().toString();
        type = "bookmark";

        BackgroundWorker backgroundWorker = new BackgroundWorker(this);
        backgroundWorker.execute(type, user_id, place_mark, buttonState);
    }
    //


    //https://docs.mapbox.com/help/tutorials/android-location-listening/
    //안드로이드 기기 위치 추적
    //현재 위치 얻어오는 콜백
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
            Log.e("LocationChangeActivity", exception.getLocalizedMessage());
            MainActivity activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getRoute_walking(Point origin, Point destination) {
        Log.e(TAG,"getRoute 실행");
        client = MapboxDirections.builder()
                .origin(origin)//출발지 위도 경도
                .destination(destination)//도착지 위도 경도
                .overview(DirectionsCriteria.OVERVIEW_FULL)//정보 받는정도 최대
                .profile(DirectionsCriteria.PROFILE_WALKING)//길찾기 방법(도보,자전거,자동차)
                .accessToken(getString(R.string.access_token))
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                Log.e(TAG,"onResponse 실행");
                System.out.println(call.request().url().toString());
                // You can get the generic HTTP info about the response
                Log.e(TAG, "Response code: " + response.code());
                if (response.body() == null) {
                    Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().routes().size() < 1) {
                    Log.e(TAG, "No routes found");
                    return;
                }
                // Print some info about the route
                currentRoute = response.body().routes().get(0);
                Log.e(TAG, "Distance: " + currentRoute.distance());

                int time = (int) (currentRoute.duration()/60);
                //예상 시간을초단위로 받아옴
                double distants = (currentRoute.distance()/1000);
                //목적지까지의 거리를 m로 받아옴

                distants = Math.round(distants*100)/100.0;
                //Math.round() 함수는 소수점 첫째자리에서 반올림하여 정수로 남긴다
                //원래 수에 100곱하고 round 실행 후 다시 100으로 나눈다 -> 둘째자리까지 남김

                Toast.makeText(getApplicationContext(), String.format("예상 시간 : " + String.valueOf(time)+" 분 \n" +
                        "목적지 거리 : " +distants+ " km"), Toast.LENGTH_LONG).show();
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

    private void getRoute_CYCLING(Point origin, Point destination) {
        Log.e(TAG,"getRoute 실행");
        client = MapboxDirections.builder()
                .origin(origin)//출발지 위도 경도
                .destination(destination)//도착지 위도 경도
                .overview(DirectionsCriteria.OVERVIEW_FULL)//정보 받는정도 최대
                .profile(DirectionsCriteria.PROFILE_CYCLING)//길찾기 방법(도보,자전거,자동차)
                .accessToken(getString(R.string.access_token))
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                Log.e(TAG,"onResponse 실행");
                System.out.println(call.request().url().toString());
                // You can get the generic HTTP info about the response
                Log.e(TAG, "Response code: " + response.code());
                if (response.body() == null) {
                    Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().routes().size() < 1) {
                    Log.e(TAG, "No routes found");
                    return;
                }
                // Print some info about the route
                currentRoute = response.body().routes().get(0);
                Log.e(TAG, "Distance: " + currentRoute.distance());

                int time = (int) (currentRoute.duration()/60);
                //예상 시간을초단위로 받아옴
                double distants = (currentRoute.distance()/1000);
                //목적지까지의 거리를 m로 받아옴
                distants = Math.round(distants*100)/100.0;

                //Math.round() 함수는 소수점 첫째자리에서 반올림하여 정수로 남긴다
                //원래 수에 100곱하고 round 실행 후 다시 100으로 나눈다 -> 둘째자리까지 남김
                Toast.makeText(getApplicationContext(), String.format("예상 시간 : " + String.valueOf(time)+" 분 \n" +
                        "목적지 거리 : " +distants+ " km"), Toast.LENGTH_LONG).show();
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


    private void getRoute_DRIVING(Point origin, Point destination) {
        Log.e(TAG,"getRoute 실행");
        client = MapboxDirections.builder()
                .origin(origin)//출발지 위도 경도
                .destination(destination)//도착지 위도 경도
                .overview(DirectionsCriteria.OVERVIEW_FULL)//정보 받는정도 최대
                .profile(DirectionsCriteria.PROFILE_DRIVING)//길찾기 방법(도보,자전거,자동차)
                .accessToken(getString(R.string.access_token))
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                Log.e(TAG,"onResponse 실행");
                System.out.println(call.request().url().toString());
                // You can get the generic HTTP info about the response
                Log.e(TAG, "Response code: " + response.code());
                if (response.body() == null) {
                    Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().routes().size() < 1) {
                    Log.e(TAG, "No routes found");
                    return;
                }
                // Print some info about the route
                currentRoute = response.body().routes().get(0);
                Log.e(TAG, "Distance: " + currentRoute.distance());

                int time = (int) (currentRoute.duration()/60);
                //예상 시간을초단위로 받아옴
                double distants = (currentRoute.distance()/1000);
                //목적지까지의 거리를 m로 받아옴
                distants = Math.round(distants*100)/100.0;
                //Math.round() 함수는 소수점 첫째자리에서 반올림하여 정수로 남긴다
                //원래 수에 100곱하고 round 실행 후 다시 100으로 나눈다 -> 둘째자리까지 남김
                Toast.makeText(getApplicationContext(), String.format("예상 시간 : " + String.valueOf(time)+" 분 \n" +
                        "목적지 거리 : " +distants+ " km"), Toast.LENGTH_LONG).show();
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
        Log.e(TAG,"drawRoute 실행");
        // Convert LineString coordinates into LatLng[]
        LineString lineString = LineString.fromPolyline(route.geometry(), PRECISION_6);
        List<Point> coordinates = lineString.coordinates();
        LatLng[] points = new LatLng[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = new LatLng(
                    coordinates.get(i).latitude(),
                    coordinates.get(i).longitude());
            Log.e(TAG, "Error: " + points[i]);
        }
        // Draw Points on MapView
//        mapboxMap.clear();
//      mapboxMap.addPolyline(new PolylineOptions().add(points).color(Color.parseColor("#3bb2d0")).width(5));
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        Log.e(Tag, "onMapReady");
        this.mapboxMap = mapboxMap;

        mapboxMap.addOnMapClickListener(this);


        //↓ 초기 지도 스타일 지정
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
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
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

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

    public void map_search(View view) {
        Log.e(TAG,"map_search 실행");
        showDialog2(view);
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

    @Override
    //지도 클릭시 자동 길찾기
    public boolean onMapClick(@NonNull LatLng point) {
        if (destinationMarker != null) {
            mapboxMap.removeMarker(destinationMarker);
        }

        destinationMarker = mapboxMap.addMarker(new MarkerOptions().position(point));//마커 추가
        destinatonPosition = Point.fromLngLat(point.getLongitude(), point.getLatitude());//클릭한곳의 좌표
        originPosition = Point.fromLngLat(Lo, La);//현재 좌표

        getRoute_walking(originPosition, destinatonPosition);   //도보 길찾기
        getRoute_navi_walking(originPosition, destinatonPosition);//도보 네비게이션
        startButton.setEnabled(true);   //네비게이션 버튼 활성화


        return false;
    }
    private void getRoute_navi_walking (Point origin, Point destinaton) {
        NavigationRoute.builder(this).accessToken(Mapbox.getAccessToken())
                .profile(DirectionsCriteria.PROFILE_WALKING)//도보 길찾기
                .origin(origin)//출발지
                .destination(destinaton).//도착지
                build().
                getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if (response.body() == null) {
                            return;
                        } else if (response.body().routes().size() ==0) {
                            return;
                        }
                        currentRoute = response.body().routes().get(0);
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                    }
                });
    }

    private void getRoute_navi_CYCLING (Point origin, Point destinaton) {
        NavigationRoute.builder(this).accessToken(Mapbox.getAccessToken())
                .profile(DirectionsCriteria.PROFILE_CYCLING)//자전거 길찾기
                .origin(origin)//출발지
                .destination(destinaton).//도착지
                build().
                getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if (response.body() == null) {
                            return;
                        } else if (response.body().routes().size() ==0) {
                            return;
                        }

                        currentRoute = response.body().routes().get(0);
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                    }
                });
    }

    private void getRoute_navi_DRIVING (Point origin, Point destinaton) {
        NavigationRoute.builder(this).accessToken(Mapbox.getAccessToken())
                .profile(DirectionsCriteria.PROFILE_DRIVING)//자동차 길찾기
                .origin(origin)//출발지
                .destination(destinaton).//도착지
                build().
                getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if (response.body() == null) {
                            return;
                        } else if (response.body().routes().size() ==0) {
                            return;
                        }

                        currentRoute = response.body().routes().get(0);
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                    }
                });
    }


}