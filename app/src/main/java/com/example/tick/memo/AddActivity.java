package com.example.tick.memo;

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by tick on 2016/4/11.
 */
public class AddActivity extends Activity{
    private EditText etName,etMain,etTime;
    private Button btnCommit,btnCancel;
    private SQLiteDatabase sdb;
    private ActivityManager am;
    private int year,month,day,hours,minute,second;
    private Calendar c;
    private PendingIntent pi;
    private AlarmManager alm;
    private boolean EDIT=false;
    private String noteId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add);
        am=ActivityManager.getInstance();
        am.addActivity(this);
        etName=(EditText) findViewById(R.id.noteName);
        etMain=(EditText) findViewById(R.id.noteMain);
        btnCommit=(Button) findViewById(R.id.btnCommit);
        btnCancel=(Button) findViewById(R.id.btnCancel);
        etTime=(EditText) findViewById(R.id.noteTime);

        Intent intent=getIntent();
        noteId=intent.getStringExtra("noteId");
        if(noteId!=null)
            EDIT=true;
        else
            EDIT=false;
        SqliteDBConnect sd=new SqliteDBConnect(AddActivity.this);
        sdb=sd.getReadableDatabase();
        if(EDIT){
            Cursor c=sdb.query("note",new String[]{"noteId","noteName","noteContent","noteTime"},
                    "noteId=?",new String[]{noteId},null,null,null);
            while (c.moveToNext()) {
                etName.setText(c.getString(c.getColumnIndex("noteName")));
                etMain.setText(c.getString(c.getColumnIndex("noteContent")));
                etTime.setText(c.getString(c.getColumnIndex("noteTime")));
            }
            c.close();
            }else {
            etTime.setText(am.returnTime());
        }

        etTime.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                c=Calendar.getInstance();
                year=c.get(Calendar.YEAR);
                month=c.get(Calendar.MONTH);
                day=c.get(Calendar.DAY_OF_MONTH);
                hours=c.get(Calendar.HOUR);
                minute=c.get(Calendar.MINUTE);
                second=c.get(Calendar.SECOND);

                DatePickerDialog dpd=new DatePickerDialog(AddActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int y, int monthOfYear, int dayOfMonth) {
                        String[] time={"",hours+":"+minute+":"+second};
                        try {
                            String[] time2 = etTime.getText().toString().trim().split(" ");
                            if (time2.length == 2) {
                                time[1] = time2[1];
                            }
                        }catch(Exception e){
                                e.printStackTrace();
                        }
                        String mo="",da="";
                        if(monthOfYear<9){
                            mo="0"+(monthOfYear+1);
                        }else {
                            mo=monthOfYear+1+"";
                        }
                        if(dayOfMonth<10){
                            da="0"+(dayOfMonth);
                        }else {
                            da=dayOfMonth+"";
                        }
                        etTime.setText(y+"-"+mo+"-"+da+" "+time[1]);
                    }
                },year,month,day);
                dpd.setTitle("设置日期");
                dpd.show();
                return true;
            }
        });

        etTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c = Calendar.getInstance();
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
                hours = c.get(Calendar.HOUR);
                minute = c.get(Calendar.MINUTE);
                second = c.get(Calendar.SECOND);

                TimePickerDialog tpd = new TimePickerDialog(AddActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String[] time = {"", hours + ":" + minute + ":" + second};
                        try {
                            time = etTime.getText().toString().trim().split(" ");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String ho = "", mi = "";
                        if (hourOfDay < 10) {
                            ho = "0" + hourOfDay;
                        } else {
                            ho = hourOfDay + "";
                        }
                        if (minute < 10) {
                            mi = "0" + minute;
                        } else {
                            mi = minute + "";
                        }
                        etTime.setText(time[0] + " " + ho + ":" + mi);
                    }
                }, hours, minute, true);
                tpd.setTitle("设置时间");
                tpd.show();
            }
        });

        btnCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder adb=new AlertDialog.Builder(AddActivity.this);
                adb.setTitle("保存");
                adb.setMessage("确定要保存吗？");
                adb.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveNote();
                    }
                });
                adb.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(AddActivity.this, "不保存", Toast.LENGTH_LONG).show();
                    }
                });
                adb.show();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder adb=new AlertDialog.Builder(AddActivity.this);
                adb.setTitle("提示");
                adb.setMessage("确定不保存吗?");
                adb.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setClass(AddActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
                adb.setNegativeButton("取消", null);
                adb.show();
            }
        });
    }

    private void saveNote() {
        String name = etName.getText().toString().trim();
        String content = etMain.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        if ("".equals(name) || "".equals(content)) {
            Toast.makeText(this, "名称和内容都不能为空", Toast.LENGTH_LONG).show();
        } else {
            if (EDIT) {
                am.saveNote(sdb, name, content, noteId, time);
                Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show();
            } else {
                am.addNote(sdb, name, content, time);
                Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
            }
            String[] t = etTime.getText().toString().trim().split(" ");
            String[] t1 = t[0].split("-");
            String[] t2 = t[1].split(":");
            Calendar c2 = Calendar.getInstance();
            c2.set(Integer.parseInt(t1[0]), Integer.parseInt(t1[1]) - 1, Integer.parseInt(t1[2]),
                    Integer.parseInt(t2[0]), Integer.parseInt(t2[1]));
            c = Calendar.getInstance();
            if (c.getTimeInMillis() + 1000 * 10 <= c2.getTimeInMillis()) {
                String messageContent;
                if (content.length() > 20) {
                    messageContent = content.substring(0, 18) + "...";
                } else {
                    messageContent = content;
                }
                Intent intent = new Intent();
                intent.setClass(this, AlarmNote.class);
                intent.putExtra("messageTitle", name);
                intent.putExtra("messageContent", messageContent);
                pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alm = (AlarmManager) getSystemService(ALARM_SERVICE);
                alm.set(AlarmManager.RTC_WAKEUP, c2.getTimeInMillis(), pi);
            }
            Intent intent2 = new Intent();
            intent2.setClass(this, MainActivity.class);

            startActivity(intent2);
            AddActivity.this.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,1,1,"关于");
        menu.add(0,2,2,"设置闹铃");
        menu.add(0,3,3,"退出");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()){
            case 1:
                AlertDialog.Builder adb=new AlertDialog.Builder(AddActivity.this);
                adb.setTitle("关于");
                adb.setMessage("备忘录");
                adb.setPositiveButton("确定", null);
                adb.show();
                break;
            case 2:
                Intent intent=new Intent();
                intent.setClass(AddActivity.this, SetAlarm.class);
                startActivity(intent);
                break;
            case 3:
                AlertDialog.Builder adb2=new AlertDialog.Builder(AddActivity.this);
                adb2.setTitle("消息");
                adb2.setMessage("真的要退出吗？");
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
            AlertDialog.Builder adb=new AlertDialog.Builder(AddActivity.this);
            adb.setTitle("消息");
            adb.setMessage("是否要保存？");
            adb.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveNote();
                }
            });
            adb.setNegativeButton("不保存", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent2 = new Intent();
                    intent2.setClass(AddActivity.this, MainActivity.class);
                    startActivity(intent2);
                }
            });
            adb.show();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sdb.close();
    }
}
