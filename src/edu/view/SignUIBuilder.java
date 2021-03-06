package edu.view;

import edu.api.gui.UIBuilder;
import edu.logic.gui.ButtonHandler;
import edu.logic.gui.Mediator;
import edu.logic.pki.KeyStoreTools;
import edu.logic.tools.PropertiesTool;
import edu.logic.tools.StringCipher;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 *
 * @author David Camilo Nova
 * @author Luis Fernando Muñoz
 */
public class SignUIBuilder extends UIBuilder{

    private JPasswordField txtPass;
    private JTable tblCertList;
    private ButtonHandler buttonHandler;
    private final PropertiesTool settings;

    /**
     *
     * @param mediator
     */
    public SignUIBuilder (Mediator mediator) throws IOException{
        super(mediator);
        mediator.registerSignerUIBuilder(this);
        settings = new PropertiesTool("settings.properties");
    }

    /**
     *
     */
    @Override
    @SuppressWarnings("empty-statement")
    public void addUIControls() {
        panelUI = new JPanel(new BorderLayout(20, 20));

        buttonHandler = new ButtonHandler();

        JPanel pnlForm = new JPanel(new BorderLayout(10,10));
        JPanel pnlTop = new JPanel(new GridLayout(3, 1, 5, 5));
        JPanel pnlFindDocument = new JPanel(new BorderLayout(10, 0));
        JPanel pnlPass = new JPanel(new BorderLayout(10, 0));
//        JPanel pnlCert = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JPanel pnlCert = new JPanel(new BorderLayout());
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.TRAILING, 10, 10));

        JLabel lblDocument = new JLabel("Documento a Firmar");
        JButton btnDocument = new FindDocumentButton("Buscar Documento", mediatorUI);
        btnDocument.addActionListener(buttonHandler);

        JPanel pnlDocument = new JPanel(new FlowLayout(FlowLayout.TRAILING, 10, 10));
        pnlDocument.add(btnDocument);

        pnlFindDocument.add(lblDocument, BorderLayout.WEST);
        pnlFindDocument.add(pnlDocument, BorderLayout.EAST);

        JLabel lblPass = new JLabel("Parafrase:");
        txtPass = new JPasswordField();
        pnlPass.add(lblPass, BorderLayout.WEST);
        pnlPass.add(txtPass, BorderLayout.CENTER);

        JLabel lblCert = new JLabel("Seleccionar llave para firmar");
        JButton bntCert = new CertRequestButton("Solicitar certificado");
        bntCert.addActionListener(buttonHandler);
        JPanel pnlCertButton = new JPanel(new FlowLayout(FlowLayout.TRAILING, 10, 10));
        pnlCertButton.add(bntCert);
        pnlCert.add(lblCert, BorderLayout.WEST);
        pnlCert.add(pnlCertButton, BorderLayout.EAST);

        pnlTop.add(pnlFindDocument);
        pnlTop.add(pnlPass);
        pnlTop.add(pnlCert);

        String[] columnNames = {"Nombre", "Fecha"};
        ArrayDeque<ArrayDeque> rows = new ArrayDeque<ArrayDeque>();
        String pass = settings.getProperty("keystore.password");
        if(pass != null){
            try {
                pass = StringCipher.decrypt(pass);
            } catch (Exception ex) {
                Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        KeyStoreTools kst = new KeyStoreTools(settings.getProperty("keystore.path"), pass);
        String[] aliases = kst.getKeyList();
        int i = 0;
        for(String alias:aliases){

            Certificate[] c = kst.getCertificateChain(alias);
            if(c[0].getType().equals("X.509")){
                ArrayDeque row = new ArrayDeque();
                row.add(alias);
                row.add(((X509Certificate) c[0]).getNotBefore().toString());
                rows.add(row);
                System.out.println(alias + " expires " + ((X509Certificate) c[0]).getNotBefore());
                i++;
            }
        }
        Object[][] data = new Object[0][0];
        if(rows.size()>0){
            data = new Object[rows.size()][rows.getFirst().size()];
            i=0;
            for(ArrayDeque deque:rows){
                int j=0;
                for(Object item:deque){
                    data[i][j] = item;
                    j++;
                }
                i++;
            }
        }
        tblCertList = new JTable(data, columnNames);

        JButton btnSignSave = new SignSaveButton("Firmar y Guardar", mediatorUI);
        btnSignSave.addActionListener(buttonHandler);
        JButton btnSignSend = new SignSendButton("Firmar y Enviar",mediatorUI);
        btnSignSend.addActionListener(buttonHandler);
        pnlButtons.add(btnSignSave);
        pnlButtons.add(btnSignSend);

        JScrollPane jspCertList = new JScrollPane(tblCertList);

        pnlForm.add(pnlTop,BorderLayout.NORTH);
        pnlForm.add(jspCertList,BorderLayout.CENTER);
        pnlForm.add(pnlButtons,BorderLayout.SOUTH);

        panelUI.add(new JPanel(), BorderLayout.NORTH);
        panelUI.add(new JPanel(), BorderLayout.WEST);
        panelUI.add(new JPanel(), BorderLayout.EAST);
        panelUI.add(new JPanel(), BorderLayout.SOUTH);
        panelUI.add(pnlForm, BorderLayout.CENTER);
    }

    /**
     *
     * @return
     */
    public String getSelectedAlias(){

        return tblCertList.getValueAt(tblCertList.getSelectedRow(), 0).toString();
    }

    /**
     *
     * @return
     */
    public char[] getPassword(){

        return txtPass.getPassword();
    }

    /**
     *
     */
    @Override
    public void initialize() {
        System.out.println("Not supported yet.");
    }

}
