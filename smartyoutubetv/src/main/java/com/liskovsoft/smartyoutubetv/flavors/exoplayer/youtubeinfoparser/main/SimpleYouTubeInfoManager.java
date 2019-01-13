package com.liskovsoft.smartyoutubetv.flavors.exoplayer.youtubeinfoparser.main;

import android.net.Uri;
import com.liskovsoft.smartyoutubetv.flavors.exoplayer.youtubeinfoparser.main.YouTubeSubParser.Subtitle;
import com.liskovsoft.smartyoutubetv.flavors.exoplayer.youtubeinfoparser.main.YouTubeMediaParser.GenericInfo;
import com.liskovsoft.smartyoutubetv.flavors.exoplayer.youtubeinfoparser.main.YouTubeMediaParser.MediaItem;

import java.io.InputStream;
import java.util.List;

public class SimpleYouTubeInfoManager implements YouTubeInfoVisitable {
    private final YouTubeMediaParser mMediaParser;
    private final YouTubeSubParser mSubParser;
    private YouTubeInfoVisitor mVisitor;

    public SimpleYouTubeInfoManager(String content) {
        mMediaParser = new YouTubeMediaParser(content);
        mSubParser = new YouTubeSubParser(content);
    }

    @Override
    public void accept(YouTubeInfoVisitor visitor) {
        mVisitor = visitor;

        GenericInfo info = mMediaParser.extractGenericInfo();

        mVisitor.onGenericInfo(info);

        Uri hlsUri = mMediaParser.extractHLSUrl();

        if (hlsUri != null) {
            mVisitor.onLiveItem(hlsUri);

            // stop here, since other items are useless, the reason is below

            // NOTE: serious bug there
            // NOTE: exo can play live stream in hls only
            // NOTE: dash live isn't playable (infinite loading)
            // mVisitor.doneVisiting();
            // return;
        }

        List<Subtitle> subs = mSubParser.extractAllSubs();

        if (subs != null) {
            for (Subtitle sub : subs) {
                mVisitor.onSubItem(sub);
            }
        }

        mMediaParser.extractMediaItemsAndDecipher(new YouTubeMediaParser.ParserListener() {
            @Override
            public void onHlsUrl(Uri url) {
                // NOTE: serious bug there
                // NOTE: exo can play live stream in hls only
                // NOTE: dash live isn't playable (infinite loading)
                mVisitor.onLiveItem(url);
            }

            @Override
            public void onRawDashContent(InputStream dashContent) {
                mVisitor.onRawDashContent(dashContent);
            }

            @Override
            public void onExtractMediaItemsAndDecipher(List<MediaItem> items) {
                for (MediaItem item : items) {
                    mVisitor.onMediaItem(item);
                }
                mVisitor.doneVisiting();
            }
        });
    }
}