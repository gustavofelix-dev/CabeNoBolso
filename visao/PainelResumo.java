
package visao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import controlador.PlanejamentoController;
import modelo.Participante;
import modelo.Despesa;

public class PainelResumo extends JPanel {

    private PlanejamentoController controller;

    private JLabel labelCustoTotal;
    private JLabel labelQuantidade;

    private JTable tabela;
    private DefaultTableModel modeloTabela;

    private JTable tabelaDespesas;
    private DefaultTableModel modeloTabelaDespesas;

    public PainelResumo(PlanejamentoController controller) {

        this.controller = controller;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel("Resumo financeiro", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 24));

        labelCustoTotal = new JLabel("Custo total: R$ 0,00");
        labelQuantidade = new JLabel("Participantes: 0");

        labelCustoTotal.setFont(new Font("Arial", Font.BOLD, 18));
        labelQuantidade.setFont(new Font("Arial", Font.BOLD, 18));

        JPanel indicadores = new JPanel(new GridLayout(1, 2, 10, 10));

        indicadores.add(labelCustoTotal);
        indicadores.add(labelQuantidade);

        JPanel painelSuperior = new JPanel(new BorderLayout(10, 20));

        painelSuperior.add(titulo, BorderLayout.NORTH);
        painelSuperior.add(indicadores, BorderLayout.CENTER);

        String[] colunas = {
            "Codigo", "Nome", "Orcamento (R$)",
            "Gasto (R$)", "Saldo (R$)",
            "Excedente (R$)", "Situacao"
        };

        modeloTabela = new DefaultTableModel(colunas, 0) {

            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };

        tabela = new JTable(modeloTabela);

        // TABELA DE DESPESAS

        String[] colunasDespesas = {
            "Descricao", "Tipo", "Valor (R$)",
            "Participantes", "Total (R$)"
        };

        modeloTabelaDespesas = new DefaultTableModel(colunasDespesas, 0) {

            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };

        tabelaDespesas = new JTable(modeloTabelaDespesas);

        JPanel painelDespesas = new JPanel(new BorderLayout());

        painelDespesas.setBorder(
                BorderFactory.createTitledBorder("Despesas do planejamento"));

        painelDespesas.add(
                new JScrollPane(tabelaDespesas), BorderLayout.CENTER);

        JPanel painelParticipantes = new JPanel(new BorderLayout());

        painelParticipantes.setBorder(
                BorderFactory.createTitledBorder("Situacao dos participantes"));

        painelParticipantes.add(
                new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel painelTabelas = new JPanel(new GridLayout(2, 1, 10, 10));

        painelTabelas.add(painelDespesas);
        painelTabelas.add(painelParticipantes);

        JButton botaoAtualizar = new JButton("Atualizar resumo");

        botaoAtualizar.addActionListener(e -> atualizarResumo());

        JPanel painelInferior = new JPanel();

        painelInferior.add(botaoAtualizar);

        add(painelSuperior, BorderLayout.NORTH);
        add(painelTabelas, BorderLayout.CENTER);
        add(painelInferior, BorderLayout.SOUTH);

        atualizarResumo();
    }

    public void atualizarResumo() {

        modeloTabela.setRowCount(0);
        modeloTabelaDespesas.setRowCount(0);

        labelCustoTotal.setText(
                "Custo total: R$ " +
                formatarValor(controller.calcularCustoTotal()));

        labelQuantidade.setText(
                "Participantes: " +
                controller.listarParticipantes().size());

        // ATUALIZAR TABELA DE DESPESAS

        for (Despesa d : controller.listarDespesas()) {

            modeloTabelaDespesas.addRow(new Object[]{
                d.getDescricao(),
                d.getTipo(),
                formatarValor(d.getValorCentavos()),
                d.getParticipantes().size(),
                formatarValor(d.calcularCustoTotal())
            });
        }

        // ATUALIZAR TABELA DE PARTICIPANTES

        for (Participante p : controller.listarParticipantes()) {

            int codigo = p.getCodigo();

            int orcamento = p.getOrcamentoCentavos();

            int gasto = controller.calcularTotalDoParticipante(codigo);

            int saldo = orcamento - gasto;

            int excedente = controller.calcularExcedente(codigo);

            boolean dentro = controller.estaDentroDoOrcamento(codigo);

            String situacao;

            if (dentro) {
                situacao = "Dentro do orcamento";
            } else {
                situacao = "Acima do orcamento";
            }

            modeloTabela.addRow(new Object[]{
                codigo,
                p.getNome(),
                formatarValor(orcamento),
                formatarValor(gasto),
                formatarValor(saldo),
                formatarValor(excedente),
                situacao
            });
        }
    }

    private String formatarValor(int centavos) {

        return String.format("%.2f", centavos / 100.0);
    }
}
