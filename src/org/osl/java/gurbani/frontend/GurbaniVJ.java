package org.osl.java.gurbani.frontend;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Boparai
 */
public class GurbaniVJ extends javax.swing.JFrame {
    // <editor-fold defaultstate="collapsed" desc="variable declaration">
    String dbJarFile = "dist/GurbaniVJ.jar";
    String dbActualFile = System.getProperty("user.home")+"/.sggsj";
    //String sdbActualFile = System.getProperty("user.dir")+"/sggsj";
    
    Connection connection = null;
    Statement statement = null;
    ResultSet resultset = null;
    
    ArrayList<String> lines = new ArrayList<String>();
    int lineID = 0;
    ArrayList<String> meanings = new ArrayList<String>();
    
    int shabad = 0;
    ArrayList<Integer> page = new ArrayList<>();
    ArrayList<Integer> line = new ArrayList<>();
    ArrayList<Integer> words = new ArrayList<>();
    int lineCount = 0;
    
    String raag = "RAAG";
    String lekhuk = "LEKHUK";
    String bani = "BANI";
    String pageL = "1 2 3";
    String lineL = "1 2 3";
    String wordL = "1 2 3";
    
    String shabadMeaning = "SHABAD MEANING";
    
    //char[] baniC = {'\u0a2c', '\u0a3e', '\u0a23', '\u0a40'}; 
    int lastSelectedItem = -1;
    String[] navigatorChoices = {
        "\u0a2c\u0a3e\u0a23\u0a40",
        "\u0a30\u0a3e\u0a17",
        "\u0a32\u0a47\u0a16\u0a15",
        "\u0a36\u0a2c\u0a26"
    };
    
    // </editor-fold>
    
    /**
     * Creates new form GurbaniVJ
     */
    public GurbaniVJ() {
        initComponents();
        /*if(extractDB()){
            initializeInterface(1);
        }*/
        if(moveDB()){
            initializeInterface(1);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="connection">
    public void connectDB(){
        try{
            Class.forName("org.sqlite.JDBC");
            //System.out.println(GurbaniVJ.class.getClassLoader().getResource("org/osl/java/gurbani/backend/sggsj").toString());
            //String dbPath = GurbaniVJ.class.getResource("sggsj").getPath();//GurbaniVJ.class.getClassLoader().getResource("org/osl/java/gurbani/backend/sggsj").toString();
            //System.out.println(dbPath);
            connection = DriverManager.getConnection("jdbc:sqlite:"+dbActualFile);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);
        } catch(Exception x){
            /*try{
                connection = DriverManager.getConnection("jdbc:sqlite:/org/osl/java/gurbani/backend/sggsj");
                statement = connection.createStatement();
                statement.setQueryTimeout(30);
            }catch(Exception y){}*/
            System.out.println("Something wrong in connectDB");
            Logger.getLogger(GurbaniVJ.class.getName()).log(Level.SEVERE, null, x);
            JOptionPane.showMessageDialog(null, x);
        }
    }
    
    public void disconnectDB(){
        try{
            if(connection != null){
                connection.close();
            }
        } catch(Exception x){
            System.out.println("Something wrong in disconnectDB");
            Logger.getLogger(GurbaniVJ.class.getName()).log(Level.SEVERE, null, x);
        }
    }
    // </editor-fold>
    
    public void initializeInterface(int s){
        int ss = 1;
        ss = s;
        try{
            //System.out.println(navigatorChoices[0]+", "+navigatorChoices[1]+", "+navigatorChoices[2]+", "+navigatorChoices[3]);
            connectDB();
            //int j = Integer.parseInt(JOptionPane.showInputDialog("Shabad Number"));
            feedData(ss);            
            disconnectDB();
            
            updateInterface();
        } catch(Exception x){
            System.out.println("Something wrong in initializeInterface");
            Logger.getLogger(GurbaniVJ.class.getName()).log(Level.SEVERE, null, x);
        }
    }
    
    public void feedData(int i){
        int shabadNum = i;
        try{
            resultset = statement.executeQuery("SELECT DISTINCT "
                    + "baniname, lekhukname, raagname, shabadid, lineid, lines, meaning "
                    + "FROM bani b, lekhuk l, raag r, shabad s, relation rl "
                    + "WHERE rl.baniid=b.baniid "
                    + "AND rl.lekhukid=l.lekhukid "
                    + "AND rl.raagid=r.raagid "
                    + "AND rl.relationid=s.shabadid "
                    + "AND rl.relationid="+shabadNum);
            
            while(resultset.next()) {
                bani = resultset.getString("baniname");
                lekhuk = resultset.getString("lekhukname");
                raag = resultset.getString("raagname");
                shabadMeaning = resultset.getString("meaning");
                
                shabad = resultset.getInt("shabadid");
                lineID = resultset.getInt("lineid");
                lineCount = resultset.getInt("lines");
            }
            
            resultset = null;
            getShabadLines();
            //System.out.println(bani+"\n"+lekhuk+"\n"+raag+"\n"+shabad+"\n"+lineID+"\n"+lineCount);
        } catch(Exception x){
            System.out.println("Something wrong in feedData");
            Logger.getLogger(GurbaniVJ.class.getName()).log(Level.SEVERE, null, x);
        }
    }
    
    public void getShabadLines(){
        try{
            lines.clear();
            meanings.clear();
            page.clear();
            line.clear();
            words.clear();
            
            for(int i = lineID; i < lineID+lineCount; i++){
                resultset = statement.executeQuery("SELECT line, meaning, page, linenumber, words "
                        + "FROM gurbani "
                        + "WHERE lineid="+i);
                
                while(resultset.next()) {
                    lines.add(resultset.getString("line"));
                    meanings.add(resultset.getString("meaning"));
                    page.add(resultset.getInt("page"));
                    line.add(resultset.getInt("linenumber"));
                    words.add(resultset.getInt("words"));
                }
            }
            pageL = getPages();
            lineL = getLines();
            wordL = getTotalWords();
            
            //System.out.println(lines+"\n"+meanings+"\n"+page+"\n"+fileLine+"\n"+words);
            //System.out.println(getPages()+"\n"+getLines()+"\n"+getTotalWords());
        } catch(Exception x){
            System.out.println("Something wrong in getShabadLines");
            Logger.getLogger(GurbaniVJ.class.getName()).log(Level.SEVERE, null, x);
        }
    }
    
    public String getPages(){
        String pages = "";
        Collections.sort(page);
        
        for(int i = 0; i<page.size(); i++){
            if(!pages.matches(".*"+String.valueOf(page.get(i))+".*")){
                //if(!pages.contains(String.valueOf(page.get(i)))){
                if(pages.length()<10){
                    pages += page.get(i)+" ";
                }
            }
        }
        
        return pages;
    }
    
    public String getLines(){
        String liness = "";
        Collections.sort(line);
        
        for(int i = 0; i<line.size(); i++){
            //System.out.println(String.valueOf("*"+fileLine.get(i)));
            if(!liness.matches(".*"+String.valueOf(line.get(i))+".*")){
                //if(!pages.contains(String.valueOf(page.get(i)))){
                if(liness.length()<20){
                    liness += line.get(i)+" ";
                }
            }
        }
        
        return liness;
    }
    
    public String getTotalWords(){
        int wordss = 0;
        
        for(int i = 0; i<words.size(); i++){
            wordss += words.get(i);
        }
        
        return String.valueOf(wordss);
    }
    
    public void prepareShabad(){
        taShabad.setText("");
        taBhaav.setText("");
        
        for(int i = 0; i < lines.size(); i++){
            taShabad.append(lines.get(i)+"\n");
            taShabad.append("\t"+meanings.get(i)+"\n\n");
        }
        
        taBhaav.append(shabadMeaning);
        
        taShabad.setSelectionStart(0);
        taShabad.setSelectionEnd(0);
        taBhaav.setSelectionStart(0);
        taBhaav.setSelectionEnd(0);
    }
    
    public void i18nInterface(String language, String country){
        Locale locale = new Locale(language, country);
        ResourceBundle rb = ResourceBundle.getBundle("org.osl.java.gurbani.i18n.Interface", locale);
        
        lblRaag.setText(rb.getString("lblRaag"));
        lblLekhuk.setText(rb.getString("lblLekhuk"));
        lblShabadNumber.setText(rb.getString("lblShabadNumber"));
        lblPage.setText(rb.getString("lblPage"));
        lblLines.setText(rb.getString("lblLines"));
        lblWords.setText(rb.getString("lblWords"));
        cmdPrevious.setText(rb.getString("cmdPrevious"));
        cmdNext.setText(rb.getString("cmdNext"));
        cmdVideo.setText(rb.getString("cmdVideo"));
        cmdAudio.setText(rb.getString("cmdAudio"));
        cmdOtherLink1.setText(rb.getString("cmdOtherLink1"));
        cmdOtherLink2.setText(rb.getString("cmdOtherLink2"));
        
        cbNavigator.setModel(new DefaultComboBoxModel(navigatorChoices));
    }
    
    public void updateInterface(){
        lblBaniName.setText(bani);
        lblRaagValue.setText(raag);
        lblLekhukValue.setText(lekhuk);
        lblShabadValue.setText(String.valueOf(shabad)+" *");
        lblPageValue.setText(pageL);
        lblLinesValue.setText(lineL+" *");
        lblWordsValue.setText(wordL);
        
        prepareShabad();
        i18nInterface("pa", "IN");
    }
    
    public static int[] setInterfaceSize(){
        Toolkit toolkit =  Toolkit.getDefaultToolkit ();
        Dimension dim = toolkit.getScreenSize();
        //System.out.println(dim.height);
        //System.out.println(dim.width);
        
        int[] userScreenSize = new int[2];
        userScreenSize[0] = dim.width;
        userScreenSize[1] = dim.height;
        
        return userScreenSize;
    }
    
    public int getSelectedNavigatorItem(){
        int selectedItem = 0;
        selectedItem = cbNavigator.getSelectedIndex();
        return selectedItem;
    }
    
    public void setSelectedNavigatorItem(){
        cbNavigator.setSelectedIndex(lastSelectedItem);
    }
    
    public boolean extractDB(){
        boolean isFileReady = false;
        File dbFile = null;
        try {
            JarFile jf = new JarFile(dbJarFile);
            Enumeration en = jf.entries();
            while(en.hasMoreElements()){
                ZipEntry je = (ZipEntry)en.nextElement();
                //System.out.println(je.getName());
                if(je.getName().contains("sggsj")){
                    dbFile = new File(dbActualFile);
                    
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dbFile)));
                    BufferedReader br = new BufferedReader(new InputStreamReader(jf.getInputStream(je)));
                    
                    String fileLine = "";
                    while((fileLine=br.readLine())!=null){
                        bw.write(fileLine+"\r");
                    }
                    
                    bw.close();
                    br.close();
                    System.out.println(dbFile.getCanonicalPath());
                }
            }
        } catch (IOException ex) {
            System.out.println("Something wrong in extractDB");
            Logger.getLogger(GurbaniVJ.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(dbFile.exists() && dbFile.length()>60){
            isFileReady = true;
        }
        return isFileReady;
    }
    
    public boolean moveDB(){
        boolean isFileReady = false;
        File srcFile = null;
        File dbFile = new File(dbActualFile);
        FileChannel sourceFC = null;
        FileChannel destFC = null;
        
        if(dbFile.exists() && dbFile.length()>60){
            isFileReady = true;
        } else{
            try {
                srcFile = new File("sggsj");
                dbFile.createNewFile();
                //System.out.println(srcFile.exists());

                sourceFC = new FileInputStream(srcFile).getChannel();
                destFC = new FileOutputStream(dbFile).getChannel();
                destFC.transferFrom(sourceFC, 0, sourceFC.size());
            } catch (IOException ex) {
                System.out.println("Something wrong in moveDB");
                Logger.getLogger(GurbaniVJ.class.getName()).log(Level.SEVERE, null, ex);
            } finally{
                try {
                    sourceFC.close();
                    destFC.close();
                    
                    srcFile.delete();
                    isFileReady = true;
                } catch (IOException ex) {
                    Logger.getLogger(GurbaniVJ.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        return isFileReady;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        lblBaniName = new javax.swing.JLabel();
        lblRaag = new javax.swing.JLabel();
        lblLekhuk = new javax.swing.JLabel();
        lblShabadNumber = new javax.swing.JLabel();
        lblShabadValue = new javax.swing.JLabel();
        lblLekhukValue = new javax.swing.JLabel();
        lblRaagValue = new javax.swing.JLabel();
        lblLinesValue = new javax.swing.JLabel();
        lblLines = new javax.swing.JLabel();
        lblWordsValue = new javax.swing.JLabel();
        lblPage = new javax.swing.JLabel();
        lblWords = new javax.swing.JLabel();
        lblPageValue = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taBhaav = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        taShabad = new javax.swing.JTextArea();
        jPanel6 = new javax.swing.JPanel();
        cmdPrevious = new javax.swing.JButton();
        cmdNext = new javax.swing.JButton();
        cbNavigator = new javax.swing.JComboBox();
        cmdAudio = new javax.swing.JButton();
        cmdVideo = new javax.swing.JButton();
        cmdOtherLink2 = new javax.swing.JButton();
        cmdOtherLink1 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTabbedPane1.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        jPanel5.setBackground(new java.awt.Color(0, 0, 0));
        jPanel5.setForeground(new java.awt.Color(255, 255, 255));

        lblBaniName.setBackground(new java.awt.Color(0, 0, 0));
        lblBaniName.setFont(new java.awt.Font("Raavi", 1, 18)); // NOI18N
        lblBaniName.setForeground(new java.awt.Color(255, 255, 255));
        lblBaniName.setText("<name of bani>");

        lblRaag.setBackground(new java.awt.Color(0, 0, 0));
        lblRaag.setFont(new java.awt.Font("Raavi", 1, 14)); // NOI18N
        lblRaag.setForeground(new java.awt.Color(255, 255, 255));
        lblRaag.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lblRaag.setText("Raag:");

        lblLekhuk.setBackground(new java.awt.Color(0, 0, 0));
        lblLekhuk.setFont(new java.awt.Font("Raavi", 1, 14)); // NOI18N
        lblLekhuk.setForeground(new java.awt.Color(255, 255, 255));
        lblLekhuk.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lblLekhuk.setText("Mahala:");

        lblShabadNumber.setBackground(new java.awt.Color(0, 0, 0));
        lblShabadNumber.setFont(new java.awt.Font("Raavi", 1, 14)); // NOI18N
        lblShabadNumber.setForeground(new java.awt.Color(255, 255, 255));
        lblShabadNumber.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lblShabadNumber.setText("Shabad Number:");

        lblShabadValue.setBackground(new java.awt.Color(0, 0, 0));
        lblShabadValue.setFont(new java.awt.Font("Raavi", 0, 14)); // NOI18N
        lblShabadValue.setForeground(new java.awt.Color(255, 255, 255));
        lblShabadValue.setText("<shabad number value>");
        lblShabadValue.setToolTipText("this value is from internal source and doesn't represent the actual serial number of the shabad in Sri Guru Granth Sahib Ji.");

        lblLekhukValue.setBackground(new java.awt.Color(0, 0, 0));
        lblLekhukValue.setFont(new java.awt.Font("Raavi", 0, 14)); // NOI18N
        lblLekhukValue.setForeground(new java.awt.Color(255, 255, 255));
        lblLekhukValue.setText("<name of mahala>");

        lblRaagValue.setBackground(new java.awt.Color(0, 0, 0));
        lblRaagValue.setFont(new java.awt.Font("Raavi", 0, 14)); // NOI18N
        lblRaagValue.setForeground(new java.awt.Color(255, 255, 255));
        lblRaagValue.setText("<name of raag>");

        lblLinesValue.setBackground(new java.awt.Color(0, 0, 0));
        lblLinesValue.setFont(new java.awt.Font("Raavi", 0, 14)); // NOI18N
        lblLinesValue.setForeground(new java.awt.Color(255, 255, 255));
        lblLinesValue.setText("<lines value>");
        lblLinesValue.setToolTipText("line numbers are not sorted according to page numbers they are from");

        lblLines.setBackground(new java.awt.Color(0, 0, 0));
        lblLines.setFont(new java.awt.Font("Raavi", 1, 14)); // NOI18N
        lblLines.setForeground(new java.awt.Color(255, 255, 255));
        lblLines.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lblLines.setText("Lines:");
        lblLines.setPreferredSize(null);

        lblWordsValue.setBackground(new java.awt.Color(0, 0, 0));
        lblWordsValue.setFont(new java.awt.Font("Raavi", 0, 14)); // NOI18N
        lblWordsValue.setForeground(new java.awt.Color(255, 255, 255));
        lblWordsValue.setText("<words value>");

        lblPage.setBackground(new java.awt.Color(0, 0, 0));
        lblPage.setFont(new java.awt.Font("Raavi", 1, 14)); // NOI18N
        lblPage.setForeground(new java.awt.Color(255, 255, 255));
        lblPage.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lblPage.setText("Page:");
        lblPage.setPreferredSize(null);

        lblWords.setBackground(new java.awt.Color(0, 0, 0));
        lblWords.setFont(new java.awt.Font("Raavi", 1, 14)); // NOI18N
        lblWords.setForeground(new java.awt.Color(255, 255, 255));
        lblWords.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lblWords.setText("Words:");
        lblWords.setPreferredSize(null);

        lblPageValue.setBackground(new java.awt.Color(0, 0, 0));
        lblPageValue.setFont(new java.awt.Font("Raavi", 0, 14)); // NOI18N
        lblPageValue.setForeground(new java.awt.Color(255, 255, 255));
        lblPageValue.setText("<page number>");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(lblShabadNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblShabadValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(lblBaniName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(lblRaag, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblLekhuk, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblLekhukValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblRaagValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(lblLines, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblPage, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblWords, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(lblWordsValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblLinesValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblPageValue, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(lblBaniName, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblRaagValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblPageValue)
                        .addComponent(lblRaag)
                        .addComponent(lblPage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblLekhukValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblLinesValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblLines, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(lblLekhuk, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblShabadNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblShabadValue)
                        .addComponent(lblWordsValue)
                        .addComponent(lblWords, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        taBhaav.setEditable(false);
        taBhaav.setColumns(20);
        taBhaav.setFont(new java.awt.Font("Raavi", 2, 18)); // NOI18N
        taBhaav.setLineWrap(true);
        taBhaav.setRows(5);
        taBhaav.setText("<Complete Meaning>");
        taBhaav.setSelectedTextColor(new java.awt.Color(0, 153, 255));
        taBhaav.setSelectionColor(new java.awt.Color(0, 0, 0));
        jScrollPane1.setViewportView(taBhaav);

        taShabad.setEditable(false);
        taShabad.setColumns(20);
        taShabad.setFont(new java.awt.Font("Raavi", 2, 18)); // NOI18N
        taShabad.setLineWrap(true);
        taShabad.setRows(5);
        taShabad.setText("<Shabad and Word-Meaning>");
        taShabad.setSelectedTextColor(new java.awt.Color(0, 153, 255));
        taShabad.setSelectionColor(new java.awt.Color(0, 0, 0));
        jScrollPane2.setViewportView(taShabad);

        jPanel6.setBackground(new java.awt.Color(0, 0, 0));
        jPanel6.setForeground(new java.awt.Color(255, 255, 255));

        cmdPrevious.setBackground(new java.awt.Color(0, 0, 0));
        cmdPrevious.setFont(new java.awt.Font("Raavi", 1, 14)); // NOI18N
        cmdPrevious.setForeground(new java.awt.Color(255, 255, 255));
        cmdPrevious.setText("Previous");
        cmdPrevious.setPreferredSize(new java.awt.Dimension(85, 38));
        cmdPrevious.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        cmdPrevious.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdPreviousActionPerformed(evt);
            }
        });

        cmdNext.setBackground(new java.awt.Color(0, 0, 0));
        cmdNext.setFont(new java.awt.Font("Raavi", 1, 14)); // NOI18N
        cmdNext.setForeground(new java.awt.Color(255, 255, 255));
        cmdNext.setText("Next");
        cmdNext.setMaximumSize(new java.awt.Dimension(85, 35));
        cmdNext.setMinimumSize(new java.awt.Dimension(85, 35));
        cmdNext.setPreferredSize(new java.awt.Dimension(85, 38));
        cmdNext.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        cmdNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdNextActionPerformed(evt);
            }
        });

        cbNavigator.setFont(new java.awt.Font("Raavi", 1, 12)); // NOI18N
        cbNavigator.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Bani", "Raag", "Lekhuk", "Shabad" }));
        cbNavigator.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbNavigatorItemStateChanged(evt);
            }
        });

        cmdAudio.setBackground(new java.awt.Color(0, 0, 0));
        cmdAudio.setFont(new java.awt.Font("Raavi", 1, 14)); // NOI18N
        cmdAudio.setForeground(new java.awt.Color(255, 255, 255));
        cmdAudio.setText("Audio");
        cmdAudio.setMaximumSize(new java.awt.Dimension(105, 38));
        cmdAudio.setMinimumSize(new java.awt.Dimension(105, 38));
        cmdAudio.setPreferredSize(new java.awt.Dimension(105, 38));
        cmdAudio.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        cmdAudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdAudioActionPerformed(evt);
            }
        });

        cmdVideo.setBackground(new java.awt.Color(0, 0, 0));
        cmdVideo.setFont(new java.awt.Font("Raavi", 1, 14)); // NOI18N
        cmdVideo.setForeground(new java.awt.Color(255, 255, 255));
        cmdVideo.setText("Video");
        cmdVideo.setMaximumSize(new java.awt.Dimension(105, 38));
        cmdVideo.setMinimumSize(new java.awt.Dimension(105, 38));
        cmdVideo.setPreferredSize(new java.awt.Dimension(105, 38));
        cmdVideo.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        cmdVideo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdVideoActionPerformed(evt);
            }
        });

        cmdOtherLink2.setBackground(new java.awt.Color(0, 0, 0));
        cmdOtherLink2.setFont(new java.awt.Font("Raavi", 1, 14)); // NOI18N
        cmdOtherLink2.setForeground(new java.awt.Color(255, 255, 255));
        cmdOtherLink2.setText("Other Link 2");
        cmdOtherLink2.setPreferredSize(new java.awt.Dimension(105, 38));
        cmdOtherLink2.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        cmdOtherLink1.setBackground(new java.awt.Color(0, 0, 0));
        cmdOtherLink1.setFont(new java.awt.Font("Raavi", 1, 14)); // NOI18N
        cmdOtherLink1.setForeground(new java.awt.Color(255, 255, 255));
        cmdOtherLink1.setText("Other Link 1");
        cmdOtherLink1.setPreferredSize(new java.awt.Dimension(105, 38));
        cmdOtherLink1.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(cmdVideo, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(21, 21, 21))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addComponent(cmdPrevious, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(cmdAudio, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(32, 32, 32)
                        .addComponent(cmdOtherLink1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, Short.MAX_VALUE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(cbNavigator, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmdNext, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmdOtherLink2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cmdNext, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmdPrevious, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbNavigator, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cmdOtherLink1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmdOtherLink2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cmdAudio, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmdVideo, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane2)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane1.addTab("Shabad", jPanel1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 514, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 795, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Search", jPanel2);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 514, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 795, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Bookmark", jPanel3);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 514, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 795, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Suggestions", jPanel4);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 823, Short.MAX_VALUE)
        );

        jTabbedPane1.getAccessibleContext().setAccessibleDescription("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmdPreviousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdPreviousActionPerformed
        int item = getSelectedNavigatorItem();
        if(lastSelectedItem>-1) {
            item = lastSelectedItem;
        }
        
        setSelectedNavigatorItem();
        
        switch(item){
            case 3: if(shabad>1)initializeInterface(--shabad);
            //default: //dispose();
        }
    }//GEN-LAST:event_cmdPreviousActionPerformed

    private void cmdNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdNextActionPerformed
        int item = getSelectedNavigatorItem();
        if(lastSelectedItem>-1) {
            item = lastSelectedItem;
        }
        //System.out.println(item);
        //System.out.println(lastSelectedItem);
        switch(item){
            case 3: if(shabad<42) initializeInterface(++shabad);
        }
        //System.out.println(shabad);
    }//GEN-LAST:event_cmdNextActionPerformed

    private void cbNavigatorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbNavigatorItemStateChanged
        lastSelectedItem = cbNavigator.getSelectedIndex();
    }//GEN-LAST:event_cbNavigatorItemStateChanged

    private void cmdVideoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdVideoActionPerformed
        VideoPlayer frame = new VideoPlayer();
        frame.play(JOptionPane.showInputDialog("Enter URL: "));
    }//GEN-LAST:event_cmdVideoActionPerformed

    private void cmdAudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdAudioActionPerformed
        AudioPlayer frame = new AudioPlayer();
        frame.play(JOptionPane.showInputDialog("Enter URL: "));
    }//GEN-LAST:event_cmdAudioActionPerformed

    /**
     * @param args the command fileLine arguments
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
            java.util.logging.Logger.getLogger(GurbaniVJ.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GurbaniVJ.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GurbaniVJ.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GurbaniVJ.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new GurbaniVJ();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
                if(setInterfaceSize()[1]>500){
                    frame.setSize(520, setInterfaceSize()[1]-90);
                }
                frame.setResizable(false);
            }
        });
    }
    
    // <editor-fold defaultstate="collapsed" desc="control variable declaration">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbNavigator;
    private javax.swing.JButton cmdAudio;
    private javax.swing.JButton cmdNext;
    private javax.swing.JButton cmdOtherLink1;
    private javax.swing.JButton cmdOtherLink2;
    private javax.swing.JButton cmdPrevious;
    private javax.swing.JButton cmdVideo;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblBaniName;
    private javax.swing.JLabel lblLekhuk;
    private javax.swing.JLabel lblLekhukValue;
    private javax.swing.JLabel lblLines;
    private javax.swing.JLabel lblLinesValue;
    private javax.swing.JLabel lblPage;
    private javax.swing.JLabel lblPageValue;
    private javax.swing.JLabel lblRaag;
    private javax.swing.JLabel lblRaagValue;
    private javax.swing.JLabel lblShabadNumber;
    private javax.swing.JLabel lblShabadValue;
    private javax.swing.JLabel lblWords;
    private javax.swing.JLabel lblWordsValue;
    private javax.swing.JTextArea taBhaav;
    private javax.swing.JTextArea taShabad;
    // End of variables declaration//GEN-END:variables
    // </editor-fold>
}
