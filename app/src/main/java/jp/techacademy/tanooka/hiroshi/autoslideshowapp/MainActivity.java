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
    private static final int PERMISSIONS_REQUEST_CODE = 100;

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

        // スタートボタンがタップされたとき、スライドショーを開始する
        mStartStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 6.0以上の場合、パーミッションを確認
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // 許可されている場合
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        // 初回だけ呼び出されて、カーソルを取得、画像を表示する
                        if (cursor == null) {
                            getCursor();
                            showImage(cursor);
                        }
                        // スライドショーを開始
                        startSlideShow(v);
                    } else {    // 許可されていない場合
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
                    }
                }
            }
        });

        // 戻るボタンの動作
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        if (cursor == null) {
                            getCursor();
                            showImage(cursor);
                        }
                        if (cursor.moveToPrevious()) {
                            showImage(cursor);
                        } else {
                            cursor.moveToLast();
                            showImage(cursor);
                        }
                    } else {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
                    }
                }
            }
        });

        // 進むボタンの動作
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        if (cursor == null) {
                            getCursor();
                            showImage(cursor);
                        }
                        if (cursor.moveToNext()) {
                            showImage(cursor);
                        } else {
                            cursor.moveToFirst();
                            showImage(cursor);
                        }
                    } else {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
                    }
                }
            }
        });
    }

    // カーソル、画像URIの定義と取得
    private Cursor getCursor() {
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        return cursor;
    }

    // スライドショーの実行
    private void startSlideShow(View v) {

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
                            } else {    // 列の最後に来たら最初に戻る
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

            // 戻る・進むボタンを有効に戻す
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
