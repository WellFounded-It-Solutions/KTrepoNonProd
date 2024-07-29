package se.infomaker.iap.theme.debug;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeDetector
{

    private SensorEventListener mSensorListener = new SensorEventListener() {
        private float accel = 0.00f;
        private float accelCurrent = SensorManager.GRAVITY_EARTH;
        private float ccelLast = SensorManager.GRAVITY_EARTH;

        public void onSensorChanged(SensorEvent se) {
            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            ccelLast = accelCurrent;
            accelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
            float delta = accelCurrent - ccelLast;
            accel = accel * 0.9f + delta;
            if (accel > 12) {
                shaken();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private long lastShake = 0;
    private OnShakeListener listener;

    private void shaken() {
        if (System.currentTimeMillis() - lastShake > 1000) {
            lastShake = System.currentTimeMillis();
            if (listener !=  null) {
                listener.onShake();
            }
        }
    }

    public OnShakeListener getListener() {
        return listener;
    }

    public void setListener(OnShakeListener listener) {
        this.listener = listener;
    }

    private final SensorManager mSensorManager;
    private boolean isStarted = false;

    public ShakeDetector(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void start() {
        if (!isStarted) {
            isStarted = true;
            mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    public void onPause() {
        if (isStarted) {
            isStarted = false;
            mSensorManager.unregisterListener(mSensorListener);
        }
    }

}
