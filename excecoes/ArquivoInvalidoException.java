//Package serve para informar que essa página pertence a essa pasta de trabalho
package excecoes;

//Lançada quando o arquivo EXISTE, mas não dá para usá-lo como planejamento. Exemplos:
//  - arquivo vazio ou cortado no meio;
//  - arquivo de texto, imagem ou qualquer outro formato que não seja um planejamento serializado;
//  - arquivo salvo por uma versão diferente das classes do modelo (serialVersionUID incompatível);
//  - arquivo que é uma pasta, ou que guarda um objeto de outro tipo.
//Quando isso acontece o planejamento que já estava em uso NÃO é alterado: o método carregar() só devolve um
//planejamento novo se a leitura inteira der certo.
public class ArquivoInvalidoException extends PersistenciaException {

    private static final long serialVersionUID = 1L;

    public ArquivoInvalidoException(String mensagem) {
        super(mensagem);
    }

    public ArquivoInvalidoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
