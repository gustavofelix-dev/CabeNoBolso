//Package serve para informar que essa página pertence a essa pasta de trabalho
package modelo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//Despesa FIXA: o valor total é dividido entre os participantes selecionados.
//Ex.: transporte de R$ 90,00 para Ana, Bruno e Carla = R$ 30,00 para cada um.
//"extends Despesa" = herança: a DespesaFixa já ganha descrição, valor, participantes e as validações da superclasse.
public class DespesaFixa extends Despesa {

    private static final long serialVersionUID = 1L;

    //Construtor: só repassa os dados para o construtor da classe Despesa (super),
    //que faz todas as validações (encadeamento de construtores, material PA2)
    public DespesaFixa(String descricao, int valorCentavos, List<Participante> participantes) {
        super(descricao, valorCentavos, participantes);
    }

    //Sobrescrita (@Override) do método abstrato: aqui está a regra da despesa fixa.
    //
    //Regra dos centavos: nem sempre a divisão é exata.
    //Ex.: R$ 100,00 = 10000 centavos para 3 pessoas
    //     10000 / 3 = 3333 (parte de cada um)
    //     10000 % 3 = 1    (sobra 1 centavo)
    //A sobra é distribuída 1 centavo por pessoa, começando pelo MENOR código:
    //     código 1 = 3334 (R$ 33,34) | código 2 = 3333 (R$ 33,33) | código 3 = 3333 (R$ 33,33) → soma 10000
    @Override
    public Map<Integer, Integer> calcularRateio() {
        Map<Integer, Integer> rateio = new TreeMap<>(); // chave = código do participante, valor = parte em centavos

        List<Participante> lista = getParticipantes(); // já vem em ordem crescente de código
        int quantidade = lista.size();
        int parteDeCada = getValorCentavos() / quantidade; // divisão inteira
        int sobra = getValorCentavos() % quantidade;        // centavos que sobraram (resto da divisão)

        for (Participante p : lista) {
            int parte = parteDeCada;
            //enquanto ainda houver sobra, o participante da vez recebe 1 centavo a mais
            if (sobra > 0) {
                parte = parte + 1;
                sobra = sobra - 1;
            }
            rateio.put(p.getCodigo(), parte);
        }
        return rateio;
    }

    @Override
    public String getTipo() {
        return "Fixa";
    }
}
