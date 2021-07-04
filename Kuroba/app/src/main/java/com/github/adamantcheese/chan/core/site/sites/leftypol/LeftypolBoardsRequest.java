package com.github.adamantcheese.chan.core.site.sites.leftypol;

import android.util.JsonReader;

import com.github.adamantcheese.chan.core.model.orm.Board;
import com.github.adamantcheese.chan.core.net.NetUtilsClasses;
import com.github.adamantcheese.chan.core.site.Site;
import com.github.adamantcheese.chan.core.site.common.CommonDataStructs;
import com.github.adamantcheese.chan.utils.Logger;

public class LeftypolBoardsRequest
        extends NetUtilsClasses.JSONProcessor<CommonDataStructs.Boards> {
    private final Site site;

    public LeftypolBoardsRequest(Site site) {
        this.site = site;
    }

    @Override
    public CommonDataStructs.Boards process(JsonReader reader) throws Exception {
        CommonDataStructs.Boards list = new CommonDataStructs.Boards();

        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            if (key.equals("boards")) {
                reader.beginArray();

                while (reader.hasNext()) {
                    Board board = readBoardEntry(reader);
                    if (board != null) {
                        list.add(board);
                    }
                }

                reader.endArray();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return list;
    }

    private Board readBoardEntry(JsonReader reader) throws Exception {
        reader.beginObject();

        Board board = new Board();
        board.siteId = site.id();
        board.site = site;

        board.spoilers = true;
        board.codeTags = true;

        while (reader.hasNext()) {
            String key = reader.nextName();

            switch (key) {
                case "name":
                    board.name = reader.nextString();
                    break;
                case "code":
                    board.code = reader.nextString();
                    break;
                case "description":
                    board.description = reader.nextString();
                    break;
                case "sfw":
                    board.workSafe = !reader.nextBoolean();
                    break;
                case "alternate_spoilers":
                    board.customSpoilers = reader.nextBoolean() ? 1 : -1;
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }

        reader.endObject();

        if (board.hasMissingInfo()) {
            Logger.d(this, "Board \"" + board.name + "\" has missing information. Discarding...");
            return null;
        }

        return board;
    }
}
