package com.hs5233.timetable;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class examInfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_info);
        init();
    }
    @Override
    protected void onResume(){
        super.onResume();
        getExam();
    }
    private void init(){
        if(eduLogin.cookieTag.equals("0")){
            Intent _intent = new Intent(examInfo.this,eduLogin.class);
            startActivity(_intent);
        }
        Button back = (Button)findViewById(R.id.button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void getExam(){
        RequestParams params = new RequestParams();
        //这一步一定要加上referer，不然会进入跳转死循环
        params.addHeader("Referer", "http://jwc.xhu.edu.cn");
        HttpUtils http = new HttpUtils();
        http.configCookieStore(eduLogin.cookie);
        http.configResponseTextCharset("gb2312");
        http.send(HttpRequest.HttpMethod.GET,
                "http://jwc.xhu.edu.cn/xskscx.aspx?xh=" + eduLogin.eduUsername,
                params,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
//                        System.out.println(responseInfo.result);
                        getContent(responseInfo.result);
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        Toast.makeText(getApplicationContext(), "获取信息失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void getContent(String content){
        content = content.replaceAll("&nbsp;","");
        Pattern p,p1;
        Matcher m,m1;
        p = Pattern.compile("<td>[^<]*?</td><td>[^<]*?</td><td>[^<]*?</td><td>[^<]*?</td><td>[^<]*?</td><td>[^<]*?</td><td>[^<]*?</td><td>[^<]*?</td>");
        m = p.matcher(content);
        //去掉第一行
        m.find();
        List<Map<String,String>> examList = new ArrayList<Map<String,String>>();
        while (m.find()){
//            System.out.println(m.group());
            p1 = Pattern.compile("<td>([^<]*?)</td>");
            m1 = p1.matcher(m.group(0));
            Map<String,String> examMap = new HashMap<String,String>();
            for(int i=0;i<8;i++){
                m1.find();
                if(i==0||i==5||i==7)continue;
                if(i==1)examMap.put("name",m1.group(1));
                if(i==2)examMap.put("student",m1.group(1));
                if(i==3)examMap.put("time",m1.group(1).isEmpty()?"暂无":m1.group(1));
                if(i==4)examMap.put("location",m1.group(1).isEmpty()?"暂无":m1.group(1));
                if(i==6)examMap.put("seat",m1.group(1).isEmpty()?"暂无":m1.group(1));
//                Log.d("********************",m1.group(1));
            }
            examList.add(examMap);
        }
        ListView examListView = (ListView)findViewById(R.id.listView);
        SimpleAdapter adapter = new SimpleAdapter(this,examList,R.layout.examlist,new String[]{"name","student","time","location","seat"},new int[]{R.id.textView62,R.id.textView64,R.id.textView66,R.id.textView68,R.id.textView70});
//        setListAdapter(adapter);
        examListView.setAdapter(adapter);
//        System.out.println(exam+"********************************");
    }
}
