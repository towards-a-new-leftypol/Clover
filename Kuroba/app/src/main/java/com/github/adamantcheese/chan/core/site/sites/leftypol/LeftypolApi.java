package com.github.adamantcheese.chan.core.site.sites.leftypol;

import android.util.JsonReader;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.github.adamantcheese.chan.core.model.Post;
import com.github.adamantcheese.chan.core.model.PostHttpIcon;
import com.github.adamantcheese.chan.core.model.PostImage;
import com.github.adamantcheese.chan.core.site.SiteEndpoints;
import com.github.adamantcheese.chan.core.site.common.CommonSite;
import com.github.adamantcheese.chan.core.site.common.vichan.VichanApi;
import com.github.adamantcheese.chan.core.site.parser.ChanReaderProcessingQueue;
import com.github.adamantcheese.chan.utils.Logger;

import org.jsoup.parser.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.HttpUrl;

import static com.github.adamantcheese.chan.core.site.SiteEndpoints.makeArgument;

public class LeftypolApi extends VichanApi {

    public LeftypolApi(CommonSite commonSite) {
        super(commonSite);
    }

    @NonNull
    @Override
    protected Pair<Integer, Long> readPostObjectWithReturn(JsonReader reader, ChanReaderProcessingQueue queue)
            throws Exception {
        Post.Builder builder = new Post.Builder();
        builder.board(queue.loadable.board);

        SiteEndpoints endpoints = queue.loadable.site.endpoints();

        // Comment + messages
        String comment = null;
        String banMessage = null;
        String warningMessage = null;

        // File
        String fileId = null;
        String fileExt = null;
        int fileWidth = 0;
        int fileHeight = 0;
        long fileSize = 0;
        boolean fileSpoiler = false;
        String fileName = null;
        String fileHash = null;

        List<PostImage> files = new ArrayList<>();

        // Country flag
        String countryCode = null;
        String trollCountryCode = null;
        String countryName = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();

            switch (key) {
                case "no":
                    builder.no(reader.nextInt());
                    break;
                case "sub":
                    builder.subject(reader.nextString());
                    break;
                case "name":
                    builder.name(reader.nextString());
                    break;
                case "com":
                    comment = reader.nextString();
                    break;
                case "warning_msg":
                    warningMessage = reader.nextString();
                    break;
                case "ban_msg":
                    banMessage = reader.nextString();
                    break;
                case "tim":
                    fileId = reader.nextString();
                    break;
                case "time":
                    builder.setUnixTimestampSeconds(reader.nextLong());
                    break;
                case "ext":
                    fileExt = reader.nextString().replace(".", "");
                    break;
                case "w":
                    fileWidth = reader.nextInt();
                    break;
                case "h":
                    fileHeight = reader.nextInt();
                    break;
                case "fsize":
                    fileSize = reader.nextLong();
                    break;
                case "filename":
                    fileName = reader.nextString();
                    break;
                case "trip":
                    builder.tripcode(reader.nextString());
                    break;
                case "country":
                    countryCode = reader.nextString();
                    break;
                case "troll_country":
                    trollCountryCode = reader.nextString();
                    break;
                case "country_name":
                    countryName = reader.nextString();
                    break;
                case "spoiler":
                    fileSpoiler = reader.nextInt() == 1;
                    break;
                case "resto":
                    int opId = reader.nextInt();
                    builder.op(opId == 0);
                    builder.opId(opId);
                    break;
                case "sticky":
                    builder.sticky(reader.nextInt() == 1);
                    break;
                case "locked":
                    builder.closed(reader.nextInt() == 1);
                    break;
                case "archived":
                    builder.archived(reader.nextInt() == 1);
                    break;
                case "replies":
                    builder.replies(reader.nextInt());
                    break;
                case "images":
                    builder.images(reader.nextInt());
                    break;
                case "unique_ips":
                    builder.uniqueIps(reader.nextInt());
                    break;
                case "last_modified":
                    builder.lastModified(reader.nextLong());
                    break;
                case "id":
                    builder.posterId(reader.nextString());
                    break;
                case "capcode":
                    builder.moderatorCapcode(reader.nextString());
                    break;
                case "extra_files":
                    reader.beginArray();

                    while (reader.hasNext()) {
                        PostImage postImage = readPostImage(reader, builder, endpoints);
                        if (postImage != null) {
                            files.add(postImage);
                        }
                    }

                    reader.endArray();
                    break;
                case "md5":
                    fileHash = reader.nextString();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        // Build the comment containing ban/warning messages
        if (comment == null) {
            comment = "";
        }
        if (warningMessage != null) {
            comment += "<br/><br/>" +
                    "<span class=\"big-quote\">⚠ </span>" +
                    "<span class=\"warn-message\">" + warningMessage + "</span>";
        }
        if (banMessage != null) {
            comment += "<br/><br/>" +
                    "<span class=\"big-red\">⚠ </span>" +
                    "<span class=\"ban-message\">" + banMessage + "</span>";
        }
        builder.comment(comment);

        // The file from between the other values.
        if (fileId != null && fileName != null && fileExt != null) {
            Map<String, String> args = makeArgument("tim", fileId, "ext", fileExt);
            PostImage image = new PostImage.Builder().serverFilename(fileId)
                    .thumbnailUrl(endpoints.thumbnailUrl(builder, false, args))
                    .spoilerThumbnailUrl(endpoints.thumbnailUrl(builder, true, args))
                    .imageUrl(endpoints.imageUrl(builder, args))
                    .filename(Parser.unescapeEntities(fileName, false))
                    .extension(fileExt)
                    .imageWidth(fileWidth)
                    .imageHeight(fileHeight)
                    .spoiler(fileSpoiler)
                    .size(fileSize)
                    .fileHash(fileHash, true)
                    .build();
            // Insert it at the beginning.
            files.add(0, image);
        }

        builder.images(files);

        if (builder.op) {
            // Update OP fields later on the main thread
            queue.setOp(builder.clone());
        }

        Post cached = queue.getCachedPost(builder.no);
        if (cached != null) {
            // Id is known, use the cached post object.
            queue.addForReuse(cached);
            return new Pair<>(builder.no, builder.lastModified); // this return is only used for pages!
        }

        if (countryCode != null && countryName != null) {
            HttpUrl countryUrl = endpoints.icon("country", makeArgument("country_code", countryCode));
            builder.addHttpIcon(new PostHttpIcon(countryUrl, countryName + "/" + countryCode));
        }

        if (trollCountryCode != null && countryName != null) {
            HttpUrl countryUrl = endpoints.icon("troll_country", makeArgument("troll_country_code", trollCountryCode));
            builder.addHttpIcon(new PostHttpIcon(countryUrl, countryName + "/t_" + trollCountryCode));
        }

        queue.addForParse(builder);
        return new Pair<>(builder.no, builder.lastModified); // this return is only used for pages!
    }
}
