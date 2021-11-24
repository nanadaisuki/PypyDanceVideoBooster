package pypydancevideobooster.downloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class U2beTool implements VideoDownloader{

    File yt_dlp;

    public U2beTool(File yt_dlp) throws Exception {
        if (!yt_dlp.getName().equals("yt-dlp.exe")) {
            throw new Exception("File not yt-dlp.exe");
        }
        this.yt_dlp = yt_dlp;
        System.out.println("U2be downloader found, calling...");
        String version = execute("--version");
        if (version.isEmpty()) {
            throw new Exception("No response from yt-dlp.exe");
        }
        System.out.println("Verified, the tools version is: \n" + version);
    }

    private String execute(String params) {
        try {
            Process process = Runtime.getRuntime().exec(yt_dlp.getPath() + " --no-check-certificates " + params);
            System.out.println(yt_dlp.getPath() + " --no-check-certificates " + params);
            InputStream inputStream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();
            InputStreamReader charReader = new InputStreamReader(inputStream);
            StringBuilder textSplicer = new StringBuilder();
            char[] buffer = new char[1024];
            int len = -1;
            while ((len = charReader.read(buffer)) != -1) {
                System.out.println(len);
                textSplicer.append(buffer);
            }
            if (textSplicer.toString().isEmpty()) {
                System.out.println("No any data responsed, trying ErrorStream. ");
                InputStreamReader errorReader = new InputStreamReader(errorStream);
                while ((len = errorReader.read(buffer)) != -1) {
                    System.out.println(len);
                    textSplicer.append(buffer);
                }
            }
            return textSplicer.toString();
        } catch (IOException e) {
            System.err.println(e);
            System.err.println("Cannot execute with params \"" + params + "\"");
            return null;
        }
    }
    
    public void downloadVideoFromU2be(String url){
        System.out.println("Downloading video from: " + url);
        this.execute(url); // It doesn't seem to work 
        System.out.println("Complete. ");
    }

    public String getTitleFromURL(String url) {
        return null; // For some reasons, I don’t know how to implement this feature for the time being.
    }

    @Override
    public void downloadFromURL(String url) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
