package darkhydra;

import frames.Loading;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class DarkHydra {
    public DarkHydra() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(DarkHydra.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Loading frm = new Loading();
    }
    
    public static void main(String[] args) {
        new DarkHydra();
    }
    
}
