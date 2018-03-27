package jp.team.e_works.screenshotapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    // オーバーレイのリクエストコード
    private static final int PERMISSION_OVERRAY_REQUEST_CODE = 1000;
    // スクリーンショットのリクエストコード
    private static final int PERMISSION_SCREENSHOT_REQUEST_CODE = 2000;
    // ストレージ
    private static final int PERMISSION_STORAGE_REQUEST_CODE = 3000;

    private static final String SEPARATOR = File.separator;
    private static final String BASE_DIR = Environment.getExternalStorageDirectory().getPath() + SEPARATOR + "ScreenshotApp";

    private WindowManager mWindowManager;
    private SubWindowFragment mSubWindow;

    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;

    private ImageReader mImageReader;
    private int mWidth;
    private int mHight;

    private Switch mSwitchStartUp;
    private Button mBtnPositionSettings;

    private int mSelectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent intent = mMediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(intent, PERMISSION_SCREENSHOT_REQUEST_CODE);

        mSwitchStartUp = (Switch) findViewById(R.id.switch_startUp);
        mSwitchStartUp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    openSubWindow();
                } else {
                    closeSubWindow();
                }
            }
        });

        mBtnPositionSettings = (Button) findViewById(R.id.btn_position);
        mBtnPositionSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPositionDialog();
            }
        });

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_STORAGE_REQUEST_CODE);
        } else {
            createDir();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mVirtualDisplay != null) {
            mVirtualDisplay.release();
        }
        closeSubWindow();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == PERMISSION_SCREENSHOT_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, "permission denied.", Toast.LENGTH_LONG).show();
                finish();
            }
            mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, intent);

            DisplayMetrics metrics = getResources().getDisplayMetrics();
            mWidth = metrics.widthPixels;
            mHight = metrics.heightPixels;
            int density = metrics.densityDpi;

            mImageReader = ImageReader.newInstance(mWidth, mHight, PixelFormat.RGBA_8888, 2);
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("CapturingDisplay",
                    mWidth, mHight, density, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mImageReader.getSurface(), null, null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResult) {
        if (requestCode == PERMISSION_STORAGE_REQUEST_CODE) {
            if (grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                createDir();
            }
        }
    }

    private void openSubWindow() {
        if (Settings.canDrawOverlays(this)) {
            if (mSubWindow != null && mSubWindow.getVariableView() != null) {
                closeSubWindow();
            }

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_TOAST,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT
            );
            params.gravity = getSubWindowButtonPosition();

            mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            mSubWindow = new SubWindowFragment();
            mWindowManager.addView(mSubWindow.loadView(this), params);

            mSwitchStartUp.setChecked(true);
            mBtnPositionSettings.setEnabled(false);
        } else {
            mSwitchStartUp.setChecked(false);
            mBtnPositionSettings.setEnabled(true);
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, PERMISSION_OVERRAY_REQUEST_CODE);
        }
    }

    private void closeSubWindow() {
        if (mSubWindow != null && mSubWindow.getVariableView() != null) {
            mWindowManager.removeView(mSubWindow.getVariableView());
            mSubWindow.removeVariableView();

            mSwitchStartUp.setChecked(false);
            mBtnPositionSettings.setEnabled(true);
        }
    }

    private int getSubWindowButtonPosition() {
        int position;
        switch (mSelectedPosition) {
            case 0: // 左上
                position = Gravity.TOP | Gravity.START;
                break;

            case 1: // 中央上
                position = Gravity.TOP | Gravity.CENTER;
                break;

            case 2: // 右上
                position = Gravity.TOP | Gravity.END;
                break;

            case 3: // 左中央
                position = Gravity.CENTER | Gravity.START;
                break;

            case 4: // 中央
                position = Gravity.CENTER;
                break;

            case 5: // 右中央
                position = Gravity.CENTER | Gravity.END;
                break;

            case 6: // 左下
                position = Gravity.BOTTOM | Gravity.START;
                break;

            case 7: // 中央下
                position = Gravity.BOTTOM | Gravity.CENTER;
                break;

            case 8: // 右下
                position = Gravity.BOTTOM | Gravity.END;
                break;

            default:
                position = Gravity.TOP | Gravity.START;
                break;
        }
        return position;
    }

    private void showPositionDialog() {
        PositionDialog dialog = new PositionDialog();
        dialog.setValue(mSelectedPosition);
        dialog.registerListener(new PositionDialog.PositiveOnClickListener() {
            @Override
            public void PositiveOnClick(int pickerPosition) {
                mSelectedPosition = pickerPosition;
            }
        });
        dialog.show(getFragmentManager(), getResources().getString(R.string.temp));
    }

    private void createDir() {
        File dir = new File(BASE_DIR);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Toast.makeText(this, "make dir failed.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void getScreenshot() {
        Image image = mImageReader.acquireLatestImage();
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();

        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * mWidth;

        Bitmap bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride,
                mHight, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        image.close();

        try {
            File file = new File(BASE_DIR + SEPARATOR + getFileName());
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
        } catch (IOException e) {
            // todo
        }
    }

    private String getFileName() {
        Calendar cal = Calendar.getInstance();
        return String.format(Locale.US, "capture%04d%02d%02d_%02d%02d%02d%03d.png", cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1),
                cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), cal.get(Calendar.MILLISECOND));
    }
}
