package pypydancevideobooster;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import static java.lang.Thread.sleep;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayListManager {

    static Map<String, String> playlistCenter;

    public PlayListManager() {
        init();
    }

    private void init() {
        playlistCenter = new HashMap();
    }

    public void downloadPlayList() {
        System.err.println("This feature has not yet been implemented. ");
    }

    public void buildCacheFile() {
        File floder = new File("playlist");
        floder.mkdir();
        File cacheFile = new File(floder.getName() + "/cache.json");
        JsonObject cacheJson = new JsonObject();

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(cacheFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PlayListManager.class.getName()).log(Level.SEVERE, null, ex);
            try {
                cacheFile.createNewFile();
                buildCacheFile();
                return;
            } catch (IOException ex1) {
                Logger.getLogger(PlayListManager.class.getName()).log(Level.SEVERE, null, ex1);
                return;
            }
        }
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(osw);
        StringBuilder sb = new StringBuilder();
        for (File jsonFile : floder.listFiles()) {
            String fileName = jsonFile.getName();
            String suffixName = fileName.substring(fileName.lastIndexOf(".") + 1);
            if (!suffixName.equals("json") || fileName.equals(cacheFile.getName())) {
                continue;
            }
            FileInputStream fis;
            try {
                fis = new FileInputStream(jsonFile);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(PlayListManager.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            Gson gson = new Gson();
            JsonArray jsonArray = gson.fromJson(br, JsonArray.class);
            Iterator<JsonElement> arrayIterator = jsonArray.iterator();
            while (arrayIterator.hasNext()) {
                JsonArray contentArray = arrayIterator.next().getAsJsonArray();
                if (contentArray.size() != 2) {
                    System.err.println("Unrecognized.");
                    System.err.println(contentArray);
                    continue;
                }
                JsonObject nameJson = contentArray.get(0).getAsJsonObject();
                JsonObject urlJson = contentArray.get(1).getAsJsonObject();
                String name = nameJson.get("content").getAsString();
                String url = urlJson.get("content").getAsString();
                String videoId = url.substring(9);
                cacheJson.addProperty(videoId, name);
                sb.append(videoId).append(": ").append(name);
                sb.append('\n');
            }
        }
        System.out.println(sb.toString());
        Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        String formatedJson = gsonBuilder.toJson(cacheJson);
        try {
            bw.write(formatedJson);
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(PlayListManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String getVideoName(String videoId) {
        return playlistCenter.get(videoId);
    }

    public static String getYoutubeLink(String videoId) {
        return "https://www.youtube.com/watch?v=" + videoId;
    }

    public static enum CDN {
        PYPYDANCE_NEARBY, PYPYDANCE_HK, PYPYDANCE_JP, PYPYDANCE_US, NO_CDN
    }

    public static String getDownloadLink(CDN cdn, String videoId) {
        switch (cdn) {
            case PYPYDANCE_NEARBY:
            //break;
            case PYPYDANCE_HK:
            //break;
            case PYPYDANCE_JP:
            //break;
            case PYPYDANCE_US:
                return "This feature has not yet been implemented. ";
            //break;
            case NO_CDN:
                System.err.println("This feature has not yet been implemented. ");
                return getYoutubeLink(videoId);
            default:
                return "The specified CDN node does not exist. ";
        }
    }

}
