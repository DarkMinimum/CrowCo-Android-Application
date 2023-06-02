package ua.dark.crowco;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.text.InputType;
import android.view.Surface;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private AutoFitTextureView textureView;
    private EditText hint;
    private CaptureRequest.Builder captureRequestBuilder;
    private AsyncJob asyncJob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hint = findViewById(R.id.hint);
        hint.setInputType(InputType.TYPE_NULL);
        textureView = findViewById(R.id.textureView);
        textureView.setAspectRatio(7, 7);

        asyncJob = new AsyncJob(findViewById(R.id.crowdCount), findViewById(R.id.checkBox), textureView);
        openCamera();
        asyncJob.pinRegularTask();
    }

    /**
     * Use CameraMetadata.LENS_FACING_BACK for back-camera and LENS_FACING_FRONT for front one.
     *
     * @param metadata enum int value
     * @return String camera id
     */
    private String getCameraIdByTag(int metadata) {
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing == metadata) {
                    return cameraId;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void openCamera() {
        try {
            cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
            String cameraId = getCameraIdByTag(CameraCharacteristics.LENS_FACING_BACK);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                LoggerWrapper.err("The application can't run due to lack of permissions");
                return;
            }
            cameraManager.openCamera(cameraId, myStateCallBack, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private final CameraDevice.StateCallback myStateCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;

        }
    };

    public void cameraPreview(View view) {
        asyncJob.pingService();
        hint.setVisibility(View.GONE);
        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        Surface surface = new Surface(surfaceTexture);
        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Collections.singletonList(surface), new MyCameraCaptureSession(captureRequestBuilder), null);
            LoggerWrapper.d("Camera capture session successfully created");
        } catch (CameraAccessException e) {
            LoggerWrapper.err("The application failed on cameraPreview");
            throw new RuntimeException(e);
        }
    }
}