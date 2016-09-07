package net.waterfoul.gooverlay.Scrapers;

import android.graphics.Bitmap;
import android.graphics.Color;

import net.waterfoul.gooverlay.OCRHelper;
import net.waterfoul.gooverlay.interop.SinglePokemon;
import net.waterfoul.gooverlay.logic.Data;

public class singlePokemon {
    /**
     * Scans the arc and tries to determine the pokemon level, returns 1 if nothing found
     *
     * @param pokemonImage The image of the entire screen
     * @return the estimated pokemon level, or 1 if nothing found
     */
    private static double getPokemonLevelFromImg(Bitmap pokemonImage, int trainerLevel) {
        double estimatedPokemonLevel = Data.trainerLevelToMaxPokeLevel(trainerLevel);
        for (double estPokemonLevel = estimatedPokemonLevel; estPokemonLevel >= 1.0; estPokemonLevel -= 0.5) {
            int index = Data.levelToLevelIdx(estPokemonLevel);
            int x = Data.arcX[index];
            int y = Data.arcY[index];
            if (pokemonImage.getPixel(x, y) == Color.rgb(255, 255, 255)) {
                return estPokemonLevel;
            }
        }
        return 1;
    }

    /**
     * get the pokemon name as analysed from a pokemon image
     *
     * @param pokemonImage the image of the whole screen
     * @return A string resulting from the scan
     */
    private static String getPokemonNameFromImg(OCRHelper ocr, Bitmap pokemonImage) {
        Bitmap name = Bitmap.createBitmap(pokemonImage, ocr.getWidthPixels() / 4, (int) Math.round(ocr.getHeightPixels() / 2.22608696),
                (int) Math.round(ocr.getWidthPixels() / 2.057), (int) Math.round(ocr.getHeightPixels() / 18.2857143));
        String hash = "name" + ocr.hashBitmap(name);
        String pokemonName = ocr.getOcrCache().get(hash);

        if (pokemonName == null) {
            name = ocr.replaceColors(name, 68, 105, 108, Color.WHITE, 200, true);
            ocr.getTesseract().setImage(name);
            pokemonName = ocr.fixOcrNumsToLetters(ocr.getTesseract().getUTF8Text().replace(" ", ""));
            if (pokemonName.toLowerCase().contains("nidora")) {
                boolean isFemale = ocr.isNidoranFemale(pokemonImage);
                if (isFemale) {
                    pokemonName = ocr.getNidoFemale();
                } else {
                    pokemonName = ocr.getNidoMale();
                }
            }
            name.recycle();
            ocr.getOcrCache().put(hash, pokemonName);
        }
        return pokemonName;
    }

    /**
     * gets the candy name from a pokenon image
     *
     * @param pokemonImage the image of the whole screen
     * @return the candy name, or "" if nothing was found
     */
    private static String getCandyNameFromImg(OCRHelper ocr, Bitmap pokemonImage) {
        Bitmap candy = Bitmap.createBitmap(pokemonImage, ocr.getWidthPixels() / 2, (int) Math.round(ocr.getHeightPixels() / 1.3724285),
                (int) Math.round(ocr.getWidthPixels() / 2.1), (int) Math.round(ocr.getHeightPixels() / 38.4));
        String hash = "candy" + ocr.hashBitmap(candy);
        String candyName = ocr.getOcrCache().get(hash);

        if (candyName == null) {
            candy = ocr.replaceColors(candy, 68, 105, 108, Color.WHITE, 200, true);
            ocr.getTesseract().setImage(candy);
            try {
                candyName = ocr.fixOcrNumsToLetters(
                        ocr.removeFirstOrLastWord(
                                ocr.getTesseract().getUTF8Text().trim().replace("-", " "), ocr.getCandyWordFirst()
                        )
                );
            } catch (StringIndexOutOfBoundsException e) {
                candyName = "";
            }
            candy.recycle();
            ocr.getOcrCache().put(hash, candyName);
        }
        return candyName;
    }

    /**
     * get the pokemon hp from a picture
     *
     * @param pokemonImage the image of the whole screen
     * @return an integer of the interpreted pokemon name, 10 if scan failed
     */
    private static int getPokemonHPFromImg(OCRHelper ocr, Bitmap pokemonImage) {
        int pokemonHP = 10;
        Bitmap hp = Bitmap.createBitmap(pokemonImage, (int) Math.round(ocr.getWidthPixels() / 2.8),
                (int) Math.round(ocr.getHeightPixels() / 1.8962963), (int) Math.round(ocr.getWidthPixels() / 3.5),
                (int) Math.round(ocr.getHeightPixels() / 34.13333333));
        String hash = "hp" + ocr.hashBitmap(hp);
        String pokemonHPStr = ocr.getOcrCache().get(hash);

        if (pokemonHPStr == null) {
            hp = ocr.replaceColors(hp, 55, 66, 61, Color.WHITE, 200, true);
            ocr.getTesseract().setImage(hp);
            pokemonHPStr = ocr.getTesseract().getUTF8Text();
            hp.recycle();
            ocr.getOcrCache().put(hash, pokemonHPStr);
        }

        if (pokemonHPStr.contains("/")) {
            try {
                pokemonHP = Integer.parseInt(ocr.fixOcrLettersToNums(pokemonHPStr.split("/")[1]).replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                pokemonHP = 10;
            }
        }
        return pokemonHP;
    }

    /**
     * get the cp of a pokemon image
     *
     * @param pokemonImage the image of the whole pokemon screen
     * @return a CP of the pokemon, 10 if scan failed
     */
    private static int getPokemonCPFromImg(OCRHelper ocr, Bitmap pokemonImage) {
        int pokemonCP;
        Bitmap cp = Bitmap.createBitmap(pokemonImage, (int) Math.round(ocr.getWidthPixels() / 3.0),
                (int) Math.round(ocr.getHeightPixels() / 15.5151515), (int) Math.round(ocr.getWidthPixels() / 3.84),
                (int) Math.round(ocr.getHeightPixels() / 21.333333333));
        cp = ocr.replaceColors(cp, 255, 255, 255, Color.BLACK, 30, false);
        ocr.getTesseract().setImage(cp);
        String cpText = ocr.fixOcrLettersToNums(ocr.getTesseract().getUTF8Text());
        if (cpText.length() >= 2) { //gastly can block the "cp" text, so its not visible...
            cpText = cpText.substring(2);
        }
        try {
            pokemonCP = Integer.parseInt(cpText);
        } catch (NumberFormatException e) {
            pokemonCP = 10;
        }
        cp.recycle();
        return pokemonCP;
    }

    /**
     * scanPokemon
     * Performs OCR on an image of a pokemon and returns the pulled info.
     *
     * @param pokemonImage The image of the pokemon
     * @param trainerLevel Current level of the trainer
     * @return an object
     */
    public static SinglePokemon scanPokemon(OCRHelper ocr, Bitmap pokemonImage, int trainerLevel) {
        double estimatedPokemonLevel = getPokemonLevelFromImg(pokemonImage, trainerLevel);
        String pokemonName = getPokemonNameFromImg(ocr, pokemonImage);
        String candyName = getCandyNameFromImg(ocr, pokemonImage);
        int pokemonHP = getPokemonHPFromImg(ocr, pokemonImage);
        int pokemonCP = getPokemonCPFromImg(ocr, pokemonImage);

        return new SinglePokemon(estimatedPokemonLevel, pokemonName, candyName, pokemonHP, pokemonCP);
    }
}
