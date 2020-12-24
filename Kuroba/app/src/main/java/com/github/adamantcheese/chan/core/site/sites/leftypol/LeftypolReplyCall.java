package com.github.adamantcheese.chan.core.site.sites.leftypol;

import androidx.annotation.Nullable;

import com.github.adamantcheese.chan.core.model.orm.Loadable;
import com.github.adamantcheese.chan.core.site.Site;
import com.github.adamantcheese.chan.core.site.common.MultipartHttpCall;
import com.github.adamantcheese.chan.core.site.http.ProgressRequestBody;

import okhttp3.HttpUrl;
import okhttp3.Request;

public abstract class LeftypolReplyCall extends MultipartHttpCall {
    private final Loadable loadable;
    private final HttpUrl siteUrl;

    public LeftypolReplyCall(Site site, Loadable loadable, HttpUrl siteUrl) {
        super(site);
        this.loadable = loadable;
        this.siteUrl = siteUrl;
    }

    @Override
    public void setup(Request.Builder requestBuilder, @Nullable ProgressRequestBody.ProgressRequestListener progressListener) {
        super.setup(requestBuilder, progressListener);

        String referer = siteUrl.newBuilder()
                .addPathSegment(loadable.boardCode)
                .addPathSegment("res")
                .addPathSegment(loadable.id + ".html")
                .build()
                .toString();

        requestBuilder.header("Referer", referer);
    }
}
