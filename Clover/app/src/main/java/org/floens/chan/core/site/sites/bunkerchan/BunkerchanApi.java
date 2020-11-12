package org.floens.chan.core.site.sites.bunkerchan;

import android.util.JsonReader;
import android.util.JsonToken;
import android.webkit.MimeTypeMap;

import com.google.gson.internal.bind.util.ISO8601Utils;

import org.floens.chan.core.model.Post;
import org.floens.chan.core.model.PostHttpIcon;
import org.floens.chan.core.model.PostImage;
import org.floens.chan.core.model.orm.Loadable;
import org.floens.chan.core.site.SiteEndpoints;
import org.floens.chan.core.site.common.CommonSite;
import org.floens.chan.core.site.parser.ChanReaderProcessingQueue;
import org.floens.chan.utils.Logger;

import java.io.IOException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import okhttp3.HttpUrl;

import static org.floens.chan.core.site.SiteEndpoints.makeArgument;

public class BunkerchanApi extends CommonSite.CommonApi {

    public BunkerchanApi(CommonSite commonSite) {
        super(commonSite);
    }

    @Override
    public void loadThread(JsonReader reader, ChanReaderProcessingQueue queue) throws Exception {

        reader.beginObject();

        // TODO: We will miss the OP this way
        while (reader.hasNext()) {
            String key = reader.nextName();
            if (key.equals("posts")) {
                reader.beginArray();
                while (reader.hasNext()) {
                    readPostObject(reader, queue);
                }
                reader.endArray();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    @Override
    public void loadCatalog(JsonReader reader, ChanReaderProcessingQueue queue) throws Exception {
        reader.beginArray();
        while (reader.hasNext()) {
            queue.addForParse(this.parsePost(queue.getLoadable(), reader));
        }
        reader.endArray();
    }

    @Override
    public void readPostObject(JsonReader reader, ChanReaderProcessingQueue queue) throws Exception {
        Post.Builder builder = new Post.Builder();
        builder.board(queue.getLoadable().board);

        SiteEndpoints endpoints = queue.getLoadable().getSite().endpoints();

        String flagName = null, flagPath = null;

        // TODO: Unify this code with the catalog post parser
        // TODO: Get this thread post parser working
        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();

            switch (key) {
                case "name":
                    builder.name(this.nextString(reader));
                    break;
                case "flag":
                    flagPath = this.nextString(reader);
                    break;
                case "flagName":
                    flagName = this.nextString(reader);
                    break;
                case "subject":
                    builder.subject(this.nextString(reader));
                    break;
                case "markdown":
                    builder.comment(this.nextString(reader));
                    break;
                case "postId":
                case "threadId":
                    builder.id(reader.nextInt());
                    break;
                case "creation":
                case "lastBump":
                    builder.lastModified(ISO8601Utils.parse(
                            this.nextString(reader),
                            new ParsePosition(0)
                    ).getTime() / 1000); // Millis to secs
                    break;
                case "postCount":
                    builder.replies(reader.nextInt());
                    break;
                case "fileCount":
                    builder.images(reader.nextInt());
                    break;
                case "locked":
                    builder.closed(reader.nextBoolean());
                    break;
                case "pinned":
                    builder.sticky(reader.nextBoolean());
                    break;
                case "files":
                    // TODO: This should be a separate function
                    reader.beginArray();
                    List<PostImage> files = new ArrayList<>();
                    while (reader.hasNext()) {
                        PostImage.Builder imgBuilder = new PostImage.Builder();
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String prop = reader.nextName();

                            switch (prop) {
                                case "originalName":
                                    imgBuilder.originalName(this.nextString(reader));
                                    break;
                                case "path":
                                    imgBuilder.imageUrl(endpoints.imageUrl(
                                            builder,
                                            makeArgument("path", this.nextString(reader))
                                    ));
                                    break;
                                case "thumb":
                                    imgBuilder.thumbnailUrl(endpoints.imageUrl(
                                            builder,
                                            makeArgument("path", this.nextString(reader))
                                    ));
                                    break;
                                case "mime":
                                    imgBuilder.extension(
                                            MimeTypeMap.getSingleton().getExtensionFromMimeType(
                                                    this.nextString(reader)
                                            )
                                    );
                                    break;
                                case "size":
                                    imgBuilder.size(reader.nextLong());
                                    break;
                                case "width":
                                    imgBuilder.imageWidth(reader.nextInt());
                                    break;
                                case "height":
                                    imgBuilder.imageHeight(reader.nextInt());
                                    break;
                            }
                        }
                        files.add(imgBuilder.build());
                        reader.endObject();
                    }
                    builder.images(files);
                    reader.endArray();
                    break;
                default:
                    // Logger.d("bunkerchan", "Unknown post json key: " + key);
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        // TODO: Read documentation on caching
        // Post cached = queue.getCachedPost(builder.id);
        // if (cached != null) {
        //     // Id is known, use the cached post object.
        //     queue.addForReuse(cached);
        //     return;
        // }

        if (flagName != null && flagPath != null) {
            builder.addHttpIcon(new PostHttpIcon(
                    endpoints.icon(builder, flagName, makeArgument("path", flagPath)),
                    flagName
            ));
        }

        queue.addForParse(builder);
    }

    private String nextString(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.skipValue();
            return "";
        } else {
            return reader.nextString();
        }
    }

    private Post.Builder parsePost(Loadable loadable, JsonReader reader) throws Exception {
        Post.Builder builder = new Post.Builder();
        SiteEndpoints endpoints = loadable.getSite().endpoints();

        builder.board(loadable.board);

        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();

            switch (key) {
                case "markdown":
                    builder.comment(this.nextString(reader));
                    break;
                case "threadId":
                    int op = reader.nextInt();
                    builder.id(op);
                    builder.opId(op);
                    break;
                case "postCount":
                    builder.replies(reader.nextInt());
                    break;
                case "fileCount":
                    builder.images(reader.nextInt());
                    break;
                case "subject":
                    builder.subject(this.nextString(reader));
                    break;
                case "locked":
                    builder.closed(reader.nextBoolean());
                    break;
                case "pinned":
                    builder.sticky(reader.nextBoolean());
                    break;
                case "lastBump":
                    Date date = ISO8601Utils.parse(this.nextString(reader), new ParsePosition(0));
                    builder.setUnixTimestampSeconds(
                            // Milli to secs
                            date.getTime() / 1000
                    );
                    break;
                case "thumb": // This is a thumb in the catalog
                    builder.images(Arrays.asList(this.buildThumbnail(
                            endpoints,
                            builder,
                            this.nextString(reader)
                    ).build()));
                    break;
                default:
                    // Logger.d("bunkerchan", "Unknown post json key: " + key);
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return builder;
    }

    // Have to do this because the catalog provides
    // only thumbnails, not full resolution images
    private PostImage.Builder buildThumbnail(SiteEndpoints endpoints, Post.Builder post, String thumb) {
        PostImage.Builder builder = new PostImage.Builder();

        // Thumbnail and file will be the same
        HttpUrl thumbUrl = endpoints.thumbnailUrl(post, false, makeArgument(
                "path",
                thumb
        ));
        builder.imageUrl(thumbUrl);
        builder.thumbnailUrl(thumbUrl);

        String[] parts = thumb.split("/");
        String file = parts[parts.length-1];
        builder.filename(file);

        if (file.endsWith("gif")) {
            builder.extension("gif");
        } else if (file.endsWith("jpeg") || file.endsWith("jpg")) {
            builder.extension("jpg");
        } else if (file.endsWith("png")) {
            builder.extension("png");
        } else {
            // If we could not identify the proper extension,
            // just use a default one
            builder.extension("png");
        }

        return builder;
    }
}
