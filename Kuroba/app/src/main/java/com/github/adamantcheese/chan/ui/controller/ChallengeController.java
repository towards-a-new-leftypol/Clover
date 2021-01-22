package com.github.adamantcheese.chan.ui.controller;

import android.content.Context;
import android.webkit.WebView;

import com.github.adamantcheese.chan.controller.Controller;
import com.github.adamantcheese.chan.core.di.NetModule;
import com.github.adamantcheese.chan.core.model.orm.Loadable;

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

        web = new WebView(context);
        web.loadUrl(behindChallenge.desktopUrl());
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setUserAgentString(NetModule.USER_AGENT);

        this.view = web;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.web.destroy();
    }
}
