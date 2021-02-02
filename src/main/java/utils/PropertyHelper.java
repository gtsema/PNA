package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyHelper {

    private static final Logger logger = LoggerFactory.getLogger(PropertyHelper.class);

    private static final String PROPERTIES_FILE = "config.properties";

    private static final String PNA_DEFAULT_IP = "192.168.0.1";
    private static final int PNA_DEFAULT_PORT = 5025;
    private static final String TABLE_DEFAULT_IP = "192.168.0.2";
    private static final int TABLE_DEFAULT_PORT = 8080;

    private static final int DEFAULT_RBW = 10;
    private static final int DEFAULT_START_FREQ = 10;
    private static final int DEFAULT_STOP_FREQ = 26500;
    private static final int DEFAULT_STEP_FREQ = 100;
    private static final int DEFAULT_AVERAGE = 1;
    private static final int DEFAULT_SMOOTH = 10;
    private static final int DEFAULT_START_ANGLE = 0;
    private static final int DEFAULT_STOP_ANGLE = 365;
    private static final int DEFAULT_STEP_ANGLE = 10;

    public static String getPnaIp() {
        try {
            String pnaIp = getProperty("pnaIp");
            if(!InputDataChecker.isValidIp(pnaIp)) throw new IllegalArgumentException();
            else return pnaIp;
        } catch (IOException e) {
            logger.debug(e.getMessage() + " Will be used default PNA IP: " + PNA_DEFAULT_IP);
            return PNA_DEFAULT_IP;
        } catch (IllegalArgumentException e) {
            logger.debug("In file of properties is incorrectly PNA IP. Will be used default PNA IP: " + PNA_DEFAULT_IP);
            return PNA_DEFAULT_IP;
        }
    }

    public static int getPnaPort() {
        try {
            String pnaPort = getProperty("pnaPort");
            if(!InputDataChecker.isValidPort(pnaPort)) throw new NumberFormatException();
            else return Integer.parseInt(pnaPort);
        } catch (IOException e) {
            logger.debug(e.getMessage() + " Will be used default PNA Port: " + PNA_DEFAULT_PORT);
            return PNA_DEFAULT_PORT;
        } catch (NumberFormatException e) {
            logger.debug("In file of properties is incorrectly PNA Port. Will be used default PNA Port: " + PNA_DEFAULT_PORT);
            return PNA_DEFAULT_PORT;
        }
    }

    public static String getTableIp() {
        try {
            String tableIp = getProperty("tableIp");
            if(!InputDataChecker.isValidIp(tableIp)) throw new IllegalArgumentException();
            else return tableIp;
        } catch (IOException e) {
            logger.debug(e.getMessage() + " Will be used default table IP: " + TABLE_DEFAULT_IP);
            return TABLE_DEFAULT_IP;
        } catch (IllegalArgumentException e) {
            logger.debug("In file of properties is incorrectly Table IP. Will be used default Table IP: " + TABLE_DEFAULT_IP);
            return TABLE_DEFAULT_IP;
        }
    }

    public static int getTablePort() {
        try {
            String tablePort = getProperty("tablePort");
            if(!InputDataChecker.isValidPort(tablePort)) throw new NumberFormatException();
            else return Integer.parseInt(tablePort);
        } catch (IOException e) {
            logger.debug(e.getMessage() + " Will be used default table Port: " + TABLE_DEFAULT_PORT);
            return TABLE_DEFAULT_PORT;
        } catch (NumberFormatException e) {
            logger.debug("In file of properties is incorrectly table Port. Will be used default table Port: " + TABLE_DEFAULT_PORT);
            return TABLE_DEFAULT_PORT;
        }
    }

    public static Integer getStartFreq() {
        try {
            String startFreq = getProperty("startFreq");
            if(!InputDataChecker.isValidFreq(startFreq)) throw new NumberFormatException();
            else return Integer.parseInt(startFreq);
        } catch (IOException e) {
            logger.debug(e.getMessage() + " Will be used default startFreq: " + DEFAULT_START_FREQ);
            return DEFAULT_START_FREQ;
        } catch (NumberFormatException e) {
            logger.debug("In file of properties is incorrectly startFreq. Will be used default startFreq: " + DEFAULT_START_FREQ);
            return DEFAULT_START_FREQ;
        }
    }

    public static Integer getStopFreq() {
        try {
            String stopFreq = getProperty("stopFreq");
            if(!InputDataChecker.isValidFreq(stopFreq)) throw new NumberFormatException();
            else return Integer.parseInt(stopFreq);
        } catch (IOException e) {
            logger.debug(e.getMessage() + " Will be used default stopFreq: " + DEFAULT_STOP_FREQ);
            return DEFAULT_STOP_FREQ;
        } catch (NumberFormatException e) {
            logger.debug("In file of properties is incorrectly stopFreq. Will be used default stopFreq: " + DEFAULT_STOP_FREQ);
            return DEFAULT_STOP_FREQ;
        }
    }

    public static Integer getStepFreq() {
        try {
            String stepFreq = getProperty("stepFreq");
            if(!InputDataChecker.isValidStepFreq(stepFreq)) throw new NumberFormatException();
            else return Integer.parseInt(stepFreq);
        } catch (IOException e) {
            logger.debug(e.getMessage() + " Will be used default stepFreq: " + DEFAULT_STEP_FREQ);
            return DEFAULT_STEP_FREQ;
        } catch (NumberFormatException e) {
            logger.debug("In file of properties is incorrectly stepFreq. Will be used default stepFreq: " + DEFAULT_STEP_FREQ);
            return DEFAULT_STEP_FREQ;
        }
    }

    public static Integer getRbw() {
        try {
            String rbw = getProperty("RBW");
            if(!InputDataChecker.isValidRbw(rbw)) throw new NumberFormatException();
            else return Integer.parseInt(rbw);
        } catch (IOException e) {
            logger.debug(e.getMessage() + " Will be used default RBW: " + DEFAULT_RBW);
            return DEFAULT_RBW;
        } catch (NumberFormatException e) {
            logger.debug("In file of properties is incorrectly RBW. Will be used default RBW: " + DEFAULT_RBW);
            return DEFAULT_RBW;
        }
    }

    public static Integer getAverage() {
        try {
            String average = getProperty("average");
            if(!InputDataChecker.isValidAverage(average)) throw new NumberFormatException();
            else return Integer.parseInt(average);
        } catch (IOException e) {
            logger.debug(e.getMessage() + " Will be used default average: " + DEFAULT_AVERAGE);
            return DEFAULT_AVERAGE;
        } catch (NumberFormatException e) {
            logger.debug("In file of properties is incorrectly average. Will be used default average: " + DEFAULT_AVERAGE);
            return DEFAULT_AVERAGE;
        }
    }

    public static Integer getSmooth() {
        try {
            String smooth = getProperty("smooth");
            if(!InputDataChecker.isValidSmooth(smooth)) throw new NumberFormatException();
            else return Integer.parseInt(smooth);
        } catch (IOException e) {
            logger.debug(e.getMessage() + " Will be used default smooth: " + DEFAULT_SMOOTH);
            return DEFAULT_SMOOTH;
        } catch (NumberFormatException e) {
            logger.debug("In file of properties is incorrectly smooth. Will be used default smooth: " + DEFAULT_SMOOTH);
            return DEFAULT_SMOOTH;
        }
    }

    public static Integer getStartAngle() {
        try {
            String startAngle = getProperty("startAngle");
            if(!InputDataChecker.isValidAngle(startAngle)) throw new NumberFormatException();
            else return Integer.parseInt(startAngle);
        } catch (IOException e) {
            logger.debug(e.getMessage() + " Will be used default startAngle: " + DEFAULT_START_ANGLE);
            return DEFAULT_START_ANGLE;
        } catch (NumberFormatException e) {
            logger.debug("In file of properties is incorrectly startAngle. Will be used default startAngle: " + DEFAULT_START_ANGLE);
            return DEFAULT_START_ANGLE;
        }
    }

    public static Integer getStopAngle() {
        try {
            String stopAngle = getProperty("stopAngle");
            if(!InputDataChecker.isValidAngle(stopAngle)) throw new NumberFormatException();
            else return Integer.parseInt(stopAngle);
        } catch (IOException e) {
            logger.debug(e.getMessage() + " Will be used default stopAngle: " + DEFAULT_STOP_ANGLE);
            return DEFAULT_STOP_ANGLE;
        } catch (NumberFormatException e) {
            logger.debug("In file of properties is incorrectly stopAngle. Will be used default stopAngle: " + DEFAULT_STOP_ANGLE);
            return DEFAULT_STOP_ANGLE;
        }
    }

    public static Integer getStepAngle() {
        try {
            String stepAngle = getProperty("stepAngle");
            if(!InputDataChecker.isValidStepAngle(stepAngle)) throw new NumberFormatException();
            else return Integer.parseInt(stepAngle);
        } catch (IOException e) {
            logger.debug(e.getMessage() + " Will be used default stepAngle: " + DEFAULT_STEP_ANGLE);
            return DEFAULT_STEP_ANGLE;
        } catch (NumberFormatException e) {
            logger.debug("In file of properties is incorrectly stepAngle. Will be used default stepAngle: " + DEFAULT_STEP_ANGLE);
            return DEFAULT_STEP_ANGLE;
        }
    }

    private static String getProperty(String attribute) throws IOException {
        try(FileInputStream propFile = new FileInputStream(PROPERTIES_FILE)) {
            Properties properties = new Properties();
            properties.load(propFile);
            return properties.getProperty(attribute);
        } catch (IOException e) {
            throw new IOException("Can not read the properties file.");
        }
    }
}
