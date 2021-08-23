package pypydancevideobooster.https;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static pypydancevideobooster.VideoCacheManager.downloading;

/**
 * Created for http://stackoverflow.com/q/16351413/1266906.
 */
public class Server extends Thread{

    public static void main(String[] args) {
        (new Server(5000)).start();
    }

    final static Object AVATAR_LOCK = new Object();
    static long waitYoutube = 5000;
    static long lastReceive = System.currentTimeMillis();

    public Server(long timeout) {
        super("Server Thread");
        Server.waitYoutube = timeout;
        lastReceive -= waitYoutube;
    }

    ExecutorService e = Executors.newCachedThreadPool();

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        while (true) {
            if (serverSocket == null) {
                System.out.println("Launching LimitServer");
                try {
                    serverSocket = new ServerSocket(9999);
                    System.out.println("LimitServer launched.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Socket socket;
            try {
                while ((socket = serverSocket.accept()) != null) {
                    e.submit(new Handler(socket));
                }
            } catch (IOException runningException) {
                runningException.printStackTrace();  // TODO: implement catch
            }
        }
    }

    public static class Handler extends Thread {

        public static final Pattern CONNECT_PATTERN = Pattern.compile("CONNECT (.+):(.+) HTTP/(1\\.[01])",
                Pattern.CASE_INSENSITIVE);
        private final Socket clientSocket;
        private boolean previousWasR = false;

        public Handler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            String request = "";
            final boolean isYoutube;
            try {
                request = readLine(clientSocket);
                //System.out.println(request);
                if (request.indexOf("youtube") > 0 || request.indexOf("google") > 0 || request.indexOf("ytimg") > 0) {
                    isYoutube = true;
                    lastReceive = System.currentTimeMillis();
                } else {
                    isYoutube = false;
                }
                Matcher matcher = CONNECT_PATTERN.matcher(request);
                if (matcher.matches()) {
                    String header;
                    do {
                        header = readLine(clientSocket);
                    } while (!"".equals(header));
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(clientSocket.getOutputStream(),
                            "ISO-8859-1");

                    final Socket forwardSocket;
                    try {
                        forwardSocket = new Socket(matcher.group(1), Integer.parseInt(matcher.group(2)));
                        //System.out.println(forwardSocket);
                    } catch (IOException | NumberFormatException e) {
                        e.printStackTrace();  // TODO: implement catch
                        outputStreamWriter.write("HTTP/" + matcher.group(3) + " 502 Bad Gateway\r\n");
                        outputStreamWriter.write("Proxy-agent: Simple/0.1\r\n");
                        outputStreamWriter.write("\r\n");
                        outputStreamWriter.flush();
                        return;
                    }
                    try {
                        outputStreamWriter.write("HTTP/" + matcher.group(3) + " 200 Connection established\r\n");
                        outputStreamWriter.write("Proxy-agent: Simple/0.1\r\n");
                        outputStreamWriter.write("\r\n");
                        outputStreamWriter.flush();

                        Thread remoteToClient = new Thread() {
                            @Override
                            public void run() {
                                forwardData(forwardSocket, clientSocket, isYoutube);
                            }
                        };
                        remoteToClient.start();
                        try {
                            if (previousWasR) {
                                int read = clientSocket.getInputStream().read();
                                if (read != -1) {
                                    if (read != '\n') {
                                        forwardSocket.getOutputStream().write(read);
                                    }
                                    forwardData(clientSocket, forwardSocket, isYoutube);
                                } else {
                                    if (!forwardSocket.isOutputShutdown()) {
                                        forwardSocket.shutdownOutput();
                                    }
                                    if (!clientSocket.isInputShutdown()) {
                                        clientSocket.shutdownInput();
                                    }
                                }
                            } else {
                                forwardData(clientSocket, forwardSocket, isYoutube);
                            }
                        } finally {
                            try {
                                remoteToClient.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();  // TODO: implement catch
                            }
                        }
                    } finally {
                        forwardSocket.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();  // TODO: implement catch
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();  // TODO: implement catch
                }
                if (request.indexOf("youtube") > 0 || request.indexOf("google") > 0 || request.indexOf("ytimg") > 0) {
                    if (System.currentTimeMillis() - lastReceive > waitYoutube) {
                        synchronized (AVATAR_LOCK) {
                            AVATAR_LOCK.notifyAll();
                        }
                        System.out.println("Remove all load limits.");
                    }
                }
            }
        }

        static long antiSpam = System.currentTimeMillis();

        private static void forwardData(Socket inputSocket, Socket outputSocket, boolean isYoutube) {
            if (isYoutube) {
                System.out.println("Avatar will be limit to load, because youtube is running.");
            }
            try {
                InputStream inputStream = inputSocket.getInputStream();
                try {
                    OutputStream outputStream = outputSocket.getOutputStream();
                    try {
                        byte[] buffer = new byte[4096];
                        int read;
                        do {
                            if ((!isYoutube && System.currentTimeMillis() - lastReceive < waitYoutube) || !downloading.isEmpty()) {
                                synchronized (AVATAR_LOCK) {
                                    if (System.currentTimeMillis() - antiSpam > waitYoutube) {
                                        System.out.println("Avatar has been limit to load, because youtube is running.");
                                        antiSpam = System.currentTimeMillis();
                                    }
                                    AVATAR_LOCK.wait(waitYoutube);
                                }
                            }
                            read = inputStream.read(buffer);
                            if (isYoutube) {
                                lastReceive = System.currentTimeMillis();
                            }
                            if (read > 0) {
                                outputStream.write(buffer, 0, read);
                                if (inputStream.available() < 1) {
                                    outputStream.flush();
                                }
                            }
                        } while (read >= 0);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        if (!outputSocket.isOutputShutdown()) {
                            outputSocket.shutdownOutput();
                        }
                    }
                } finally {
                    if (!inputSocket.isInputShutdown()) {
                        inputSocket.shutdownInput();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();  // TODO: implement catch
            }
        }

        private String readLine(Socket socket) throws IOException {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int next;
            readerLoop:
            while ((next = socket.getInputStream().read()) != -1) {
                if (previousWasR && next == '\n') {
                    previousWasR = false;
                    continue;
                }
                previousWasR = false;
                switch (next) {
                    case '\r':
                        previousWasR = true;
                        break readerLoop;
                    case '\n':
                        break readerLoop;
                    default:
                        byteArrayOutputStream.write(next);
                        break;
                }
            }
            return byteArrayOutputStream.toString("ISO-8859-1");
        }
    }
}
