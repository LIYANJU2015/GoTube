package free.studio.tube.businessobjects;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

/**
 * Created by liyanju on 2018/6/12.
 */

public class TubeSearchSuggistion implements SearchSuggestion {

    private String suggistion;

    public TubeSearchSuggistion(String suggistion) {
        this.suggistion = suggistion;
    }

    public TubeSearchSuggistion(Parcel in) {
        suggistion = in.readString();
    }

    @Override
    public String getBody() {
        return suggistion;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(suggistion);
    }

    public static final Creator<TubeSearchSuggistion> CREATOR = new Creator<TubeSearchSuggistion>() {
        @Override
        public TubeSearchSuggistion createFromParcel(Parcel source) {
            return new TubeSearchSuggistion(source);
        }

        @Override
        public TubeSearchSuggistion[] newArray(int size) {
            return new TubeSearchSuggistion[size];
        }
    };
}
