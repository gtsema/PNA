package hardware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Executor implements java.io.Closeable {
    private static final Logger logger = LoggerFactory.getLogger(Executor.class);

    private final int TIMEOUT = 100;
    private final int trySleep = 1000;
    private final int tryCount = 5;

    private Socket socket;
    private String ip;
    private int port;

    private BufferedWriter out;
    private BufferedReader in;

    public Executor(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void connect() throws IOException {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), TIMEOUT);
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            throw new IOException("failed to connect.");
        }
    }

    public void clearIn() throws IOException {
        if(in.ready()) in.readLine();
    }

    public String execQuery(String query) throws IOException {
        try {
            out.write(query + '\n');
            out.flush();

            int count = tryCount;
            while(true) {
                if(in.ready()) {
                    return in.readLine();
                } else {
                    if(count-- == 0) throw new IOException("no response is received.");
                    else Thread.sleep(trySleep);
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new IOException(e.getMessage());
        }
    }

    public void execCommand(String cmd) throws IOException {
        out.write(cmd + '\n');
        out.flush();
    }

    @Override
    public synchronized void close() throws IOException {
        if(socket != null) socket.close();
        if(in != null) in.close();
        if(out != null) out.close();
    }
}
