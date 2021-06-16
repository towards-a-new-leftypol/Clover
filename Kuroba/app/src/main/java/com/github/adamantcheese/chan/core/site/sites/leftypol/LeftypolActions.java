package com.github.adamantcheese.chan.core.site.sites.leftypol;

import com.github.adamantcheese.chan.core.model.orm.Loadable;
import com.github.adamantcheese.chan.core.net.NetUtils;
import com.github.adamantcheese.chan.core.site.SiteAuthentication;
import com.github.adamantcheese.chan.core.site.common.CommonSite;
import com.github.adamantcheese.chan.core.site.common.MultipartHttpCall;
import com.github.adamantcheese.chan.core.site.common.vichan.VichanActions;
import com.github.adamantcheese.chan.core.site.http.HttpCall;
import com.github.adamantcheese.chan.core.site.http.ReplyResponse;
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
    public void setupPost(Loadable loadable, MultipartHttpCall call) {
        super.setupPost(loadable, call);

        call.parameter("user_flag", loadable.draft.flag);
        if (loadable.draft.captchaResponse != "" && loadable.draft.captchaResponse != null) {
            call.parameter("captcha", loadable.draft.captchaResponse);
        }
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

    @Override
    public void handlePost(ReplyResponse replyResponse, Response response, String result) {
        super.handlePost(replyResponse, response, result);

        // On a success server response, mark the post as posted
        if (response.isSuccessful()) {
            replyResponse.posted = true;
        }
    }

    @Override
    public boolean postRequiresAuthentication() {
        return true;
    }

    @Override
    public SiteAuthentication postAuthenticate() {
        return SiteAuthentication.fromSecurimage(this.rootUrl + "captcha.php");
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
