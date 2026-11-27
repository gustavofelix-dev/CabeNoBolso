
package visao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import controlador.PlanejamentoController;
import modelo.Participante;

public class PainelParticipantes extends JPanel {

    private JTextField campoCodigo;
    private JTextField campoNome;
    private JTextField campoOrcamento;

    private JButton botaoCadastrar;
    private JButton botaoEditar;
    private JButton botaoLimpar;

    private JTable tabela;
    private DefaultTableModel modeloTabela;

    private PlanejamentoController controller;

    public PainelParticipantes(PlanejamentoController controller) {

        this.controller = controller;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formulario = new JPanel(new GridLayout(3, 2, 10, 10));

        campoCodigo = new JTextField();
        campoNome = new JTextField();
        campoOrcamento = new JTextField();

        formulario.add(new JLabel("Codigo:"));
        formulario.add(campoCodigo);

        formulario.add(new JLabel("Nome:"));
        formulario.add(campoNome);

        formulario.add(new JLabel("Orcamento (R$):"));
        formulario.add(campoOrcamento);

        botaoCadastrar = new JButton("Cadastrar");
        botaoEditar = new JButton("Editar");
        botaoLimpar = new JButton("Limpar");

        JPanel painelBotoes = new JPanel();

        painelBotoes.add(botaoCadastrar);
        painelBotoes.add(botaoEditar);
        painelBotoes.add(botaoLimpar);

        JPanel painelSuperior = new JPanel(new BorderLayout(10, 10));

        painelSuperior.add(formulario, BorderLayout.CENTER);
        painelSuperior.add(painelBotoes, BorderLayout.SOUTH);

        String[] colunas = {"Codigo", "Nome", "Orcamento (R$)"};

        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };

        tabela = new JTable(modeloTabela);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(painelSuperior, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        botaoCadastrar.addActionListener(e -> cadastrar());
        botaoEditar.addActionListener(e -> editar());
        botaoLimpar.addActionListener(e -> limparCampos());

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selecionarParticipante();
            }
        });

        atualizarTabela();
    }

    private void cadastrar() {

        try {

            int codigo = Integer.parseInt(campoCodigo.getText().trim());
            String nome = campoNome.getText();
            String orcamento = campoOrcamento.getText();

            controller.cadastrarParticipante(codigo, nome, orcamento);

            atualizarTabela();
            limparCampos();

            JOptionPane.showMessageDialog(this,
                    "Participante cadastrado com sucesso!");

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(this,
                    "Informe um codigo inteiro valido.",
                    "Erro", JOptionPane.ERROR_MESSAGE);

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editar() {

        if (tabela.getSelectedRow() == -1) {

            JOptionPane.showMessageDialog(this,
                    "Selecione um participante na tabela.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);

            return;
        }

        try {

            int codigo = Integer.parseInt(campoCodigo.getText().trim());
            String nome = campoNome.getText();
            String orcamento = campoOrcamento.getText();

            controller.editarParticipante(codigo, nome, orcamento);

            atualizarTabela();
            limparCampos();

            JOptionPane.showMessageDialog(this,
                    "Participante atualizado com sucesso!");

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selecionarParticipante() {

        int linha = tabela.getSelectedRow();

        if (linha != -1) {

            campoCodigo.setText(tabela.getValueAt(linha, 0).toString());
            campoNome.setText(tabela.getValueAt(linha, 1).toString());
            campoOrcamento.setText(tabela.getValueAt(linha, 2).toString());

            campoCodigo.setEditable(false);
            botaoCadastrar.setEnabled(false);
        }
    }

    private void atualizarTabela() {

        modeloTabela.setRowCount(0);

        for (Participante p : controller.listarParticipantes()) {

            modeloTabela.addRow(new Object[]{
                p.getCodigo(),
                p.getNome(),
                String.format("%.2f", p.getOrcamentoCentavos() / 100.0)
            });
        }
    }

    private void limparCampos() {

        tabela.clearSelection();

        campoCodigo.setText("");
        campoNome.setText("");
        campoOrcamento.setText("");

        campoCodigo.setEditable(true);
        botaoCadastrar.setEnabled(true);

        campoCodigo.requestFocus();
    }

    // ATUALIZACAO APOS CARREGAMENTO

    public void atualizarPainel() {

        limparCampos();
        atualizarTabela();
    }
}
