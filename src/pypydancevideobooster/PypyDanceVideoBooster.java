package pypydancevideobooster;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;
import test.RemovedFeatures;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import pypydancevideobooster.PlayListManager.CDN;
import pypydancevideobooster.https.Server;
import pypydancevideobooster.https.Server2;

public class PypyDanceVideoBooster implements HttpHandler {

    boolean dev = false;

    public static void main(String[] args) {
        try {
            checkRunWithCmd(args);
            new Server2().s();
            new PypyDanceVideoBooster().run(args);
        } catch (Exception e) {
            exception(e);
        }
    }

    public static void checkRunWithCmd(String[] args) {
        if (args.length > 0) {
            return;
        }
        if (System.console() == null) {
            JOptionPane.showMessageDialog(null, "You can only run it from console, for example, enter \"java -jar PypyDanceVideoBooster.jar\" in cmd ", "ERROR", ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public synchronized void run(String[] args) throws IOException, InterruptedException {
        String port = null;
        long youtubeTimeout = 5000;
        System.out.print("Launching PypyDanceVideoBooster with params ");
        for (String arg : args) {
            System.out.print(arg);
            System.out.print(" ");
        }
        System.out.println("");
        for (String arg : args) {
            if (arg.charAt(0) == 45) {
                String[] parm;
                if (arg.contains("=")) {
                    parm = arg.substring(1).split("=");
                } else {
                    parm = new String[]{arg.substring(1), ""};
                }
                if (parm[0].equals("port") && parm[1].matches("\\d{1,6}")) {
                    port = parm[1];
                }
                if (parm[0].equals("dev") && parm[1].equals("true")) {
                    dev = true;
                    System.out.println("dev mode");
                }
                if (parm[0].equals("youtubeTimeout") && parm[1].matches("\\d+")) {
                    youtubeTimeout = Integer.parseInt(parm[1]);
                    System.out.println("Set youtube timeout to " + youtubeTimeout);
                }
                if (parm[0].equals("buildlist")) {
                    PlayListManager.buildCacheList();
                }
                if (parm[0].equals("buildcache")) {
                    if (parm[1].contains("PYPYDANCE")) {
                        System.out.println("Forbidden to spread the generated cache files, absolutely forbidden! ! ");
                        CDN cdn = CDN.valueOf(parm[1]);
                        RemovedFeatures.buildCacheFile(cdn);
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
        PlayListManager.init();
        File floder = new File("media");
        floder.mkdir();
        System.out.println("Media floder: " + floder.getCanonicalPath());
        VideoCacheManager.init(floder);
        System.out.println("Bind the CacheServer on port " + port + ", launching...");
        if (port == null) {
            List<String> list = new ArrayList(Arrays.asList(args));
            list.add("-port=12345");
            String[] array = new String[list.size()];
            list.toArray(array);
            this.run(array);
            return;
        }

        new Server(youtubeTimeout).start();

        HttpServerProvider provider = HttpServerProvider.provider();
        InetSocketAddress address = new InetSocketAddress(Integer.parseInt(port));
        while (true) {
            if (httpServer == null) {
                System.out.println("Launching CacheServer");
                try {
                    httpServer = provider.createHttpServer(address, 4);
                    httpServer.setExecutor(e);
                    httpServer.createContext("/", this);
                    httpServer.start();
                    System.out.println("CacheServer launched.");
                } catch (BindException be) {
                    System.err.println(be.getLocalizedMessage());
                    httpServer = null;
                }
            }
            wait(5000);
        }
    }
    HttpServer httpServer = null;
    ExecutorService e = Executors.newCachedThreadPool();

    @Override
    public void handle(HttpExchange http) {
        System.gc();
        System.err.println("Activiting threads: " + ((ThreadPoolExecutor) e).getActiveCount());
        printMemory();
        if (((ThreadPoolExecutor) e).getActiveCount() > 2) {
            System.out.println("Hey...");
        }
        if (((ThreadPoolExecutor) e).getActiveCount() > 3) {
            System.out.println("You shouldn't do that.");
        }
        if (((ThreadPoolExecutor) e).getActiveCount() > 4) {
            System.out.println("nooooooooooooooooo!!");
            if (!dev) {
                System.exit(0);
            }
        }

        String requestHeader = headersToString(http, true);
        System.out.println("\nRequestHeaders: ");
        System.out.println(requestHeader);
        System.out.println("");
        String requestMethod = http.getRequestMethod();
        if (requestMethod.equals("GET") || requestMethod.equals("HEAD")) {
        } else {
            System.out.println("Request method \"" + requestMethod + "\" not implemented!");
            http.close();
            return;
        }
        String hostName = http.getRequestURI().getHost();;
        if (dev || hostName.equals("storage-jp.llss.io") || hostName.equals("storage-cdn.llss.io")) {
        } else {
            System.err.println(hostName);
            System.err.println("Bad configure.");
            http.close();
            return;
        }
        String fileName = http.getRequestURI().toString();
        Pattern r = Pattern.compile("[a-zA-Z0-9_\\-]+.mp4");
        Matcher m = r.matcher(fileName);
        if (m.find()) {
            System.out.println("Request file " + fileName);
            fileName = "/" + m.group(0);
        } else {
            System.err.println(fileName + " not mp4 file! ");
            http.close();
            return;
        }
        fileName = "media" + fileName;
        File example = new File(fileName);
        try {
            switch (requestMethod) {
                case "GET":
                    this.sendFile(http, example, false);
                    break;
                case "HEAD":
                    this.sendFile(http, example, true);
                    break;
            }
        } catch (Exception e) {
            exception(e);
        } finally {
            http.close();
        }
        if (httpServer == null) {
            System.out.println("force stop!");
            http.close();
            http.getHttpContext().getServer().stop(1);
        }
        if (printMemory() > 512) {
            System.err.println("Too many memory usage, server will be shutdown.");
            if (httpServer != null) {
                http.close();
                httpServer.stop(1);
            }
            httpServer = null;
            synchronized (this) {
                this.notify();
            }
        }
    }

    public static String headersToString(HttpExchange http, boolean requestOrResponse) {
        Headers headers;
        StringBuilder headerBuilder = new StringBuilder();
        if (requestOrResponse) {
            headerBuilder.append(http.getRequestMethod()).append(" ");
            headerBuilder.append(http.getRequestURI()).append(" ");
            headerBuilder.append(http.getProtocol());
            headers = http.getRequestHeaders();
        } else {
            headerBuilder.append(http.getProtocol()).append(" ");
            int reponseCode = http.getResponseCode();
            headerBuilder.append(reponseCode);
            if (reponseCode == 200) {
                headerBuilder.append(" OK");
            }
            headers = http.getResponseHeaders();
        }

        for (Entry<String, List<String>> entry : headers.entrySet()) {
            headerBuilder.append("\n");
            headerBuilder.append(entry.getKey());
            headerBuilder.append(": ");
            for (String s : entry.getValue()) {
                headerBuilder.append(s);
            }
        }
        return headerBuilder.toString();
    }

    private void outputReponseHeaders(HttpExchange http) {
        System.out.println("\nReponseHeaders: ");
        System.out.println(headersToString(http, false));
        System.out.println("");
    }

    private void sendFile(HttpExchange http, File f, boolean onlyHead) throws FileNotFoundException, IOException, InterruptedException {
        String videoId = f.getName().replace(".mp4", "");
        Headers headers = http.getResponseHeaders();
        headers.add("Server", "server1");
        String connection = http.getRequestHeaders().get("Connection").get(0);
        if (connection.equals("close")) {
            headers.add("Connection", "close");
        } else {
            headers.add("Connection", "keep-alive");
        }
        headers.add("Content-type", "video/mp4");
        System.out.println("file: " + f.getPath() + " exists: " + String.valueOf(f.exists()));
        if (f.exists() && f.length() > 0) {
            System.out.println("Request " + http.getRequestURI() + " HIT CACHE!");
            if (onlyHead) {
                System.out.println("Only head");
                headers.add("Accept-Ranges", "bytes");
                http.sendResponseHeaders(200, f.length());
                this.outputReponseHeaders(http);
                return;
            }
            List<String> rangeHeader = http.getRequestHeaders().get("Range");
            List<String> etagHeader = http.getRequestHeaders().get("If-None-Match");
            List<String> dateHeader = http.getRequestHeaders().get("If-Modified-Since");

            int startRange, endRange, rangeLength;
            if (false || rangeHeader == null || rangeHeader.isEmpty()) {
                startRange = 0;
                endRange = (int) f.length();
                rangeLength = endRange - startRange;
                headers.add("Accept-Ranges", "bytes");
                http.sendResponseHeaders(200, f.length());
            } else {
                String[] rangeString = rangeHeader.get(0).replace("bytes=", "").split("-");
                if (rangeString.length == 1) {
                    startRange = Integer.parseInt(rangeString[0]);
                    endRange = (int) f.length() - 1;
                } else {
                    startRange = Integer.parseInt(rangeString[0]);
                    endRange = Integer.parseInt(rangeString[1]);
                }
                rangeLength = endRange - startRange;
                rangeLength += 1;// its fixed?
                System.out.println("Request range " + startRange + "-" + endRange);
                headers.add("Content-Range", "bytes " + startRange + "-" + endRange + "/" + f.length());
                http.sendResponseHeaders(206, rangeLength);

            }

            this.outputReponseHeaders(http);
            byte[] buffer = new byte[rangeLength];
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
            //BufferedOutputStream bos = new BufferedOutputStream(http.getResponseBody());
            OutputStream bos = http.getResponseBody();
            try {
                bis.skip(startRange);
                bis.read(buffer, 0, rangeLength);
                System.out.println("Playing " + PlayListManager.getVideoName(videoId));
                bos.write(buffer);
            } catch (Exception e) {
                if (e instanceof IndexOutOfBoundsException) {
                    System.err.println("bufferSize: " + buffer.length + " start: " + startRange + " rangeLength: " + rangeLength);
                }
                System.err.println("close with exception: " + e.getLocalizedMessage());
                if (e.getLocalizedMessage() == null) {
                    e.printStackTrace();
                }
            } finally {
                buffer = null;
                try {
                    bis.close();
                    bos.close();
                } catch (IOException e1) {
                    if (e1.getMessage().equals("insufficient bytes written to stream")) {

                    }
                }
                System.gc();
            }
        } else {
            VideoCacheManager.download(videoId, CDN.PYPYDANCE_JP);
            this.sendFile(http, f, onlyHead);
        }
    }

    public int printMemory() {
        double mb = 1024 * 1024 * 1.0;
        double totalMemory = Runtime.getRuntime().totalMemory() / mb;
        double freeMemory = Runtime.getRuntime().freeMemory() / mb;
        double maxMemory = Runtime.getRuntime().maxMemory() / mb;
        int totalUsage = (int) (totalMemory - freeMemory);
        System.err.println("TotalUsage: " + totalUsage);
        return totalUsage;
    }

    public static String headersToString(HttpURLConnection request) {
        StringBuilder headerBuilder = new StringBuilder();
        for (Entry<String, List<String>> entry : request.getHeaderFields().entrySet()) {
            headerBuilder.append(entry.getKey()).append(": ");
            List s = entry.getValue();
            for (int i = 0; i < s.size(); i++) {
                headerBuilder.append(s.get(i));
                if (i + 1 < s.size()) {
                    headerBuilder.append("; ");
                }
            }
        }
        return headerBuilder.toString();
    }

    private static void exception(Exception e) {
        StringBuilder out = new StringBuilder();
        for (StackTraceElement ste : e.getStackTrace()) {
            out.append(ste);
            out.append("\n");
        }
        StringBuilder title = new StringBuilder(e.toString());
        System.err.println(title);
        e.printStackTrace(System.err);
        //JOptionPane.showMessageDialog(null, out.toString(), title.toString(), JOptionPane.ERROR_MESSAGE);
    }

}
