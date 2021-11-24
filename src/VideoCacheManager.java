package pypydancevideobooster;

import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.Thread.sleep;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import pypydancevideobooster.PlayListManager.CDN;
import static pypydancevideobooster.server.VideoCacheServer.headersToString;

public class VideoCacheManager {

    private static Map<String, File> cacheStorage;

    public static void init(File cacheFloder) {
        cacheStorage = new HashMap();
        for (File file : cacheFloder.listFiles()) {
            String name = file.getName();
            if (name.contains(".mp4")) {
                cacheStorage.put(name.replace(".mp4", ""), file);
            }
        }
        System.out.println("VideoCache loaded " + cacheStorage.size() + " entries ");
    }

    public static File get(String videoId) {
        File video = cacheStorage.get(videoId);
        if (video == null) {
            return null;
        } else if (video.length() == 0) {
            return null;
        } else {
            return video;
        }
    }

    public static List<String> downloading = new ArrayList();

    public static boolean download(String videoId, CDN cdn) {
        HttpURLConnection request = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        JFrame jFrame = new JFrame();
        try {
            if (downloading.contains(videoId)) {
                System.err.println("Request video file  "+ videoId +" already downloading!");
                while (downloading.contains(videoId)){
                    System.out.println("Waiting for other download request complete. ");
                    try {
                        sleep(5000);
                    } catch (InterruptedException e) {
                        System.out.println("download failed.");
                    }
                }
                return false;
            } else {
                downloading.add(videoId);
            }
            URL url = new URL(PlayListManager.getDownloadLink(videoId, cdn));
            File videoFile = new File("media\\" + videoId + ".mp4");
            System.out.println("Downloading file " + videoFile.getPath() + " from " + url.toString());
            request = (HttpURLConnection) url.openConnection();
            int mood = request.getResponseCode();
            if (mood != 200) {
                System.err.println("download response code: " + mood);
                System.err.println(url.toString() + "\n" + headersToString(request));
                throw new IllegalStateException("It's in a bad mood...");
            }
            InputStream is = request.getInputStream();
            int length = Integer.parseInt(request.getHeaderField("Content-Length"));
            //System.out.println("Content-Lengrh: " + length);
            int realLength = is.available();
            if (realLength == 0) {
                System.out.println("Waiting connection intput stream");
            }
            while (realLength == 0) {
                realLength = is.available();
            }
            if (realLength != length) {
                //System.err.println("InputStream length " + realLength + " not match HTTP length " + length);
            }
            int bufferSize = length;
            byte[] buffer = new byte[bufferSize];
            bis = new BufferedInputStream(is);
            System.out.println("Download connection ok.");
            fos = new FileOutputStream(videoFile);
            System.out.println("Download output stream ok.");
            jFrame.setSize(320, 80);
            JLabel jLabel = new JLabel();
            JProgressBar jProgressBar = new JProgressBar();
            jProgressBar.setOpaque(false);
            jLabel.setSize(310, 50);
            jLabel.setText("name");
            jLabel.setHorizontalAlignment(SwingConstants.CENTER);
            jLabel.setVerticalAlignment(SwingConstants.CENTER);
            jFrame.add(jLabel);
            jProgressBar.setValue(50);
            jFrame.add(jProgressBar);
            jFrame.addWindowListener(new WindowAdapter() {
                InputStream is;

                @Override
                public void windowClosing(WindowEvent we) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        System.err.println(e);
                        System.err.println("Cant close input stream");
                    }
                }

                public WindowAdapter addCallback(InputStream is) {
                    this.is = is;
                    return this;
                }
            }.addCallback(is));
            jFrame.setTitle("Downloading...");
            jFrame.setResizable(false);
            jFrame.setVisible(true);
            jFrame.setAlwaysOnTop(true);

            int readCount = 0, lastRemain = length;
            jLabel.setText(videoFile.getName());
            String videoName = PlayListManager.getVideoName(videoId);
            while (readCount < length) {
                int remain = length - readCount;
                lastRemain = remain;
                readCount += bis.read(buffer, readCount, remain);
                int process = (int) ((double) readCount / length * 100);
                jProgressBar.setValue(process);
                if (process > 0) {
                    jFrame.setTitle(videoFile.getName() + " ..." + process + "%");
                    jLabel.setText(videoName);
                    jFrame.setAlwaysOnTop(false);
                }
            }
            fos.write(buffer);
            System.gc();
            cacheStorage.put(videoId, videoFile);
        } catch (IndexOutOfBoundsException e) {
            System.err.println(e);
            System.err.println("Cant dowmlopad");
            return false;
        } catch (HeadlessException | IOException | NumberFormatException e) {
            System.err.println(e);
            return false;
        } catch (IllegalStateException e) {
            try {
                System.err.println("Let's have a cup of coffee and take a break. ");
                sleep(1000);// I think 1 iops its ok.
            } catch (InterruptedException ex) {
                Logger.getLogger(VideoCacheManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (fos != null) {
                    fos.close();
                }
                if (request != null) {
                    request.disconnect();
                }
            } catch (IOException ex) {
                Logger.getLogger(VideoCacheManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            downloading.remove(videoId);
            jFrame.dispose();
        }
        return true;
    }

    public File compress(String videoId) {
        return null;
    }

    public File deCompress(String videoId) {
        return null;
    }

    public boolean isCompress(String videoId) {
        return false;
    }
}
