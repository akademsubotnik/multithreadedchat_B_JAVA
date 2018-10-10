/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author greg_mbp
 * @email gregjsmith@gmx.com
 *
 */

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.*;


public class Client_GUI extends javax.swing.JFrame {

    static Socket s;
    static DataInputStream din;
    static DataOutputStream dout;
    String username;
    ArrayList<String> clients = new ArrayList();
    Boolean flag;
    
    /**
     * Creates new form Client_GUI
     */
    public Client_GUI() {
        initComponents();
    }
    
            public class Client implements Runnable{
        Client(Socket sock){
            try{
                s = sock;
                din = new DataInputStream(s.getInputStream());
                dout = new DataOutputStream(s.getOutputStream());                   
                     
                dout.writeInt(9);
                dout.writeInt((username + ":has connected.:connect").getBytes().length);//write length of the message
                dout.write((username + ":has connected.:connect").getBytes());//write message
                dout.flush();
                
                msg_area.setEditable(false);
                connectButton.setEnabled(false);
                msg_send.setEnabled(true);
                sendFileButton.setEnabled(true);
            }
            catch(IOException e){
                System.out.println(e);
            }
        }

        @Override
        public synchronized void run() {
            String message = null;
            String chat = "chat";
            String[] data = null;
            byte[] data_in;
            int length;
            int msg_type;
            flag = true;
            

                while(flag == true){
                    //msg_type guide
                        /*
                            users = 7
                            chat = 8
                            connect = 9
                            file = 10
                        */
                try{
                    msg_type = din.readInt();
                    System.out.println("server msg_type:" + msg_type);
                    
                    if(msg_type == 7){
                        length = din.readInt();
                        data_in = new byte[length];
                        din.readFully(data_in,0,data_in.length);
                        message = new String(data_in);
                        data = message.split(":");
                        
                        msg_area.append("users-->");
                        msg_area.append(data[1]);
                        msg_area.append("\n");
                    }
                                        
                    else if(msg_type == 8){
                        length = din.readInt();
                        data_in = new byte[length];
                        din.readFully(data_in,0,data_in.length);
                        message = new String(data_in);
                        data = message.split(":");
                        
                        msg_area.append(data[0] + ": " + data[1] + "\n");
                    }                   
                    
                    else if(msg_type == 9){
                        length = din.readInt();
                        data_in = new byte[length];
                        din.readFully(data_in,0,data_in.length);
                        message = new String(data_in);
                        data = message.split(":");
                        
                        msg_area.removeAll();
                        clients.add(data[0]);
                        msg_area.append(data[0] + ": " + data[1] + "\n");
                    }
                    
                    //recieve a file
                    else if(msg_type == 10){                          
                        
                            int unlength = din.readInt();//read username length
                            byte[] un = new byte[unlength];
                            din.read(un,0,unlength);
                            String unstring = new String(un);
                            System.out.println("unstring:" + unstring);
                            int n = JOptionPane.showConfirmDialog(null,"Download file from:" + unstring + "?", "File From: " + unstring, JOptionPane.YES_NO_OPTION);
                        if(n == JOptionPane.YES_OPTION){
                                System.out.println("File Recieved Started...");
                                //file length
                                length = din.readInt();

                                //Create file with recieved file name
                                String recievedFileName;                           
                                int fileNameLength = din.readInt();
                                byte [] fileName_bytes = new byte[fileNameLength];
                                din.read(fileName_bytes, 0, fileNameLength);
                                recievedFileName = new String(fileName_bytes);
                                File nf = new File(recievedFileName);

                                //Read File
                                int bytes_consumed = 0;
                                int current_byte = 0;
                                FileOutputStream file_os = null;
                                BufferedOutputStream buffered_os = null;

                                try{

                                    // receive file
                                    byte [] filebytearray  = new byte [length];
                                    System.out.println("File size:" + filebytearray.length);
                                    file_os = new FileOutputStream(recievedFileName);
                                    buffered_os = new BufferedOutputStream(file_os);
                                    bytes_consumed = din.read(filebytearray,0,filebytearray.length);
                                    current_byte = bytes_consumed;                              

                                    while(!(current_byte >= filebytearray.length)){
                                        bytes_consumed = din.read(filebytearray, current_byte,(filebytearray.length - current_byte));
                                        current_byte = current_byte + bytes_consumed;
                                    }

                                    buffered_os.write(filebytearray, 0 , current_byte);

                                    buffered_os.flush();
                                    System.out.println("File:" + recievedFileName + "downloaded.");
                                    System.out.println(current_byte + "bytes read.");

                                    file_os.close();
                                    buffered_os.close();
                                }
                                catch(Exception e){
                                    System.out.println(e);
                                }
                                
                            }
                            else{
                            
                            System.out.println("File Recieved Started...will not be reconstituted into a file");
                            
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
                                bytes_consumed = din.read(filebytearray,0,filebytearray.length);
                                current_byte = bytes_consumed;                              
                                
                                while(!(current_byte >= filebytearray.length)){
                                    bytes_consumed = din.read(filebytearray, current_byte,(filebytearray.length - current_byte));
                                    current_byte = current_byte + bytes_consumed;
                                }
                                
                                
                                System.out.println("File byte transfer complete.");
                                System.out.println("Bytes Discarded");
                                
                            }
                            catch(Exception e){
                                System.out.println(e);
                            }
                            }
                                                       
                    }
                    else{
                        System.out.println("No conditions were met.");                        
                    }                    
            }
            catch(IOException e){
                System.out.println("Line 98:" + e);
            }
            }
        }
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        connectButton = new javax.swing.JButton();
        jScrollPane0 = new javax.swing.JScrollPane();
        msg_area = new javax.swing.JTextArea();
        msg_text = new javax.swing.JTextField();
        msg_send = new javax.swing.JButton();
        unTF = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        sendFileButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        connectButton.setFont(new java.awt.Font("Dialog", 1, 8)); // NOI18N
        connectButton.setText("Connect");
        connectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectButtonActionPerformed(evt);
            }
        });

        msg_area.setFont(new java.awt.Font("Dialog", 0, 8)); // NOI18N
        jScrollPane0.setViewportView(msg_area);

        msg_text.setFont(new java.awt.Font("Dialog", 0, 8)); // NOI18N

        msg_send.setFont(new java.awt.Font("Dialog", 1, 8)); // NOI18N
        msg_send.setText("Send");
        msg_send.setEnabled(false);
        msg_send.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                msg_sendActionPerformed(evt);
            }
        });

        unTF.setFont(new java.awt.Font("Dialog", 0, 8)); // NOI18N

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 8)); // NOI18N
        jLabel1.setText("quit: quit");

        jLabel2.setFont(new java.awt.Font("Dialog", 1, 8)); // NOI18N
        jLabel2.setText("users: users");

        sendFileButton.setFont(new java.awt.Font("Dialog", 1, 8)); // NOI18N
        sendFileButton.setText("SendFile");
        sendFileButton.setEnabled(false);
        sendFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendFileButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(connectButton, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(unTF, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane0)
                    .addComponent(msg_text))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(msg_send)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 61, Short.MAX_VALUE)
                                .addComponent(sendFileButton))
                            .addComponent(jLabel2))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(connectButton)
                            .addComponent(unTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sendFileButton))
                        .addGap(35, 35, 35)
                        .addComponent(jScrollPane0, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 44, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(msg_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(msg_send))
                .addGap(33, 33, 33))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void connectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectButtonActionPerformed

        // TODO add your handling code here:        
            username = unTF.getText();
            unTF.setEditable(false);
            try{
                new Thread(new Client(new Socket("localhost", 1201))).start();
            }
            catch(IOException e){
                System.out.println(e);
        }
    }//GEN-LAST:event_connectButtonActionPerformed

    private void msg_sendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_msg_sendActionPerformed
        // TODO add your handling code here:

        String msgout = "";
   
        try{
        msgout = msg_text.getText().trim();
            if(msgout.equals("quit")){
                //close os,is,socket
                //do this in disconnect method
                //Clear msg_text field and msg_area
                flag = false;
                msg_text.setText("");
                msg_area.setText("");
                dout.writeInt(6);
                dout.writeInt((username + ":" + msgout).getBytes().length);
                dout.write((username + ":" + msgout).getBytes());
                dout.flush();
                din.close();
                dout.close();
                s.close();
                sendFileButton.setEnabled(false);
                msg_send.setEnabled(false);           
            }
            else if(msgout.equals("users")){
                dout.writeInt(7);
                dout.writeInt((username + ":" + ":" + msgout).getBytes().length);
                dout.write((username + ":" + ":" + msgout).getBytes());
                dout.flush();
                msg_text.setText("");
            }

            else{
                dout.writeInt(8);
                dout.writeInt((username + ":" + msgout + ":" + "chat").getBytes().length);
                dout.write((username + ":" + msgout + ":" + "chat").getBytes());
                dout.flush();
                msg_text.setText("");          
            }
        }
        catch(IOException e){
            System.out.println(e);
        }
        
    }//GEN-LAST:event_msg_sendActionPerformed

    private void sendFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendFileButtonActionPerformed
        // TODO add your handling code here:
        JButton open; 
        open = new JButton();
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new java.io.File("."));
        fc.setDialogTitle("File Chooser");
        if(fc.showOpenDialog(open) == JFileChooser.APPROVE_OPTION){
            //Create a Byte Array
            File selectedFile = fc.getSelectedFile();
            String fileName = selectedFile.getName();
            byte[] filebytearray = new byte[(int) selectedFile.length()];
            System.out.println("File size:" + filebytearray.length);
            
            try{
            FileInputStream fin = new FileInputStream(selectedFile);
            BufferedInputStream bin = new BufferedInputStream(fin);
            bin.read(filebytearray,0,filebytearray.length);
            
            System.out.println("Sending Files...");
            dout.writeInt(10);//msg_type
            dout.writeInt(username.length());//write username length
            dout.write((username).getBytes());//write username
            
            dout.writeInt(filebytearray.length);//file size
            dout.writeInt(fileName.length());//file name size
            dout.writeBytes(fileName);//file name
            
            
            dout.write(filebytearray, 0, filebytearray.length);//write file
            dout.flush();
            bin.close();
            fin.close();
            System.out.println("Files Sent...");
            
            }
            catch(Exception e){
                System.out.println(e);
            }
        }


    }//GEN-LAST:event_sendFileButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Client_GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Client_GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Client_GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Client_GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Client_GUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton connectButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane0;
    private javax.swing.JTextArea msg_area;
    private javax.swing.JButton msg_send;
    private javax.swing.JTextField msg_text;
    private javax.swing.JButton sendFileButton;
    private javax.swing.JTextField unTF;
    // End of variables declaration//GEN-END:variables
}
