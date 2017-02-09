package com.hs5233.timetable;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class eduLogin extends AppCompatActivity {

    static public CookieStore cookie;
    static public String cookieTag = "0";
    static public String eduUsername;
    private ImageView imageView;
    private String login_VIEWSTATE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edu_login);
        init();
    }
    private void init(){
        getCookie();
        imageView = (ImageView)findViewById(R.id.imageView);
        final EditText username = (EditText)findViewById(R.id.editText);
        final EditText password = (EditText)findViewById(R.id.editText2);
        final EditText checkWord = (EditText)findViewById(R.id.editText3);
        Button login = (Button)findViewById(R.id.button);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImage();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog waiting = new ProgressDialog(eduLogin.this);
                // 设置进度条风格，风格为圆形，旋转的
                waiting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                // 设置ProgressDialog 标题
                waiting.setTitle("登录");
                // 设置ProgressDialog 提示信息
                waiting.setMessage("登录教务系统中，请稍后……");
                // 设置ProgressDialog 的进度条是否不明确
                waiting.setIndeterminate(false);
                // 设置ProgressDialog 是否可以按退回按键取消
                waiting.setCancelable(false);

                eduUsername = username.getText().toString();
                HttpUtils http = new HttpUtils();
                RequestParams params = new RequestParams();

                params.addBodyParameter("__VIEWSTATE", login_VIEWSTATE);
                params.addBodyParameter("txtUserName", username.getText().toString());
                params.addBodyParameter("TextBox2", password.getText().toString());
                params.addBodyParameter("txtSecretCode", checkWord.getText().toString());
                try {
                    String tmp = "学生";
                    String tmp_utf8 = new String(tmp.getBytes("UTF-8"));
                    String tmp_unicode = new String(tmp_utf8.getBytes(), "UTF-8");
                    String tmp_gbk = new String(tmp_unicode.getBytes("GBK"));
                    params.addBodyParameter("RadioButtonList1", tmp_gbk);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                params.addBodyParameter("Button1", "");
                params.addBodyParameter("lbLanguage", "");
                params.addBodyParameter("hidPdrs", "");
                params.addBodyParameter("hidsc", "");

                params.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
                params.addHeader("Referer", "http://jwc.xhu.edu.cn/default2.aspx");

                http.configCookieStore(cookie);
                http.send(HttpRequest.HttpMethod.POST,
                        "http://jwc.xhu.edu.cn/default2.aspx",
                        params,
                        new RequestCallBack<String>() {
                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                waiting.cancel();
//                                Log.d("EDURESULT", responseInfo.result);
                                getContent();
                            }

                            @Override
                            public void onStart() {
                                // 让ProgressDialog显示
                                waiting.show();
                            }

                            @Override
                            public void onFailure(HttpException error, String msg) {
                                waiting.cancel();
                                Toast.makeText(getApplicationContext(), "登录失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                                getImage();
//                                Log.d("EDURESULT", msg);
                            }
                        });
            }
        });
    }

    private void getCookie(){
        final HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.GET,
                "http://jwc.xhu.edu.cn/default2.aspx",
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        DefaultHttpClient httpHeader = (DefaultHttpClient) http.getHttpClient();
                        cookie = httpHeader.getCookieStore();
//                        Log.d("EDUHEADER",responseInfo.result);
//                        Log.d("EDUHEADER",cookie.toString());
//                        System.out.println(responseInfo.result);
                        Pattern p = Pattern.compile("<input type=\"hidden\" name=\"__VIEWSTATE\" value=\"([^\"]*)");
                        Matcher m = p.matcher(responseInfo.result);
//                        System.out.println(m);
                        if (m.find()) {
//                            System.out.println(m.group(1) + "--ZHENGZEBIAODASHI--");
                            login_VIEWSTATE = m.group(1);
                            getImage();
                        } else {
                            Toast.makeText(getApplicationContext(), "获取参数失败，请稍后再试！", Toast.LENGTH_SHORT).show();
                            getImage();
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
//                        Log.d("EDUHEADER", msg);
                        Toast.makeText(getApplicationContext(), "访问失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                        getImage();
                    }
                });
    }

    //获取验证码
    private void getImage(){
        final BitmapUtils bitmapUtils = new BitmapUtils(this);
        HttpUtils http = new HttpUtils();
        http.configCookieStore(cookie);
        http.download("http://jwc.xhu.edu.cn/CheckCode.aspx",
                "/sdcard/HStimetable/image.img",
                false,
                true,
                new RequestCallBack<File>() {
                    @Override
                    public void onSuccess(ResponseInfo<File> responseInfo) {
                        bitmapUtils.display(imageView, "/sdcard/HStimetable/image.img");
                        bitmapUtils.clearCache();
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
//                        Log.d("EDUHEADER", msg);
                        Toast.makeText(getApplicationContext(), "获取验证码失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //验证是否登陆成功，并获取隐藏参数
    private void getContent(){
        HttpUtils http = new HttpUtils();
        http.configCookieStore(cookie);
        http.send(HttpRequest.HttpMethod.GET,
                "http://jwc.xhu.edu.cn/xs_main.aspx?xh=" + eduUsername,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
//                        Log.d("EDURESULT",responseInfo.result);
//                        System.out.println(responseInfo.result);
                        Pattern p = Pattern.compile("<span id=\"xhxm\">([^<]*)");
                        Matcher m = p.matcher(responseInfo.result);
                        if (m.find()) {
//                            Log.d("XUESHENGXINGMING",m.group(1));
                            cookieTag = "1";
                            Toast.makeText(getApplicationContext(), "登录成功", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "登录失败，请确认账号密码是否正确", Toast.LENGTH_SHORT).show();
                            getImage();
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
//                        Log.d("EDUHEADER", msg);
                        Toast.makeText(getApplicationContext(), "获取信息失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                        getImage();
                    }
                });
    }
}