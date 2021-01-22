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
package com.github.adamantcheese.chan.core.site.sites.leftypol;

import androidx.annotation.NonNull;

import com.github.adamantcheese.chan.core.model.orm.Board;
import com.github.adamantcheese.chan.core.model.orm.Loadable;
import com.github.adamantcheese.chan.core.site.SiteIcon;
import com.github.adamantcheese.chan.core.site.common.CommonSite;
import com.github.adamantcheese.chan.core.site.common.vichan.VichanApi;
import com.github.adamantcheese.chan.core.site.common.vichan.VichanCommentParser;
import com.github.adamantcheese.chan.core.site.common.vichan.VichanEndpoints;

import okhttp3.HttpUrl;

public class Leftypol extends CommonSite {
    private static final String ROOT = "https://leftypol.org/";

    public static final CommonSiteUrlHandler URL_HANDLER = new CommonSiteUrlHandler() {
        @Override
        public HttpUrl getUrl() {
            return HttpUrl.parse(ROOT);
        }

        @Override
        public String[] getMediaHosts() {
            return new String[]{ROOT};
        }

        @Override
        public String[] getNames() {
            return new String[]{"Leftypol"};
        }

        @Override
        public String desktopUrl(Loadable loadable, int postNo) {
            if (loadable.isCatalogMode()) {
                return getUrl().newBuilder().addPathSegment(loadable.boardCode).toString();
            } else if (loadable.isThreadMode()) {
                return getUrl().newBuilder()
                        .addPathSegment(loadable.boardCode)
                        .addPathSegment("res")
                        .addPathSegment(loadable.no + ".html")
                        .fragment(postNo + "")
                        .toString();
            } else {
                return getUrl().toString();
            }
        }
    };

    public Leftypol() {
        setName("Leftypol");
        setIcon(SiteIcon.fromFavicon(HttpUrl.parse(ROOT + "favicon.ico")));
    }

    @Override
    public void setup() {

        setBoards(
                Board.fromSiteNameCode(this, "Leftist Politically Incorrect", "leftypol"),
                Board.fromSiteNameCode(this, "Overboard", "overboard"),
                Board.fromSiteNameCode(this, "Random", "b"),
                Board.fromSiteNameCode(this, "Hobby", "hobby"),
                Board.fromSiteNameCode(this, "Technology", "tech"),
                Board.fromSiteNameCode(this, "Education", "edu"),
                Board.fromSiteNameCode(this, "Games", "games"),
                Board.fromSiteNameCode(this, "anime", "anime"),
                Board.fromSiteNameCode(this, "meta", "meta"),
                Board.fromSiteNameCode(this, "dead", "dead"),
                Board.fromSiteNameCode(this, "gulag", "gulag")
        );

        setResolvable(URL_HANDLER);

        setConfig(new CommonConfig() {
            @Override
            public boolean siteFeature(SiteFeature siteFeature) {
                return  super.siteFeature(siteFeature) ||
                        siteFeature == SiteFeature.POSTING ||
                        siteFeature == SiteFeature.POST_DELETE ||
                        siteFeature == SiteFeature.POST_REPORT;
            }
        });

        setEndpoints(new LeftypolEndpoints(this, ROOT, ROOT));
        setActions(new LeftypolActions(this, ROOT));
        setApi(new LeftypolApi(this));
        setParser(new LeftypolCommentParser());
    }

    @NonNull
    @Override
    public ChunkDownloaderSiteProperties getChunkDownloaderSiteProperties() {
        return new ChunkDownloaderSiteProperties(Integer.MAX_VALUE, true);
    }
}
