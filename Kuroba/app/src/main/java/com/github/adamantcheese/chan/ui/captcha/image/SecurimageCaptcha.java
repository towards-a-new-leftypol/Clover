/*
 * Kuroba - *chan browser https://github.com/Adamantcheese/Kuroba/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.adamantcheese.chan.ui.captcha.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.adamantcheese.chan.R;
import com.github.adamantcheese.chan.core.net.NetUtils;
import com.github.adamantcheese.chan.core.net.NetUtilsClasses;
import com.github.adamantcheese.chan.core.settings.ChanSettings;
import com.github.adamantcheese.chan.core.site.Site;
import com.github.adamantcheese.chan.core.site.SiteAuthentication;
import com.github.adamantcheese.chan.ui.captcha.AuthenticationLayoutCallback;
import com.github.adamantcheese.chan.ui.captcha.AuthenticationLayoutInterface;
import com.github.adamantcheese.chan.utils.BackgroundUtils;

import java.util.Random;

import okhttp3.HttpUrl;

import static com.github.adamantcheese.chan.core.site.SiteAuthentication.Type.SECURIMAGE;

public class SecurimageCaptcha
        extends FrameLayout
        implements AuthenticationLayoutInterface {
    private SiteAuthentication auth;

    private final ImageView captchaImage;
    private final EditText captchaAnswer;

    private final Random random = new Random();

    private AuthenticationLayoutCallback callback;

    private boolean isAutoReply;

    public SecurimageCaptcha(@NonNull Context context) {
        this(context, null, 0);
    }

    public SecurimageCaptcha(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SecurimageCaptcha(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        View view = inflate(context, R.layout.layout_captcha_securimage, this);

        if (ChanSettings.moveInputToBottom.get()) {
            LinearLayout topLevel = findViewById(R.id.layout_captcha_securimage_top_level);
            topLevel.setGravity(Gravity.BOTTOM);
        }
        captchaImage = view.findViewById(R.id.layout_captcha_securimage_image);
        captchaAnswer = view.findViewById(R.id.layout_captcha_securimage_input);
        Button captchaVerifyButton = view.findViewById(R.id.layout_captcha_securimage_verify_button);
        Button captchaReloadButton = view.findViewById(R.id.layout_captcha_securimage_reload_button);

        captchaVerifyButton.setOnClickListener(v -> sendAnswer());
        captchaReloadButton.setOnClickListener(v -> reset());
    }

    @Override
    public void initialize(Site site, AuthenticationLayoutCallback callback, boolean autoReply) {
        this.callback = callback;
        this.isAutoReply = autoReply;

        this.auth = site.actions().postAuthenticate();
        if (auth.type != SECURIMAGE) {
            callback.onFallbackToV1CaptchaView(isAutoReply);
            return;
        }
    }

    @Override
    public void reset() {
        String url = this.auth.baseUrl + "?" + Math.abs(random.nextInt());
        NetUtils.makeBitmapRequest(HttpUrl.parse(url), new NetUtilsClasses.BitmapResult() {
            @Override
            public void onBitmapFailure(@NonNull HttpUrl source, Exception e) {
                callback.onAuthenticationFailed(e);
            }

            @Override
            public void onBitmapSuccess(@NonNull HttpUrl source, @NonNull Bitmap bitmap) {
                BackgroundUtils.runOnMainThread(() -> {
                    captchaImage.setImageBitmap(bitmap);
                });
            }
        });
    }

    @Override
    public void hardReset() {
        this.reset();
    }

    private void sendAnswer() {
        // Securimage captchas are always "successful", since they don't actually make a request to
        // a separate endpoint, but rather the captcha answer gets sent together with the whole post
        // data
        this.callback.onAuthenticationComplete(
                this,
                null,
                captchaAnswer.getText().toString(),
                this.isAutoReply
        );
    }
}
