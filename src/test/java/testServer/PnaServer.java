package testServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;

public class PnaServer {
    private String responce = "pna server: ";

    private String ip;
    private int port;

    private Socket client;
    private ServerSocket server;

    private BufferedReader in;
    private BufferedWriter out;

    Random rnd = new Random();
    int pointsFreq;
    int minData = 50;
    int maxData = 60;

    private boolean isRun;

    public PnaServer(String ip, int port) throws IOException {
        this.ip = ip;
        this.port = port;
    }

    public void start() {
        isRun = true;
        while(isRun) {
            try {
                server = new ServerSocket();
                server.bind(new InetSocketAddress(ip, port));
                System.out.println("pna server: on");

                client = server.accept();
                System.out.println("pna server: connect");

                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

                StringBuilder cmd = new StringBuilder();

                while (true) {
                    int ch = in.read();

                    if(ch == -1) {
                        break;
                    } else if(ch != 10) {
                        cmd.append((char)ch);
                    } else {
                        if(cmd.toString().equals("stop")) {
                            System.out.println(responce + "<- stop");
                            isRun = false;
                        } else if(cmd.toString().equals("*OPC?")) {
                            System.out.println(responce + "<- *OPC?");
                            send("+1");
                        } else if(cmd.toString().equals("*IDN?")) {
                            System.out.println(responce + "<- *IDN?");
                            send("Agilent Technologies,N5222A,US51220131,A.09.90.02");
                        } else if(cmd.toString().startsWith("CALC1:PAR:DEF:EXT 'Measure'")) {
                            System.out.println(responce + "Measure type - " + cmd.toString().split(",")[1]);
                        } else if(cmd.toString().startsWith("SENS1:FREQ:STAR")) {
                            System.out.println(responce + "Start freq. - " + cmd.toString().split(" ")[1]);
                        } else if(cmd.toString().startsWith("SENS1:FREQ:STOP")) {
                            System.out.println(responce + "Stop freq. - " + cmd.toString().split(" ")[1]);
                        } else if(cmd.toString().startsWith("SENS1:SWE:POIN")) {
                            pointsFreq = Integer.parseInt(cmd.toString().split(" ")[1]);
                            System.out.println(responce + "Points freq. - " + pointsFreq);
                        } else if(cmd.toString().startsWith("SENS1:BAND:RES")) {
                            System.out.println(responce + "rbw - " + cmd.toString().split(" ")[1]);
                        } else if(cmd.toString().startsWith("SENS1:AVER:COUN")) {
                            System.out.println(responce + "Average - " + cmd.toString().split(" ")[1]);
                        } else if(cmd.toString().startsWith("CALC1:SMO:APER")) {
                            System.out.println(responce + "Smooth - " + cmd.toString().split(" ")[1]);
                        } else if(cmd.toString().equals("CALC1:DATA? FDATA")) {
                            System.out.println(responce + "receive data");
                            String measureData = rnd.doubles().limit(pointsFreq).map(n -> (n * (maxData - minData)) + minData).mapToObj(n -> String.format(Locale.US, "%.3f", n)).collect(Collectors.joining(", "));
                            send(measureData);
                        } else System.out.println(responce + "wrong command");

                        cmd.delete(0, cmd.length());
                    }
                }
            } catch (IOException e) {
                //ignore
            } finally {
                System.out.println("pna server: off");
                try {
                    in.close();
                    out.close();
                    server.close();
                    client.close();
                } catch (IOException e) {
                    System.err.println("Unable to close server: " + e.getMessage());
                }
            }
        }
    }

    private void send(String responce) {
        try {
            out.write(responce + '\n');
            out.flush();
            System.out.println("pna server: -> " + responce);
        } catch (IOException e) {
            System.err.println("Error sending message");
        }
    }
}
