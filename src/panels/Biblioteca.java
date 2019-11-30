package panels;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.users.FullAccount;
import data.Connect;
import data.UserInfo;
import dependencies.WrapLayout;
import frames.Aviso;
import frames.SelecPath;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.Timer;
import org.apache.commons.io.FileUtils;

public class Biblioteca extends javax.swing.JPanel {

    public Biblioteca() {
        initComponents();
        init();
    }
    
    private void init() {
        conn = Connect.getInstance().conn;
        pnlLeft.setLayout(new WrapLayout());
        btnBaixar.setEnabled(false);
        btnJogar.setEnabled(false);
        
        try {
            ps = conn.prepareStatement("select idJogo from jogo_usuario where idUsuario = ?");
            ps.setInt(1, UserInfo.getIdUsuario());
            rs = ps.executeQuery();
            while(rs.next()) {
                pnlLeft.add(new BibliotecaLeft(rs.getInt("idJogo")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Biblioteca.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void loadGame(int idd) {
        try {
            this.currentGame = idd;
            
            ps = conn.prepareStatement("select tituloJogo, descJogo, imagem1, imagem4 from jogo where idJogo = ?");
            ps.setInt(1, idd);
            rs = ps.executeQuery();
            rs.next();
            lblTitle.setText(rs.getString("tituloJogo"));
            txtDesc.setText(rs.getString("descJogo"));
            
            url = new URL(rs.getString("imagem1"));
            c = ImageIO.read(url).getScaledInstance(131, 131, Image.SCALE_SMOOTH);
            lblFoto.setIcon(new ImageIcon(c));
            
            url = new URL(rs.getString("imagem4"));
            c = ImageIO.read(url).getScaledInstance(730, 90, Image.SCALE_SMOOTH);
            lblCapa.setIcon(new ImageIcon(c));
        } catch (SQLException | IOException ex) {
            Logger.getLogger(Biblioteca.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(new File("C:\\Users\\"+System.getProperty("user.name")+"\\Downloads\\DarkHydraGames\\"+idd+"\\GameFiles\\").exists()) {
            btnBaixar.setEnabled(false);
            btnBaixar.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, new java.awt.Color(255, 153, 153)));
            btnJogar.setEnabled(true);
            btnJogar.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, new java.awt.Color(153, 255, 153)));
        } else {
            btnBaixar.setEnabled(true);
            btnBaixar.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, new java.awt.Color(153, 255, 153)));
            btnJogar.setEnabled(false);
            btnJogar.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, new java.awt.Color(255, 153, 153)));
        }
    }
    
    Timer timer = new Timer(50, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ae) {
            counter++;
            bar.setValue(counter);
            System.out.println(counter+"% Downloaded");
            if (counter > 100) {
                timer.stop();
                gamezip.delete();
                btnBaixar.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, new java.awt.Color(255, 153, 153)));
                btnJogar.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, new java.awt.Color(153, 255, 153)));
                btnJogar.setEnabled(true);
                Aviso fa = new Aviso("Jogo baixado com sucesso.");
            } else if (counter==5) {
                btnBaixar.setEnabled(false);
            } else if (counter==10) {
                File gamedir = new File("/"+currentGame);
                gamedir.mkdir();
            } else if (counter==20) {
                try {
                    ps = conn.prepareStatement("select zipJogo from jogo where idJogo = ?");
                    ps.setInt(1, currentGame);
                    rs = ps.executeQuery();
                    if(rs.next()) {
                        File file = new File("C:\\Users\\"+System.getProperty("user.name")+"\\Downloads\\DarkHydraGames\\"+currentGame);
                        file.mkdirs();
                        gamezip = new File("C:\\Users\\"+System.getProperty("user.name")+"\\Downloads\\DarkHydraGames\\"+currentGame+"\\gamezip.zip");
                        
                        DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial");
                        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

                        // Get current account info
                        FullAccount account = client.users().getCurrentAccount();
                        System.out.println(account.getName().getDisplayName());
                        
                        
                        DbxDownloader<FileMetadata> downloader = client.files().download("/"+rs.getString("zipJogo"));
                        try {
                            FileOutputStream out = new FileOutputStream(gamezip);
                            downloader.download(out);
                            out.close();
                        } catch (DbxException ex) {
                            System.out.println(ex.getMessage());
                        }
                    }
                } catch (SQLException | IOException ex) {
                    Logger.getLogger(Biblioteca.class.getName()).log(Level.SEVERE, null, ex);
                } catch (DbxException ex) {
                    Logger.getLogger(Biblioteca.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (counter==50) {
                try(ZipFile file = new ZipFile("C:\\Users\\"+System.getProperty("user.name")+"\\Downloads\\DarkHydraGames\\"+currentGame+"\\gamezip.zip")) {
                    FileSystem fileSystem = FileSystems.getDefault();
                    //Get file entries
                    Enumeration<? extends ZipEntry> entries = file.entries();

                    //We will unzip files in this folder
                    String uncompressedDirectory = "C:\\Users\\"+System.getProperty("user.name")+"\\Downloads\\DarkHydraGames\\"+currentGame+"\\GameFiles\\";
                    Files.createDirectory(fileSystem.getPath(uncompressedDirectory));

                    //Iterate over entries
                    while (entries.hasMoreElements())
                    {
                        ZipEntry entry = entries.nextElement();
                        //If directory then create a new directory in uncompressed folder
                        if (entry.isDirectory())
                        {
                            System.out.println("Creating Directory:" + uncompressedDirectory + entry.getName());
                            Files.createDirectories(fileSystem.getPath(uncompressedDirectory + entry.getName()));
                        }
                        //Else create the file
                        else
                        {
                            InputStream is = file.getInputStream(entry);
                            BufferedInputStream bis = new BufferedInputStream(is);
                            String uncompressedFileName = uncompressedDirectory + entry.getName();
                            Path uncompressedFilePath = fileSystem.getPath(uncompressedFileName);
                            Files.createFile(uncompressedFilePath);
                            FileOutputStream fileOutput = new FileOutputStream(uncompressedFileName);
                            while (bis.available() > 0)
                            {
                                fileOutput.write(bis.read());
                            }
                            fileOutput.close();
                            System.out.println("Criado :" + entry.getName());
                        }
                    }
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            } else if (counter==75) {
                //gamezip.delete();
            } else if (counter==90) {
                gamesini = new File("C:\\Users\\"+System.getProperty("user.name")+"\\Downloads\\DarkHydraGames\\gamesinfo.txt");
                if(!gamesini.exists()) {
                    try {
                        gamesini.createNewFile();
                    } catch (IOException ex) {
                        Logger.getLogger(Biblioteca.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    ps = conn.prepareStatement("select pathExec from jogo where idJogo = ?");
                    ps.setInt(1, currentGame);
                    rs = ps.executeQuery();
                    rs.next();
                    gamepath = "C:\\Users\\"+System.getProperty("user.name")+"\\Downloads\\DarkHydraGames\\"+currentGame+"\\GameFiles\\"+rs.getString("pathExec");
                    Files.write(Paths.get("C:\\Users\\"+System.getProperty("user.name")+"\\Downloads\\DarkHydraGames\\gamesinfo.txt"), (System.lineSeparator()+"["+currentGame+"]:"+gamepath+" ;").getBytes(), StandardOpenOption.APPEND);                           

                } catch (SQLException ex) {
                    Logger.getLogger(Biblioteca.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Biblioteca.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    });
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        pnlLeft = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        lblCapa = new javax.swing.JLabel();
        lblFoto = new javax.swing.JLabel();
        lblTitle = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtDesc = new javax.swing.JTextArea();
        btnBaixar = new javax.swing.JButton();
        btnJogar = new javax.swing.JButton();
        bar = new javax.swing.JProgressBar();

        setBackground(new java.awt.Color(20, 20, 20));

        jPanel3.setBackground(new java.awt.Color(20, 20, 20));

        pnlLeft.setBackground(new java.awt.Color(25, 25, 25));

        javax.swing.GroupLayout pnlLeftLayout = new javax.swing.GroupLayout(pnlLeft);
        pnlLeft.setLayout(pnlLeftLayout);
        pnlLeftLayout.setHorizontalGroup(
            pnlLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 248, Short.MAX_VALUE)
        );
        pnlLeftLayout.setVerticalGroup(
            pnlLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 484, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(pnlLeft);

        jPanel1.setBackground(new java.awt.Color(25, 25, 25));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lblTitle.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(240, 240, 240));

        jScrollPane2.setBorder(null);
        jScrollPane2.setHorizontalScrollBar(null);

        txtDesc.setBackground(new java.awt.Color(25, 25, 25));
        txtDesc.setColumns(20);
        txtDesc.setForeground(new java.awt.Color(220, 220, 220));
        txtDesc.setLineWrap(true);
        txtDesc.setRows(5);
        txtDesc.setWrapStyleWord(true);
        txtDesc.setBorder(null);
        jScrollPane2.setViewportView(txtDesc);

        btnBaixar.setForeground(new java.awt.Color(240, 240, 240));
        btnBaixar.setText("Baixar");
        btnBaixar.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 3, 0, 0, new java.awt.Color(255, 153, 153)));
        btnBaixar.setContentAreaFilled(false);
        btnBaixar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBaixarActionPerformed(evt);
            }
        });

        btnJogar.setForeground(new java.awt.Color(240, 240, 240));
        btnJogar.setText("Jogar");
        btnJogar.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 3, 0, 0, new java.awt.Color(255, 153, 153)));
        btnJogar.setContentAreaFilled(false);
        btnJogar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnJogarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblCapa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(lblFoto, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE)
                            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(bar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(btnBaixar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnJogar, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(lblCapa, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblFoto, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBaixar, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnJogar, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnJogarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnJogarActionPerformed
        charr = 0;
        charnumend = 0;
        path = "";
        
        try {
            pathstring = FileUtils.readFileToString(new File("C:\\Users\\"+System.getProperty("user.name")+"\\Downloads\\DarkHydraGames\\gamesinfo.txt"), "utf-8");
        } catch (IOException ex) {
            Logger.getLogger(Biblioteca.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("ID do jogo a ser aberto: "+currentGame);
        System.out.println(pathstring);
        if(pathstring.contains("["+currentGame+"]:")) {
            charnumend = pathstring.indexOf("["+currentGame+"]:");
            charnumend += 3 + String.valueOf(currentGame).length();
            charr = pathstring.charAt(charnumend);
            while(!Character.toString(charr).matches(";")) {
                charr = pathstring.charAt(charnumend);
                charnumend++;
                
                if(Character.toString(charr).matches("|")) {
                    
                } else {
                    path += charr;
                }
            }
            
            path = path.replaceAll(".$", "");
            path = path.replaceAll(".$", "");
            System.out.println(path);
            
            try {
                Runtime.getRuntime().exec(path);
            } catch (IOException ex) {
                Logger.getLogger(Biblioteca.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } else {
            Aviso fa = new Aviso("Falha ao achar executável, favor editar gamesinfo.txt");
        }
    }//GEN-LAST:event_btnJogarActionPerformed

    private void btnBaixarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBaixarActionPerformed
        counter = 1;
        timer.start();
    }//GEN-LAST:event_btnBaixarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar bar;
    private javax.swing.JButton btnBaixar;
    private javax.swing.JButton btnJogar;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblCapa;
    private javax.swing.JLabel lblFoto;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlLeft;
    private javax.swing.JTextArea txtDesc;
    // End of variables declaration//GEN-END:variables
    
    private Connection conn;
    private PreparedStatement ps;
    private ResultSet rs;
    private URL url;
    private Image c;
    private int currentGame;
    private File gamezip;
    private String pathstring;
    private String path = "";
    private int counter = 1;
    private String gamepath;
    private char charr;
    private int charnumend;
    private File gamesini, gameexec;
    private static final String ACCESS_TOKEN = "8LXGEz2EpoAAAAAAAAAAJFzVCGVoRqBpT_UnFCRMl8xO0eLHma1MfsIls38qkjST";
}
