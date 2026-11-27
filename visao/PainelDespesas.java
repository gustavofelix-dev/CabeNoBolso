
package visao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import controlador.PlanejamentoController;
import modelo.Participante;
import modelo.Despesa;

public class PainelDespesas extends JPanel {

    private JTextField campoDescricao;
    private JTextField campoValor;
    private JComboBox<String> campoTipo;

    private JButton botaoCadastrar;
    private JButton botaoEditar;
    private JButton botaoExcluir;
    private JButton botaoLimpar;

    private JTable tabela;
    private DefaultTableModel modeloTabela;

    private PlanejamentoController controller;
    private JPanel painelParticipantes;

    private Map<Integer, JCheckBox> caixas = new TreeMap<>();

    public PainelDespesas(PlanejamentoController controller) {

        this.controller = controller;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formulario = new JPanel(new GridLayout(3, 2, 10, 10));

        campoDescricao = new JTextField();
        campoValor = new JTextField();

        String[] tipos = {"Fixa", "Por pessoa"};
        campoTipo = new JComboBox<>(tipos);

        formulario.add(new JLabel("Descricao:"));
        formulario.add(campoDescricao);

        formulario.add(new JLabel("Valor (R$):"));
        formulario.add(campoValor);

        formulario.add(new JLabel("Tipo:"));
        formulario.add(campoTipo);

        painelParticipantes = new JPanel();

        painelParticipantes.setLayout(
                new BoxLayout(painelParticipantes, BoxLayout.Y_AXIS));

        painelParticipantes.setBorder(
                BorderFactory.createTitledBorder("Participantes envolvidos"));

        JScrollPane rolagemParticipantes =
                new JScrollPane(painelParticipantes);

        rolagemParticipantes.setPreferredSize(new Dimension(0, 120));

        botaoCadastrar = new JButton("Cadastrar");
        botaoEditar = new JButton("Editar");
        botaoExcluir = new JButton("Excluir");
        botaoLimpar = new JButton("Limpar");

        JPanel painelBotoes = new JPanel();

        painelBotoes.add(botaoCadastrar);
        painelBotoes.add(botaoEditar);
        painelBotoes.add(botaoExcluir);
        painelBotoes.add(botaoLimpar);

        JPanel painelSuperior = new JPanel(new BorderLayout(10, 10));

        painelSuperior.add(formulario, BorderLayout.NORTH);
        painelSuperior.add(rolagemParticipantes, BorderLayout.CENTER);
        painelSuperior.add(painelBotoes, BorderLayout.SOUTH);

        String[] colunas = {
            "Descricao", "Tipo", "Valor (R$)",
            "Participantes", "Total (R$)"
        };

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
        botaoExcluir.addActionListener(e -> excluir());
        botaoLimpar.addActionListener(e -> limparCampos());

        tabela.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {
                selecionarDespesa();
            }
        });

        atualizarParticipantes();
        atualizarTabela();
    }

    public void atualizarParticipantes() {

        List<Integer> selecionados = obterSelecionados();

        painelParticipantes.removeAll();
        caixas.clear();

        for (Participante p : controller.listarParticipantes()) {

            JCheckBox caixa = new JCheckBox(
                    p.getCodigo() + " - " + p.getNome());

            caixa.setSelected(selecionados.contains(p.getCodigo()));

            caixas.put(p.getCodigo(), caixa);
            painelParticipantes.add(caixa);
        }

        painelParticipantes.revalidate();
        painelParticipantes.repaint();
    }

    private List<Integer> obterSelecionados() {

        List<Integer> selecionados = new ArrayList<>();

        for (Map.Entry<Integer, JCheckBox> item : caixas.entrySet()) {

            if (item.getValue().isSelected()) {
                selecionados.add(item.getKey());
            }
        }

        return selecionados;
    }

    private void cadastrar() {

        try {

            controller.cadastrarDespesa(
                    campoDescricao.getText(),
                    campoValor.getText(),
                    (String) campoTipo.getSelectedItem(),
                    obterSelecionados());

            atualizarTabela();
            limparCampos();

            JOptionPane.showMessageDialog(this,
                    "Despesa cadastrada com sucesso!");

        } catch (IllegalArgumentException e) {

            mostrarErro(e.getMessage());
        }
    }

    private void editar() {

        int linha = tabela.getSelectedRow();

        if (linha == -1) {

            mostrarErro("Selecione uma despesa na tabela.");
            return;
        }

        try {

            Despesa antiga = controller.listarDespesas().get(linha);

            controller.editarDespesa(
                    antiga,
                    campoDescricao.getText(),
                    campoValor.getText(),
                    (String) campoTipo.getSelectedItem(),
                    obterSelecionados());

            atualizarTabela();
            limparCampos();

            JOptionPane.showMessageDialog(this,
                    "Despesa atualizada com sucesso!");

        } catch (IllegalArgumentException e) {

            mostrarErro(e.getMessage());
        }
    }

    private void excluir() {

        int linha = tabela.getSelectedRow();

        if (linha == -1) {

            mostrarErro("Selecione uma despesa na tabela.");
            return;
        }

        int resposta = JOptionPane.showConfirmDialog(
                this,
                "Deseja realmente excluir esta despesa?",
                "Confirmar exclusao",
                JOptionPane.YES_NO_OPTION);

        if (resposta == JOptionPane.YES_OPTION) {

            try {

                Despesa despesa =
                        controller.listarDespesas().get(linha);

                controller.excluirDespesa(despesa);

                atualizarTabela();
                limparCampos();

                JOptionPane.showMessageDialog(this,
                        "Despesa excluida com sucesso!");

            } catch (IllegalArgumentException e) {

                mostrarErro(e.getMessage());
            }
        }
    }

    private void selecionarDespesa() {

        int linha = tabela.getSelectedRow();

        if (linha == -1) {
            return;
        }

        Despesa d = controller.listarDespesas().get(linha);

        campoDescricao.setText(d.getDescricao());

        campoValor.setText(
                String.format("%.2f", d.getValorCentavos() / 100.0));

        campoTipo.setSelectedItem(d.getTipo());

        for (JCheckBox caixa : caixas.values()) {
            caixa.setSelected(false);
        }

        for (Participante p : d.getParticipantes()) {

            JCheckBox caixa = caixas.get(p.getCodigo());

            if (caixa != null) {
                caixa.setSelected(true);
            }
        }

        botaoCadastrar.setEnabled(false);
    }

    private void atualizarTabela() {

        modeloTabela.setRowCount(0);

        for (Despesa d : controller.listarDespesas()) {

            modeloTabela.addRow(new Object[]{
                d.getDescricao(),
                d.getTipo(),
                String.format("%.2f", d.getValorCentavos() / 100.0),
                d.getParticipantes().size(),
                String.format("%.2f", d.calcularCustoTotal() / 100.0)
            });
        }
    }

    private void limparCampos() {

        tabela.clearSelection();

        campoDescricao.setText("");
        campoValor.setText("");
        campoTipo.setSelectedIndex(0);

        for (JCheckBox caixa : caixas.values()) {
            caixa.setSelected(false);
        }

        botaoCadastrar.setEnabled(true);

        campoDescricao.requestFocus();
    }

    private void mostrarErro(String mensagem) {

        JOptionPane.showMessageDialog(
                this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    // ATUALIZACAO APOS CARREGAMENTO

    public void atualizarPainel() {

        limparCampos();
        atualizarParticipantes();
        atualizarTabela();
    }
}
