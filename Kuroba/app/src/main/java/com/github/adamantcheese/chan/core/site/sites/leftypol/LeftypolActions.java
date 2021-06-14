package com.github.adamantcheese.chan.core.site.sites.leftypol;

import com.github.adamantcheese.chan.Chan;
import com.github.adamantcheese.chan.core.di.NetModule;
import com.github.adamantcheese.chan.core.model.orm.Board;
import com.github.adamantcheese.chan.core.model.orm.Loadable;
import com.github.adamantcheese.chan.core.net.NetUtils;
import com.github.adamantcheese.chan.core.site.SiteAuthentication;
import com.github.adamantcheese.chan.core.site.common.CommonSite;
import com.github.adamantcheese.chan.core.site.common.MultipartHttpCall;
import com.github.adamantcheese.chan.core.site.common.vichan.VichanActions;
import com.github.adamantcheese.chan.core.site.http.HttpCall;
import com.github.adamantcheese.chan.core.site.http.ReplyResponse;
import com.github.adamantcheese.chan.ui.layout.ReplyLayout;
import com.github.adamantcheese.chan.utils.BackgroundUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
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
    public boolean postRequiresAuthentication() {
        return true;
    }

    @Override
    public SiteAuthentication postAuthenticate() {
        return SiteAuthentication.fromSecurimage(this.rootUrl + "captcha.php");
    }

    @Override
    public Future<List<ReplyLayout.Flag>> flags(Board b) {
        FutureTask<List<ReplyLayout.Flag>> futureTask = new FutureTask<>(() -> {
            // Build a request to "/status.php"
            OkHttpClient.Builder cb = Chan.instance(NetModule.OkHttpClientWithUtils.class).newBuilder();
            Request.Builder rb = new Request.Builder().url(this.rootUrl + "status.php");
            Call call = cb.build().newCall(rb.build());

            // Send the request, check if the captcha is enabled
            Response r = call.execute();
            if (r.isSuccessful()) {
                JSONObject json = new JSONObject(r.body().string());
                r.body().close();
                JSONObject flags = json.getJSONObject("flags");
                ArrayList<ReplyLayout.Flag> result = new ArrayList<>();

                for (Iterator<String> it = flags.keys(); it.hasNext(); ) {
                    String flagCode = it.next();
                    String flagName = flags.getString(flagCode);

                    Map<String, String> args = new HashMap<>();
                    args.put("country_code", flagCode);

                    result.add(new ReplyLayout.Flag(
                            flagName,
                            flagCode,
                            site.endpoints().icon("country", args)
                    ));
                }

                return result;
            } else {
                r.body().close();
                throw new Exception(r.message());
            }
        });
        BackgroundUtils.runOnBackgroundThread(() -> futureTask.run());
        return futureTask;
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
