package utils;

import org.apache.commons.validator.routines.InetAddressValidator;

public class InputDataChecker {

    private static final InetAddressValidator validator = InetAddressValidator.getInstance();

    public static boolean isValidIp(String ip) {
        return validator.isValidInet4Address(ip);
    }

    public static boolean isValidPort(String port_) {
        try {
            int port = Integer.parseInt(port_);
            return port >= 0 && port <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidFreq(String freq_) {
        try {
            int freq = Integer.parseInt(freq_);
            return freq >= 10 && freq <= 26500;
        } catch (NumberFormatException e) {return false; }
    }

    public static boolean isValidStepFreq(String stepFreq_) {
        try {
            int stepFreq = Integer.parseInt(stepFreq_);
            return stepFreq >= 1 && stepFreq <= 13250;
        } catch (NumberFormatException e) {return false; }
    }

    public static boolean isValidRbw(String rbw_) {
        try {
            int rbw = Integer.parseInt(rbw_);
            return rbw >= 1 && rbw <= 1000;
        } catch (NumberFormatException e) {return false; }
    }

    public static boolean isValidAverage(String mean_) {
        try {
            int mean = Integer.parseInt(mean_);
            return mean >= 1 && mean <= 65536;
        } catch (NumberFormatException e) {
            return false;}
    }

    public static boolean isValidSmooth(String smooth_) {
        try {
            int smooth = Integer.parseInt(smooth_);
            return smooth >= 1 && smooth <= 25;
        } catch (NumberFormatException e) {return false; }
    }

    public static boolean isValidAngle(String angle_) {
        try {
            int angle = Integer.parseInt(angle_);
            return angle >= 0 && angle <= 359;
        } catch (NumberFormatException e) {return false; }
    }

    public static boolean isValidStepAngle(String stepAngle_) {
        try {
            int stepAngle = Integer.parseInt(stepAngle_);
            return stepAngle >= 1 && stepAngle <= 180;
        } catch (NumberFormatException e) {return false; }
    }
}
