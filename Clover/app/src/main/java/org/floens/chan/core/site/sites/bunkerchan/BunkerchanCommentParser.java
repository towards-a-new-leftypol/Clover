package org.floens.chan.core.site.sites.bunkerchan;

import android.text.SpannableString;
import android.text.TextUtils;

import org.floens.chan.core.model.Post;
import org.floens.chan.core.model.PostLinkable;
import org.floens.chan.core.site.parser.CommentParser;
import org.floens.chan.core.site.parser.PostParser;
import org.floens.chan.core.site.parser.StyleRule;
import org.floens.chan.ui.theme.Theme;
import org.floens.chan.utils.AndroidUtils;
import org.floens.chan.utils.Logger;
import org.jsoup.nodes.Element;

import java.util.regex.Pattern;

public class BunkerchanCommentParser extends CommentParser {
    public BunkerchanCommentParser() {
        addDefaultRules();

        // TODO: Board links don't work (i.e ">>>/tech/")
        rule(StyleRule
                .tagRule("a")
                .cssClass("quoteLink")
                .blockElement(true)
                .action(this::handleAnchor));

        rule(StyleRule
                .tagRule("span")
                .cssClass("redText")
                .bold()
                .size(AndroidUtils.sp(18))
                .color(StyleRule.Color.INLINE_QUOTE));

        rule(StyleRule
                .tagRule("span")
                .cssClass("greenText")
                .blockElement(true)
                .color(StyleRule.Color.INLINE_QUOTE));

        rule(StyleRule
                .tagRule("span")
                .cssClass("orangeText")
                .blockElement(true)
                .color(StyleRule.Color.QUOTE));

        setQuotePattern(Pattern.compile(".*#(\\d+)"));
        setFullQuotePattern(Pattern.compile("/(\\w+)/\\w+/(\\d+)\\.html#(\\d+)"));
    }
}
