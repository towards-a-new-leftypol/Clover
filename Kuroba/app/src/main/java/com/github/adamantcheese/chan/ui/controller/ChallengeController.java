package com.github.adamantcheese.chan.ui.controller;

import android.content.Context;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.adamantcheese.chan.R;
import com.github.adamantcheese.chan.controller.Controller;
import com.github.adamantcheese.chan.core.di.NetModule;
import com.github.adamantcheese.chan.core.model.orm.Loadable;
import com.github.adamantcheese.chan.core.site.common.CommonSite;
import com.github.adamantcheese.chan.utils.AndroidUtils;

public class ChallengeController extends Controller {
    protected Loadable behindChallenge;

    private WebView web;

    public ChallengeController(Context context, Loadable behindChallenge) {
        super(context);
        this.behindChallenge = behindChallenge;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        navigation.title = AndroidUtils.getString(R.string.challenge_screen);

        web = new WebView(context);
        web.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });

        CommonSite.CommonCallModifier siteCallModifier = behindChallenge.site.callModifier();
        if (siteCallModifier != null) {
            siteCallModifier.modifyWebView(web);
        }

        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setUserAgentString(NetModule.USER_AGENT);
        web.loadUrl(behindChallenge.desktopUrl());

        this.view = web;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.web.destroy();
    }
}
