package com.github.adamantcheese.chan.core.site.sites.leftypol;

import com.github.adamantcheese.chan.core.model.orm.Loadable;
import com.github.adamantcheese.chan.core.net.NetUtils;
import com.github.adamantcheese.chan.core.net.NetUtilsClasses;
import com.github.adamantcheese.chan.core.site.SiteAuthentication;
import com.github.adamantcheese.chan.core.site.common.CommonDataStructs;
import com.github.adamantcheese.chan.core.site.common.CommonSite;
import com.github.adamantcheese.chan.core.site.common.MultipartHttpCall;
import com.github.adamantcheese.chan.core.site.common.vichan.VichanActions;
import com.github.adamantcheese.chan.core.site.http.HttpCall;
import com.github.adamantcheese.chan.core.site.http.ReplyResponse;
import com.github.adamantcheese.chan.core.site.sites.chan4.Chan4;
import com.github.adamantcheese.chan.utils.BackgroundUtils;
import com.github.adamantcheese.chan.utils.Logger;

import okhttp3.HttpUrl;
import okhttp3.Response;

public class LeftypolActions extends VichanActions {
    private final HttpUrl rootUrl;

    public LeftypolActions(CommonSite commonSite, String rootUrl) {
        super(commonSite);

        this.rootUrl = HttpUrl.parse(rootUrl);
    }

    @Override
    public void boards(BoardsListener listener) {
        NetUtils.makeJsonRequest(this.site.endpoints().boards(), new NetUtilsClasses.ResponseResult<CommonDataStructs.Boards>() {
            @Override
            public void onFailure(Exception e) {
                Logger.e(this, "Failed to get boards from server", e);
                listener.onBoardsReceived(new CommonDataStructs.Boards());
            }

            @Override
            public void onSuccess(CommonDataStructs.Boards result) {
                listener.onBoardsReceived(result);
            }
        }, new LeftypolBoardsRequest(this.site));
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
    public boolean postRequiresAuthentication() {
        return false;
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
