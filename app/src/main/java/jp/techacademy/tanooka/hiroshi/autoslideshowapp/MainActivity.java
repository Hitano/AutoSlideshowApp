package jp.techacademy.tanooka.hiroshi.autoslideshowapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    // パーミッション判定
    private static final int PERMISSIONS_REQUEST_CODE = 1;

    Timer mTimer;
    double mTimerSec = 0.0;

    Handler mHandler = new Handler();

    Button mStartStopButton;
    Button mBackButton;
    Button mNextButton;

    Cursor cursor;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStartStopButton = (Button)findViewById(R.id.startStopButton);
        mBackButton = (Button)findViewById(R.id.backButton);
        mNextButton = (Button)findViewById(R.id.nextButton);

        // カーソル、画像の初期化
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // 最初の画像の表示
        cursor.moveToFirst();
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setImageURI(imageUri);

        // スタートボタンがタップされたとき、スライドショーを開始する
        mStartStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSlideShow(v);
            }
        });

        // 戻るボタンの動作
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Message", "PushBackButton");
                if (cursor.moveToPrevious()) {
                    showImage(cursor);
                } else {
                    cursor.moveToLast();
                    showImage(cursor);
                }
            }
        });

        // 進むボタンの動作
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Message", "PushNextButton");
                if (cursor.moveToNext()) {
                    showImage(cursor);
                } else {
                    cursor.moveToFirst();
                    showImage(cursor);
                }
            }
        });
    }

    // スライドショーの実行
    private void startSlideShow(View v) {
        Log.d("Message", "PushStartStopButton");

        // スライドショーを再生する
        if (mTimer == null) {
            mTimer = new Timer();

            // 2秒ごとに画像を切り替えるタイマー
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mTimerSec += 2.0;

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (cursor.moveToNext()) {
                                showImage(cursor);
                            } else {
                                cursor.moveToFirst();
                                showImage(cursor);
                            }
                        }
                    });
                }
            }, 1000, 2000);

            // 再生ボタンを停止ボタンに変更する
            mStartStopButton.setText("停止");

            // 再生中は戻る・進むボタンを無効にする
            mBackButton.setEnabled(false);
            mNextButton.setEnabled(false);

        } else if (mTimer != null) {    // スライドショーを停止する
            mTimer.cancel();
            mTimer = null;

            // 停止ボタンを再生ボタンに戻す
            mStartStopButton.setText("再生");

            mBackButton.setEnabled(true);
            mNextButton.setEnabled(true);
        }
    }

    // 取得したカーソルから対応する画像を表示する
    private void showImage(Cursor cursor) {
        // カーソルから画像のURIを取得
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        Log.d("ANDROID.id", String.valueOf(id));
        Log.d("ANDROID.Uri", String.valueOf(imageUri));

        // URIから画像を表示する
        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setImageURI(imageUri);
    }
}
