package arem;

//Imports para el lab 3 algoritmos de routing
import arem.Nodo;
import java.io.*;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;
//-------------------------------------------

import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.RoomInfo;
//import org.jivesoftware.smackx.si.packet.StreamInitiation.File;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;
import org.jxmpp.util.XmppStringUtils;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.util.Collection;
import java.util.Scanner;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaCollector;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.roster.AbstractRosterListener;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;

/**
 * Hello world!
 */
public final class App {
    private App() {
    }

    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */

    //#region Lab3 functions
    public static int findIndex(int arr[], int t)
    {
  
        // if array is Null
        if (arr == null) {
            return -1;
        }
  
        // find length of array
        int len = arr.length;
        int i = 0;
  
        // traverse in the array
        while (i < len) {
  
            // if the i-th element is t
            // then return the index
            if (arr[i] == t) {
                return i;
            }
            else {
                i = i + 1;
            }
        }
        return -1;
    }
    
    public static void showNetworkMatrix(int m[][],int n){
        System.out.println("\n\nNetwork Matrix 1st row and colomn showing nodes(or hops) id\n\n");
        for(int i=0;i<=n;i++){
            for(int j=0;j<=n;j++){
                if(i==0&&j==0)
                    System.out.print("nodes-   ");
                else if(j==0)System.out.print(m[i][j]+"        ");
                else System.out.print(m[i][j]+"    ");
            }
            System.out.println();
        }
    }

    //Flooding algorithm
    public static void FloodAlgo(Nodo nodos[], int s, int d, int n, String msg, EntityBareJid jid, ChatManager chatManager) throws XmppStringprepException, NotConnectedException, InterruptedException{
        System.out.println("\n"+"Nodo fuente: "+ nodos[s-1].getNodo() +"\n");

        //Es la variable que va a llevar listado de todos los nodos recorridos
        String listNd = "";
        String message = "Mensaje: " + msg;

        boolean recibido = false;
        /* Se crea un buffer para poder llevar el control de los nodos que se van a ejecutar y tiene el tamaño de todos los nodos del grafo
         * menos del nodo source que es de donde parte el algoritmo
        */
        int [] Buffer = new int[n-1];
        int k; 
        //Aqui buscamos al nodo source y empezamos el proceso de ir en todas direcciones con sus vecinos
        for (int i = 0; i <= n - 1; i++){
            if (nodos[i].getIdNodo() == s){
                System.out.println("Nodo " + nodos[i].getNodo() + " Saltos: " + nodos[i].getSaltos());
                int size = nodos[i].getTrayecto().length;
                for (int j = 0; j <= size - 1; j++){
                    String newMessage;
                    Buffer[j] = nodos[i].getTrayecto()[j];
                    nodos[Buffer[j] - 1].setRecibido(true);
                    nodos[Buffer[j] - 1].setSatlos(nodos[i].getSaltos());
                    System.out.println("Nodo: " + nodos[i].getNodo() + " -----> " + "Nodo: " + nodos[Buffer[j] - 1].getNodo());
                    listNd += "Nodo: " + nodos[i].getNodo() + " -----> " + "Nodo: " + nodos[Buffer[j] - 1].getNodo() + "\n";

                    //Mensaje enviado al nodo
                    jid = JidCreate.entityBareFrom(nodos[Buffer[j] - 1].getNames());
                    Chat chat = chatManager.chatWith(jid);

                    //Variable que reune toda la info para enviar a los nodos
                    newMessage = nodos[Buffer[j] - 1].Message(nodos[i].getNodo(), nodos[d - 1].getNodo()) + listNd + message;

                    chat.send(newMessage);

                    if (nodos[Buffer[j] - 1].getIdNodo() == d){
                        recibido = true;
                        System.out.println("\n" + "Saltos dados desde nodo fuente: " + nodos[Buffer[j] - 1].getSaltos());
                        System.out.println("\n" + "Mensaje entregado al Nodo: " + nodos[Buffer[j] - 1].getNodo() + "\n");
                        break;
                    }
                }
                break;
            }
        }
        
        //
        int turno = 0;
        k = Buffer[turno]; 

        while (!recibido) {
            for (int i = 0; i <= nodos[k-1].getTrayecto().length - 1; i++){
                int indice = nodos[k-1].getTrayecto()[i];
                int index = findIndex(Buffer, 0);
                if (nodos[indice-1].getIdNodo() != s ){
                    if(nodos[indice-1].getIdNodo() != d){
                        if (!nodos[indice-1].getRecibido()){
                            String newMessage;
                            Buffer[index] = nodos[indice-1].getIdNodo();
                            nodos[indice - 1].setRecibido(true);
                            nodos[indice - 1].setSatlos(nodos[k-1].getSaltos());
                            System.out.println("Nodo: " + nodos[k-1].getNodo() + " -----> " + "Nodo: " + nodos[indice-1].getNodo());
                            listNd += "Nodo: " + nodos[k-1].getNodo() + " -----> " + "Nodo: " + nodos[indice-1].getNodo() + "\n";

                            //Mensaje enviado al nodo
                            jid = JidCreate.entityBareFrom(nodos[indice-1].getNames());
                            Chat chat = chatManager.chatWith(jid);

                            //Variable que reune toda la info para enviar a los nodos
                            newMessage = nodos[indice-1].Message(nodos[i].getNodo(), nodos[d - 1].getNodo()) + listNd + message;

                            chat.send(newMessage);
                        }
                    }else{
                        String newMessage;
                        Buffer[index] = nodos[indice-1].getIdNodo();
                        nodos[indice - 1].setRecibido(true);
                        nodos[indice - 1].setSatlos(nodos[k-1].getSaltos());
                        recibido = true;
                        System.out.println("Nodo: " + nodos[k-1].getNodo() + " -----> " + "Nodo: " + nodos[indice-1].getNodo());
                        listNd += "Nodo: " + nodos[k-1].getNodo() + " -----> " + "Nodo: " + nodos[indice-1].getNodo() + "\n";

                        //Mensaje enviado al nodo
                        jid = JidCreate.entityBareFrom(nodos[indice-1].getNames());
                        Chat chat = chatManager.chatWith(jid);

                        //Variable que reune toda la info para enviar a los nodos
                        newMessage = nodos[indice-1].Message(nodos[i].getNodo(), nodos[d - 1].getNodo()) + listNd + message;

                        chat.send(newMessage);

                        System.out.println("\n" + "Saltos dados desde nodo fuente: " + nodos[indice - 1].getSaltos());
                        System.out.println("\n" + "Mensaje entregado al Nodo: " + nodos[indice-1].getNodo() + "\n");
                    }
                }
            }
            turno += 1;
            k = Buffer[turno];
        }
        System.out.println("Buffer de nodos: " + Arrays.toString(Buffer));
    }
    //#endregion

    public static List<Object> getGrafo() throws FileNotFoundException, IOException, ParseException{
        JSONParser parser = new JSONParser();

        //Se inicializa el json que contiene la topologia del grafo
        Object obj = parser.parse(new FileReader("/home/arem/Documents/Universidad/Decimo semestre/Redes/Proyecto 1/Chat_XMPP/client-xmpp/src/assets/topologia.json"));
        JSONObject jsonObject = (JSONObject)obj;
        String type = (String)jsonObject.get("type");
        System.out.println("Se ha cargado el archivo tipo: " + type + "\n"); 
        JSONObject config = (JSONObject)jsonObject.get("config");
        
        //Se inicizliza el json que contiene los nombres de los grafos
        Object names = parser.parse(new FileReader("/home/arem/Documents/Universidad/Decimo semestre/Redes/Proyecto 1/Chat_XMPP/client-xmpp/src/assets/names.json"));
        JSONObject namesObject = (JSONObject)names;
        String typeName = (String)namesObject.get("type");
        System.out.println("Se ha cargado el archivo tipo: " + typeName); 
        JSONObject nameConfig = (JSONObject)namesObject.get("config");

        //Se inicializa el array que contendra el grafo
        int n = config.size();
        int [][]network = new int[n + 1][n + 1];

        // Es para agregar a la matriz en numeor de columnas y filas
        for(int i=1;i<=n;i++){
            network[0][i]=i;
            network[i][0]=i;
        }

        Nodo[] nodos;
        nodos = new Nodo[n];

        final Integer[] innerK = new Integer[1];
        innerK[0] = 1;
        config.keySet().forEach(keyStr ->{
            // Object keyvalue = config.get(keyStr);
            // System.out.println("key: " + keyStr);
            
            //Se convierte en Array al objeto json de la respectiva llave para luego iterarla
            JSONArray subjects = (JSONArray)config.get(keyStr);
            // VAriable que tiene los nombres de todos los nodos por key
            String name = (String)nameConfig.get(keyStr);
            
            Iterator iterator = subjects.iterator();
            int [] id = new int [subjects.size()];
            String [] idName = new String [subjects.size()];
            int k = 0;
            while (iterator.hasNext()) {
                String str = (String)iterator.next();
                
                id [k] = (int)str.charAt(0) - 64;
                idName [k] = str;
                //Se rellena la matrix netword con los datos del json
                network[innerK[0]][(int)str.charAt(0) - 64] = 1;
                k += 1;
            }
            //Para llenar de 0 los nodos que no estan conectados
            for (int i = 1; i < n; i++){
                if (network[innerK[0]][i] != 1) network[innerK[0]][i] = 0;     
            }
            

            String idNodo = (String)keyStr;
            //Iniciacion de nodo
            nodos[innerK[0] - 1] = new Nodo();
            nodos[innerK[0] - 1].setData(id, idName, (String)keyStr, (int)idNodo.charAt(0) - 64,false, name);
            innerK[0] = innerK[0] + 1;
        });
        showNetworkMatrix(network,n);
        System.out.println("\n" + "------Todos los preparativos estan listos para Flooding------" + "\n");  
        return Arrays.asList(nodos, n); 
    }

    public static void menuInicio(){
        System.out.println("""
                --------Menu----------
                    1. Crear usuario
                    2. Iniciar sesion
                    3. Opciones
                    4. Salir
                ----------------------
                """); 
    }

    public static void menuPrincipal(){
        System.out.println("""
                --------Menu Principal----------
                    1. Lista de todos los usuarios
                    2. Agregar contacto
                    3. Ver los datos de un usuario
                    4. Chat con usuario
                    5. Unirse a un room
                    6. Cambiar estatus
                    7. Algoritmo Flooding
                    8. Cerrar sesion
                    9. Eliminar cuenta
                   10. Opciones
                --------------------------------
                """); 
    }

    public static void main(String[] args) {

        new Thread(){    
        public void run(){
            try{
                //Configuracion para la conexion con el servidor
                XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                //.setUsernameAndPassword("hola5","hola")
                .setXmppDomain("alumchat.fun")
                .setHost("alumchat.fun")
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setSendPresence(true)
                .build();

                AbstractXMPPConnection connection = new XMPPTCPConnection(config);
                connection.connect(); //Establishes a connection to the server
                System.out.println("Connected");
                
                Scanner conteiner = new Scanner(System.in);
                int op;
                menuInicio();
                //Menu inicial
                while(true){
                    System.out.print("> ");
                    op = Integer.parseInt(conteiner.nextLine());
                    System.out.print(" ");
                    if (op == 1){
                        //#region Pedido de datos crear usuario
                        String user;
                        String pass; 
                        System.out.println("Crear usuario");
                        System.out.println("Ingrese nombre de usuario: ");
                        System.out.print("> ");
                        user = conteiner.nextLine();
                        System.out.println("Ingrese contrasena: ");
                        System.out.print("> "); 
                        pass = conteiner.nextLine();
                        //#endregion
                        //#region Sign Up
                        AccountManager manager = AccountManager.getInstance(connection);
                        Localpart nickname = Localpart.from(user);

                        //Se crea la cuenta verificando si ya existe
                        try {
                            if (manager.supportsAccountCreation()) {
                                manager.sensitiveOperationOverInsecureConnection(true);
                                manager.createAccount(nickname, pass);

                            }
                        } catch (SmackException.NoResponseException e) {
                            e.printStackTrace();
                        } catch (XMPPException.XMPPErrorException e) {
                            e.printStackTrace();
                            System.out.println("Ya existe cuenta");
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }
                        //#endregion
                        System.out.println("Usuario Creado1"); 
                        menuInicio();
                    }else if (op == 2){
                        //#region Pedido de datos inicio de sesion
                        String user;
                        String pass; 
                        System.out.println("Iniciar sesion");
                        System.out.println("Ingrese nombre de usuario: ");
                        System.out.print("> ");
                        user = conteiner.nextLine();
                        System.out.println("Ingrese contrasena: ");
                        System.out.print("> "); 
                        pass = conteiner.nextLine();
                        //#endregion
                        
                        //Login
                        connection.login(user, pass); //Logs in
                        System.out.println("Inicio de sesion exitoso");

                        //#region Listeners y declaracion de variables
                        //Account Manager
                        AccountManager manager = AccountManager.getInstance(connection);
                        //Jids
                        EntityBareJid jid = null;
                        //Presence
                        Presence presence;
                        //#region Chats
                        ChatManager chatManager = ChatManager.getInstanceFor(connection);
                        chatManager.addIncomingListener(new IncomingChatMessageListener() {
                            @Override
                            public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
                              System.out.println("New message from " + from + ": " + message.getBody());
                            }
                        });
                        //#endregion
                        //#region Roster
                        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);
                        Roster roster = Roster.getInstanceFor(connection);

                        roster.addRosterListener(new RosterListener() {
                            public void entriesAdded(Collection<Jid> addresses) { }
                            public void entriesDeleted(Collection<Jid> addresses) {  }
                            public void entriesUpdated(Collection<Jid> addresses) {  }
                            public void presenceChanged(Presence presence) { 
                                System.out.println("Presence changed: " + presence.getFrom() + " " + presence);
                            }
                        });
                        if (!roster.isLoaded()) 
                            try {
                                roster.reloadAndWait();
                            } catch (SmackException.NotLoggedInException | SmackException.NotConnectedException | InterruptedException e) {
                                e.printStackTrace();
                        }
                        //#endregion
                        //#endregion

                        //#region Funciones principales
                        menuPrincipal();
                        String messege = "";
                        Nodo[] nodos = null;
                        int countNodos = 0;
                        while(connection.isConnected()){
                            System.out.print("> ");
                            op = Integer.parseInt(conteiner.nextLine());
                            System.out.print("\n");
                            switch(op){
                                case 1:
                                    //#region Mostrar todo los usuarios
                                    Collection<RosterEntry> entries = roster.getEntries();
                                    System.out.println("------Lista de usuarios -----");
                                    for (RosterEntry entry : entries) {
                                        presence = roster.getPresence(entry.getJid());
                                        System.out.println("JId: " + entry.getJid());
                                        System.out.println("User: " + presence.getType().name());
                                        System.out.println("Status: " + presence.getStatus());
                                        System.out.println("Available: " + presence.isAvailable());
                                    }
                                    System.out.println("-----------------------------");
                                    //#endregion     
                                    break;
                                case 2:
                                    //#region Solicitud de amistad
                                    System.out.println("-----Solicitud de amistas-----");;
                                    System.out.println("Ingrese el nombre del contacto que desea agregar");;
                                    System.out.print(">");
                                    jid = JidCreate.entityBareFrom(conteiner.nextLine() + "@" + connection.getHost());
                                    try {
                                        if (!roster.contains(jid)) {
                                            roster.createItemAndRequestSubscription(jid, conteiner.nextLine(), null);
                                            System.out.println("Se ha enviado exitosamente la solicitud");
                                        } else {
                                            System.out.println("ya es un compa");
                                        }

                                    } catch (SmackException.NotLoggedInException e) {
                                        e.printStackTrace();
                                    } catch (SmackException.NoResponseException e) {
                                        e.printStackTrace();
                                    } catch (SmackException.NotConnectedException e) {
                                        e.printStackTrace();
                                    } catch (XMPPException.XMPPErrorException e) {
                                        e.printStackTrace();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    //#endregion
                                    break;
                                case 3:
                                    //#region Informacion de un usuario
                                    System.out.println("-----Informacion de un usuario-----");;
                                    System.out.println("Ingrese el nombre del usuario para ver su informacion");;
                                    System.out.print(">");
                                    if (!roster.isLoaded()) 
                                    try {
                                        roster.reloadAndWait();
                                    } catch (SmackException.NotLoggedInException | SmackException.NotConnectedException | InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    jid = JidCreate.entityBareFrom(conteiner.nextLine() + "@" + connection.getHost());
                                    presence = roster.getPresence(jid);
                                    System.out.println("----------Informacion----------");;
                                    System.out.println("Jid: " + presence.getFrom()); 
                                    System.out.println("User name: " + presence.getType().name());
                                    System.out.println("Status: " + presence.getStatus());
                                    System.out.println("Root: " + presence.getElementName());
                                    System.out.println("Mode: " + presence.getMode()); 	
                                    System.out.println("Priority: " + presence.getPriority()); 	 	
                                    System.out.println("Available: " + presence.isAvailable()); 	
                                    System.out.println("-------------------------------");;
                                    //#endregion 	
                                    break;
                                case 4:
                                    //#region Chat 1v1
                                    System.out.println("Ingrese con quien quiere chatear");
                                    System.out.print(">");
                                    jid = JidCreate.entityBareFrom(conteiner.nextLine() + "@" + connection.getHost());
                                    Chat chat = chatManager.chatWith(jid);
                                    
                                    System.out.println("-----Chat con " + jid + "-----");;
                                    System.out.println("Para ver la opciones en el menu precione 1: ");
                                    while(!messege.contains("~")){
                                        System.out.print("> (help: 1)");
                                        messege = conteiner.nextLine();
                                        if (messege.contains("1"))
                                                System.out.println("Presione ~ para salir");
                                        else if (!messege.contains("~"))
                                            chat.send(messege);
                                    }
                                    System.out.println("-----Has salido del chat-----");
                                    //#endregion 
                                    break;
                                case 5:
                                    //#region Chat Grupal
                                    System.out.println("Ingrese el room al que quiere ingresar");
                                    System.out.print(">");
                                    jid = JidCreate.entityBareFrom(conteiner.nextLine() + "@conference." + connection.getHost());
                                    MultiUserChatManager managerCum = MultiUserChatManager.getInstanceFor(connection);
                                    MultiUserChat muc = managerCum.getMultiUserChat(jid);
                                    System.out.println("Ingrese su apodo");
                                    System.out.print(">");
                                    Resourcepart room = Resourcepart.from(conteiner.nextLine());
                                    if (!muc.isJoined())
                                        muc.join(room);
                                    
                                    muc.addMessageListener(new MessageListener() {
                                        @Override
                                        public void processMessage(Message message){
                                            System.out.println("Message listener Received message in send message: "
                                            + (message != null ? message.getBody() : "NULL") + "  , Message sender :" + message.getFrom());;
                                        }
                                    });
                                    
                                    System.out.println("-----Chat con " + jid + "-----");;
                                    System.out.println("Para ver la opciones en el menu precione 1: ");
                                    while(!messege.contains("~")){
                                            System.out.print("> (help: 1)");
                                            messege = conteiner.nextLine();
                                            if (messege.contains("1"))
                                                System.out.println("Presione ~ para salir");
                                            else if (!messege.contains("~"))
                                                muc.sendMessage(messege);
                                        }    
                                    muc.leave();
                                    System.out.println("-----Has salido del room-----");
                                    //#endregion 
                                    break;
                                case 6:
                                    //#region resence
                                    System.out.println("-----Cambio de estado-----");
                                    Stanza presenceSTZ;
                                    System.out.println("""
                                            Cambien su estado:
                                            1. Available.
                                            2. Away.
                                            3. Free to chat.
                                            4. Do not disturb.
                                            5. Away for an extended period of time.
                                            """
                                                );
                                                
                                    while(true){
                                        System.out.print(">");
                                        op = Integer.parseInt(conteiner.nextLine());
                                        System.out.print("Ingrese un status:");
                                        System.out.print(">");
                                        messege = conteiner.nextLine();
                                        switch(op){
                                            case 1:
                                                presenceSTZ = new Presence(Type.available, messege, 42, Mode.available);
                                                connection.sendStanza(presenceSTZ);       
                                                break;
                                            case 2:
                                                presenceSTZ = new Presence(Type.available, messege, 42, Mode.away);
                                                connection.sendStanza(presenceSTZ);  
                                                break;
                                            case 3:
                                                presenceSTZ = new Presence(Type.available, messege, 42, Mode.chat);
                                                connection.sendStanza(presenceSTZ);   
                                                break;
                                            case 4:
                                                presenceSTZ = new Presence(Type.available, messege, 42, Mode.dnd);
                                                connection.sendStanza(presenceSTZ);  
                                                break;
                                            case 5:
                                                presenceSTZ = new Presence(Type.available, messege, 42, Mode.xa);
                                                connection.sendStanza(presenceSTZ);  
                                                break;
                                            default:
                                                System.out.println("Elija una de las opciones disponibles");
                                                break;
                                            }
                                        if (op < 5)
                                            System.out.println("Su estado ha sido cambiado");
                                            break;
                                    }
                                    //#endregion
                                    break;

                                case 7:
                                    if (nodos == null && countNodos == 0){
                                        List<Object> objects = getGrafo();
                                        Object[] grafo = objects.toArray();
                                        nodos = (Nodo[])grafo[0];
                                        countNodos = (int)grafo[1];
                                    }

                                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));   
                                    int c; //Una variable solo para obtener los inputs  
                                    String msg; // Una variable para obtner el input del mensaje a enviar a los nodos
                                    System.out.print(" Enter the source node  id  :  ");
                                    c = Integer.valueOf(br.readLine());  
                                    int s=c;//source node id variable
                                    System.out.print(" Enter the  destination node id  :  ");
                                    c=Integer.valueOf(br.readLine());
                                    int d=c;//destination node id variable
                                    System.out.print(" Enter the message to pass  :  ");
                                    msg = String.valueOf(br.readLine());

                                    FloodAlgo(nodos, s, d, countNodos, msg, jid, chatManager);

                                    break;
                                case 8:
                                    //#region Cerrar sesion
                                    System.out.println("Sesion cerrada"); 
                                    connection.disconnect();
                                    //#endregion
                                    break;
                                case 9:
                                    //#region Borrar cuenta
                                    System.out.println("Se ha borrado la cuenta"); 
                                    manager.deleteAccount();
                                    connection.disconnect();
                                    //#endregion
                                    break;
                                case 10:
                                    menuPrincipal();
                                    break;
                                default:
                                    System.out.println("Elija una de las opciones disponibles");
                                    break;
                            }
                        }
                        //#endregion
                        connection.connect();// se tiene que volver a conectar al servidor
                        menuInicio();
                    }else if (op == 3){
                        menuInicio();
                    }else if (op == 4){
                        break;
                    }else {
                        System.out.println(" ");
                        System.out.println("Ingrese una opcion correcta");
                        System.out.println(" ");
                    }
                }

                //Codigo para enviar archivos
                //#region send files
                // EntityFullJid jid = JidCreate.entityFullFrom("hola2@alumchat.fun/smack");

                // FileTransferManager manager = FileTransferManager.getInstanceFor(connection);

                // OutgoingFileTransfer transfer = manager.createOutgoingFileTransfer(jid);

                // try{
                //     transfer.sendFile(new File("hola.txt"), "You won't believe this!");
                // }catch (SmackException.NoResponseException e) {
                //     e.printStackTrace();
                // }catch (SmackException.NotConnectedException e) {
                //     e.printStackTrace();
                // }
                
                // // Create the file transfer manager
                // final FileTransferManager manager = FileTransferManager.getInstanceFor(connection);
                // // Create the listener
                // manager.addFileTransferListener(new FileTransferListener() {
                //     public void fileTransferRequest(FileTransferRequest request) {
                //     // Check to see if the request should be accepted
                //     if (shouldAccept(request)) {
                //         // Accept it
                //         IncomingFileTransfer transfer = request.accept();
                //         transfer.recieveFile(new File("shakespeare_complete_works.txt"));
                //     } else {
                //         // Reject it
                //         request.reject();
                //     }
                // }
                // });
                //#endregion
                    
                System.out.println("Disconnected");
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        }.start();
    }
}
