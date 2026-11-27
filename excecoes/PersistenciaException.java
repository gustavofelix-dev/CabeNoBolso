//Package serve para informar que essa página pertence a essa pasta de trabalho
package excecoes;

//Exceção geral para qualquer falha ao SALVAR ou CARREGAR o planejamento em arquivo.
//
//Ela é uma exceção VERIFICADA (extends Exception): quem chama salvar() ou carregar() é obrigado pelo compilador
//a tratar (try/catch) ou repassar (throws). Escolhemos assim porque falha de arquivo pode acontecer mesmo com o
//programa certo (disco cheio, pasta sem permissão, arquivo apagado...), então a interface gráfica precisa
//estar preparada e mostrar uma mensagem clara em vez de "quebrar".
//
//As validações do modelo (código duplicado, valor negativo, despesa sem participantes...) continuam sendo
//IllegalArgumentException, como a parte A e a parte B do grupo já fizeram. Esta classe cuida só dos problemas de arquivo.
//
//Hierarquia:
//  Exception
//    └── PersistenciaException              (falha genérica: gravação, leitura, permissão, caminho inválido)
//          ├── ArquivoNaoEncontradoException (o arquivo não existe)
//          └── ArquivoInvalidoException      (o arquivo existe, mas não é um planejamento válido)
//Assim, quem quiser tratar tudo igual captura só PersistenciaException; quem quiser tratar cada caso
//de um jeito (ex.: arquivo ausente = começar vazio) captura a subclasse antes.
public class PersistenciaException extends Exception {

    //serialVersionUID é um identificador da versão da classe (Exception também é Serializable)
    private static final long serialVersionUID = 1L;

    //Construtor só com a mensagem que será mostrada para o usuário
    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    //Construtor com mensagem e causa: guarda o erro técnico original (ex.: IOException)
    //sem perder a mensagem amigável. A causa fica disponível em getCause() para depuração.
    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
