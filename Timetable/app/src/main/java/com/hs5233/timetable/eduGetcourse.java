package com.hs5233.timetable;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class eduGetcourse extends AppCompatActivity {

    private String year;
    private String term;
    private String course_VIEWSTATE;
    private String nowCourse;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edu_getcourse);
        init();
        initYearChoose();
        initTermChoose();
    }
    private void init(){
        if(eduLogin.cookieTag.equals("0")){
            Intent _intent = new Intent(eduGetcourse.this,eduLogin.class);
            startActivity(_intent);
        }
        Button cancel = (Button)findViewById(R.id.button7);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button importCourse = (Button)findViewById(R.id.button6);
        importCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestParams params = new RequestParams();
                //这一步一定要加上referer，不然会进入跳转死循环
                params.addHeader("Referer", "http://jwc.xhu.edu.cn");
                HttpUtils http = new HttpUtils();
                http.configCookieStore(eduLogin.cookie);
                http.configResponseTextCharset("gb2312");
                http.send(HttpRequest.HttpMethod.GET,
                        "http://jwc.xhu.edu.cn/xskbcx.aspx?xh=" + eduLogin.eduUsername,
                        params,
                        new RequestCallBack<String>() {
                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                //保存本学期课表
                                nowCourse = responseInfo.result;

                                Pattern p = Pattern.compile("<input type=\"hidden\" name=\"__VIEWSTATE\" value=\"([^\"]*)");
                                Matcher m = p.matcher(responseInfo.result);
                                if (m.find()) {
                                    course_VIEWSTATE = m.group(1);
                                    getCourse();
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

    private void getCourse(){
        RequestParams params = new RequestParams();
        //这一步一定要加上referer，不然会进入跳转死循环
        params.addHeader("Referer", "http://jwc.xhu.edu.cn");
        params.addBodyParameter("__EVENTTARGET", "xnd");
        params.addBodyParameter("__EVENTARGUMENT", "");
        params.addBodyParameter("__VIEWSTATE", course_VIEWSTATE);
        params.addBodyParameter("xnd",year);
        params.addBodyParameter("xqd", term);
        HttpUtils http = new HttpUtils();
        http.configCookieStore(eduLogin.cookie);
        http.configResponseTextCharset("gb2312");
        http.send(HttpRequest.HttpMethod.POST,
                "http://jwc.xhu.edu.cn/xskbcx.aspx?xh=" + eduLogin.eduUsername,
                params,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        getContent(responseInfo.result);
//                        System.out.println(responseInfo.result);
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        Toast.makeText(getApplicationContext(), "获取信息失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getContent(String content){
        Pattern pCheck,p,p1,p2;
        Matcher mCheck,m,m1,m2;
        pCheck = Pattern.compile("<td>第16节</td>");
        mCheck = pCheck.matcher(content);
        if(mCheck.find())content = nowCourse;
        SQLiteDatabase db = openOrCreateDatabase("Timetable.db",MODE_PRIVATE,null);
        //清空数据表
        ContentValues values = new ContentValues();
        db.delete("courses", "id>0", null);
        values.put("seq", "0");
        db.update("sqlite_sequence", values, "name='courses'", null);
        values.clear();
        //标记为已导入课程表
        values.put("content", "1");
        db.update("config", values, "function='coursesImport'", null);
        values.clear();
        //去除无用信息
        content = content.replaceAll("\\d{10}\\|","");
        content = content.replaceAll("&nbsp;","");
        content = content.replaceAll("<td rowspan=\"\\d*\" width=\"1%\">.*?</td>","");
        content = content.replaceAll("<td width=\"1%\">第\\d*节</td>","");
        content = content.replaceAll("<td>第\\d*节</td>","");
        content = content.replaceAll("</tr>","<tr>");
        //字符串切割为数组，方便获取数据
        String[] contentArrTmp = content.split("<tr>");
        List<String> contentList = new ArrayList<String>();
        int week;
        //去除多余数组
        for(int i=0;i<24;i++) {
            if(i>4&&i%2!=0){
                contentList.add(contentArrTmp[i]);
            }
        }
        //去除多余数组
        for(int i=contentList.size();i>0;i--){
//            System.out.println(contentList.get(i) + "--ZHENGZEBIAODASHI-TmpCoursename-");
            if(i%2!=0)contentList.remove(i);
        }
        finish();
        //捕获有用数据
        for(int serial=0;serial<contentList.size();serial++){
            //获取第N节课完整数据，循环大于5次，依次是第1、3、5、7、9
            p = Pattern.compile("<td.*?>(.*?)</td>");
            m = p.matcher(contentList.get(serial));
            //数据分节，获取数据重复课程
            week = 0;
            while (m.find()) {
//                System.out.println(m.group(1) + "--ZHENGZEBIAODASHI--");
                p1 = Pattern.compile("[^><]*<br>[^<>\\{]*\\{[^\\}]*\\}<br>[^><]*<br>[^<]*");
                m1 = p1.matcher(m.group(1));
                while(m1.find()){
//                    System.out.println(m1.group(0) + "--ZHENGZEBIAODASHI--Tmp--");
                    String[] courseArrTmp = m1.group(0).split("<br>");
                    values.put("coursename", courseArrTmp[0]);

                    //获取开课周数
                    p2 = Pattern.compile("第(\\d*)\\-(\\d*)周");
                    m2 = p2.matcher(courseArrTmp[1]);
                    if(m2.find()){
//                        System.out.println(m2.group(1) + "--ZHENGZEBIAODASHI-TmpWeekEnd-");
//                        System.out.println(m2.group(2) + "--ZHENGZEBIAODASHI-TmpWeekStart-");
                        values.put("startweek", m2.group(1));
                        values.put("endweek", m2.group(2));
                    }
//                    System.out.println(courseArrTmp[0] + "--ZHENGZEBIAODASHI-TmpCoursename-");
//                    System.out.println(courseArrTmp[1] + "--ZHENGZEBIAODASHI-TmpWeek-");
//                    System.out.println(courseArrTmp[2] + "--ZHENGZEBIAODASHI-TmpTeacher-");
//                    System.out.println(courseArrTmp[3] + "--ZHENGZEBIAODASHI-TmpRoom-");
                    values.put("week", week);
                    values.put("serial", serial);
                    values.put("teacher", courseArrTmp[2]);
                    values.put("room", courseArrTmp[3]);
                    db.insert("courses", null, values);
                    values.clear();
                }
                ++week;
            }
        }
        Toast.makeText(getApplicationContext(),"导入课表成功！",Toast.LENGTH_SHORT).show();
        db.close();
        finish();
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
        final Spinner yearsSpinner = (Spinner)findViewById(R.id.spinner2);
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
        Spinner termsSpinner = (Spinner)findViewById(R.id.spinner3);
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
