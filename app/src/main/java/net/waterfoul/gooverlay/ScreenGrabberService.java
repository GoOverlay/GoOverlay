package net.waterfoul.gooverlay;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

public class ScreenGrabberService extends OCRService {
    private static final int DELAY = 750;
    private ScreenGrabber screen;

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            fetchScreen();
            handler.postDelayed(this, DELAY);
        }
    };

    private void fetchScreen() {
        Bitmap bmp = screen.grabScreen();
        if (bmp == null) {
            return;
        }
        processImage(bmp);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ScreenGrab", "Start");
        screen = ScreenGrabber.getInstance();

        super.onStartCommand(intent, flags, startId);

        handler.postDelayed(runnable, DELAY);
        fetchScreen();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("ScreenGrab", "Stop");
        handler.removeCallbacksAndMessages(null);
    }
}
