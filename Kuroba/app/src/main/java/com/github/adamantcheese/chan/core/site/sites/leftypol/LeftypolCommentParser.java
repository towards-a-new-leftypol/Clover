package com.github.adamantcheese.chan.core.site.sites.leftypol;

import com.github.adamantcheese.chan.core.settings.ChanSettings;
import com.github.adamantcheese.chan.core.site.common.vichan.VichanCommentParser;
import com.github.adamantcheese.chan.core.site.parser.StyleRule;
import com.github.adamantcheese.chan.utils.AndroidUtils;

public class LeftypolCommentParser extends VichanCommentParser {

    public LeftypolCommentParser() {
        super();

        rule(StyleRule
                .tagRule("span")
                .cssClass("heading")
                .bold()
                .foregroundColor(StyleRule.ForegroundColor.RED)
                .size(AndroidUtils.sp(ChanSettings.fontSize.get() * 1.2f))
        );
    }
}
