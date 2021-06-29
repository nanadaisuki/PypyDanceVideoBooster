package pypydancevideobooster.https;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import static java.lang.Thread.sleep;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

public class Server2 implements HttpHandler {

    File h;
    File ho = new File("C:\\Windows\\System32\\drivers\\etc\\hosts");

    public Server2() {
        i();
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        System.out.println("hello pypydance!");
        String fileName = he.getRequestURI().toString();
        Pattern r = Pattern.compile("[a-zA-Z0-9_\\-]+.mp4");
        Matcher m = r.matcher(fileName);
        if (m.find()) {
            System.out.println("Request file " + fileName);
            fileName = "/" + m.group(0);
        } else {
            System.err.println(fileName);
            return;
        }
        he.getResponseHeaders().add("Location", "http://storage-cdn.llss.io" + fileName);
        he.sendResponseHeaders(307, 0);
        he.close();
    }

    private void i() {
        for (File f : new File(System.getProperty("user.home")).listFiles()) {
            String n = f.getName();
            String s = n.substring(n.lastIndexOf(".") + 1);
            if (s.equals("keystore")) {
                h = f;
                return;
            }
        }
        h = null;
    }

    public void s() throws Exception {
        if (h == null) {
            //si();
            System.err.println("keystore not found! first run \"keytool -genkey -keyalg rsa\"");
            try {
                sleep(10000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Server2.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.exit(1);
        }
        System.out.println("found,enter keystore password: ");
        Scanner sc = new Scanner(System.in);
        String pw = "123456";
        System.out.println("password: " + pw + " , launching...");
        ats();
        HttpsServer https = HttpsServer.create(new InetSocketAddress(InetAddress.getByName("jd.pypy.moe"), 443), 0);
        https.createContext("/", this);
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(h), pw.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, pw.toCharArray());
        SSLContext sslContext = SSLContext.getInstance("SSLv3");
        sslContext.init(kmf.getKeyManagers(), null, null);
        HttpsConfigurator httpsConfigurator = new HttpsConfigurator(sslContext);
        https.setHttpsConfigurator(httpsConfigurator);
        https.start();
        System.out.println("Server2 launched.");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("exit");
                rfs();
            }

        });
    }

    public void ats() {
        if (cts()) {
            return;
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ho, true)));
            bw.write("\r\n127.0.0.1 jd.pypy.moe\r\n");
            bw.close();
            cts();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Server2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Server2.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(Server2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public boolean cts() {
        boolean b = false;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(ho)));
            String read;
            while ((read = br.readLine()) != null) {
                System.out.println(read);
                if (read.equals("127.0.0.1 jd.pypy.moe")) {
                    b = true;
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Server2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Server2.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(Server2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return b;
    }

    public void rfs() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(ho)));
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = br.readLine()) != null) {
                System.out.println(s);
                if (s.equals("127.0.0.1 jd.pypy.moe")) {
                } else {
                    sb.append(s);
                    sb.append("\r\n");
                }
            }
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ho, true)));
            bw.write(sb.toString());
            bw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Server2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Server2.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(Server2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void si() {
        try {
            Runtime r = Runtime.getRuntime();
            String k = "keytool -genkey -keyalg rsa";
            Process p = r.exec(k);
            InputStream is = p.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            Thread t = new Thread() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        if (br != null) {
                            try {
                                String read = br.readLine();
                                if (read == null) {
                                    return;
                                }
                                System.out.println(read);
                            } catch (IOException ex) {
                                Logger.getLogger(Server2.class.getName()).log(Level.SEVERE, null, ex);
                                System.err.println("Read failed!");
                                return;
                            }
                        }
                    }
                }

            };
            t.start();
            OutputStream os = p.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            Scanner sc = new Scanner(System.in);
            while (p.isAlive()) {
                String rp = sc.nextLine();
                bw.write(rp);
                bw.newLine();
                bw.flush();
            }
            System.out.println("Step1 Done. ");
        } catch (IOException ex) {
            Logger.getLogger(Server2.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        } finally {

        }

    }
}
