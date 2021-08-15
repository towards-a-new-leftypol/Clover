package com.github.adamantcheese.chan.core.site.sites.leftychan;

import com.github.adamantcheese.chan.core.settings.ChanSettings;
import com.github.adamantcheese.chan.core.site.common.vichan.VichanCommentParser;
import com.github.adamantcheese.chan.core.site.parser.StyleRule;
import com.github.adamantcheese.chan.utils.AndroidUtils;

public class LeftychanCommentParser extends VichanCommentParser {

    public LeftychanCommentParser() {
        super();

        int bigFontSize = AndroidUtils.sp(ChanSettings.fontSize.get() * 1.2f);

        rule(StyleRule
                .tagRule("span")
                .cssClass("heading")
                .bold()
                .foregroundColor(StyleRule.ForegroundColor.RED)
                .size(bigFontSize)
        );

        rule(StyleRule
                .tagRule("span")
                .cssClass("underline")
                .underline()
        );

        rule(StyleRule
                .tagRule("span")
                .cssClass("strikethrough")
                .strikeThrough()
        );

        rule(StyleRule
                .tagRule("span")
                .cssClass("orangeQuote")
                .foregroundColor(StyleRule.ForegroundColor.QUOTE)
        );

        rule(StyleRule
                .tagRule("span")
                .cssClass("big-quote")
                .bold()
                .foregroundColor(StyleRule.ForegroundColor.QUOTE)
                .size(bigFontSize)
        );

        rule(StyleRule
                .tagRule("span")
                .cssClass("warn-message")
                .bold()
                .italic()
                .foregroundColor(StyleRule.ForegroundColor.QUOTE)
                .size(bigFontSize)
        );

        rule(StyleRule
                .tagRule("span")
                .cssClass("big-red")
                .bold()
                .foregroundColor(StyleRule.ForegroundColor.RED)
                .size(bigFontSize)
        );

        rule(StyleRule
                .tagRule("span")
                .cssClass("ban-message")
                .bold()
                .italic()
                .foregroundColor(StyleRule.ForegroundColor.RED)
                .size(bigFontSize)
        );
    }
}
