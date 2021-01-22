package com.github.adamantcheese.chan.ui.controller;

import android.content.Context;
import android.webkit.WebView;

import com.github.adamantcheese.chan.controller.Controller;
import com.github.adamantcheese.chan.core.model.orm.Loadable;

public class ChallengeController extends Controller {
    protected Loadable behindChallenge;

    public ChallengeController(Context context, Loadable behindChallenge) {
        super(context);
        this.behindChallenge = behindChallenge;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        WebView web = new WebView(context);

        web.loadUrl(behindChallenge.desktopUrl());
        web.getSettings().setJavaScriptEnabled(true);
        this.view = web;
    }
}
