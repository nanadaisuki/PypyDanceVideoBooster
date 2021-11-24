package pypydancevideobooster.downloader;

import java.io.IOException;

public interface VideoDownloader {

    public void downloadFromURL(String url) throws IOException;

}
