package com.example.saubhagyam.thetalklist;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.saubhagyam.thetalklist.Services.LoginService;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


public class Login extends Activity {
    EditText email_address, pwd;
   public Button signIn, create_account, forgetPassword;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    public LoginButton loginButton;
    CallbackManager callbackManager;
    String email;
    String pass;
    Typeface typeface;
Dialog dialog;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.example.saubhagyam.thetalklist",
                    PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        setContentView(R.layout.activity_login);

       /* loginButton.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday", "user_friends"));*/
        loginButton.setReadPermissions("email");
        pref = getSharedPreferences("loginStatus", Context.MODE_PRIVATE);
        editor = pref.edit();


        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                Log.e("token", String.valueOf(loginResult.getAccessToken()));

                getUserDetails(loginResult);




            }

            @Override
            public void onCancel() {

                Toast.makeText(getApplicationContext(), "Login Cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext()  , "error "+error, Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
         typeface = Typeface.createFromAsset(getAssets(), "fonts/GothamBookRegular.ttf");

        loginButton = (LoginButton) findViewById(R.id.facebook_login_btn);
        callbackManager = CallbackManager.Factory.create();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        email_address = (EditText) findViewById(R.id.emaillogin);
        email_address.setTypeface(typeface);
        email_address.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        pwd = (EditText) findViewById(R.id.passwordLogin);
        pwd.setTypeface(typeface);
        signIn = (Button) findViewById(R.id.signinButton);
        signIn.setTypeface(typeface);
        create_account = (Button) findViewById(R.id.loginCreateAccount);
        create_account.setTypeface(typeface);
        forgetPassword = (Button) findViewById(R.id.loginforgetPassword);
        forgetPassword.setTypeface(typeface);
        loginButton.setTypeface(typeface);
        loginButton.setTextSize(16);

    }




    SplashScreen splashScreen;

    @Override
    protected void onResume() {
        super.onResume();

splashScreen=new SplashScreen();
        AppEventsLogger.activateApp(this);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                email = email_address.getText().toString();
                pass = pwd.getText().toString();
                if (email.equals("")) {
                    email_address.setError("Required");
                }
                if (pass.equals("")) {
                    pwd.setError("Required");
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    email_address.setError("Invalid Email Address..!");
                } else {


                    dialog=new Dialog(Login.this);
                    dialog.setContentView(R.layout.threedotprogressbar);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();


                    LoginService loginService = new LoginService();



                    String URL = "https://www.thetalklist.com/api/login?email=" + email + "&password=" + pass;


                    RequestQueue queue = Volley.newRequestQueue(getApplicationContext());


                    final StringRequest sr = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("response", response);
                            dialog.dismiss();

                            UserData data = UserData.getInstance();
                            data.setLoginServResponse(response);


                            try {

                                JSONObject jsonObject = new JSONObject(response);


                                Log.e("response", response);

                                int status = (int) jsonObject.get("status");
                                if (status == 1) {

                                    String Err = (String) jsonObject.get("error");
                                    Toast.makeText(Login.this, Err, Toast.LENGTH_SHORT).show();

                                } else {


                                    JSONObject resultObj = (JSONObject) jsonObject.get("result");
                                    int roleId = resultObj.getInt("roleId");
                                    String UserName = (String) resultObj.get("username");
                                    int userId = resultObj.getInt("id");
                                    String mail = resultObj.getString("email");

                                    editor.putString("LoginWay", "InternalLogin");
                                    editor.putString("loginResponse", response);
                                    editor.putString("user", UserName);
                                    editor.putString("pass", pass);
                                    editor.putInt("roleId", roleId);
                                    editor.putBoolean("logSta", true);
                                    editor.putString("usernm", resultObj.getString("usernm"));
                                    editor.putInt("userId", userId);
                                    editor.putString("credit_balance", resultObj.getString("credit_balance"));
                                    editor.putString("usernm", resultObj.getString("usernm"));
                                    editor.putInt("id", resultObj.getInt("id"));
                                    editor.putString("city", resultObj.getString("city"));
                                    editor.putString("nativeLanguage", resultObj.getString("nativeLanguage"));
                                    editor.putString("otherLanguage", resultObj.getString("otherLanguage"));
                                    editor.putInt("status", 0);
                                    editor.putInt("gender", resultObj.getInt("gender"));
                                    editor.putString("cell", resultObj.getString("cell"));
                                    editor.putFloat("hRate", Float.parseFloat(resultObj.getString("hRate")));
                                    if (resultObj.getString("avgRate").equals(""))
                                    editor.putFloat("avgRate", 0.0f);
                                    else
                                    editor.putFloat("avgRate", Float.parseFloat(resultObj.getString("avgRate")));


                                    if (resultObj.getString("ttl_points").equals(""))
                                    editor.putFloat("ttl_points", 0.0f);
                                    else
                                    editor.putFloat("ttl_points", Float.parseFloat(resultObj.getString("ttl_points")));


                                    editor.putInt("country", resultObj.getInt("country"));
                                    editor.putInt("province", resultObj.getInt("province"));
                                    editor.putFloat("money", 0.0f);
                                    editor.putString("email", mail);
                                    editor.apply();


                                    Intent i = new Intent(getApplicationContext(), SettingFlyout.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    i.putExtra("status", 0);
                                    i.putExtra("roleId", roleId);


                                    i.putExtra("username", UserName);
                                    startActivity(i);
                                }
                                } catch(JSONException e){
                                    e.printStackTrace();
                                }


                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            dialog.dismiss();

                            if (error.getClass().equals(TimeoutError.class)) {
                                // Show timeout error message
                                Toast.makeText(getApplicationContext(),
                                        "Oops. Timeout error!",
                                        Toast.LENGTH_LONG).show();
                            }
                            if (error.getClass().equals(ServerError.class)) {
                                // Show timeout error message
                                Toast.makeText(getApplicationContext(),
                                        "We are sorry for our Absence..! Wait for a While... We are setting up for you",
                                        Toast.LENGTH_LONG).show();
                            }


                            Log.d("error", error.toString());
                            Toast.makeText(Login.this, error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    sr.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    queue.add(sr);
                }
            }
        });


        create_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getApplicationContext(), SignUp.class);
                startActivity(i);

            }
        });



        forgetPassword.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getApplicationContext(), ForgetPassword.class);
                startActivity(i);

            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
        moveTaskToBack(false);
    }

    protected void getUserDetails(final LoginResult loginResult) {
        Log.e("token", String.valueOf(AccessToken.getCurrentAccessToken()));
        Log.e("login result", String.valueOf(loginResult));
        Toast.makeText(Login.this, "token "+loginResult.getAccessToken(), Toast.LENGTH_SHORT).show();

        if (loginResult.getAccessToken()!=null){
            GraphRequest request = GraphRequest.newMeRequest(
                    /*loginResult.getAccessToken()*/AccessToken.getCurrentAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            Log.e("LoginActivity", response.toString());

                            // Application code
                            try {

//                                    final String email = object.getString("email");
//                                    final String birthday = object.getString("birthday");
                                final int id = object.getInt("id");

                                final String name = object.getString("name");
                                final String gender = object.getString("gender");
                                final String first_name = object.getString("first_name");
                                final String last_name = object.getString("last_name");


                                Log.e("facebook id", String.valueOf(id));
                                Log.e("facebook name",name);
                                Log.e("facebook fn",first_name);
                                Log.e("facebook ln",last_name);


                                Log.e("in graph request","in graph request yeeeeeeeeeeeeeeeeeeeeee");
                                Profile profile=Profile.getCurrentProfile();
                                profile.getProfilePictureUri(200,200);

                                final String url="https://www.thetalklist.com/api/fblogin?email="+email+"&facebook_id="+loginResult.getAccessToken().getUserId()+"&firstname="+first_name+"&lastname="+last_name+"&gender="+gender+"&birthdate="+"";
//                                    final String url="https://www.thetalklist.com/api/fblogin?facebook_id="+loginResult.getAccessToken().getUserId()+"&firstname="+first_name+"&lastname="+last_name;
//                                    final String url="http://52.8.60.79/api/fblogin?facebook_id="+id+"&firstname="+first_name+"&lastname="+last_name;
                                SharedPreferences pref = getSharedPreferences("loginStatus", Context.MODE_PRIVATE);
                                final SharedPreferences.Editor editor = pref.edit();

                                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Log.e("facebook login url", url);
                                        Log.e("facebook login response", response);

                                        try {
                                            JSONObject obj=new JSONObject(response);
                                            if (obj.getInt("status")==0) {
                                                JSONObject resObj=obj.getJSONObject("result");

                                                final int roleId = resObj.getInt("roleId");
                                                editor.putString("LoginWay", "FacebookLogin");
                                                editor.putString("loginResponse", response);
                                                editor.putString("firstname", first_name);
                                                editor.putString("lastname", last_name);
//                                                    editor.putString("email", email);
                                                editor.putString("facebook_id", loginResult.getAccessToken().getUserId());
                                                editor.putInt("id", resObj.getInt("id"));
                                                editor.putInt("roleId",roleId);
                                                editor.putInt("gender", resObj.getInt("gender"));
                                                editor.putInt("country", resObj.getInt("country"));
                                                editor.putInt("province", resObj.getInt("province"));
                                                editor.putString("cell", resObj.getString("cell"));
                                                if (obj.getInt("login")==1)
                                                    editor.putInt("status",0);
                                                else editor.putInt("status",1);
                                                editor.putString("city", resObj.getString("city"));
                                                editor.putString("nativeLanguage", resObj.getString("nativeLanguage"));
                                                editor.putString("otherLanguage", resObj.getString("otherLanguage"));
                                                editor.putFloat("hRate", Float.parseFloat(resObj.getString("hRate")));
                                                if (resObj.getString("avgRate").equals(""))
                                                    editor.putFloat("avgRate", 0.0f);
                                                else
                                                    editor.putFloat("avgRate", Float.parseFloat(resObj.getString("avgRate")));
                                                if (resObj.getString("ttl_points").equals(""))
                                                    editor.putFloat("ttl_points", 0.0f);
                                                else
                                                    editor.putFloat("ttl_points", Float.parseFloat(resObj.getString("ttl_points")));

                                                if (gender.equals("male"))
                                                    editor.putInt("gender", 1);
                                                else editor.putInt("gender", 0);
//                                                    editor.putString("birthdate", birthday);
                                                editor.apply();

//                                                    Toast.makeText(Login.this, "Login Sucessfully..!", Toast.LENGTH_SHORT).show();
                                                SettingFlyout settingFlyout = new SettingFlyout();
                                                Intent i = new Intent(getApplicationContext(), settingFlyout.getClass());
                                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(i);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }




                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError volleyError) {
                                        Toast.makeText(Login.this, "Something wemt wrong..!", Toast.LENGTH_SHORT).show();
                                        editor.clear().apply();
                                    }
                                }) ;

                                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                                queue.add(stringRequest);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }


            );
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,gender,birthday,first_name,last_name");
            request.setParameters(parameters);
            request.executeAsync();
        }
        else {
            Toast.makeText(getApplicationContext(), "recall callbacks", Toast.LENGTH_SHORT).show();
            loginButton.performClick();
        }


       /* Bundle permission_param = new Bundle();
        permission_param.putString("fields", "id,name,email,picture.width(120).height(120)");
        data_request.setParameters(permission_param);
        data_request.executeAsync();*/

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }
}