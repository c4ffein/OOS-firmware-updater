package fr.witchdoctors.c4ffein.oosfirmwareextractor;

import android.os.Parcel;
import android.os.Parcelable;

public class LevelAndName implements Parcelable {
    private int level;
    private String name;

    LevelAndName(int level, String name) {
        this.level = level;
        this.name = name;
    }

    public static final Creator<LevelAndName> CREATOR = new Creator<LevelAndName>() {
        @Override
        public LevelAndName createFromParcel(Parcel in) {
            return new LevelAndName(in);
        }

        @Override
        public LevelAndName[] newArray(int size) {
            return new LevelAndName[size];
        }
    };

    int getLevel() {
        return level;
    }

    String getName() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.level);
        dest.writeString(this.name);
    }

    private LevelAndName(Parcel in) {
        this.level = in.readInt();
        this.name = in.readString();
    }
}
