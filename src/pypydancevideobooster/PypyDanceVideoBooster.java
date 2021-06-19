package pypydancevideobooster;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

public class PypyDanceVideoBooster implements HttpHandler {

    boolean dev = false;

    public static void main(String[] args) {
        try {
            new PypyDanceVideoBooster().run(args);
            //downloadFile("speedtest-sgp1.digitalocean.com", new File("media/10mb.test"));
            //downloadFile("storage-cdn.llss.io", new File("media/GNxgQK50KWE.mp4"));
        } catch (Exception e) {
            exception(e);
        }
    }

    public synchronized void run(String[] args) throws IOException, InterruptedException {
        String port = null;
        for (String arg : args) {
            System.out.println(arg);
            if (arg.charAt(0) == 45 && arg.contains("=")) {
                String[] parm = arg.substring(1).split("=");
                if (parm[0].equals("port") && parm[1].matches("\\d{1,6}")) {
                    port = parm[1];
                }
                if (parm[0].equals("dev") && parm[1].equals("true")) {
                    dev = true;
                    System.out.println("dev mode");
                }
            }
        }
        File floder = new File("media");
        floder.mkdir();
        System.out.println("Media floder: " + floder.getCanonicalPath());
        System.out.println("Launching the server on port " + port);
        if (port == null) {
            Integer[] sz = {3, 2};
            List<String> list = new ArrayList(Arrays.asList(args));
            list.add("-port=12345");
            String[] array = new String[list.size()];
            list.toArray(array);
            this.run(array);
            return;
        }
        HttpServerProvider provider = HttpServerProvider.provider();
        InetSocketAddress address = new InetSocketAddress(Integer.parseInt(port));
        while (true) {
            if (httpServer == null) {
                httpServer = provider.createHttpServer(address, 4);
                httpServer.setExecutor(e);
                httpServer.createContext("/", this);
                httpServer.start();
                System.out.println("HttpServer launched.");
            }
            wait(1000);
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
            return;
        }
        String hostName = http.getRequestURI().getHost();
        System.out.println(hostName);
        if (hostName.equals("storage-jp.llss.io") || hostName.equals("storage-cdn.llss.io")) {
        } else {
            System.err.println("Bad configure.");
            return;
        }
        String fileName = http.getRequestURI().toString();
        Pattern r = Pattern.compile("[a-zA-Z0-9_\\-]+.mp4");
        Matcher m = r.matcher(fileName);
        if (m.find()) {
            System.out.println("Request file " + fileName);
            fileName = "/" + m.group(0);
        } else {
            System.err.println(fileName);
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
        }
        if (printMemory() > 512) {
            System.err.println("Too many memory usage, server will be shutdown.");
            http.getHttpContext().getServer().stop(1);
            httpServer = null;
        }
    }

    public String headersToString(HttpExchange http, boolean requestOrResponse) {
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
        Headers headers = http.getResponseHeaders();
        headers.add("Server", "server1");
        String connection = http.getRequestHeaders().get("Connection").get(0);
        if (connection.equals("close")) {
            headers.add("Connection", "close");
        } else {
            headers.add("Connection", "keep-alive");
        }
        headers.add("Content-type", "video/mp4");
        headers.add("Accept-Ranges", "bytes");
        System.out.println("file: " + f.getPath() + " exists: " + String.valueOf(f.exists()));
        if (f.exists() && f.length() > 0) {
            System.out.println("Request " + http.getRequestURI() + " HIT CACHE!");
            if (onlyHead) {
                System.out.println("Only head");
                http.sendResponseHeaders(200, f.length());
                this.outputReponseHeaders(http);
                return;
            }
            List<String> rangeHeader = http.getRequestHeaders().get("Range");
            int startRange, endRange, rangeLength;
            if (true || rangeHeader == null || rangeHeader.isEmpty()) {
                startRange = 0;
                endRange = (int) f.length();
                rangeLength = endRange - startRange;
                http.sendResponseHeaders(200, f.length());
            } else {
                String[] rangeString = rangeHeader.get(0).replace("bytes=", "").split("-");
                if (rangeString.length == 1) {
                    startRange = Integer.parseInt(rangeString[0]);
                    endRange = (int) f.length();
                } else {
                    startRange = Integer.parseInt(rangeString[0]);
                    endRange = Integer.parseInt(rangeString[1]);
                }
                rangeLength = endRange - startRange;
                System.out.println("Request range " + startRange + "-" + endRange);
                headers.add("Content-Range", startRange + "-" + endRange + "/" + f.length());
                http.sendResponseHeaders(206, rangeLength);

            }

            this.outputReponseHeaders(http);
            byte[] buffer = new byte[rangeLength];
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
            //BufferedOutputStream bos = new BufferedOutputStream(http.getResponseBody());
            OutputStream bos = http.getResponseBody();
            try {
                bis.read(buffer, startRange, rangeLength);

                bos.write(buffer);
            } catch (Exception e) {
                System.err.println("close with exception: " + e.getLocalizedMessage());
                Thread.currentThread().interrupt();
            } finally {
                buffer = null;
                http.close();
                bis.close();

                bos.close();
                System.out.println("connection close");
                System.gc();
            }
        } else {
            if (downloadFile("storage-cdn.llss.io", f, http.getResponseBody())) {
                this.sendFile(http, f, onlyHead);
                return;
            }
            http.sendResponseHeaders(404, 0);
            this.outputReponseHeaders(http);
            throw new FileNotFoundException(f.getCanonicalPath());
        }
    }

    static List<String> downloading = new ArrayList();
    static long lastCall = System.currentTimeMillis();

    private static boolean downloadFile(String host, File outputFile, OutputStream syncStream) {
        try {
            if (System.currentTimeMillis() - lastCall < 1000 || downloading.contains(outputFile.getName())) {
                System.err.println("already downloading!");
                return false;
            } else {
                downloading.add(outputFile.getName());
            }
            URL url = new URL("http://" + host + "/" + outputFile.getName());
            System.out.println("Downloading file " + outputFile.getCanonicalPath() + " from " + url.toString());
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            int mood = request.getResponseCode();
            if (mood == 200 || mood == 206 || mood == 304) {
                //It's ok
            } else {
                System.out.println(headersToString(request));
                throw new IllegalStateException("It's in a bad mood...");
            }
            if (mood != 200) {
                System.err.println("download response code: " + mood);
            }
            InputStream is = request.getInputStream();
            int length = Integer.parseInt(request.getHeaderField("Content-Length"));
            System.out.println("Content-Lengrh: " + length);
            int realLength = is.available();
            if (realLength == 0) {
                System.out.println("Waiting connection intput stream");
            }
            while (realLength == 0) {
                realLength = is.available();
            }
            if (realLength != length) {
                System.err.println("InputStream length " + realLength + " not match HTTP length " + length);
            }
            int bufferSize = length;
            byte[] buffer = new byte[bufferSize];
            BufferedInputStream bis = new BufferedInputStream(is);
            System.out.println("Download connection ok.");
            FileOutputStream fos = new FileOutputStream(outputFile);
            System.out.println("Download output stream ok.");
            JFrame jFrame = new JFrame();
            jFrame.setSize(300, 80);
            JLabel jLabel = new JLabel();
            JProgressBar jProgressBar = new JProgressBar();
            jLabel.setSize(300, 50);
            jLabel.setText("name");
            jLabel.setHorizontalAlignment(SwingConstants.CENTER);
            jLabel.setVerticalAlignment(SwingConstants.CENTER);
            jFrame.add(jLabel);
            jProgressBar.setValue(50);
            jFrame.add(jProgressBar);
            jFrame.addWindowListener(new WindowAdapter() {
                InputStream is;

                @Override
                public void windowClosing(WindowEvent e) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        exception(ex);
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

            int readCount = 0, lastCount = 0, updateRate = 25;
            jLabel.setText(outputFile.getName());
            while (readCount < length) {
                int remain = length - readCount;

                readCount += bis.read(buffer, readCount, remain);
                if (syncStream != null) {

                }
                int process = (int) ((double) readCount / length * 100);

                jProgressBar.setValue(process);
                if (process > 0) {
                    jFrame.setTitle(outputFile.getName());
                }
                process /= updateRate;
                if (process != lastCount) {
                    System.out.println("Downloading..." + process * updateRate + "%");
                    lastCount = process;
                }
            }
            fos.write(buffer);
            bis.close();
            fos.close();
            request.disconnect();
            System.out.println("Download done.");
            System.gc();
            jFrame.dispose();
            downloading.remove(outputFile.getName());
            return true;
        } catch (Exception e) {
            exception(e);
            downloading.remove(outputFile.getName());;
            return false;
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
