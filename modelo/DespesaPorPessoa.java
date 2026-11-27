//Package serve para informar que essa página pertence a essa pasta de trabalho
package modelo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//Despesa POR PESSOA: cada participante selecionado paga o valor informado (valor unitário).
//Ex.: almoço de R$ 20,00 para Ana, Bruno e Carla = R$ 20,00 para cada um (R$ 60,00 no total).
//"extends Despesa" = herança: reaproveita tudo da classe Despesa e só muda o cálculo do rateio.
public class DespesaPorPessoa extends Despesa {

    private static final long serialVersionUID = 1L;

    //Construtor: repassa os dados para a classe Despesa (super), que faz as validações
    public DespesaPorPessoa(String descricao, int valorPorPessoaCentavos, List<Participante> participantes) {
        super(descricao, valorPorPessoaCentavos, participantes);
    }

    //Sobrescrita (@Override) do método abstrato: aqui está a regra da despesa por pessoa.
    //Não há divisão, então não há centavos sobrando: cada um paga exatamente o valor informado.
    @Override
    public Map<Integer, Integer> calcularRateio() {
        Map<Integer, Integer> rateio = new TreeMap<>(); // chave = código do participante, valor = parte em centavos
        for (Participante p : getParticipantes()) {
            rateio.put(p.getCodigo(), getValorCentavos());
        }
        return rateio;
    }

    @Override
    public String getTipo() {
        return "Por pessoa";
    }
}
