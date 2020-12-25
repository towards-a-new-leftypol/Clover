package com.github.adamantcheese.chan.core.site.sites.leftypol;

import com.github.adamantcheese.chan.core.model.orm.Loadable;
import com.github.adamantcheese.chan.core.net.NetUtils;
import com.github.adamantcheese.chan.core.site.common.CommonSite;
import com.github.adamantcheese.chan.core.site.common.MultipartHttpCall;
import com.github.adamantcheese.chan.core.site.common.vichan.VichanActions;
import com.github.adamantcheese.chan.core.site.http.HttpCall;
import com.github.adamantcheese.chan.core.site.http.ReplyResponse;
import com.github.adamantcheese.chan.core.site.sites.leftypol.LeftypolReplyCall;
import com.github.adamantcheese.chan.utils.BackgroundUtils;

import okhttp3.HttpUrl;
import okhttp3.Response;

public class LeftypolActions extends VichanActions {
    private final HttpUrl rootUrl;

    public LeftypolActions(CommonSite commonSite, String rootUrl) {
        super(commonSite);

        this.rootUrl = HttpUrl.parse(rootUrl);
    }

    @Override
    public void post(Loadable loadableWithDraft, PostListener postListener) {
        ReplyResponse replyResponse = new ReplyResponse(loadableWithDraft);

        MultipartHttpCall call = new LeftypolReplyCall(site, loadableWithDraft, rootUrl) {
            @Override
            public void process(Response response, String result) {
                handlePost(replyResponse, response, result);
            }
        };

        call.url(site.endpoints().reply(loadableWithDraft));

        if (requirePrepare()) {
            BackgroundUtils.runOnBackgroundThread(() -> {
                prepare(call, replyResponse);
                BackgroundUtils.runOnMainThread(() -> {
                    setupPost(loadableWithDraft, call);
                    makePostCall(call, replyResponse, postListener);
                });
            });
        } else {
            setupPost(loadableWithDraft, call);
            makePostCall(call, replyResponse, postListener);
        }
    }

    private void makePostCall(HttpCall call, ReplyResponse replyResponse, PostListener postListener) {
        NetUtils.makeHttpCall(call, new HttpCall.HttpCallback<HttpCall>() {
            @Override
            public void onHttpSuccess(HttpCall httpCall) {
                postListener.onPostComplete(replyResponse);
            }

            @Override
            public void onHttpFail(HttpCall httpCall, Exception e) {
                postListener.onPostError(e);
            }
        });
    }
}
