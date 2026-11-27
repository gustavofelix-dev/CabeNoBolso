//Package serve para informar que essa página pertence a essa pasta de trabalho
package modelo;

//pacote que permite o uso de serialização (a Pessoa 3 vai salvar o planejamento em arquivo, e as despesas vão junto)
import java.io.Serializable;
//pacotes das coleções genéricas que vamos usar
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//Classe ABSTRATA: ela reúne o que toda despesa tem em comum (descrição, valor e participantes),
//mas não sabe como dividir o valor, porque isso depende do tipo da despesa.
//Por isso ela não pode ser instanciada: não existe "new Despesa(...)", só "new DespesaFixa(...)" ou "new DespesaPorPessoa(...)".
//(É a mesma ideia da FormaGeometrica do material PA2: a superclasse tem os getters, mas o calcularArea() fica abstrato.)
public abstract class Despesa implements Serializable {

    //serialVersionUID é um identificador da versão da classe, usado na serialização e desserialização
    private static final long serialVersionUID = 1L;

    //atributos da classe despesa (todos private = encapsulamento)

    //Descrição da despesa (ex.: "Transporte")
    private String descricao;
    //Valor informado em centavos (mesmo padrão do orçamento do Participante, para evitar problemas com casas decimais)
    //Na despesa fixa é o valor total; na despesa por pessoa é o valor que cada um paga
    private int valorCentavos;
    //Participantes envolvidos na despesa.
    //Usamos TreeMap pelo mesmo motivo do Planejamento: a chave é o código, então
    //1) não deixa o mesmo participante entrar duas vezes e
    //2) mantém os participantes em ordem crescente de código, que é a ordem usada para distribuir os centavos no rateio.
    private TreeMap<Integer, Participante> participantes = new TreeMap<>();

    //Construtor da classe despesa.
    //É "protected" porque só as subclasses (DespesaFixa e DespesaPorPessoa) chamam ele, através do super(...).
    //Todas as validações ficam aqui: se algum dado for inválido, o objeto nem chega a ser criado,
    //então nada é alterado no planejamento.
    protected Despesa(String descricao, int valorCentavos, List<Participante> listaParticipantes) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Adicione uma descrição para a despesa!"); // recusa descrição vazia
        }
        if (valorCentavos <= 0) {
            throw new IllegalArgumentException("O valor da despesa deve ser maior que zero!"); // recusa valor zero ou negativo
        }
        if (listaParticipantes == null || listaParticipantes.isEmpty()) {
            throw new IllegalArgumentException("Selecione pelo menos um participante!"); // recusa despesa sem participantes
        }

        //percorre a lista recebida e coloca cada participante no TreeMap
        for (Participante p : listaParticipantes) {
            if (p == null) {
                throw new IllegalArgumentException("Participante inválido na despesa!");
            }
            if (participantes.containsKey(p.getCodigo())) {
                throw new IllegalArgumentException("O participante " + p.getNome()
                        + " foi selecionado mais de uma vez!"); // recusa participante repetido
            }
            participantes.put(p.getCodigo(), p);
        }

        this.descricao = descricao.trim();
        this.valorCentavos = valorCentavos;
    }

    //MÉTODOS ABSTRATOS (cada subclasse é obrigada a implementar do seu jeito = polimorfismo)

    //Calcula quanto cada participante paga nessa despesa.
    //Retorna um mapa: chave = código do participante, valor = parte dele em centavos.
    //(Esse é o formato que o Planejamento da parte A já usa em calcularTotalDoParticipante e calcularCustoTotal)
    public abstract Map<Integer, Integer> calcularRateio();

    //Retorna o nome do tipo da despesa ("Fixa" ou "Por pessoa"), para mostrar na tela
    public abstract String getTipo();

    //getters da classe despesa
    //Não criamos setters: para editar uma despesa, o Planejamento troca a despesa antiga por uma nova (ver editarDespesa).
    //Assim a validação do construtor sempre vale e uma edição inválida não deixa a despesa "pela metade".

    public String getDescricao() {
        return descricao;
    }

    public int getValorCentavos() {
        return valorCentavos;
    }

    //Retorna os participantes em ordem crescente de código.
    //A lista é "não modificável": quem recebe pode ler, mas não pode adicionar nem remover ninguém por fora.
    public List<Participante> getParticipantes() {
        return Collections.unmodifiableList(new ArrayList<>(participantes.values()));
    }

    //Soma as partes de todos os participantes = custo total dessa despesa
    //(na fixa é o próprio valor; na por pessoa é valor x quantidade de participantes)
    public int calcularCustoTotal() {
        int total = 0;
        for (int parte : calcularRateio().values()) {
            total = total + parte;
        }
        return total;
    }

    //Comando para imprimir os dados da despesa, convertendo de centavos para reais
    @Override
    public String toString() {
        return descricao + " (" + getTipo() + ") - R$ " + String.format("%.2f", valorCentavos / 100.0);
    }
}
