
//Package serve para informar que essa página pertence a essa pasta de trabalho
package modelo;

//pacote que permite o uso de serialização, que é a conversão de um objeto em uma sequência de bytes para que ele possa ser facilmente armazenado ou transmitido
import java.io.Serializable;

//Criando a classe participantes com implemento
public class Participante implements Serializable {

    //serialVersionUID é um identificador único para a classe, usado durante o processo de serialização e desserialização para garantir que a versão da classe seja compatível com os dados serializados.
    private static final long serialVersionUID = 1L;
    //atributos da classe participantes

    //Código de cada participante
    private final int codigo; //Adicionei o final para que o código não possa ser alterado depois de criado.
    //Nome de cada participante
    private String nome;
    //Orçamento em centavos de cada participante (está em centavos pq evita problemas com casas decimais)
    private int orcamentoCentavos;

    //Construtor da classe participantes
    public Participante(int codigo, String nome, int orcamentoCentavos) {
        if (codigo <= 0) {
            throw new IllegalArgumentException("Código inválido. Deve ser maior que zero.");
        }
        this.codigo = codigo;
        setNome(nome); // Usando o setter para validar o nome
        setOrcamentoCentavos(orcamentoCentavos); // Usando o setter para validar o orçamento
    
    }
   
    //getters e setters da classe participantes
    //O get vai retornar o valor do atributo e o set vai alterar o valor do atributo nas próximas classes
    public int getCodigo() {
        return codigo;
    }
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Adicione um nome!"); // recusa nome vazio
        }
        this.nome = nome.trim();
    }

    public int getOrcamentoCentavos() {
        return orcamentoCentavos;
    }

    public void setOrcamentoCentavos(int orcamentoCentavos) {
        if (orcamentoCentavos < 0) {
            throw new IllegalArgumentException("O orçamento não pode ser negativo!"); // recusa orçamento negativo
        }
        this.orcamentoCentavos = orcamentoCentavos;
    }


    // Comando para imprimir os dados do participante, formatando o orçamento para duas casas decimais e convertendo de centavos para reais.
    @Override
    public String toString() {
        return codigo + " - " + nome + " - R$ " + String.format("%.2f", orcamentoCentavos / 100.0);
    }
}
