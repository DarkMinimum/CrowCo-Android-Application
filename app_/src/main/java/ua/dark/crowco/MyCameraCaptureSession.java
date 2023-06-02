package ua.dark.crowco;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;

import androidx.annotation.NonNull;

public class MyCameraCaptureSession extends CameraCaptureSession.StateCallback {

    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder captureRequestBuilder;

    public MyCameraCaptureSession(CaptureRequest.Builder captureRequestBuilder) {
        this.captureRequestBuilder = captureRequestBuilder;
    }

    @Override
    public void onConfigured(@NonNull CameraCaptureSession session) {
        captureSession = session;
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigureFailed(@NonNull CameraCaptureSession session) {

    }

}
