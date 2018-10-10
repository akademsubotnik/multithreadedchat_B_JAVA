/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gregsmith
 */

import java.net.*;
import java.io.*;
import java.util.*;


public class Server_class_0 extends Server_GUI implements Runnable{
    ServerSocket serverSocket;
    Socket new_clientSocket;
    

    Map user_os = null;
    private static ArrayList clientInputStreams = null;

    
    Server_class_0(){

    }
    @Override
    public void run() {
        try{
            //Create ServerSocket and create clientsocket for each client that connects
            new Thread(new ServerSocketHandler()).start();

        }
        catch(UnsupportedOperationException e){
            System.out.println("Not supported yet. :" + e);
        }
    
    }
        
    public class ServerSocketHandler implements Runnable{

        @Override
        public void run() {
            user_os = new HashMap();
            clientInputStreams = new ArrayList();
            
            int portNumber = 1201;
            try{
                serverSocket = new ServerSocket(portNumber);

                while(true){
                    System.out.println("Created New Client");
                    new_clientSocket = serverSocket.accept();
                    new Thread(new ClientHandler(new_clientSocket)).start();
                }
            }
            catch(IOException e){
                System.out.println(e);
            }
        }
        
    }

    
    public class ClientHandler implements Runnable{
        DataInputStream din;
        DataOutputStream dout;
        Socket client_socket;

        
        ClientHandler(Socket sock){
            this.client_socket = sock;
            
            try{
                this.din = new DataInputStream(sock.getInputStream());
                this.dout = new DataOutputStream(sock.getOutputStream()); 
            }
            catch(IOException e){
                System.out.println(e);
            }
        }

        @Override
        public void run() {
            

            String message;
            String[] data;
            String chat = "chat";
            byte[] data_in;
            Boolean flag = true;
            int length;
            int msg_type;
                
            while(flag == true){
                try{
                    
                      msg_type = din.readInt();
                      System.out.println("server msg_type:" + msg_type);
                        
                        //quit
                        if(msg_type == 6){
                            length = din.readInt();
                            System.out.println("Length:" + length);
                            data_in = new byte[length];
                            din.readFully(data_in,0,data_in.length);
                            message = new String(data_in);
                            data = message.split(":");
                            
                            synchronized (this){
                                clientInputStreams.remove(din);
                                user_os.remove(data[0]);                                
                                din.close();
                                dout.close();
                                client_socket.close();                                
                            }
                            tellAll((data[0] + ":has disconnected." + ":" + chat),8);
                            break;
                        }
                        //users
                        else if(msg_type == 7){
                            length = din.readInt();
                            System.out.println("Length:" + length);
                            data_in = new byte[length];
                            din.readFully(data_in,0,data_in.length);
                            message = new String(data_in);
                            data = message.split(":");  
                            
                            tellOne(data[0],7);
                        }
                        //chat
                        else if(msg_type == 8){
                            length = din.readInt();
                            System.out.println("Length:" + length);
                            data_in = new byte[length];
                            din.readFully(data_in,0,data_in.length);
                            message = new String(data_in);                            
                            data = message.split(":"); 
                            
                            tellAll(message,8);
                        }
                        
                        //connect
                        else if(msg_type == 9){
                            length = din.readInt();
                            System.out.println("Length:" + length);
                            data_in = new byte[length];
                            din.readFully(data_in,0,data_in.length);
                            message = new String(data_in);
                            data = message.split(":");          
                            
                            tellAll((data[0] + ":" + data[1] + ":" + chat),9);
                            user_os.put(data[0],dout);
                            clientInputStreams.add(din);
                        }
                        //file
                        else if(msg_type == 10){
                            
                            System.out.println("File Recieved Started...");
                            
                            //get username
                            int unlength = din.readInt();//read username length
                            byte[] un = new byte[unlength];
                            din.read(un,0,unlength);
                            String unstring = new String(un);
                            System.out.println("unstring:" + unstring);
                            
                            //file size
                            length = din.readInt();
                            
                            //file name
                            String recievedFileName;                           
                            int fileNameLength = din.readInt();
                            byte [] fileName_bytes = new byte[fileNameLength];
                            din.read(fileName_bytes, 0, fileNameLength);
                            recievedFileName = new String(fileName_bytes);
                            
                            //Read File
                            int bytes_consumed = 0;
                            int current_byte = 0;
                            
                            try{

                                // receive file
                                byte [] filebytearray  = new byte [length];
                                System.out.println("File size:" + filebytearray.length);
                                bytes_consumed = din.read(filebytearray,0,filebytearray.length);
                                current_byte = bytes_consumed;                              
                                
                                while(!(current_byte >= filebytearray.length)){
                                    bytes_consumed = din.read(filebytearray, current_byte,(filebytearray.length - current_byte));
                                    current_byte = current_byte + bytes_consumed;
                                }
                                
                                System.out.println("File:" + recievedFileName + " transfer to clients begun.");
                                sendFile(filebytearray, filebytearray.length, recievedFileName, unstring, 10);
                                
                            }
                            catch(Exception e){
                                System.out.println(e);
                            }
                            
                        }
                        else{
                            System.out.println("No conditions were met.\n");
                        }
                }
                catch(Exception e){

                    System.out.println(e);
                }
            }
            
        }
    
    }
    
        public void sendFile(byte[] file,int filesize, String fileName, String sendingclient, int msg_type_arg) throws Exception{
            
            synchronized (this){
                //Get set of keys from hashmap
                Set setOfKeys = user_os.keySet();
                //Get the iterator instance from the Set
                Iterator it = setOfKeys.iterator();
                while(it.hasNext()){
                    String key = (String)it.next();
                    if(!(key.equals(sendingclient))){
                        DataOutputStream writer = (DataOutputStream)user_os.get(key);//get the outputstream for the client
                        //send the file to the client

                        writer.writeInt(msg_type_arg);//write message type(10)
                         
                        System.out.println("Sending Files...");
                        //Sending Client Name
                        writer.writeInt(sendingclient.length());//write username length
                        writer.write((sendingclient).getBytes());//write username
                        
                        writer.writeInt(filesize);//file size
                        
                        //file name information
                        writer.writeInt(fileName.length());//file name size
                        writer.writeBytes(fileName);//file name
                        
                        
                        writer.write(file, 0, file.length);//write file
                        writer.flush();
                        System.out.println("Files Sent...");
                    }
                }

            }
              
        }

                   
    
        public void tellOne(String user, int msg_type_arg) throws Exception{
            synchronized (this){
                DataOutputStream userRequesting = (DataOutputStream)user_os.get(user);

                String userAll = " : ";

                  //Get all users from user_os hashmap and append to userAll
                  Set setOfKeys = user_os.keySet();
                  Iterator it = setOfKeys.iterator();
                  while(it.hasNext()){
                      String key = (String)it.next();
                      userAll += key + " ";
                  }


                  userAll += " :users";
                  
                  userRequesting.writeInt(msg_type_arg);//write message type
                  userRequesting.writeInt(userAll.getBytes().length);//write length of the message
                  userRequesting.write(userAll.getBytes());//write message
                  userRequesting.flush();
                  
            }
            
        }
 
        public void tellAll(String message, int msg_type_arg) throws Exception{
            synchronized (this){
                //Get set of keys from hashmap
                Set setOfKeys = user_os.keySet();
                //Get the iterator instance from the Set
                Iterator it = setOfKeys.iterator();
                while(it.hasNext()){
                    String key = (String)it.next();
                    DataOutputStream writer = (DataOutputStream)user_os.get(key);
                    
                    writer.writeInt(msg_type_arg);//write message type
                    writer.writeInt(message.getBytes().length);//write length of the message
                    writer.write(message.getBytes());//write message
                    writer.flush();
                }

            }

        } 
    
}
