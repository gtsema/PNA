package testServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TableServer {
    private String responce = "table server: ";

    private String ip;
    private int port;

    private Socket client;
    private ServerSocket server;

    private BufferedReader in;
    private BufferedWriter out;

    private enum rotateDirection {right, left};

    private volatile int currentAngle;
    private rotateDirection direction = rotateDirection.right;
    private int speed = 500;

    private Thread rotating = new Thread();

    private boolean isRun;

    public TableServer(String ip, int port) throws IOException {
        this.ip = ip;
        this.port = port;
    }

    public void start() {
        isRun = true;
        while (isRun) {
            currentAngle = 2;
            try {
                server = new ServerSocket();
                server.bind(new InetSocketAddress(ip, port));
                System.out.println(responce + "on");

                client = server.accept();
                System.out.println(responce + "connect");

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

                        } else if(cmd.toString().equals("STATUS")) {
                            System.out.println(responce + "<- STATUS");
                            if(rotating.isAlive()) send("Busy");
                            else send("Stopped");

                        } else if(cmd.toString().equals("GETANG")) {
                            System.out.println(responce + "<- GETANG");
                            send(String.valueOf(currentAngle));

                        } else if(cmd.toString().startsWith("SETANG")) {
                            System.out.println(responce + "<- SETANG");
                            send("Started");
                            int angle = Integer.parseInt(cmd.toString().replaceAll("\\D", ""));
                            setang(angle);

                        } else if(cmd.toString().equals("SETDIRR")) {
                            System.out.println(responce + "<- SETDIRR");
                            direction = rotateDirection.right;
                            send("Right");

                        } else if(cmd.toString().equals("SETDIRL")) {
                            System.out.println(responce + "<- SETDIRL");
                            direction = rotateDirection.left;
                            send("Left");

                        } else if(cmd.toString().equals("STOP")) {
                            System.out.println(responce + "<- STOP");
                            rotating.interrupt();
                        }

                        cmd.delete(0, cmd.length());
                    }
                }
            } catch (IOException e) {
                //ignore
            } finally {
                System.out.println("table server: off");
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

    private void setang(int angle) {
        rotating = new Thread(new Runnable() {
            int factor = direction == rotateDirection.right ? 1 : -1;
            @Override
            public synchronized void run() {
                for(; currentAngle != angle; currentAngle += factor ) {
                    try { Thread.sleep(speed); } catch (Exception ignore) {};
                    System.out.println("Current angle: " + currentAngle);
                }
                System.out.println("Current angle: " + currentAngle);
            }
        });

        rotating.start();
    }

    private void send(String responce) {
        try {
            out.write(responce + '\n');
            out.flush();
            System.out.println("table server: -> " + responce);
        } catch (IOException e) {
            System.err.println("Error sending message");
        }
    }
}
