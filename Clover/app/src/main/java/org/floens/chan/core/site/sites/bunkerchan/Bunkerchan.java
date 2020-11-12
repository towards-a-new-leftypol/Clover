package org.floens.chan.core.site.sites.bunkerchan;

import androidx.annotation.Nullable;

import org.floens.chan.core.model.Post;
import org.floens.chan.core.model.orm.Board;
import org.floens.chan.core.model.orm.Loadable;
import org.floens.chan.core.site.Site;
import org.floens.chan.core.site.SiteAuthentication;
import org.floens.chan.core.site.SiteIcon;
import org.floens.chan.core.site.common.CommonSite;
import org.floens.chan.core.site.common.MultipartHttpCall;
import org.floens.chan.core.site.common.vichan.VichanActions;
import org.floens.chan.core.site.common.vichan.VichanApi;
import org.floens.chan.core.site.common.vichan.VichanCommentParser;
import org.floens.chan.core.site.common.vichan.VichanEndpoints;
import org.floens.chan.core.site.http.DeleteRequest;
import org.floens.chan.core.site.http.Reply;
import org.floens.chan.core.site.parser.CommentParser;

import java.util.Map;

import okhttp3.HttpUrl;

public class Bunkerchan extends CommonSite {
    private static final String ROOT_URL = "https://bunkerchan.xyz";

    public static final CommonSiteUrlHandler URL_HANDLER = new CommonSiteUrlHandler() {
        @Override
        public Class<? extends Site> getSiteClass() {
            return Bunkerchan.class;
        }

        @Override
        public HttpUrl getUrl() {
            return HttpUrl.parse(ROOT_URL);
        }

        @Override
        public String[] getNames() {
            return new String[]{"Bunkerchan"};
        }

        @Override
        public String desktopUrl(Loadable loadable, @Nullable Post post) {
            if (loadable.isCatalogMode()) {
                return getUrl()
                        .newBuilder()
                        .addPathSegment(loadable.boardCode)
                        .addPathSegment("catalog.json")
                        .toString();
            } else if (loadable.isThreadMode()) {
                return getUrl().newBuilder()
                        .addPathSegment(loadable.boardCode)
                        .addPathSegment("res")
                        .addPathSegment(String.valueOf(loadable.no) + ".json")
                        .toString();
            } else {
                return getUrl().toString();
            }
        }
    };

    @Override
    public void setup() {
        setName("Bunkerchan");
        setIcon(SiteIcon.fromFavicon(HttpUrl.parse(ROOT_URL + "/.static/favicon.ico")));
        setBoardsType(BoardsType.STATIC);
        setResolvable(URL_HANDLER);

        setBoards(
                Board.fromSiteNameCode(this, "leftypol", "leftypol"),
                Board.fromSiteNameCode(this, "b", "b"),
                Board.fromSiteNameCode(this, "GET", "GET"),
                Board.fromSiteNameCode(this, "hobby", "hobby"),
                Board.fromSiteNameCode(this, "gulag", "gulag"),
                Board.fromSiteNameCode(this, "games", "games"),
                Board.fromSiteNameCode(this, "edu", "edu"),
                Board.fromSiteNameCode(this, "anime", "anime"),
                Board.fromSiteNameCode(this, "tech", "tech"),
                Board.fromSiteNameCode(this, "ref", "ref")
        );

        setConfig(new CommonConfig() {
            @Override
            public boolean feature(Feature feature) {
                return false;
                       // TODO: Implement these
                       // feature == Feature.POSTING ||
                       // feature == Feature.POST_DELETE
                       // feature == Feature.POST_REPORT
                       // feature == Feature.LOGIN;
            }
        });

        setEndpoints(new BunkerchanEndpoints(this, this.ROOT_URL));

        // TODO: Implement
        setActions(new CommonActions(this) {
        });

        setApi(new BunkerchanApi(this));

        setParser(new BunkerchanCommentParser());
    }
}
