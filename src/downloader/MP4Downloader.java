package pypydancevideobooster.downloader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pypydancevideobooster.PlayListManager;
import pypydancevideobooster.VideoCacheManager;

public class MP4Downloader implements VideoDownloader {

    private static final Pattern ALLOWED_FILENAME = Pattern.compile("[a-zA-Z0-9_\\-]+.mp4");

    @Override
    public void downloadFromURL(String url) {
        Matcher m = ALLOWED_FILENAME.matcher(url);
        m.find();
        String mp4Name = m.group(0);
        VideoCacheManager.download(mp4Name.replace(".mp4", ""), PlayListManager.CDN.PYPYDANCE_JP);
    }

}
