
package visao;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;

import controlador.PlanejamentoController;
import excecoes.PersistenciaException;

public class JanelaPrincipal extends JFrame {

    public JanelaPrincipal() {

        setTitle("Cabe no Bolso");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        PlanejamentoController controller = new PlanejamentoController();

        JTabbedPane abas = new JTabbedPane();

        PainelParticipantes painelParticipantes = new PainelParticipantes(controller);
        PainelDespesas painelDespesas = new PainelDespesas(controller);
        PainelResumo painelResumo = new PainelResumo(controller);

        abas.addTab("Participantes", painelParticipantes);
        abas.addTab("Despesas", painelDespesas);
        abas.addTab("Resumo", painelResumo);

        abas.addChangeListener(e -> {

            if (abas.getSelectedComponent() == painelDespesas) {
                painelDespesas.atualizarParticipantes();
            }

            if (abas.getSelectedComponent() == painelResumo) {
                painelResumo.atualizarResumo();
            }
        });

        // BOTOES DE PERSISTENCIA

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton botaoSalvar = new JButton("Salvar planejamento");
        JButton botaoCarregar = new JButton("Carregar planejamento");

        painelBotoes.add(botaoSalvar);
        painelBotoes.add(botaoCarregar);

        // ACAO DO BOTAO SALVAR

        botaoSalvar.addActionListener(e -> {

            JFileChooser seletor = new JFileChooser();

            seletor.setDialogTitle("Salvar planejamento");

            seletor.setFileFilter(
                    new FileNameExtensionFilter("Arquivos de planejamento (*.dat)", "dat"));

            seletor.setSelectedFile(new File("planejamento.dat"));

            if (seletor.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {

                File arquivo = seletor.getSelectedFile();

                if (!arquivo.getName().toLowerCase().endsWith(".dat")) {
                    arquivo = new File(arquivo.getAbsolutePath() + ".dat");
                }

                if (arquivo.exists()) {

                    int resposta = JOptionPane.showConfirmDialog(
                            this,
                            "O arquivo ja existe. Deseja substitui-lo?",
                            "Confirmar salvamento",
                            JOptionPane.YES_NO_OPTION);

                    if (resposta != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                try {

                    controller.salvarPlanejamento(arquivo);

                    JOptionPane.showMessageDialog(
                            this,
                            "Planejamento salvo com sucesso!");

                } catch (PersistenciaException ex) {

                    JOptionPane.showMessageDialog(
                            this,
                            ex.getMessage(),
                            "Erro ao salvar",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // ACAO DO BOTAO CARREGAR

        botaoCarregar.addActionListener(e -> {

            JFileChooser seletor = new JFileChooser();

            seletor.setDialogTitle("Carregar planejamento");

            seletor.setFileFilter(
                    new FileNameExtensionFilter("Arquivos de planejamento (*.dat)", "dat"));

            if (seletor.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

                int resposta = JOptionPane.showConfirmDialog(
                        this,
                        "Carregar um arquivo substituira o planejamento atual. Deseja continuar?",
                        "Confirmar carregamento",
                        JOptionPane.YES_NO_OPTION);

                if (resposta != JOptionPane.YES_OPTION) {
                    return;
                }

                try {

                    controller.carregarPlanejamento(seletor.getSelectedFile());

                    painelParticipantes.atualizarPainel();
                    painelDespesas.atualizarPainel();
                    painelResumo.atualizarResumo();

                    JOptionPane.showMessageDialog(
                            this,
                            "Planejamento carregado com sucesso!");

                } catch (PersistenciaException ex) {

                    JOptionPane.showMessageDialog(
                            this,
                            ex.getMessage(),
                            "Erro ao carregar",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        add(painelBotoes, BorderLayout.NORTH);
        add(abas, BorderLayout.CENTER);
    }
}
