package lsr;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;

public class Client {
    private final String SERVER_HOST = "alumchat.fun";
    private final int SERVER_PORT = 5222;
    private XMPPConnection connection;

    public Client() {
        ConnectionConfiguration config = new ConnectionConfiguration(SERVER_HOST, SERVER_PORT);
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        config.setDebuggerEnabled(false);

        connection = new XMPPConnection(config);

        try {
            connection.connect();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void login (String username, String password){
        try{
            connection.login(username, password);
        }catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public XMPPConnection getConnection(){
        return connection;
    }


}
