package pypydancevideobooster;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadLimiter {

    double byteRatePerMil = 0;

    public DownloadLimiter setMbRate(long mbRatePerSec) {
        long kbRatePerSec = mbRatePerSec * 1024;
        long byteRatePerSec = kbRatePerSec * 1024;
        this.byteRatePerMil = byteRatePerSec / 1000D;
        return this;
    }

    public long getMbRatePerSec() {
        double byteRatePerSec = byteRatePerMil * 1000;
        long kbRatePerSec = (long) (byteRatePerSec / 1024);
        long mbRatePerSec = kbRatePerSec / 1024;
        return mbRatePerSec;
    }

    long lastLen = 0;
    long lastTime = 0;

    public void limitRate(long len) {
        long nowTime = System.currentTimeMillis();
        long takeTime = nowTime - lastTime;
        long byteRate = lastLen / takeTime;
        if (byteRate > byteRatePerMil) {
            long limit = (long) (byteRate - byteRatePerMil) * takeTime;
            try {
                System.out.println(limit);
                Thread.sleep(limit);
            } catch (InterruptedException ex) {
                Logger.getLogger(DownloadLimiter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{

        }
        lastLen = len;
    }

}
