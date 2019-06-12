package com.example.ar;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


//로그인
public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    //언어 설정
    private Button btn_en, btn_ko;
    private Locale myLocale;

    MainActivity mainActivity;

    //login
    static EditText UseridEt, PasswordEt;
    static String loginId, loginPwd, loginName, loginTeg;
    static int facebook_login;

    // FB add
    private LoginButton loginButton;
    private CircleImageView circleImageView;
    private CallbackManager callbackManager;
    String first_name, last_name, email, id, image_url;


    // mapbox
    private TextView btnShowLocation;
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;

    static int nonMember = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 숨기기
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        //상태바 숨기기
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);
        //언어 정보 불러오기
        loadLocale();


        this.btn_en = (Button) findViewById(R.id.eng);
        this.btn_ko = (Button) findViewById(R.id.korea);
        this.btn_en.setOnClickListener(this);
        this.btn_ko.setOnClickListener(this);

        //loginSession
        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);

        loginId = auto.getString("inputId", null);
        loginPwd = auto.getString("inputPwd", null);
        loginName = auto.getString("inputName", null);
        loginTeg = auto.getString("inputTeg", null);

        nonMember = 0;
        if(facebook_login == 0){
            mainActivity.facebook_login = 0;
        }

        if (loginId != null && loginPwd != null ) {
            if(loginTeg != null){
                mainActivity.facebook_login = 1;
            }
            if(loginTeg == null){
                mainActivity.facebook_login = 0;
            }

            Toast.makeText(LoginActivity.this, loginId + "님 자동로그인 입니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        //.loginSession

        //login
        UseridEt = (EditText)findViewById(R.id.etUserName);
        PasswordEt = (EditText)findViewById(R.id.etPassword);
        //.login

        // FB add
        printKeyHash();

        loginButton = findViewById(R.id.login_button);
       // txtName = findViewById(R.id.profile_name); // nav_header_view.findViewById(R.id.txtName);
        //txtEmail = findViewById(R.id.profile_email);
       // circleImageView = findViewById(R.id.profile_pic);

        callbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions(Arrays.asList("email", "public_profile"));
        checkLoginStatus();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mainActivity.facebook_login = 1;
            }
            @Override
            public void onCancel() {
            }
            @Override
            public void onError(FacebookException error) {
            }
        });

        // 비회원 로그인
        btnShowLocation = (TextView) findViewById(R.id.Naver_button);
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                nonMember = 1;
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        // 비회원 로그인
    }

    // facebook
    private void printKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.example.ar", PackageManager.GET_SIGNATURES);
            for(Signature signature:info.signatures){
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash ", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    AccessTokenTracker tokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken)
        {
            if (currentAccessToken == null) {
                //txtname.setText("");
                //txtemail.setText("");
                //circleImageView.setImageResource(0);
                //Toast.makeText(LoginActivity.this, "User Logged out", Toast.LENGTH_LONG).show();
            }
            else
            {
                loaduserProfile(currentAccessToken);
            }
        }
    };


    private void loaduserProfile(AccessToken newAccessToken) {
        BackgroundWorker backgroundWorker1 = new BackgroundWorker(this);
        GraphRequest request = GraphRequest.newMeRequest(newAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    first_name = object.getString("first_name");
                    last_name = object.getString("last_name");
                    email = object.getString("email");
                    id = object.getString("id");
                    image_url = "https://graph.facebook.com/"+id+"/picture?type=normal";

                    String type = "facebookLogin";

                    backgroundWorker1.execute(type, id, email, first_name);
                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions.dontAnimate();

//                    Glide.with(LoginActivity.this).load(image_url).into(circleImageView);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name, last_name, email, id");
        request.setParameters(parameters);
        request.executeAsync();

    }

    private void checkLoginStatus()
    {
        if (AccessToken.getCurrentAccessToken() != null)
        {
            loaduserProfile(AccessToken.getCurrentAccessToken());
        }

    }
    // .facebook

    public void Login_onClick(View v){
        String username = UseridEt.getText().toString();
        String password = PasswordEt.getText().toString();
        String type = "login";
        BackgroundWorker backgroundWorker = new BackgroundWorker(this);
        backgroundWorker.execute(type, username, password);
    }

    //회원가입 액티비티로
    public void Signup_onClick(View v){
        Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
        startActivity(intent);
    }

    //버튼 클릭으로 언어 설정 스위치 케이스
    @Override
    public void onClick(View v) {
        String lang = "ko";
        switch (v.getId()) {
            case R.id.eng:
                lang = "en";
                break;
            case R.id.korea:
                lang = "ko";
                break;
            default:
                break;
        }
        changeLang(lang);
        Intent intent = getIntent();
        finish();//한번 닫고 다시 실행
        startActivity(intent);

    }
    public void loadLocale()
    {
        Log.e("A","loadLocale 실행");
        String langPref = "Language";
        SharedPreferences prefs = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        String language = prefs.getString(langPref, "");
        changeLang(language);
    }
    public void saveLocale(String lang)
    {
        Log.e("A","saveLocale 실행");
        String langPref = "Language";
        SharedPreferences prefs = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(langPref, lang);
        editor.commit();
    }
    public void changeLang(String lang)
    {
        if (lang.equalsIgnoreCase(""))
            return;
        Log.e("A","changeLang 실행");
        myLocale = new Locale(lang);
        saveLocale(lang);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
      //  updateTexts();
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (myLocale != null){
            newConfig.locale = myLocale;
            Locale.setDefault(myLocale);
            getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
        }
    }
}