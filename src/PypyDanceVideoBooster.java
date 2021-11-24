package pypydancevideobooster;

//import pdvb_removedfeatures.RemovedFeatures;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import pypydancevideobooster.PlayListManager.CDN;
import pypydancevideobooster.server.NetworkRegulatorProxyServer;
import pypydancevideobooster.server.VideoCacheServer;

public class PypyDanceVideoBooster {

    public static final int HTTP_PORT = 80;
    public static final int SSL_PORT = 443;
    public static final File FLODER_VIDEO = new File("media\\");
    public static final File FLODER_PLAYLIST = new File("playlist\\");

    public static void main(String[] args) throws IOException {
        boolean ide = false;
        for (String arg : args) {
            if (arg.charAt(0) == 45) {
                String[] parm;
                if (arg.contains("=")) {
                    parm = arg.substring(1).split("=");
                } else {
                    parm = new String[]{arg.substring(1), ""};
                }
                if (parm[0].equals("ide")) {
                    ide = true;
                }
                if (parm[0].equals("buildlist")) {
                    PlayListManager.buildCacheList();
                }
                if (parm[0].equals("buildcache")) {
                    if (parm[1].contains("PYPYDANCE")) {
                        System.out.println("Forbidden to spread the generated cache files, absolutely forbidden! ! ");
                        CDN cdn = CDN.valueOf(parm[1]);
                        //RemovedFeatures.buildCacheFile(cdn);
                    } else {
                        System.err.println("WARNING! Use the feature may cause your computer to get banned.");
                        System.out.println("To get started, you need to add \"=\" and the CDN name after this parameter. "
                                + "If you want to use the CDN of K.MOG, the CDN name is \"PYPYDANCE_\" and two letters, such as \"PYPYDANCE_JP\". "
                                + "If you don't want to use CDN, you can also fill in \"NO_CDN\". "
                                + "Currently, the new version of https API is not supported, because it is very complicated to interface with the https interface. I want to use this time for other projects. ");
                    }
                }
            }
        }
        if (System.console() == null && !ide) {
            JOptionPane.showMessageDialog(null, "You can only run it from console, for example, enter \"java -jar PypyDanceVideoBooster.jar\" in cmd ", "ERROR", ERROR_MESSAGE);
            System.exit(1);
        }
        PlayListManager.init();
        FLODER_VIDEO.mkdir();
        System.out.println("Media floder: " + FLODER_VIDEO.getCanonicalPath());
        VideoCacheManager.init(FLODER_VIDEO);
        System.out.println("Bind the CacheServer on port " + HTTP_PORT + ", launching...");

        new NetworkRegulatorProxyServer(1000)
                .start();
        new VideoCacheServer(80, 2).start();
    }

}
