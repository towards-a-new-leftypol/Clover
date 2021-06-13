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
package com.github.adamantcheese.chan.core.site;

import com.github.adamantcheese.chan.core.model.InternalSiteArchive;
import com.github.adamantcheese.chan.core.model.orm.Board;
import com.github.adamantcheese.chan.core.model.orm.Loadable;
import com.github.adamantcheese.chan.core.site.common.CommonDataStructs.Boards;
import com.github.adamantcheese.chan.core.site.common.CommonDataStructs.ChanPages;
import com.github.adamantcheese.chan.core.site.http.DeleteRequest;
import com.github.adamantcheese.chan.core.site.http.DeleteResponse;
import com.github.adamantcheese.chan.core.site.http.LoginRequest;
import com.github.adamantcheese.chan.core.site.http.LoginResponse;
import com.github.adamantcheese.chan.core.site.http.ReplyResponse;
import com.github.adamantcheese.chan.ui.layout.ReplyLayout;

import java.util.List;
import java.util.concurrent.Future;

public interface SiteActions {
    void boards(BoardsListener boardsListener);

    void pages(Board board, PagesListener pagesListener);

    interface BoardsListener {
        void onBoardsReceived(Boards boards);
    }

    interface PagesListener {
        void onPagesReceived(Board b, ChanPages pages);
    }

    void post(Loadable loadableWithDraft, PostListener postListener);

    interface PostListener {
        void onPostComplete(ReplyResponse replyResponse);

        void onUploadingProgress(int percent);

        void onPostError(Exception exception);
    }

    boolean postRequiresAuthentication();

    /**
     * If {@link ReplyResponse#requireAuthentication} was {@code true}, or if
     * {@link #postRequiresAuthentication()} is {@code true}, get the authentication
     * required to post.
     * <p>
     * <p>Some sites know beforehand if you need to authenticate, some sites only report it
     * after posting. That's why there are two methods.</p>
     *
     * @return an {@link SiteAuthentication} model that describes the way to authenticate.
     */
    SiteAuthentication postAuthenticate();

    void delete(DeleteRequest deleteRequest, DeleteListener deleteListener);

    interface DeleteListener {
        void onDeleteComplete(DeleteResponse deleteResponse);

        void onDeleteError(Exception e);
    }

    void archive(Board board, ArchiveListener archiveListener);

    interface ArchiveListener {
        void onArchive(InternalSiteArchive internalSiteArchive);

        void onArchiveError();
    }

    void login(LoginRequest loginRequest, LoginListener loginListener);

    void logout();

    boolean isLoggedIn();

    LoginRequest getLoginDetails();

    interface LoginListener {
        void onLoginComplete(LoginResponse loginResponse);

        void onLoginError(Exception e);
    }

    /**
     * Fetches a list of flags available on a board
     * @return A Future that is not running. The code calling this function is responsible for starting the future
     */
    Future<List<ReplyLayout.Flag>> flags(Board b);
}
