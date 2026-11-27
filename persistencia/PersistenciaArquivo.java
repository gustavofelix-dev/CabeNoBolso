//Package serve para informar que essa página pertence a essa pasta de trabalho
package persistencia;

//pacotes de entrada e saída (arquivos) e de serialização, que converte um objeto em uma sequência de bytes e de volta
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import excecoes.ArquivoInvalidoException;
import excecoes.ArquivoNaoEncontradoException;
import excecoes.PersistenciaException;
import modelo.Planejamento;

//Classe de ARQUIVO que implementa o contrato PersistenciaPlanejamento usando SERIALIZAÇÃO DE OBJETOS.
//
//Como funciona a serialização aqui: o Planejamento é gravado com um único writeObject(). O Java percorre o objeto
//inteiro (TreeMap de participantes, lista de despesas, cada Despesa com o seu TreeMap de participantes...) e grava tudo
//que for Serializable. Por isso as classes do modelo implementam Serializable e têm serialVersionUID.
//
//Detalhe importante: dentro de UM mesmo writeObject, se o mesmo objeto aparece em dois lugares, o Java grava ele UMA vez
//e depois só guarda uma referência. É assim que a Ana do mapa de participantes e a Ana dentro das despesas continuam
//sendo o MESMO objeto depois de carregar (relações preservadas, sem duplicar registros). Por isso salvamos o
//planejamento inteiro de uma vez e nunca participantes e despesas em gravações separadas.
public class PersistenciaArquivo implements PersistenciaPlanejamento {

    //Local do arquivo. É "final": a classe sempre trabalha com o mesmo arquivo, definido no construtor.
    private final Path caminho;

    //Construtor com o caminho em texto (ex.: "dados/planejamento.dat")
    public PersistenciaArquivo(String caminho) {
        if (caminho == null || caminho.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o caminho do arquivo!"); // recusa caminho vazio
        }
        this.caminho = Paths.get(caminho.trim());
    }

    //Construtor com File: facilita o uso com o JFileChooser do Swing, que devolve um File
    public PersistenciaArquivo(File arquivo) {
        if (arquivo == null) {
            throw new IllegalArgumentException("Informe o arquivo!");
        }
        this.caminho = arquivo.toPath();
    }

    //Mostra qual arquivo está sendo usado (útil para mensagens na interface)
    public String getCaminho() {
        return caminho.toString();
    }

    //SALVAR
    //
    //Estratégia segura: primeiro grava tudo em um arquivo TEMPORÁRIO na mesma pasta e só no final troca pelo arquivo
    //definitivo. Assim, se faltar espaço no disco ou der qualquer erro no meio da gravação, o arquivo salvo
    //anteriormente continua intacto (não fica um arquivo pela metade).
    @Override
    public void salvar(Planejamento planejamento) throws PersistenciaException {
        if (planejamento == null) {
            throw new IllegalArgumentException("Não há planejamento para salvar!");
        }
        if (Files.isDirectory(caminho)) {
            throw new PersistenciaException("Não foi possível salvar: \"" + caminho + "\" é uma pasta, não um arquivo.");
        }

        Path temporario = null;
        try {
            Path absoluto = caminho.toAbsolutePath();
            Files.createDirectories(absoluto.getParent()); // cria a pasta (ex.: "dados") se ela ainda não existir
            //o temporário fica na mesma pasta e tem o mesmo nome com ".tmp" no final (ex.: planejamento.dat.tmp)
            temporario = absoluto.resolveSibling(absoluto.getFileName() + ".tmp");

            //try-with-resources: o fluxo é FECHADO automaticamente ao sair do bloco, dando certo ou errado.
            //O BufferedOutputStream acumula os bytes na memória e grava em blocos, o que é mais rápido.
            try (ObjectOutputStream saida = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(temporario)))) {
                saida.writeObject(planejamento);
            }

            //gravação terminou bem: agora sim troca o arquivo antigo (se existir) pelo novo
            Files.move(temporario, caminho, StandardCopyOption.REPLACE_EXISTING);

        } catch (NotSerializableException e) {
            //só acontece se alguma classe do modelo esquecer de implementar Serializable
            throw new PersistenciaException("Não foi possível salvar: a classe " + e.getMessage()
                    + " não é serializável.", e);
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível salvar o planejamento em \"" + caminho
                    + "\". Verifique se a pasta existe, se há espaço em disco e se você tem permissão de escrita.", e);
        } finally {
            apagarTemporario(temporario); // não deixa lixo na pasta se algo deu errado
        }
    }

    //CARREGAR
    //
    //Devolve um Planejamento NOVO, lido do arquivo. Quem chamou decide o que fazer com ele
    //(normalmente trocar o planejamento em uso). Se qualquer coisa falhar, uma exceção é lançada ANTES
    //de devolver algo, então o planejamento que já estava em uso nunca fica alterado nem pela metade.
    @Override
    public Planejamento carregar() throws PersistenciaException {
        if (!Files.exists(caminho)) {
            throw new ArquivoNaoEncontradoException("Arquivo não encontrado: \"" + caminho + "\".");
        }
        if (Files.isDirectory(caminho)) {
            throw new ArquivoInvalidoException("\"" + caminho + "\" é uma pasta, não um arquivo de planejamento.");
        }

        try (ObjectInputStream entrada = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(caminho)))) {

            Object lido = entrada.readObject();

            //o arquivo pode ser uma serialização válida, mas de outra coisa (ex.: um simples texto)
            if (!(lido instanceof Planejamento)) {
                throw new ArquivoInvalidoException("O arquivo \"" + caminho + "\" não contém um planejamento.");
            }
            return (Planejamento) lido;

        } catch (EOFException e) {
            //o arquivo acabou antes do esperado: está vazio ou foi cortado
            throw new ArquivoInvalidoException("O arquivo \"" + caminho + "\" está vazio ou incompleto.", e);
        } catch (InvalidClassException e) {
            //o serialVersionUID gravado é diferente do da classe atual: arquivo de outra versão do programa
            throw new ArquivoInvalidoException("O arquivo \"" + caminho
                    + "\" foi gerado por uma versão incompatível do programa.", e);
        } catch (ObjectStreamException e) {
            //cabeçalho inválido ou dados corrompidos (ex.: StreamCorruptedException): não é um arquivo de planejamento
            throw new ArquivoInvalidoException("O arquivo \"" + caminho
                    + "\" não é um arquivo de planejamento válido ou está corrompido.", e);
        } catch (ClassNotFoundException e) {
            //o arquivo cita uma classe que não existe neste programa
            throw new ArquivoInvalidoException("O arquivo \"" + caminho
                    + "\" usa classes que este programa não conhece.", e);
        } catch (IOException e) {
            //qualquer outro problema de leitura (sem permissão, arquivo bloqueado, falha do disco...)
            throw new PersistenciaException("Não foi possível ler o arquivo \"" + caminho
                    + "\". Verifique se você tem permissão de leitura.", e);
        }
    }

    //Método de apoio: apaga o arquivo temporário, se ele ainda existir.
    //É "private" porque só esta classe usa (encapsulamento). Se nem isso for possível, ignoramos o erro:
    //o importante (salvar ou avisar a falha) já foi resolvido.
    private void apagarTemporario(Path temporario) {
        if (temporario == null) {
            return;
        }
        try {
            Files.deleteIfExists(temporario);
        } catch (IOException e) {
            // nada a fazer: é só uma limpeza
        }
    }
}
