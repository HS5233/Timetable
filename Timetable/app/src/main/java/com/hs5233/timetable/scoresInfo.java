package com.hs5233.timetable;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class scoresInfo extends AppCompatActivity {

    private String year;
    private String term;
    private String score_VIEWSTATE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores_info);
        init();
        initYearChoose();
        initTermChoose();
    }
    private void init(){
        if(eduLogin.cookieTag.equals("0")){
            Intent _intent = new Intent(scoresInfo.this,eduLogin.class);
            startActivity(_intent);
        }
        Button back = (Button)findViewById(R.id.button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Button scoreSelect = (Button)findViewById(R.id.button6);
        scoreSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestParams params = new RequestParams();
                //这一步一定要加上referer，不然会进入跳转死循环
                params.addHeader("Referer", "http://jwc.xhu.edu.cn");
                HttpUtils http = new HttpUtils();
                http.configCookieStore(eduLogin.cookie);
                http.configResponseTextCharset("gb2312");
                http.send(HttpRequest.HttpMethod.GET,
                        "http://jwc.xhu.edu.cn/xscjcx.aspx?xh=" + eduLogin.eduUsername,
                        params,
                        new RequestCallBack<String>() {
                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                //保存本学期课表
                                Pattern p = Pattern.compile("<input type=\"hidden\" name=\"__VIEWSTATE\" value=\"([^\"]*)");
                                Matcher m = p.matcher(responseInfo.result);
                                if (m.find()) {
                                    score_VIEWSTATE = m.group(1);
//                                    Log.d("***************",m.group(1));
                                    getScore();
                                } else {
                                    Toast.makeText(getApplicationContext(), "登录失败，请确认账号密码是否正确", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(HttpException error, String msg) {
                                Toast.makeText(getApplicationContext(), "获取信息失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
    private void getScore(){
        RequestParams params = new RequestParams();
        //这一步一定要加上referer，不然会进入跳转死循环
        params.addHeader("Referer", "http://jwc.xhu.edu.cn");
        params.addBodyParameter("__EVENTTARGET", "");
        params.addBodyParameter("__EVENTARGUMENT", "");
        params.addBodyParameter("__VIEWSTATE", score_VIEWSTATE);
        params.addBodyParameter("hidLanguage", "");
        params.addBodyParameter("ddlXN", year);
        params.addBodyParameter("ddlXQ",term);
        params.addBodyParameter("ddl_kcxz","");
        try {
            String tmp = "学期成绩";
            String tmp_utf8 = new String(tmp.getBytes("UTF-8"));
            String tmp_unicode = new String(tmp_utf8.getBytes(), "UTF-8");
            String tmp_gbk = new String(tmp_unicode.getBytes("GBK"));
            params.addBodyParameter("btn_xq",tmp_gbk);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpUtils http = new HttpUtils();
        http.configCookieStore(eduLogin.cookie);
        http.configResponseTextCharset("gb2312");
        http.send(HttpRequest.HttpMethod.POST,
                "http://jwc.xhu.edu.cn/xscjcx.aspx?xh=" + eduLogin.eduUsername,
                params,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        System.out.println(responseInfo.result);
                        dispalyScore(responseInfo.result);
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        Toast.makeText(getApplicationContext(), "获取信息失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void dispalyScore(String content){
        content = content.replaceAll("&nbsp;","");
        Pattern p,p1;
        Matcher m,m1;
        p = Pattern.compile("<td>[^<]*?</td><td>[^<]*?</td><td>[^<]*?</td><td>[^<]*?</td><td>[^<]*?</td><td>[^<]*?</td><td>[^<]*?</td><td>[^<]*?</td><td>[^<]*?</td><td>[^<]*?</td><td>[^<]*?</td><td>[^<]*?</td><td>[^<]*?</td><td>[^<]*?</td><td>[^<]*?</td>");
        m = p.matcher(content);
        //去掉第一行
        m.find();
        List<Map<String,String>> scoreList = new ArrayList<Map<String,String>>();
        while (m.find()){
//            System.out.println(m.group());
            p1 = Pattern.compile("<td>([^<]*?)</td>");
            m1 = p1.matcher(m.group(0));
            Map<String,String> scoreMap = new HashMap<String,String>();
            for(int i=0;i<15;i++){
                m1.find();
                if(i==3)scoreMap.put("name",m1.group(1));
                if(i==6)scoreMap.put("credits",m1.group(1));
                if(i==8)scoreMap.put("score",m1.group(1));
                if(i==14)scoreMap.put("tag",m1.group(1).isEmpty()?"　　":"重修");
                Log.d("********************"+i,m1.group(1));
            }
            scoreList.add(scoreMap);
        }
//        Toast.makeText(this,"21321321",Toast.LENGTH_SHORT).show();
        ListView scoreListView = (ListView)findViewById(R.id.listView2);
        SimpleAdapter adapter = new SimpleAdapter(this,scoreList,R.layout.scorelist,new String[]{"name","credits","score","tag"},new int[]{R.id.textView71,R.id.textView72,R.id.textView73,R.id.textView74});
        scoreListView.setAdapter(adapter);
    }
    public void initYearChoose(){
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy");
        String date = sDateFormat.format(new java.util.Date());
        //初始化学年选择的spinner
        List<String> years = new ArrayList<String>();
        int year_tmp = Integer.parseInt(date) - 5;
        for(int i=0;i<11;i++,year_tmp++){
            years.add(year_tmp+"-"+(year_tmp+1));
        }
        final Spinner yearsSpinner = (Spinner)findViewById(R.id.spinner);
        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,years);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearsSpinner.setAdapter(adapter);
        yearsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                year = adapter.getItem(position).toString();
//                Log.d("SPINNERDATA",adapter.getItem(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    public void initTermChoose(){
        //初始化学期选择的spinner
        List<String> terms = new ArrayList<String>();
        terms.add("1");
        terms.add("2");
        terms.add("3");
        Spinner termsSpinner = (Spinner)findViewById(R.id.spinner1);
        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,terms);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        termsSpinner.setAdapter(adapter);
        termsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                term = adapter.getItem(position).toString();
//                Log.d("SPINNERDATA",adapter.getItem(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
