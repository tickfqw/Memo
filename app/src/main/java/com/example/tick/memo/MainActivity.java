package com.example.tick.memo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends Activity {
    private ListView lv;
    private SqliteDBConnect sd;
    private static int page_size=8;
    private static int page_no=1,page_count=0,count=0;
    private Button btnAdd,btnFirst,btnEnd;
    private ImageButton btnNext,btnPre;
    private SimpleAdapter sa;
    private ProgressBar m_ProgressBar;
    private ActivityManager am;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        am=ActivityManager.getInstance();
        am.addActivity(this);

        btnAdd =(Button) findViewById(R.id.btnAdd);
        btnFirst=(Button) findViewById(R.id.btnFirst);
        btnPre=(ImageButton) findViewById(R.id.btnPre);
        btnNext=(ImageButton) findViewById(R.id.btnNext);
        btnEnd=(Button) findViewById(R.id.btnEnd);

        m_ProgressBar=(ProgressBar) findViewById(R.id.progressBar);
        lv=(ListView) findViewById(R.id.listview);

        sd=new SqliteDBConnect(MainActivity.this);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Map<String,Object> map=(Map<String,Object>) arg0.getItemAtPosition(arg2);
                Intent intent=new Intent();
                intent.putExtra("noteId",map.get("noteId").toString());
                intent.setClass(MainActivity.this,Lookover.class);
                startActivity(intent);
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                final Map<String, Object> map = (Map<String, Object>) arg0.getItemAtPosition(arg2);
                AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
                adb.setTitle(map.get("noteName").toString());

                adb.setItems(new String[]{"删除", "修改"},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        SQLiteDatabase sdb = sd.getReadableDatabase();
                                        sdb.delete("note", "noteId=?", new String[]{map.get("noteId").toString()});
                                        Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                                        sdb.close();
                                        fenye();
                                        break;
                                    case 1:
                                        Intent intent = new Intent();
                                        intent.putExtra("noteId", map.get("noteId").toString());
                                        intent.setClass(MainActivity.this, AddActivity.class);
                                        startActivity(intent);
                                        break;
                                }
                            }
                        });
                adb.show();
                return true;
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_ProgressBar.setVisibility(View.VISIBLE);
                m_ProgressBar.setProgress(0);
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, AddActivity.class);
                startActivity(intent);
            }
        });

        btnFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(page_no==1){
                    Toast.makeText(MainActivity.this,"已经是首页了",Toast.LENGTH_SHORT).show();
                }else {
                    page_no=1;
                }
                fenye();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(page_no==page_count){
                    Toast.makeText(MainActivity.this,"已经是末页了",Toast.LENGTH_SHORT).show();
                }else{
                    page_no+=1;
                }
                fenye();
            }
        });

        btnPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(page_no==1){
                    Toast.makeText(MainActivity.this,"已经是首页了",Toast.LENGTH_SHORT).show();
                }else {
                    page_no-=1;
                }
                fenye();
            }
        });

        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(page_no==page_count){
                    Toast.makeText(MainActivity.this,"已经是末页了",Toast.LENGTH_SHORT).show();
                }else{
                    page_no=page_count;
                }
                fenye();
            }
        });
    }

    @Override
    protected void onResume() {
        Toast.makeText(MainActivity.this,"Restart",Toast.LENGTH_SHORT).show();
        fenye();
        super.onResume();
    }


    public void fenye(){
        SQLiteDatabase sdb=sd.getReadableDatabase();
        count=0;
        Cursor cl=sdb.query("note",new String[]{"noteId","noteName","noteTime"},null,null,null,null,"noteId asc");
        while (cl.moveToNext()){
            int noteid=cl.getInt(cl.getColumnIndex("noteId"));
            if(noteid>count)
                count = noteid;
        }
        cl.close();
        page_count=count%page_size==0?count/page_size:count/page_size+1;
        if(page_no<1){
            page_no=1;
        }
        if(page_no>page_count){
            page_no=page_count;
        }
        Cursor c=sdb.rawQuery("select noteId,noteName,noteTime from note limit ?,?",new String[]{
                (page_no-1)*page_size+"",page_size+""
        });
        List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
        while (c.moveToNext()){
            Map<String,Object> map=new HashMap<String,Object>();
            String strName=c.getString(c.getColumnIndex("noteName"));
            if(strName.length()>20){
                map.put("noteName",strName.substring(0,20)+"...");
            }else{
                map.put("noteName",strName);
            }
            map.put("noteTime",c.getString(c.getColumnIndex("noteTime")));
            map.put("noteId",c.getInt(c.getColumnIndex("noteId")));
            list.add(map);
        }
        c.close();
        sdb.close();
        if(count>0){
            sa=new SimpleAdapter(MainActivity.this,list,R.layout.items,new String[]{
                    "noteName","noteTime"},new int[]{ R.id.noteName,R.id.noteTime});
            lv.setAdapter(sa);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,1,1,"设置铃声");
        menu.add(0,2,2,"退出");
        return super.onCreateOptionsMenu(menu);
    }
    public boolean onMenuItemSelected(int featureId,MenuItem item){
        switch (item.getItemId()){
            case 1:
                Intent intent=new Intent();
                intent.setClass(MainActivity.this, SetAlarm.class);
                startActivity(intent);
                break;
            case 2:
                AlertDialog.Builder adb2=new AlertDialog.Builder(MainActivity.this);
                adb2.setTitle("消息");
                adb2.setMessage("真的要退出吗?");
                adb2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        am.exitAllProgress();
                    }
                });
                adb2.setNegativeButton("取消", null);
                adb2.show();
                break;
            default:
                break;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            AlertDialog.Builder adb=new AlertDialog.Builder(MainActivity.this);
            adb.setTitle("消息");
            adb.setMessage("真的要退出吗");
            adb.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    am.exitAllProgress();
                }
            });
            adb.setNegativeButton("取消", null);
            adb.show();
        }
        return super.onKeyDown(keyCode, event);
    }
}
