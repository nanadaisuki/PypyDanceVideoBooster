package pypydancevideobooster;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.Thread.sleep;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static pypydancevideobooster.NewJFrame.jLabel1;
import static pypydancevideobooster.NewJFrame.jProgressBar1;

public class PypyDanceVideoBooster implements HttpHandler {

    public static void main(String[] args) {
        new NewJFrame().main(args);
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
        HttpServer httpServer = provider.createHttpServer(address, 1000);
        httpServer.setExecutor(e);
        httpServer.createContext("/", this);
        httpServer.start();
        this.wait();
    }

    ExecutorService e = Executors.newCachedThreadPool();

    @Override
    public void handle(HttpExchange http) {
        if (((ThreadPoolExecutor) e).getActiveCount() > 2) {
            System.out.println("Hey...");
        }
        if (((ThreadPoolExecutor) e).getActiveCount() > 3) {
            System.out.println("You shouldn't do that.");
        }
        if (((ThreadPoolExecutor) e).getActiveCount() > 4) {
            System.out.println("nooooooooooooooooo!!");
            System.exit(0);
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
        if(!hostName.equals("storage-cdn.llss.io")){
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
    }

    private void sendHead(HttpExchange http, File example) {

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
            ByteArrayOutputStream baos = new ByteArrayOutputStream((int) f.length());
            Files.copy(f.toPath(), baos);
            byte[] bytes;
            List<String> rangeHeader = http.getRequestHeaders().get("Range");
            if (true || rangeHeader == null || rangeHeader.isEmpty()) {
                bytes = baos.toByteArray();
                http.sendResponseHeaders(200, bytes.length);
            } else {
                String[] rangeString = rangeHeader.get(0).replace("bytes=", "").split("-");
                int[] range = new int[2];
                int reponseCode = 206;
                int rangeLength = rangeString.length;
                if (rangeLength == 1) {
                    rangeString = new String[]{rangeString[0], String.valueOf(f.length())};
                    if (rangeString[0].equals("0")) {
                        reponseCode = 206;
                    }
                }
                range[0] = Integer.parseInt(rangeString[0]);
                range[1] = Integer.parseInt(rangeString[1]);
                range[1] = rangeLength == 1 ? range[1] : range[1] + 1;
                System.out.println("Request range " + range[0] + "-" + range[1]);
                headers.add("Content-Range", range[0] + "-" + (range[1] - 1) + "/" + f.length());
                bytes = new byte[range[1] - range[0]];
                System.out.println("step1");
                System.arraycopy(baos.toByteArray(), range[0], bytes, 0, range[1] - range[0]);
                System.out.println("step2");
                http.sendResponseHeaders(206, bytes.length);

            }

            this.outputReponseHeaders(http);
            OutputStream responseBody = http.getResponseBody();
            try {
                responseBody.write(bytes);
            } catch (Exception e) {
                exception(e);
            } finally {
                System.out.println("close");
                responseBody.close();
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
            System.out.println("Download connection ok.");
            FileOutputStream fos = new FileOutputStream(outputFile);
            System.out.println("Download output stream ok.");
            int readCount = 0, lastCount = 0, updateRate = 25;
            if (jLabel1 != null) {
                jLabel1.setText(outputFile.getName());
            }
            while (readCount < length) {
                int remain = length - readCount;
                readCount += is.read(buffer, readCount, remain);
                if (syncStream != null) {

                }
                int process = (int) ((double) readCount / length * 100);
                if (jProgressBar1 != null) {
                    jProgressBar1.setValue(process);
                }
                process /= updateRate;
                if (process != lastCount) {
                    System.out.println("Downloading..." + process * updateRate + "%");
                    lastCount = process;
                }
            }
            fos.write(buffer);
            request.disconnect();
            System.out.println("Download done.");
            downloading.remove(outputFile.getName());
            return true;
        } catch (Exception e) {
            exception(e);
            downloading.remove(outputFile.getName());;
            return false;
        }
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
