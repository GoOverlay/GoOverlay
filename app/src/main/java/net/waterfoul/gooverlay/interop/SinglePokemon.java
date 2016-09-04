package net.waterfoul.gooverlay.interop;

import android.os.Parcel;
import android.os.Parcelable;

public class SinglePokemon implements Parcelable {
    private double estimatedPokemonLevel;
    private String pokemonName;
    private String candyName;
    private int pokemonHP;
    private int pokemonCP;

    public static final Parcelable.Creator<SinglePokemon> CREATOR =
        new Parcelable.Creator<SinglePokemon>() {
            public SinglePokemon createFromParcel(Parcel in) {
                return new SinglePokemon(in);
            }

            public SinglePokemon[] newArray(int size) {
                return new SinglePokemon[size];
            }
        };

    public SinglePokemon(Parcel in) {
        estimatedPokemonLevel = in.readDouble();
        pokemonName = in.readString();
        candyName = in.readString();
        pokemonHP = in.readInt();
        pokemonCP = in.readInt();
    }

    public SinglePokemon(
        double estimatedPokemonLevel,
        String pokemonName,
        String candyName,
        int pokemonHP,
        int pokemonCP
    ) {
        this.estimatedPokemonLevel = estimatedPokemonLevel;
        this.pokemonName = pokemonName;
        this.candyName = candyName;
        this.pokemonHP = pokemonHP;
        this.pokemonCP = pokemonCP;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(estimatedPokemonLevel);
        dest.writeString(pokemonName);
        dest.writeString(candyName);
        dest.writeInt(pokemonHP);
        dest.writeInt(pokemonCP);
    }

    public double getEstimatedPokemonLevel() {
        return estimatedPokemonLevel;
    }

    public String getPokemonName() {
        return pokemonName;
    }

    public String getCandyName() {
        return candyName;
    }

    public int getPokemonHP() {
        return pokemonHP;
    }

    public int getPokemonCP() {
        return pokemonCP;
    }

    public boolean isFailed() {
        //XXX replace by proper logic.
        //the default values for a failed scan, if all three fail, then probably scrolled down.
        return candyName.equals("") && pokemonHP == 10 && pokemonCP == 10;
    }
}
