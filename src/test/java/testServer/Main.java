package testServer;

public class Main {
    public static void main(String[] args) throws Exception {
        PnaServer pnaServer = new PnaServer("127.0.0.2", 5025);
        TableServer tableServer = new TableServer("127.0.0.3", 666);

        new Thread(pnaServer::start).start();
        new Thread(tableServer::start).start();
    }
}
