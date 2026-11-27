//Package serve para informar que essa página pertence a essa pasta de trabalho
package persistencia;

import excecoes.PersistenciaException;
import modelo.Planejamento;

//INTERFACE de persistência: define o CONTRATO (o que precisa existir) sem dizer COMO é feito.
//O enunciado pede "operações de salvar e carregar, implementadas por uma classe de arquivo".
//Quem usa a persistência (o controlador) conhece só esta interface, nunca a classe concreta:
//
//    PersistenciaPlanejamento persistencia = new PersistenciaArquivo("dados/planejamento.dat");
//
//Se um dia o grupo quisesse salvar em outro lugar (banco de dados, JSON...), bastaria criar outra classe que
//implemente esta interface, sem mexer no controlador, na visão nem no modelo (baixo acoplamento).
public interface PersistenciaPlanejamento {

    //Salva o planejamento COMPLETO (participantes, despesas e a relação entre eles).
    //Se der erro, lança PersistenciaException com uma mensagem clara.
    //Se já existia um arquivo, ele só é substituído quando a gravação nova termina com sucesso.
    void salvar(Planejamento planejamento) throws PersistenciaException;

    //Lê o planejamento salvo e devolve um objeto NOVO (nunca altera o que está em uso).
    //Lança ArquivoNaoEncontradoException se não houver arquivo e ArquivoInvalidoException se ele não servir;
    //as duas são subclasses de PersistenciaException.
    Planejamento carregar() throws PersistenciaException;
}
