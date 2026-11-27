
package controlador;

import modelo.Planejamento;
import modelo.Participante;
import modelo.Despesa;
import modelo.DespesaFixa;
import modelo.DespesaPorPessoa;

import persistencia.PersistenciaPlanejamento;
import persistencia.PersistenciaArquivo;
import excecoes.PersistenciaException;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

public class PlanejamentoController {

    private Planejamento planejamento;

    public PlanejamentoController() {
        planejamento = new Planejamento();
    }

    // PARTICIPANTES

    public void cadastrarParticipante(int codigo, String nome, String orcamento) {

        int centavos = converterParaCentavos(orcamento);

        planejamento.cadastrarParticipante(codigo, nome, centavos);
    }

    public void editarParticipante(int codigo, String nome, String orcamento) {

        int centavos = converterParaCentavos(orcamento);

        planejamento.alterarParticipante(codigo, nome, centavos);
    }

    public Collection<Participante> listarParticipantes() {

        return planejamento.getTodosParticipantes();
    }

    // DESPESAS

    public void cadastrarDespesa(String descricao, String valor,
            String tipo, List<Integer> codigos) {

        Despesa despesa = criarDespesa(descricao, valor, tipo, codigos);

        planejamento.incluirDespesa(despesa);
    }

    public void editarDespesa(Despesa antiga, String descricao,
            String valor, String tipo, List<Integer> codigos) {

        Despesa nova = criarDespesa(descricao, valor, tipo, codigos);

        planejamento.editarDespesa(antiga, nova);
    }

    public void excluirDespesa(Despesa despesa) {

        planejamento.excluirDespesa(despesa);
    }

    public List<Despesa> listarDespesas() {

        return planejamento.getDespesas();
    }

    private Despesa criarDespesa(String descricao, String valor,
            String tipo, List<Integer> codigos) {

        int centavos = converterParaCentavos(valor);

        List<Participante> participantes = new ArrayList<>();

        for (int codigo : codigos) {

            Participante p = planejamento.buscarParticipante(codigo);

            if (p == null) {
                throw new IllegalArgumentException(
                        "Participante nao encontrado!");
            }

            participantes.add(p);
        }

        if ("Fixa".equals(tipo)) {

            return new DespesaFixa(descricao, centavos, participantes);

        } else if ("Por pessoa".equals(tipo)) {

            return new DespesaPorPessoa(descricao, centavos, participantes);
        }

        throw new IllegalArgumentException("Tipo de despesa invalido.");
    }

    // RESUMO FINANCEIRO

    public int calcularCustoTotal() {

        return planejamento.calcularCustoTotal();
    }

    public int calcularTotalDoParticipante(int codigo) {

        return planejamento.calcularTotalDoParticipante(codigo);
    }

    public boolean estaDentroDoOrcamento(int codigo) {

        return planejamento.estaDentroDoOrcamento(codigo);
    }

    public int calcularExcedente(int codigo) {

        return planejamento.calcularExcedente(codigo);
    }

    // PERSISTENCIA

    public void salvarPlanejamento(File arquivo)
            throws PersistenciaException {

        PersistenciaPlanejamento persistencia =
                new PersistenciaArquivo(arquivo);

        persistencia.salvar(planejamento);
    }

    public void carregarPlanejamento(File arquivo)
            throws PersistenciaException {

        PersistenciaPlanejamento persistencia =
                new PersistenciaArquivo(arquivo);

        Planejamento carregado = persistencia.carregar();

        planejamento = carregado;
    }

    // CONVERSAO MONETARIA

    private int converterParaCentavos(String valor) {

        try {

            return new BigDecimal(valor.trim().replace(",", "."))
                    .movePointRight(2)
                    .intValueExact();

        } catch (NumberFormatException | ArithmeticException e) {

            throw new IllegalArgumentException(
                    "Informe um valor monetario valido.");
        }
    }
}
