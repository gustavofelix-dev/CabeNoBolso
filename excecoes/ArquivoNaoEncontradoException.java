//Package serve para informar que essa página pertence a essa pasta de trabalho
package excecoes;

//Lançada quando se tenta CARREGAR um planejamento de um arquivo que não existe.
//É o caso normal da primeira execução do programa (ainda não há nada salvo): a interface pode capturar
//essa exceção e simplesmente iniciar um planejamento vazio, como pede o enunciado.
//"extends PersistenciaException" = herança: continua sendo uma falha de persistência,
//então um catch (PersistenciaException e) também a captura.
public class ArquivoNaoEncontradoException extends PersistenciaException {

    private static final long serialVersionUID = 1L;

    public ArquivoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public ArquivoNaoEncontradoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
