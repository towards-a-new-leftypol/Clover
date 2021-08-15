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
package com.github.adamantcheese.chan.core.site.sites.leftychan;

import com.github.adamantcheese.chan.core.model.orm.Board;
import com.github.adamantcheese.chan.core.model.orm.Loadable;
import com.github.adamantcheese.chan.core.site.Site;
import com.github.adamantcheese.chan.core.site.SiteIcon;
import com.github.adamantcheese.chan.core.site.common.CommonSite;

import java.util.List;

import okhttp3.HttpUrl;

public class Leftychan extends CommonSite {
    private static final String ROOT = "https://leftychan.net/";

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
            return new String[]{"Leftychan"};
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

        @Override
        public Loadable resolveLoadable(Site site, HttpUrl url) {
            List<String> parts = url.pathSegments();
            if (!parts.isEmpty()) {
                String boardCode = parts.get(0);
                Board board = site.board(boardCode);
                if (board != null) {
                    if (parts.size() < 3) {
                        // Board mode
                        return Loadable.forCatalog(board);
                    } else {
                        // Thread mode
                        int no;
                        try {
                            no = Integer.parseInt(parts.get(2).replace(".html", ""));
                        } catch (NumberFormatException ignored) {
                            no = -1;
                        }

                        int post = -1;
                        String fragment = url.fragment();
                        if (fragment != null) {
                            try {
                                post = Integer.parseInt(fragment);
                            } catch (NumberFormatException ignored) {
                            }
                        }

                        if (no >= 0) {
                            Loadable loadable = Loadable.forThread(board, no, "");
                            if (post >= 0) {
                                loadable.markedNo = post;
                            }

                            return loadable;
                        }
                    }
                }
            }

            return null;
        }
    };

    public Leftychan() {
        setName("Leftychan");
        setIcon(SiteIcon.fromFavicon(HttpUrl.parse(ROOT + "favicon.ico")));
    }

    @Override
    public void setup() {

        setBoardsType(BoardsType.DYNAMIC);
        setResolvable(URL_HANDLER);

        setConfig(new CommonConfig() {
            @Override
            public boolean siteFeature(SiteFeature siteFeature) {
                return  super.siteFeature(siteFeature) ||
                        siteFeature == SiteFeature.POSTING ||
                        siteFeature == SiteFeature.POST_DELETE ||
                        siteFeature == SiteFeature.POST_REPORT ||
                        siteFeature == SiteFeature.FLAG_LIST;
            }
        });

        setEndpoints(new LeftychanEndpoints(this, ROOT, ROOT));
        setActions(new LeftychanActions(this, ROOT));
        setApi(new LeftychanApi(this));
        setParser(new LeftychanCommentParser());
    }
}
