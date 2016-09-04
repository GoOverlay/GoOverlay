package net.waterfoul.gooverlay;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class WatchPoGoRunningSvc extends Service {
    private static final int DELAY = 1000;
    private static boolean running = false;

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String appName = getTopAppName();
            if(appName.equals("com.nianticlabs.pokemongo")) {
                if(!running) {
                    running = true;
                    Log.d("WatchPoGo", "Running");
                    Notification.setMessage("Running...");

                    Intents.broadcast(Intents.POKEMON_GO_OPEN, WatchPoGoRunningSvc.this);
                    // TODO: Support screenshots
                    Intent intent = new Intent(WatchPoGoRunningSvc.this, ScreenGrabberService.class);
                    WatchPoGoRunningSvc.this.startService(intent);
                }
            } else if(running) {
                running = false;
                Log.d("WatchPoGo", "Waiting");
                Notification.setMessage("Waiting for Pokemon Go");

                Intents.closeApp(WatchPoGoRunningSvc.this);
                // TODO: Support screenshots
                Intent intent = new Intent(WatchPoGoRunningSvc.this, OCRService.class);
                WatchPoGoRunningSvc.this.stopService(intent);
            }
            handler.postDelayed(this, DELAY);
        }
    };

    public WatchPoGoRunningSvc() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("WatchPoGo", "Start");
        handler.postDelayed(runnable, DELAY);
        Notification.makeNotification(this);
        Notification.setMessage("Waiting for Pokemon Go");

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("WatchPoGo", "Stop");
        handler.removeCallbacksAndMessages(null);
        Notification.removeNotification(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public String getTopAppName() {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            String currentApp = null;
            UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> applist = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
            if (applist != null && applist.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : applist) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }

            return currentApp;
        } else {
            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            String mm=(manager.getRunningTasks(1).get(0)).topActivity.getPackageName();
            return mm;
        }
    }
}
