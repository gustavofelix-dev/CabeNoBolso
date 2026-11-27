import javax.swing.SwingUtilities;
import visao.JanelaPrincipal;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JanelaPrincipal janela = new JanelaPrincipal();
            janela.setVisible(true);

        });
    }
}