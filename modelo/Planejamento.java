package modelo; //Package serve para informar que essa página pertence a essa pasta de trabalho
import java.io.Serializable;//pacote que permite o uso de serialização, que é a conversão de um objeto em uma sequência de bytes para que ele possa ser facilmente armazenado ou transmitido
import java.util.TreeMap; //pacote que permite o uso de TreeMap, que é uma implementação de mapa ordenado em Java, onde os elementos são armazenados em uma árvore binária de busca, permitindo a ordenação automática das chaves. (Ou seja, ele vai organizar os participantes de acordo com o código)
import java.util.Collection; //pacote que permite o uso de Collection, que é uma interface que representa um grupo de objetos, permitindo operações como adição, remoção e iteração sobre os elementos do grupo. (Ou seja, tipo genérico de coleção, usado para devolver os participantes)
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Planejamento implements Serializable {

    private static final long serialVersionUID = 1L; //serialVersionUID é um identificador único para a classe, usado durante o processo de serialização e desserialização para garantir que a versão da classe seja compatível com os dados serializados.

    private TreeMap<Integer, Participante> participantes = new TreeMap<>(); // chave = código do participante, valor = o próprio participante; esse é o atributo de planejamento
    private List<Despesa> despesas = new ArrayList<>(); //atributo que puxa a lista de despesas






    //PARTICIPANTES



    //Primeiro vamos cadastrar novos participantes
    public void cadastrarParticipante(int codigo, String nome, int orcamentoCentavos) {
        if (participantes.containsKey(codigo)) {
            throw new IllegalArgumentException("Código já cadastrado!"); // recusa código duplicado
        }
        Participante participante = new Participante(codigo, nome, orcamentoCentavos);
        participantes.put(codigo, participante);//cadastra novo participante no TreeMap
    }
    //Método para buscar participantes cadastrados a partir do código
    public Participante buscarParticipante(int codigo) {
        return participantes.get(codigo); //retorna o participante de acordo com o código
    }
    //Método para alterar participantes cadastrados
    public void alterarParticipante(int codigo, String novoNome, int novoOrcamentoCentavos) {
        Participante participante = participantes.get(codigo);
        
        if (participante == null) {
            throw new IllegalArgumentException("Participante não encontrado!"); // recusa código que não existe
        }
        new Participante(codigo, novoNome, novoOrcamentoCentavos); //valida os novos dados do participante antes de alterar
        participante.setNome(novoNome);//Altera o nome do participante para novoNome
        participante.setOrcamentoCentavos(novoOrcamentoCentavos); //Altera o orçamento do participante para novoOrcamentoCentavos
    }

    //Métodos para retornar todos os participantes cadastrados
    public Collection<Participante> getTodosParticipantes() {
        return Collections.unmodifiableCollection(participantes.values()); //retorna todos os participantes cadastrados
    }

    //Metodos para excluir participante
    public void excluirParticipante(int codigo) {
    if (!participantes.containsKey(codigo)) {
        throw new IllegalArgumentException("Participante não encontrado!");
    }
    for (Despesa d : despesas) {
        for (Participante p : d.getParticipantes()) {
            if (p.getCodigo() == codigo) {
                throw new IllegalArgumentException(
                    "Não é possível excluir: o participante está vinculado a uma despesa.");
            }
        }
    }
    participantes.remove(codigo);
}
    

    //DESPESAS


    public void incluirDespesa(Despesa d) {
        if (d == null) {
            throw new IllegalArgumentException("Despesa inválida!"); //Incluir despesas de participantes
        }
        validarParticipantesCadastrados(d); // [PARTE B] linha nova: só aceita participantes cadastrados neste planejamento
        despesas.add(d);
    }

    public void excluirDespesa(Despesa d) {
        if (!despesas.remove(d)) {
            throw new IllegalArgumentException("Despesa não encontrada!");//Excluir despesas de participantes
        }
    }

    // [PARTE B] métodos novos: editarDespesa e validarParticipantesCadastrados

    //Método para editar uma despesa.
    //A despesa antiga é trocada pela nova NA MESMA POSIÇÃO da lista (comando set do List, material PA4).
    //Como a nova despesa já foi validada no construtor dela, uma edição inválida nem chega aqui
    //e a despesa antiga continua como estava.
    //Também permite trocar o tipo (ex.: de Fixa para Por pessoa), porque a nova pode ser de outra subclasse.
    public void editarDespesa(Despesa antiga, Despesa nova) {
        int posicao = despesas.indexOf(antiga); // procura a posição da despesa antiga na lista (-1 se não achar)
        if (posicao == -1) {
            throw new IllegalArgumentException("Despesa não encontrada!"); // recusa editar despesa que não existe
        }
        if (nova == null) {
            throw new IllegalArgumentException("Despesa inválida!");
        }
        validarParticipantesCadastrados(nova); // os participantes da nova despesa precisam estar cadastrados
        despesas.set(posicao, nova); // troca a antiga pela nova
    }

    //Método de apoio: confere se todos os participantes da despesa estão cadastrados neste planejamento.
    //É "private" porque só o próprio Planejamento usa (encapsulamento).
    private void validarParticipantesCadastrados(Despesa d) {
        for (Participante p : d.getParticipantes()) {
            if (!participantes.containsKey(p.getCodigo())) {
                throw new IllegalArgumentException("O participante " + p.getNome()
                        + " não está cadastrado neste planejamento!");
            }
        }
    }

    //Observação: não é preciso um método para "recalcular os totais".
    //calcularTotalDoParticipante, calcularCustoTotal e calcularExcedente (parte A) percorrem a lista de despesas
    //toda vez que são chamados. Então, depois de incluir, editar ou excluir uma despesa, os totais já saem atualizados.

    public List<Despesa> getDespesas() {
        return Collections.unmodifiableList(despesas); //get despesas
    }

  
    //Cálculos em centavos

    // soma a parte do participante em cada despesa
    public int calcularTotalDoParticipante(int codigo) {
        int total = 0;
        for (Despesa d : despesas) {
            Map<Integer, Integer> rateio = d.calcularRateio();
            if (rateio.containsKey(codigo)) {
                total = total + rateio.get(codigo);
            }
        }
        return total;
    }

    // soma o valor de todas as partes de todas as despesas
    public int calcularCustoTotal() {
        int total = 0;
        for (Despesa d : despesas) {
            for (int parte : d.calcularRateio().values()) {
                total = total + parte;
            }
        }
        return total;
    }

    // total igual ao orçamento ainda está dentro do limite
    public boolean estaDentroDoOrcamento(int codigo) {
        Participante p = participantes.get(codigo);
        if (p == null) {
            throw new IllegalArgumentException("Participante não encontrado!");
        }
        return calcularTotalDoParticipante(codigo) <= p.getOrcamentoCentavos();
    }

    // quanto passou do orçamento (0 se está dentro)
    public int calcularExcedente(int codigo) {
        Participante p = participantes.get(codigo);
        if (p == null) {
            throw new IllegalArgumentException("Participante não encontrado!");
        }
        int excedente = calcularTotalDoParticipante(codigo) - p.getOrcamentoCentavos();
        return Math.max(excedente, 0);
    }



}
