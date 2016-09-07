package net.waterfoul.gooverlay;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.LruCache;

import com.googlecode.tesseract.android.TessBaseAPI;
import net.waterfoul.gooverlay.interop.SinglePokemon;
import net.waterfoul.gooverlay.logic.Data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by Sarav on 8/25/2016.
 * A class to scan a screenshot and extract useful information visible in the bitmap.
 */
public class OCRHelper {

    private static OCRHelper instance = null;
    private TessBaseAPI tesseract = null;
    public LruCache<String, String> ocrCache = new LruCache<>(200);
    private int heightPixels;
    private int widthPixels;
    private boolean candyWordFirst;
    private String nidoFemale;
    private String nidoMale;

    private OCRHelper(String dataPath, int widthPixels, int heightPixels, String nidoFemale, String nidoMale) {
        tesseract = new TessBaseAPI();
        tesseract.init(dataPath, "eng");
        tesseract.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
        tesseract.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST,
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789/♀♂");
        this.heightPixels = heightPixels;
        this.widthPixels = widthPixels;
        this.candyWordFirst = isCandyWordFirst();
        this.nidoFemale = nidoFemale;
        this.nidoMale = nidoMale;
    }

    public int getWidthPixels() {
        return widthPixels;
    }

    public int getHeightPixels() {
        return heightPixels;
    }

    public LruCache<String, String> getOcrCache() {
        return ocrCache;
    }

    public TessBaseAPI getTesseract() {
        return tesseract;
    }

    public String getNidoFemale() {
        return nidoFemale;
    }

    public String getNidoMale() {
        return nidoMale;
    }

    public boolean getCandyWordFirst() {
        return candyWordFirst;
    }

    /**
     * init
     * Initializes the OCR helper and readies it for use
     *
     * @param dataPath Path the OCR data files.
     * @return Bitmap with replaced colors
     */
    public static OCRHelper init(String dataPath, int widthPixels, int heightPixels, String nidoFemale,
                                 String nidoMale) {
        if (instance == null) {
            instance = new OCRHelper(dataPath, widthPixels, heightPixels, nidoFemale, nidoMale);
        }
        return instance;
    }

    public void exit() {
        if (tesseract != null) {
            tesseract.stop();
            tesseract.end();
            tesseract = null;
            instance = null;
        } else {
            Timber.e("Avoided NPE on OCRHelper.exit()");
            //The exception is to ensure we get a stack trace. It's not thrown.
            Timber.e(new Throwable());
        }
    }

    private boolean isCandyWordFirst() {
        //Check if language makes the pokemon name in candy second; France/Spain/Italy have Bonbon/Caramelos pokeName.
        String language = Locale.getDefault().getLanguage();
        HashSet<String> specialCandyOrderLangs = new HashSet<>(Arrays.asList("fr", "es", "it"));
        return specialCandyOrderLangs.contains(language);
    }

    /**
     * replaceColors
     * Replaces colors in a bitmap that are not farther away from a specific color than a given
     * threshold.
     *
     * @param myBitmap     The bitmap to check the colors for.
     * @param keepCr       The red color to keep
     * @param keepCg       The green color to keep
     * @param keepCb       The blue color to keep
     * @param replaceColor The color to replace mismatched colors with
     * @param distance     The distance threshold.
     * @param simpleBG     Whether the bitmap has a simple background
     * @return Bitmap with replaced colors
     */
    public Bitmap replaceColors(Bitmap myBitmap, int keepCr, int keepCg, int keepCb, int replaceColor, int distance,
                                 boolean simpleBG) {
        int[] allpixels = new int[myBitmap.getHeight() * myBitmap.getWidth()];
        myBitmap.getPixels(allpixels, 0, myBitmap.getWidth(), 0, 0, myBitmap.getWidth(), myBitmap.getHeight());
        int bgColor = replaceColor;
        int distanceSq = distance * distance;

        if (simpleBG) {
            bgColor = allpixels[0];
        }

        for (int i = 0; i < allpixels.length; i++) {
            /* Avoid unnecessary math for obviously background color. This removes most of the math
             * for candy, HP and name bitmaps. */
            if (allpixels[i] == bgColor) {
                allpixels[i] = replaceColor;
                continue;
            }
            int rDiff = keepCr - Color.red(allpixels[i]);
            int gDiff = keepCg - Color.green(allpixels[i]);
            int bDiff = keepCb - Color.blue(allpixels[i]);
            int dSq = rDiff * rDiff + gDiff * gDiff + bDiff * bDiff;
            if (dSq > distanceSq) {
                allpixels[i] = replaceColor;
            }
        }

        myBitmap.setPixels(allpixels, 0, myBitmap.getWidth(), 0, 0, myBitmap.getWidth(), myBitmap.getHeight());
        return myBitmap;
    }

    /**
     * Get the hashcode for a bitmap
     */
    public String hashBitmap(Bitmap bmp) {
        int[] allpixels = new int[bmp.getHeight() * bmp.getWidth()];
        bmp.getPixels(allpixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
        return Integer.toHexString(Arrays.hashCode(allpixels));
    }

    /**
     * Correct some OCR errors in argument where only letters are expected.
     */
    public static String fixOcrNumsToLetters(String src) {
        return src.replace("1", "l").replace("0", "o");
    }

    /**
     * Correct some OCR errors in argument where only numbers are expected.
     */
    public static String fixOcrLettersToNums(String src) {
        return src.replace("O", "0").replace("l", "1").replace("Z", "2");
    }

    /**
     * Dont missgender the poor nidorans.
     * <p/>
     * Takes a subportion of the screen, and averages the color to check the average values and compares to known
     * male / female average
     *
     * @param pokemonImage The screenshot of the entire application
     * @return True if the nidoran is female
     */
    public boolean isNidoranFemale(Bitmap pokemonImage) {
        Bitmap pokemon = Bitmap.createBitmap(pokemonImage, widthPixels / 3, Math.round(heightPixels / 4),
                Math.round(widthPixels / 3), Math.round(heightPixels / 5));
        int[] pixelArray = new int[pokemon.getHeight() * pokemon.getWidth()];
        pokemon.getPixels(pixelArray, 0, pokemon.getWidth(), 0, 0, pokemon.getWidth(), pokemon.getHeight());
        int greenSum = 0;
        int blueSum = 0;

        // a loop that sums the color values of all the pixels in the image of the nidoran
        for (int pixel : pixelArray) {
            blueSum += Color.green(pixel);
            greenSum += Color.blue(pixel);
        }
        int greenAverage = greenSum / pixelArray.length;
        int blueAverage = blueSum / pixelArray.length;
        //Average male nidoran has RGB value ~~ 136,165,117
        //Average female nidoran has RGB value~ 135,190,140
        int femaleGreenLimit = 175; //if average green is over 175, its probably female
        int femaleBlueLimit = 130; //if average blue is over 130, its probably female
        boolean isFemale = true;
        if (greenAverage < femaleGreenLimit && blueAverage < femaleBlueLimit) {
            isFemale = false; //if neither average is above the female limit, then it's male.
        }
        return isFemale;
    }

    @NonNull
    public static String removeFirstOrLastWord(String src, boolean removeFirst) {
        if (removeFirst) {
            int fstSpace = src.indexOf(' ');
            if (fstSpace != -1)
                return src.substring(fstSpace + 1);
        } else {
            int lstSpace = src.lastIndexOf(' ');
            if (lstSpace != -1)
                return src.substring(0, lstSpace);
        }
        return src;
    }
}
