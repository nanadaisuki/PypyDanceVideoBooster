package pypydancevideobooster.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pypydancevideobooster.PlayListManager;
import pypydancevideobooster.PypyDanceVideoBooster;
import pypydancevideobooster.downloader.MP4Downloader;
import pypydancevideobooster.downloader.VideoDownloader;

public class VideoCacheServer implements HttpHandler {

    private static final HttpServerProvider PROVIDER = HttpServerProvider.provider();
    private static final List<String> ALLOWED_METHOD = Arrays.asList("HEAD", "GET");
    private static final Pattern ALLOWED_FILENAME = Pattern.compile("[a-zA-Z0-9_\\-]+.mp4");
    private final ThreadPoolExecutor POOL;
    private final HttpServer SERVER;
    private final VideoDownloader DOWNLOADER;

    public VideoCacheServer(int port, int max_qps) throws IOException {
        this.POOL = new ThreadPoolExecutor(max_qps, max_qps, 5, TimeUnit.MINUTES, new LinkedBlockingDeque<>(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                System.out.println("[SERVER_POOL] New thread created. ");
                return t;
            }
        }, new ThreadPoolExecutor.AbortPolicy()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                if (t != null) {
                    System.err.println("[SERVER_POOL] Exception in thread " + Thread.currentThread().getName() + " " + t.getMessage());
                }
            }

        };
        InetSocketAddress localhost = new InetSocketAddress(port);
        this.SERVER = PROVIDER.createHttpServer(localhost, max_qps);
        this.DOWNLOADER = new MP4Downloader();
    }

    public void start() {
        SERVER.setExecutor(POOL);
        SERVER.createContext("/", this);
        SERVER.start();
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        try {
            this.safeHandleNoException(he);
        } catch (Exception e) {
            System.err.println(e);
            System.err.println("Successfully captured the exception, connection handle process interrupted, try to close the connection.");
        } finally {
            try {
                he.close();
                System.out.println("Connection closed. ");
            } catch (Exception e) {
                System.err.println(e);
                System.err.println("Warning! The connection was not successfully closed, which may cause a memory leak. ");
            }
        }
    }

    public void safeHandleNoException(HttpExchange http) {
        int qps = POOL.getActiveCount();
        int max_qps = POOL.getMaximumPoolSize();
        if (qps >= max_qps / 2) {
            System.out.println("Currently, the cache servers qps is " + qps + ", the server allowed qps of maximum is " + max_qps);
        }
        Headers requestHeader = http.getRequestHeaders();
        Headers responseHeader = http.getResponseHeaders();
        String requestMethod = http.getRequestMethod();
        if (!ALLOWED_METHOD.contains(requestMethod)) {
            System.out.println("Request method \"" + requestMethod + "\" not implemented, connection will be initerrupt. ");
            return;
        }
        String url = http.getLocalAddress().getHostName() + http.getRequestURI().toString(), mp4Name;
        Matcher m = ALLOWED_FILENAME.matcher(url);
        if (m.find()) {
            mp4Name = m.group(0);
            System.out.println("Request video file " + mp4Name + " url: " + url);
        } else {
            System.err.println("Request " + url + " not video file! ");
            return;
        }

        File videoFile = new File(PypyDanceVideoBooster.FLODER_VIDEO.getPath() + "\\" + mp4Name);
        if (!videoFile.exists() || videoFile.length() == 0) {
            System.out.println("File not exists, try to download");
            try {
                DOWNLOADER.downloadFromURL(url);
            } catch (IOException e) {
                System.err.println("Failed to download " + mp4Name + ", reason: " + e.getMessage());
                try {
                    http.sendResponseHeaders(404, 0);
                } catch (IOException ex) {
                    System.err.println("Failed to response the connection, reason: " + e.getMessage());
                }
                return;
            }
        }
        try {
            System.out.println("Found video file at " + videoFile.getCanonicalPath());
        } catch (IOException ex) {
            System.err.println("Invalid file path " + videoFile.getPath());
            return;
        }
        if (http.getRequestMethod().equals("HEAD")) {
            System.out.println("Requesting " + url + " on a HEAD request, connection will be close. ");
            try {
                http.sendResponseHeaders(200, -1); // The length of the request must be configured in here, and it cannot be a number of 0 and above, otherwise an exception will be thrown because this is a HEAD request. 
                return;
            } catch (IOException e) {
                System.err.println("Failed to sent HEAD response header, reason: " + e.getMessage());
                return;
            }
        }
        List<String> rangeHeader = http.getRequestHeaders().get("Range");
        List<String> etagHeader = http.getRequestHeaders().get("If-None-Match");
        List<String> dateHeader = http.getRequestHeaders().get("If-Modified-Since");
        responseHeader.add("Content-type", "video/mp4");
        boolean noRange = false;
        int startRange, endRange, rangeLength;
        if (noRange || rangeHeader == null || rangeHeader.isEmpty()) {
            try {
                startRange = 0;
                endRange = (int) videoFile.length();
                rangeLength = endRange - startRange;
                http.sendResponseHeaders(200, rangeLength);
            } catch (IOException e) {
                System.err.println("Failed to sent response header, reason: " + e.getMessage());
                return;
            }
        } else {
            try {
                String[] rangeString = rangeHeader.get(0).replace("bytes=", "").split("-");
                if (rangeString.length == 1) {
                    startRange = Integer.parseInt(rangeString[0]);
                    endRange = (int) videoFile.length() - 1;
                } else {
                    startRange = Integer.parseInt(rangeString[0]);
                    endRange = Integer.parseInt(rangeString[1]);
                }
                rangeLength = endRange - startRange;
                rangeLength += 1;// We calculated the byte array index before, but now we need the file length, so we need to add 1. 
                responseHeader.add("Content-Range", "bytes " + startRange + "-" + endRange + "/" + videoFile.length());
                http.sendResponseHeaders(206, rangeLength);
            } catch (NumberFormatException e) {
                System.err.println("Range: " + rangeHeader.get(0));
                System.err.println("Failed to sent response header, reason: " + e.getMessage());
                return;
            } catch (IOException e) {
                System.err.println("Content-Range: " + responseHeader.get("Content-Range").get(0));
                System.err.println("Failed to sent response header, reason: " + e.getMessage());
                return;
            }
        }
        System.out.println("Requesting " + url + " on " + (http.getResponseCode() == 200 ? "200(HTTP OK)" : "206(Partial Content)") + ", data responding...");

        byte[] buffer = new byte[1024]; // Although I don’t know why, the result of repeated experiments is that a byte array that is too large will cause memory leaks during socket writting, so here I use 1024. 
        BufferedInputStream bis = null;
        OutputStream os = null;
        try {
            String videoId = videoFile.getName().split("\\.")[0];
            System.out.println("Playing " + PlayListManager.getVideoName(videoId));
            bis = new BufferedInputStream(new FileInputStream(videoFile));
            os = http.getResponseBody();
            bis.skip(startRange);
            int readCount = 0, remain = rangeLength;
            if (bis.available() != rangeLength) {
                System.err.println("Warning! FileInputStreamLength: " + bis.available() + " does not match HttpRequestRangeLength: " + rangeLength);
            }
            while (remain > 0) {
                //System.out.println("remain: " + remain);
                readCount = bis.read(buffer, 0, 1024);
                //System.out.println("read: " + readCount);
                if (readCount < 0) {
                    System.out.println("stream ended.");
                    break;
                };
                os.write(buffer, 0, readCount);
                remain -= readCount;
            }
        } catch (IOException e) {
            System.err.println("Failed to sent response data, reason: " + e.getMessage());
            return;
        } finally {
            try {
                Arrays.fill(buffer, (byte) 0);
                buffer = null;
                if (bis != null) {
                    bis.close();
                }
                if (os != null) {
                    os.close();
                }
                System.gc();
            } catch (IOException e) {
                System.err.println("Failed to close stream, reason: " + e.getMessage());
                return;
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

        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            headerBuilder.append("\n");
            headerBuilder.append(entry.getKey());
            headerBuilder.append(": ");
            for (String s : entry.getValue()) {
                headerBuilder.append(s);
            }
        }
        return headerBuilder.toString();
    }

    public static String headersToString(HttpURLConnection request) {
        StringBuilder headerBuilder = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : request.getHeaderFields().entrySet()) {
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
}
