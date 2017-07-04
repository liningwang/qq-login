package com.example.administrator.loginqq;

import android.graphics.Bitmap;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    public Tencent mTencent;
    UserInfo mInfo;
    String openId;
    String result="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void loginQQ(View view) {
        getUserInfo();
    }
    private void getUserInfo(){
        mTencent = Tencent.createInstance("222222", this);
        mTencent.login(this, "all", loginListener);
    }
    IUiListener loginListener = new BaseUiListener() {
        @Override
        protected void doComplete(JSONObject values) {
            Log.d("SDKQQAgentPref", "AuthorSwitch_SDK:" + SystemClock.elapsedRealtime());
            initOpenidAndToken(values);
            isRegister();
        }
    };

    private void isRegister(){
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    URL url = new URL("http://192.168.0.2:8080/LoginQQ/servlet/LoginTence?"
                            +"openId="+openId+"&method=login");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    InputStream inputStream = con.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String content="";
                    result="";
                    while ((content = bufferedReader.readLine()) != null) {
                        result += content;
                    }
                    JSONObject object = new JSONObject(result);
                    int flag = object.getInt("flag");
                    //等于1表示登录成功，0 没有注册
                    if(flag == 1) {
                        final String name = object.getString("name");
                        String address = object.getString("address");
                        String url1 = object.getString("url");
                        String openid = object.getString("openid");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"登录成功了，欢迎"+name,Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                    } else {
                        updateUserInfo();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    public void initOpenidAndToken(JSONObject jsonObject) {
        try {
            String token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
            String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
            openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires)
                    && !TextUtils.isEmpty(openId)) {
                mTencent.setAccessToken(token, expires);
                mTencent.setOpenId(openId);
            }
        } catch(Exception e) {
        }
    }

    private void updateUserInfo(){
        IUiListener listener = new IUiListener() {

            @Override
            public void onError(UiError e) {

            }

            @Override
            public void onComplete(final Object response) {
                JSONObject object = (JSONObject) response;
                try {
                    final String nickname = object.getString("nickname");
                    final String gender = object.getString("gender");
                    final String city = object.getString("city");
                    final String photo = object.getString("figureurl_qq_2");

                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                URL url = new URL("http://192.168.0.2:8080/LoginQQ/servlet/LoginTence?"
                                +"nickname="+nickname+"&gender="+gender+"&city="+city
                                        +"&figureurl_qq_2="+photo+"&openId="+openId+"&method=register");
                                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                                InputStream inputStream = con.getInputStream();
                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                                String content="";
                                result="";
                                while ((content = bufferedReader.readLine()) != null) {
                                    result += content;
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this,result+" 同时欢迎 " + nickname + " 进入",Toast.LENGTH_LONG)
                                                .show();
                                    }
                                });

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("wang",object.toString());
            }

            @Override
            public void onCancel() {

            }
        };
        mInfo = new UserInfo(this, mTencent.getQQToken());
        mInfo.getUserInfo(listener);
    }
    private class BaseUiListener implements IUiListener {

        @Override
        public void onComplete(Object response) {

            JSONObject jsonResponse = (JSONObject) response;

            doComplete((JSONObject)response);
        }

        protected void doComplete(JSONObject values) {
        }

        @Override
        public void onError(UiError e) {

        }

        @Override
        public void onCancel() {

        }
    }

}
