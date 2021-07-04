package com.github.adamantcheese.chan.core.site.sites.leftypol;

import com.github.adamantcheese.chan.core.model.Post;
import com.github.adamantcheese.chan.core.site.common.CommonSite;
import com.github.adamantcheese.chan.core.site.common.vichan.VichanEndpoints;
import com.github.adamantcheese.chan.utils.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import okhttp3.HttpUrl;

public class LeftypolEndpoints extends VichanEndpoints {
    public LeftypolEndpoints(CommonSite commonSite, String rootUrl, String sysUrl) {
        super(commonSite, rootUrl, sysUrl);
    }

    @Override
    public HttpUrl imageUrl(Post.Builder post, Map<String, String> arg) {
        return root.builder().s(arg.get("file_path")).url();
    }

    @Override
    public HttpUrl thumbnailUrl(Post.Builder post, boolean spoiler, Map<String, String> arg) {
        String thumb = arg.get("thumb_path");

        if (spoiler) {
            if (post.board.customSpoilers > 0) {
                return root.builder()
                    .s("static")
                    .s("spoiler_alunya.png")
                    .url();
            }
            return root.builder()
                    .s("static")
                    .s("spoiler.png")
                    .url();
        }

        return root.builder().s(thumb).url();
    }

    @Override
    public HttpUrl report(Post post) {
        return root.builder()
                .s("report.php")
                .url
                .addQueryParameter("post", "delete_" + post.no)
                .addQueryParameter("board", post.boardCode)
                .build();
    }

    @Override
    public HttpUrl boards() {
        return root.builder().s("status.php").url();
    }
}
