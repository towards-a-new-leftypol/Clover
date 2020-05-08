/*
 * Kuroba - *chan browser https://github.com/Adamantcheese/Kuroba/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.adamantcheese.chan.ui.theme;

import android.graphics.Typeface;

import androidx.annotation.NonNull;

import com.github.adamantcheese.chan.R;
import com.github.adamantcheese.chan.core.site.parser.PostParser;
import com.github.adamantcheese.chan.utils.StringUtils;

import static com.github.adamantcheese.chan.utils.AndroidUtils.getAttrColor;

/**
 * A Theme object, a wrapper around a Android theme<br>
 * Used for setting the toolbar color, and passed around {@link PostParser} to give spans their correct colors.<br>
 * Technically the parser should not do UI, but it is important that spans do not get created on a UI thread for performance.
 */
public class Theme {
    /**
     * The display name of the theme
     */
    public final String name;
    /**
     * The style resource id associated with the theme
     */
    public final int resValue;
    /**
     * This is the main color for the theme, use primaryColorStyleId from it to retrieve R.attr.colorPrimary
     */
    public MaterialColorStyle primaryColor;
    /**
     * This is the color for any accented items (FABs, etc.); use accentStyleId from it to retrieve R.attr.colorAccent
     */
    public MaterialColorStyle accentColor;

    private MaterialColorStyle defaultPrimary;
    private MaterialColorStyle defaultAccent;

    public Typeface mainFont = ROBOTO_MEDIUM;
    public Typeface altFont = ROBOTO_CONDENSED;
    public boolean altFontIsMain = false;
    public boolean isLightTheme;

    // Span colors, kept here for performance reasons
    public int subjectColor;
    public int nameColor;
    public int detailsColor;

    private static final Typeface ROBOTO_MEDIUM = Typeface.create("sans-serif-medium", Typeface.NORMAL);
    private static final Typeface ROBOTO_CONDENSED = Typeface.create("sans-serif-condensed", Typeface.NORMAL);

    public Theme(
            String displayName,
            int resValue,
            MaterialColorStyle primaryColor,
            MaterialColorStyle accentColor,
            boolean lightTheme
    ) {
        this.name = displayName;
        this.resValue = resValue;
        this.primaryColor = primaryColor;
        defaultPrimary = primaryColor;
        this.accentColor = accentColor;
        defaultAccent = accentColor;
        isLightTheme = lightTheme;

        // Span color setup
        subjectColor = getAttrColor(this, R.attr.post_subject_color);
        nameColor = getAttrColor(this, R.attr.post_name_color);
        detailsColor = getAttrColor(this, R.attr.post_details_color);
    }

    public Theme(
            String displayName,
            int resValue,
            MaterialColorStyle primaryColor,
            MaterialColorStyle accentColor,
            Typeface mainFont,
            Typeface altFont,
            boolean lightTheme
    ) {
        this(displayName, resValue, primaryColor, accentColor, lightTheme);
        this.mainFont = mainFont;
        this.altFont = altFont;
    }

    public void reset() {
        primaryColor = defaultPrimary;
        accentColor = defaultAccent;
    }

    @NonNull
    @Override
    public String toString() {
        return name + "," + primaryColor.name() + "," + accentColor.name();
    }

    public enum MaterialColorStyle {
        // Use UPPER_CASE_SNAKE_CASE for any new items added to this enum; settings saving and display names depend on it
        RED(R.style.PrimaryRed, R.style.AccentRed),
        PINK(R.style.PrimaryPink, R.style.AccentPink),
        PURPLE(R.style.PrimaryPurple, R.style.AccentPurple),
        DEEP_PURPLE(R.style.PrimaryDeepPurple, R.style.AccentDeepPurple),
        INDIGO(R.style.PrimaryIndigo, R.style.AccentIndigo),
        BLUE(R.style.PrimaryBlue, R.style.AccentBlue),
        LIGHT_BLUE(R.style.PrimaryLightBlue, R.style.AccentLightBlue),
        CYAN(R.style.PrimaryCyan, R.style.AccentCyan),
        TEAL(R.style.PrimaryTeal, R.style.AccentTeal),
        GREEN(R.style.PrimaryGreen, R.style.AccentGreen),
        LIGHT_GREEN(R.style.PrimaryLightGreen, R.style.AccentLightGreen),
        LIME(R.style.PrimaryLime, R.style.AccentLime),
        YELLOW(R.style.PrimaryYellow, R.style.AccentYellow),
        AMBER(R.style.PrimaryAmber, R.style.AccentAmber),
        ORANGE(R.style.PrimaryOrange, R.style.AccentOrange),
        DEEP_ORANGE(R.style.PrimaryDeepOrange, R.style.AccentDeepOrange),
        BROWN(R.style.PrimaryBrown, R.style.AccentBrown),
        GREY(R.style.PrimaryGrey, R.style.AccentGrey),
        BLUE_GREY(R.style.PrimaryBlueGrey, R.style.AccentBlueGrey),

        DARK(R.style.PrimaryDark, R.style.AccentDark),
        BLACK(R.style.PrimaryBlack, R.style.AccentBlack);

        /**
         * This style contains colorPrimary and colorVariant
         */
        public final int primaryColorStyleId;
        /**
         * This style containscolorAccent
         */
        public final int accentStyleId;

        MaterialColorStyle(int primaryColorStyleId, int accentStyleId) {
            this.primaryColorStyleId = primaryColorStyleId;
            this.accentStyleId = accentStyleId;
        }

        public String prettyName() {
            return StringUtils.caseAndSpace(name(), "_");
        }
    }
}
