package hardware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TableController {
    private static final Logger logger = LoggerFactory.getLogger(TableController.class);

    private Executor executor;

    public TableController(Executor executor) {
        this.executor = executor;
    }

    public String getStatus() throws IOException {
        executor.clearIn();
        return executor.execQuery("STATUS");
    }

    public void setAngleFast(int angle) throws Exception {
        executor.clearIn();
        int currentAngle = getAngle();

        int track = angle - currentAngle;
        if(track > 0 && track <= 360 - track || track < 0 && -track >= 360 + track) {
            setRightRotation();
        } else setLeftRotation();

        setAngle(angle);
    }

    public void setAngle(int angle) throws IOException {
        executor.clearIn();
        if(executor.execQuery(String.format("SETANG%03d", angle)).equals("Started")) {
            while (!getStatus().equals("Stopped"));
        } else throw new IOException("table not started");
    }

    public int getAngle() throws Exception {
        executor.clearIn();
        return Integer.parseInt(executor.execQuery("GETANG"));
    }

    public String getRotation() throws IOException {
        executor.clearIn();
        return executor.execQuery("GETDIR");
    }

    public String setRightRotation() throws IOException {
        executor.clearIn();
        return executor.execQuery("SETDIRR");
    }

    public String setLeftRotation() throws IOException {
        executor.clearIn();
        return executor.execQuery("SETDIRL");
    }

    public String getSpeed() throws IOException {
        executor.clearIn();
        return executor.execQuery("GETSPD");
    }

    public String setSpeed(int spd) throws IOException {
        executor.clearIn();
        return executor.execQuery(String.format("SETSPD%1d", spd));
    }

    public String stop() throws IOException {
        executor.clearIn();
        return executor.execQuery("STOP");
    }

    public boolean checkReady() throws IOException {
        return getStatus().equals("Stopped");
    }
}
