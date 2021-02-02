package hardware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PnaController {

    private static final Logger logger = LoggerFactory.getLogger(PnaController.class);

    private Executor executor;

    public PnaController(Executor executor) {
        this.executor = executor;
    }

    public void tunePna(String measureType, int startFreq, int stopFreq, int stepFreq, int rbw, int average, int smooth) throws Exception {
        executor.execCommand("ABOR");

        executor.execCommand("SYST:FPReset;");
        executor.execCommand("DISP:WIND1:STATE ON");
        executor.execCommand(String.format("CALC1:PAR:DEF:EXT 'Measure', '%s'", measureType));
        executor.execCommand("DISP:WIND1:TRAC1:FEED 'Measure'");

        executor.execCommand(String.format("SENS1:FREQ:STAR %d000000", startFreq));
        executor.execCommand(String.format("SENS1:FREQ:STOP %d000000", stopFreq));
        executor.execCommand(String.format("SENS1:SWE:POIN %d", ((stopFreq - startFreq) / stepFreq) + 1));

        executor.execCommand(String.format("SENS1:BAND:RES %d000", rbw));

        executor.execCommand(String.format("SENS1:AVER:COUN %d", average));
        executor.execCommand("SENS1:AVER:MODE POIN");
        executor.execCommand("SENS1:AVER ON");

        executor.execCommand("CALC:PAR:MNUM 1");
        executor.execCommand(String.format("CALC1:SMO:APER %d", smooth));
        executor.execCommand("CALC1:SMO ON");

        executor.execCommand("CALC1:PAR:SEL 'Measure'");
        executor.execCommand("FORM ASCii,0");
    }

    public ArrayList<Double> getDbData() throws IOException {
        try {
            return Stream.of(executor.execQuery("CALC1:DATA? FDATA")).flatMap(s -> Arrays.stream(s.split(","))
                .map(Double::parseDouble))
                .collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public String getStatusData() throws IOException {
        return executor.execQuery("*IDN?");
    }

    public boolean checkReady() throws IOException {
        executor.clearIn();
        return executor.execQuery("*OPC?").equals("+1");
    }

    public void close() throws IOException {
        executor.close();
    }
}
