package net.waterfoul.gooverlay;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;

import net.waterfoul.gooverlay.interop.SinglePokemon;
import net.waterfoul.gooverlay.Scrapers.singlePokemon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.ReentrantLock;

import timber.log.Timber;

public abstract class OCRService extends Service {
    public static OCRService currentInstance = null;

    private ReentrantLock imageLock = new ReentrantLock();
    public static DisplayMetrics displayMetrics;
    private OCRHelper ocr;

    private Bitmap currentImage = null;
    int[] location_center_button_1 = new int[2];
    int[] location_center_button_2 = new int[2];
    int[] location_right_button_1 = new int[2];
    int[] location_right_button_2 = new int[2];
    int[] location_pokemon_white_check = new int[2];

    int color_button_teal = Color.rgb(28, 135, 150);
    int color_white_pokemon_select =  Color.rgb(254, 255, 254);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initLocations();
        initOCR();

        currentInstance = this;

        return START_NOT_STICKY;
    }

    private void initOCR() {
        String extdir = getExternalFilesDir(null).toString();
        if (!new File(extdir + "/tessdata/eng.traineddata").exists()) {
            copyAssetFolder(getAssets(), "tessdata", extdir + "/tessdata");
        }

        ocr = OCRHelper.init(
                extdir,
                displayMetrics.widthPixels,
                displayMetrics.heightPixels,
                getResources().getString(R.string.pokemon029),
                getResources().getString(R.string.pokemon032)
        );
    }

    private static boolean copyAssetFolder(AssetManager assetManager, String fromAssetPath, String toPath) {

        String[] files = new String[0];

        try {
            files = assetManager.list(fromAssetPath);
        } catch (IOException exception) {
            Timber.e("Exception thrown in copyAssetFolder()");
            Timber.e(exception);
        }
        new File(toPath).mkdirs();
        boolean res = true;
        for (String file : files)
            if (file.contains(".")) {
                res &= copyAsset(assetManager, fromAssetPath + "/" + file, toPath + "/" + file);
            } else {
                res &= copyAssetFolder(assetManager, fromAssetPath + "/" + file, toPath + "/" + file);
            }
        return res;

    }

    private static boolean copyAsset(AssetManager assetManager, String fromAssetPath, String toPath) {
        try {
            InputStream in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            OutputStream out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            out.flush();
            out.close();
            return true;
        } catch (IOException exception) {
            Timber.e("Exception thrown in copyAsset()");
            Timber.e(exception);
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    @Override
    public void onDestroy() {
        if(currentInstance == this) {
            currentInstance = null;
        }
        ocr.exit();
    }

    protected void initLocations() {
        displayMetrics = this.getResources().getDisplayMetrics();

        location_center_button_1[0] = (int) Math.round(displayMetrics.widthPixels * (720/1440.0));
        location_center_button_1[1] = (int) Math.round(displayMetrics.heightPixels * (2342/2560.0));

        location_center_button_2[0] = (int) Math.round(displayMetrics.widthPixels * (720/1440.0));
        location_center_button_2[1] = (int) Math.round(displayMetrics.heightPixels * (2410/2560.0));

        location_right_button_1[0] = (int) Math.round(displayMetrics.widthPixels * (1240/1440.0));
        location_right_button_1[1] = (int) Math.round(displayMetrics.heightPixels * (2306/2560.0));

        location_right_button_2[0] = (int) Math.round(displayMetrics.widthPixels * (1240/1440.0));
        location_right_button_2[1] = (int) Math.round(displayMetrics.heightPixels * (2436/2560.0));

        location_pokemon_white_check[0] = (int) Math.round(displayMetrics.widthPixels * (78/1440.0));
        location_pokemon_white_check[1] = (int) Math.round(displayMetrics.heightPixels * (152/2560.0));
    }

    protected void processImage(Bitmap bmp) {
        imageLock.lock();
        try {
            if (currentImage != null && !currentImage.isRecycled()) {
                currentImage.recycle();
            }
            currentImage = bmp;
            if (hasCenterButton(bmp)) {
                if (hasRightButton(bmp)) {
                    // This can only be one of the two pokemon screens
                    if (pokemonWhiteCheckPasses(bmp)) {
                        //On the pokemon select screen
                        Intents.broadcast(Intents.POKEMON_SELECT_OPEN, this);
                    } else {
                        //On the single pokemon screen
                        Intents.broadcast(Intents.SINGLE_POKEMON_OPEN, this);
                    }
                    return;
                }
            }

            Intents.closeCurrent(this);
        } finally {
            imageLock.unlock();
        }
    }

    protected boolean hasCenterButton(Bitmap bmp) {
        return locationMatchesColor(bmp, location_center_button_1, color_button_teal) ||
                locationMatchesColor(bmp, location_center_button_2, color_button_teal);
    }

    protected boolean hasRightButton(Bitmap bmp) {
        return locationMatchesColor(bmp, location_right_button_1, color_button_teal) ||
                locationMatchesColor(bmp, location_right_button_2, color_button_teal);
    }

    protected boolean pokemonWhiteCheckPasses(Bitmap bmp) {
        return locationMatchesColor(bmp, location_pokemon_white_check, color_white_pokemon_select);
    }

    private boolean locationMatchesColor(Bitmap bmp, int[] location, int color) {
        return bmp.getPixel(location[0], location[1]) == color;
    }

    public SinglePokemon fetchSinglePokemon() {
        imageLock.lock();
        try {
            return singlePokemon.scanPokemon(ocr, currentImage, 22);
        } finally {
            imageLock.unlock();
        }
    }
}
