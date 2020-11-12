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
import java.util.List;

import okhttp3.HttpUrl;

import static org.floens.chan.core.site.SiteEndpoints.makeArgument;

public class BunkerchanApi extends CommonSite.CommonApi {

    public BunkerchanApi(CommonSite commonSite) {
        super(commonSite);
    }

    @Override
    public void loadThread(JsonReader reader, ChanReaderProcessingQueue queue) throws Exception {
        List<Post.Builder> posts = this.parsePosts(queue.getLoadable(), reader);

        Post.Builder op = posts.get(0);

        for (Post.Builder post : posts) {
            post.opId(op.id);
            queue.addForParse(post);
        }
    }

    @Override
    public void loadCatalog(JsonReader reader, ChanReaderProcessingQueue queue) throws Exception {
        reader.beginArray();
        while (reader.hasNext()) {
            queue.addForParse(this.parsePosts(queue.getLoadable(), reader).get(0));
        }
        reader.endArray();
    }

    @Override
    public void readPostObject(JsonReader reader, ChanReaderProcessingQueue queue) throws Exception {
        Post.Builder builder = this.parsePosts(queue.getLoadable(), reader).get(0);

        // TODO: Read documentation on caching
        // Post cached = queue.getCachedPost(builder.id);
        // if (cached != null) {
        //     // Id is known, use the cached post object.
        //     queue.addForReuse(cached);
        //     return;
        // }

        queue.addForParse(builder);
    }

    private List<Post.Builder> parsePosts(Loadable loadable, JsonReader reader) throws Exception {
        SiteEndpoints endpoints = loadable.getSite().endpoints();
        ArrayList<Post.Builder> list = new ArrayList<>();
        Post.Builder builder = new Post.Builder();

        builder.board(loadable.board);
        list.add(builder);

        String flagName = null, flagPath = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();

            switch (key) {
                case "flag":
                    flagPath = this.nextString(reader);
                    break;
                case "flagName":
                    flagName = this.nextString(reader);
                    break;
                case "posts":
                    reader.beginArray();
                    while (reader.hasNext()) {
                        list.addAll(this.parsePosts(loadable, reader));
                    }
                    reader.endArray();
                    break;
                default:
                    this.setPostProperty(endpoints, builder, key, reader);
                    break;
            }
        }
        reader.endObject();

        if (flagName != null && flagPath != null) {
            builder.addHttpIcon(new PostHttpIcon(
                    endpoints.icon(builder, flagName, makeArgument("path", flagPath)),
                    flagName
            ));
        }

        return list;
    }

    private String nextString(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.skipValue();
            return "";
        } else {
            return reader.nextString();
        }
    }

    private int nextInt(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.skipValue();
            return 0;
        } else {
            return reader.nextInt();
        }
    }

    // Sets a single property of the Post
    private boolean setPostProperty(SiteEndpoints endpoints, Post.Builder builder, String prop, JsonReader reader) throws Exception {
        switch (prop) {
            case "name":
                builder.name(this.nextString(reader));
                break;
            case "postId":
                builder.id(this.nextInt(reader));
                break;
            case "creation":
            case "lastBump":
                builder.setUnixTimestampSeconds(ISO8601Utils.parse(
                        this.nextString(reader),
                        new ParsePosition(0)
                ).getTime() / 1000); // Millis to secs
                break;
            case "markdown":
                builder.comment(this.nextString(reader));
                break;
            case "threadId":
                int op = this.nextInt(reader);
                builder.id(op);
                builder.opId(op);
                builder.op(true);
                break;
            case "postCount":
                builder.replies(this.nextInt(reader));
                break;
            case "fileCount":
                builder.images(this.nextInt(reader));
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
            case "thumb": // This is a thumb in the catalog
                builder.images(Arrays.asList(this.buildThumbnail(
                        endpoints,
                        builder,
                        this.nextString(reader)
                ).build()));
                break;
            case "files":
                // TODO: This should be a separate function
                reader.beginArray();
                List<PostImage> files = new ArrayList<>();
                while (reader.hasNext()) {
                    PostImage.Builder imgBuilder = new PostImage.Builder();
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String imgProp = reader.nextName();

                        switch (imgProp) {
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
                                imgBuilder.imageWidth(this.nextInt(reader));
                                break;
                            case "height":
                                imgBuilder.imageHeight(this.nextInt(reader));
                                break;
                            default:
                                Logger.d("bunkerchan", "Unknown image property: " + imgProp);
                                reader.skipValue();
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
                Logger.d("bunkerchan", "Unknown post property: " + prop);
                reader.skipValue();
                return false;
        }

        return true;
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
