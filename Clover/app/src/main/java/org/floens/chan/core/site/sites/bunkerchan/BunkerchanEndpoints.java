package org.floens.chan.core.site.sites.bunkerchan;

import org.floens.chan.core.model.Post;
import org.floens.chan.core.model.orm.Board;
import org.floens.chan.core.model.orm.Loadable;
import org.floens.chan.core.site.common.CommonSite;

import java.util.Locale;
import java.util.Map;

import okhttp3.HttpUrl;

public class BunkerchanEndpoints extends CommonSite.CommonEndpoints {
    protected final CommonSite.SimpleHttpUrl root;

    public BunkerchanEndpoints(CommonSite commonSite, String rootUrl) {
        super(commonSite);
        root = new CommonSite.SimpleHttpUrl(rootUrl);
    }

    @Override
    public HttpUrl catalog(Board board) {
        return root
               .builder()
               .s(board.code)
               .s("catalog.json")
               .url();
    }

    @Override
    public HttpUrl thread(Board board, Loadable loadable) {
        return root
               .builder()
               .s(board.code)
               .s("res")
               .s(loadable.no + ".json")
               .url();
    }

    @Override
    public HttpUrl thumbnailUrl(Post.Builder post, boolean spoiler, Map<String, String> arg) {
        // TODO: Actually implement
        return this.imageUrl(post, arg);
    }

    @Override
    public HttpUrl imageUrl(Post.Builder post, Map<String, String> arg) {
        return root.url().resolve(arg.get("path"));
    }

    @Override
    public HttpUrl icon(Post.Builder post, String icon, Map<String, String> arg) {
        return root.url().resolve(arg.get("path"));
    }

    // TODO: Implement
    // @Override
    // public HttpUrl reply(Loadable loadable) {

    // }

    // TODO: Implement
    // public HttpUrl delete(Post post) {
    //     return sys.builder().s("post.php").url();
    // }
}
