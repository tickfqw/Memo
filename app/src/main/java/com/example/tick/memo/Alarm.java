package com.example.tick.memo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

/**
 * Created by tick on 2016/4/11.
 */
public class Alarm extends Activity{
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm);
        try{
            mMediaPlayer=MediaPlayer.create(Alarm.this,ActivityManager.getUri());
            mMediaPlayer.setVolume(300,300);
            mMediaPlayer.setLooping(true);
        }catch (Exception e){
            Toast.makeText(Alarm.this,"音乐文件播放异常",Toast.LENGTH_SHORT).show();
        }
        mMediaPlayer.start();
        Intent intent=getIntent();
        String messageTitle=intent.getStringExtra("messageTitle");
        String massageContent=intent.getStringExtra("messageContent");
        AlertDialog.Builder adb=new AlertDialog.Builder(Alarm.this);
        adb.setTitle(messageTitle);
        adb.setMessage(massageContent);
        adb.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                finish();
            }
        });
        adb.show();
    }
}
