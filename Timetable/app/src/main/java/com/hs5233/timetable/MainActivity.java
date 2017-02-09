package com.hs5233.timetable;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initDatabase();
//        login();
//        logout();
    }
    @Override
    protected void onResume(){
        super.onResume();
        initWeeksChoose();
    }
    public void login(){
        SQLiteDatabase db = openOrCreateDatabase("Timetable.db",MODE_PRIVATE,null);
        ContentValues values = new ContentValues();
        values.put("username", "HS5233");
        values.put("type", "edu");
        db.insert("user", null, values);
        values.clear();
        Toast.makeText(getApplicationContext(),"登录成功",Toast.LENGTH_SHORT).show();
        db.close();
    }
    public void initDatabase(){
        //初始化数据库
        //课程表
        SQLiteDatabase db = openOrCreateDatabase("Timetable.db", MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS courses (id integer primary key autoincrement,coursename text not null,teacher text not null,room text not null,week integer not null,serial integer not null,startweek integer not null,endweek integer not null)");
        //用户数据表
        db.execSQL("CREATE TABLE IF NOT EXISTS config (id integer primary key autoincrement,function text not null,content text not null)");
        db.close();
    }
    public void getCourses(String today){
        SQLiteDatabase db = openOrCreateDatabase("Timetable.db",MODE_PRIVATE,null);
        //点击Spinner进行查询，匹配数字
        Pattern p = Pattern.compile("第(\\d*)周");
        Matcher m = p.matcher(today);
        if(m.find())today = m.group(1);
//        Toast.makeText(this,today,Toast.LENGTH_SHORT).show();
        Cursor data = db.rawQuery("SELECT content FROM config WHERE function='coursesImport'", null);
        if(data.moveToNext()){
//            Log.d("DATABASEDATA",data.getString(data.getColumnIndex("content")));
            if(data.getString(data.getColumnIndex("content")).equals("0")){
                if(eduLogin.cookieTag.equals("0")){
                    //获取课表
                    Intent _intent = new Intent(MainActivity.this,eduGetcourse.class);
                    startActivity(_intent);
                }
            }else{
                Cursor course = null;
                String courseStr = "";
                TextView courseText;
                String courseTmp = "";
                int view;

                List<String> courseArr = new ArrayList<String>();
                int colorLocation = 0;
                boolean colorCheck = false;
                String[] color = {"#FFCC99","#FFCCCC","#FFCC33","#FF99FF","#FF9999","#FF9933","#FFFF66","#CCFFFF","#99CCFF","#CCFF99","#CC99FF","#CCFFCC","#FF6633","#9999FF","#99FFCC","#99CCCC","#9999CC","#66FFCC","#6699FF","#66CCFF","#33FF99","#3399FF","#FFCC66","#FF9966","#FF6666","#CCCC99","#66FF66"};

                for(int week=0;week<7;week++) {
                    for (int serial = 0; serial<5; serial++) {
                        course = db.rawQuery("SELECT coursename,teacher,room FROM courses WHERE startweek<='"+today+"' AND endweek>='"+today+"' AND week='"+week+"' AND serial='"+serial+"'", null);
                        while (course.moveToNext()) {
//                            Toast.makeText(getApplicationContext(),course.getString(course.getColumnIndex("coursename")),Toast.LENGTH_SHORT).show();
                            courseStr += course.getString(course.getColumnIndex("coursename")) + "\n";
                            courseTmp = course.getString(course.getColumnIndex("coursename"));
                            courseStr += course.getString(course.getColumnIndex("teacher")) + "\n";
                            courseStr += course.getString(course.getColumnIndex("room")) + "\n";
                        }
                            //动态获取控件ID
                            view = this.getResources().getIdentifier("textView" + (24 + serial + week * 5), "id", this.getPackageName());
                            courseText = (TextView) findViewById(view);

                            //设置背景色
                            if(!courseTmp.equals("")){
                                for(int i=0;i<courseArr.size();i++){
                                    if(courseArr.get(i).equals(courseTmp)){
                                        courseText.setBackgroundColor(Color.parseColor(color[i]));
                                        colorCheck = true;
                                        break;
                                    }
                                }
                                if(!colorCheck){
                                    courseArr.add(courseTmp);
                                    courseText.setBackgroundColor(Color.parseColor(color[colorLocation]));
                                    ++colorLocation;
                                }
                                colorCheck = false;
                            }
                            courseText.setText(courseStr);
                            courseTmp = "";
                            courseStr = "";
                    }
//                Toast.makeText(getApplicationContext(),"2312321",Toast.LENGTH_SHORT).show();
                }
                course.close();
               /* Cursor course = db.rawQuery("SELECT coursename,teacher,room,startweek,endweek,week,serial FROM courses WHERE id>0", null);
                while (course.moveToNext()) {
                    Log.d("COURSEDATA",course.getString(course.getColumnIndex("coursename"))+"+"+course.getString(course.getColumnIndex("teacher"))+"+"+course.getString(course.getColumnIndex("room"))+"+"+course.getString(course.getColumnIndex("startweek"))+"+"+course.getString(course.getColumnIndex("endweek"))+"+"+course.getString(course.getColumnIndex("week"))+"+"+course.getString(course.getColumnIndex("serial")));
                }*/
            }
            data.close();
            db.close();
        }else{
            //第一次打开APP，先插入数据
            ContentValues values = new ContentValues();
            values.put("function", "coursesImport");
            values.put("content", "0");
            db.insert("config", null, values);
            data.close();
            db.close();
            getCourses("1");
        }
    }
    public void logout(){
        SQLiteDatabase db = openOrCreateDatabase("Timetable.db",MODE_PRIVATE,null);
        ContentValues values = new ContentValues();
        values.put("seq", "0");
        db.delete("user", "id>0", null);
        db.update("sqlite_sequence", values, "name='user'",null);
        values.clear();
        db.close();
    }
    public void initWeeksChoose(){
        //初始化周数选择的spinner
        List<String> weeks = new ArrayList<String>();
        for(int i=1;i<26;i++){
            weeks.add("第"+i+"周");
        }
        Spinner weeksSpinner = (Spinner)findViewById(R.id.spinner);
        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,weeks);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weeksSpinner.setAdapter(adapter);
        weeksSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getCourses(adapter.getItem(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void init(){
        Button changeTerm = (Button)findViewById(R.id.button2);
        changeTerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent _intent = new Intent(MainActivity.this,eduGetcourse.class);
                startActivity(_intent);
            }
        });
        Button other = (Button)findViewById(R.id.button4);
        other.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent _intent = new Intent(MainActivity.this,Other.class);
                startActivity(_intent);
            }
        });
    }
}
