//Package serve para informar que essa página pertence a essa pasta de trabalho
package persistencia;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

import excecoes.ArquivoInvalidoException;
import excecoes.ArquivoNaoEncontradoException;
import excecoes.PersistenciaException;
import modelo.Despesa;
import modelo.DespesaFixa;
import modelo.DespesaPorPessoa;
import modelo.Participante;
import modelo.Planejamento;

//Testes da parte de persistência e exceções, no mesmo estilo do Teste.java e do TesteParteB.java:
//cada verificação imprime [OK] ou [FALHOU] com o esperado e o obtido.
//Cobre os testes "Salvar e reiniciar" e "Problema no arquivo" da seção 4 do enunciado.
//Para executar, as classes do pacote modelo (partes A e B) já precisam estar compiladas junto.
public class TestePersistencia {

    //contadores para o resumo final
    private static int verificacoes = 0;
    private static int falhas = 0;

    public static void main(String[] args) throws Exception {

        //Todos os arquivos dos testes ficam em uma pasta temporária, apagada no final,
        //para não bagunçar a pasta "dados" do projeto.
        Path pasta = Files.createTempDirectory("teste_persistencia");
        try {
            testarSalvarERecuperar(pasta);
            testarSalvarDuasVezes(pasta);
            testarPlanejamentoVazio(pasta);
            testarArquivoAusente(pasta);
            testarArquivosInvalidos(pasta);
            testarCaminhosInvalidos(pasta);
        } finally {
            apagarPasta(pasta);
        }

        System.out.println("\n=== Resumo ===");
        System.out.println(verificacoes + " verificações, " + falhas + " falha(s).");
        if (falhas > 0) {
            System.exit(1);
        }
    }

    //SALVAR E REINICIAR (caso de referência da seção 4)
    private static void testarSalvarERecuperar(Path pasta) throws PersistenciaException {
        System.out.println("=== Salvar e reiniciar (caso de referência) ===");
        Planejamento original = criarCasoDeReferencia();
        String arquivo = pasta.resolve("caso.dat").toString();

        //a variável é do tipo da INTERFACE: o teste usa o contrato, não a classe concreta
        PersistenciaPlanejamento persistencia = new PersistenciaArquivo(arquivo);
        persistencia.salvar(original);

        //"reiniciar" = outro objeto de persistência lendo o mesmo arquivo, resultando em um planejamento novo
        Planejamento recuperado = new PersistenciaArquivo(arquivo).carregar();

        conferir("É outro objeto (não o mesmo da memória)", true, recuperado != original);
        conferir("Quantidade de participantes", 3, recuperado.getTodosParticipantes().size());
        conferir("Quantidade de despesas (sem duplicar)", 4, recuperado.getDespesas().size());
        conferir("Nome do participante 1", "Ana", recuperado.buscarParticipante(1).getNome());
        conferir("Orçamento da Ana (centavos)", 7000, recuperado.buscarParticipante(1).getOrcamentoCentavos());
        conferir("Orçamento do Bruno (centavos)", 6000, recuperado.buscarParticipante(2).getOrcamentoCentavos());

        //resultados do caso principal: total individual e custo total em centavos
        conferir("Total Ana", 7500, recuperado.calcularTotalDoParticipante(1));
        conferir("Total Bruno", 6000, recuperado.calcularTotalDoParticipante(2));
        conferir("Total Carla", 7500, recuperado.calcularTotalDoParticipante(3));
        conferir("Custo total", 21000, recuperado.calcularCustoTotal());
        conferir("Excedente Ana", 500, recuperado.calcularExcedente(1));
        conferir("Excedente Bruno", 0, recuperado.calcularExcedente(2));
        conferir("Excedente Carla", 500, recuperado.calcularExcedente(3));
        conferir("Bruno dentro do orçamento (total = limite)", true, recuperado.estaDentroDoOrcamento(2));

        //a ordem das despesas e o tipo (polimorfismo) precisam voltar iguais
        Despesa transporte = recuperado.getDespesas().get(0);
        Despesa almoco = recuperado.getDespesas().get(1);
        conferir("1ª despesa", "Transporte", transporte.getDescricao());
        conferir("Tipo da 1ª despesa", "Fixa", transporte.getTipo());
        conferir("Tipo da 2ª despesa", "Por pessoa", almoco.getTipo());
        conferir("Classe da 1ª despesa", "DespesaFixa", transporte.getClass().getSimpleName());
        conferir("Classe da 2ª despesa", "DespesaPorPessoa", almoco.getClass().getSimpleName());

        //Vínculo: o participante dentro da despesa é o MESMO objeto do cadastro do planejamento.
        //Se fossem cópias separadas, alterar o orçamento no cadastro não apareceria nas despesas.
        conferir("Participante da despesa é o mesmo do cadastro", true,
                recuperado.getDespesas().get(0).getParticipantes().get(0) == recuperado.buscarParticipante(1));

        //Teste "Alteração de orçamento" depois de recuperar: orçamento da Ana a R$ 80,00 deixa ela dentro do limite
        recuperado.alterarParticipante(1, "Ana", 8000);
        conferir("Ana dentro do orçamento com R$ 80,00", true, recuperado.estaDentroDoOrcamento(1));
        conferir("Total da Ana continua R$ 75,00", 7500, recuperado.calcularTotalDoParticipante(1));
        conferir("Excedente Ana com R$ 80,00", 0, recuperado.calcularExcedente(1));

        //o planejamento original (da memória) não pode ter sido afetado pela alteração no recuperado
        conferir("Original intacto (orçamento da Ana)", 7000, original.buscarParticipante(1).getOrcamentoCentavos());
    }

    //Salvar de novo no mesmo arquivo substitui o conteúdo, sem acumular registros nem deixar lixo na pasta
    private static void testarSalvarDuasVezes(Path pasta) throws PersistenciaException, IOException {
        System.out.println("\n=== Salvar duas vezes no mesmo arquivo ===");
        Path arquivo = pasta.resolve("duas_vezes.dat");
        PersistenciaPlanejamento persistencia = new PersistenciaArquivo(arquivo.toString());

        Planejamento planejamento = criarCasoDeReferencia();
        persistencia.salvar(planejamento);
        persistencia.salvar(planejamento);
        conferir("Despesas depois de salvar 2 vezes", 4, persistencia.carregar().getDespesas().size());
        conferir("Participantes depois de salvar 2 vezes", 3, persistencia.carregar().getTodosParticipantes().size());

        //Teste "Exclusão de despesa": exclui a atividade recreativa, salva e recupera
        planejamento.excluirDespesa(planejamento.getDespesas().get(2));
        persistencia.salvar(planejamento);
        Planejamento semAtividade = persistencia.carregar();
        conferir("Despesas depois de excluir e salvar", 3, semAtividade.getDespesas().size());
        conferir("Total por pessoa sem a atividade", 6000, semAtividade.calcularTotalDoParticipante(1));
        conferir("Custo total sem a atividade", 18000, semAtividade.calcularCustoTotal());

        //arquivos temporários não podem sobrar na pasta
        long temporarios;
        try (Stream<Path> arquivos = Files.list(pasta)) {
            temporarios = arquivos.filter(p -> p.getFileName().toString().endsWith(".tmp")).count();
        }
        conferir("Arquivos .tmp sobrando na pasta", 0, (int) temporarios);
    }

    //Planejamento sem nenhum dado também precisa salvar e carregar
    private static void testarPlanejamentoVazio(Path pasta) throws PersistenciaException {
        System.out.println("\n=== Planejamento vazio ===");
        PersistenciaPlanejamento persistencia = new PersistenciaArquivo(pasta.resolve("vazio.dat").toString());
        persistencia.salvar(new Planejamento());
        Planejamento recuperado = persistencia.carregar();
        conferir("Participantes", 0, recuperado.getTodosParticipantes().size());
        conferir("Despesas", 0, recuperado.getDespesas().size());
        conferir("Custo total", 0, recuperado.calcularCustoTotal());
    }

    //PROBLEMA NO ARQUIVO (1/2): arquivo ausente permite iniciar planejamento vazio
    private static void testarArquivoAusente(Path pasta) {
        System.out.println("\n=== Arquivo ausente ===");
        PersistenciaPlanejamento persistencia = new PersistenciaArquivo(pasta.resolve("nao_existe.dat").toString());
        Planejamento emUso = criarCasoDeReferencia(); // planejamento que o usuário já está usando

        try {
            emUso = persistencia.carregar(); // se desse certo, trocaria o planejamento em uso
            conferir("Deveria lançar ArquivoNaoEncontradoException", true, false);
        } catch (ArquivoNaoEncontradoException e) {
            System.out.println("Mensagem: " + e.getMessage());
            conferir("Exceção correta (ArquivoNaoEncontradoException)", true, true);
        } catch (PersistenciaException e) {
            conferir("Deveria ser ArquivoNaoEncontradoException, mas foi " + e.getClass().getSimpleName(), true, false);
        }
        conferir("Planejamento em uso preservado (despesas)", 4, emUso.getDespesas().size());

        //como a interface faz ao iniciar o programa sem arquivo: começa com um planejamento vazio
        Planejamento inicial;
        try {
            inicial = persistencia.carregar();
        } catch (PersistenciaException e) {
            inicial = new Planejamento();
        }
        conferir("Planejamento vazio iniciado", 0, inicial.getTodosParticipantes().size());
    }

    //PROBLEMA NO ARQUIVO (2/2): arquivo inválido gera mensagem e preserva o planejamento em uso
    private static void testarArquivosInvalidos(Path pasta) throws IOException {
        System.out.println("\n=== Arquivos inválidos ===");

        //1) arquivo de texto qualquer
        Path texto = pasta.resolve("texto.dat");
        Files.write(texto, "isto não é um planejamento".getBytes());
        verificarInvalido("Arquivo de texto", texto);

        //2) arquivo vazio
        Path vazio = pasta.resolve("zero_bytes.dat");
        Files.write(vazio, new byte[0]);
        verificarInvalido("Arquivo vazio", vazio);

        //3) arquivo cortado no meio: salva um planejamento válido e depois apaga metade dos bytes
        Path cortado = pasta.resolve("cortado.dat");
        try {
            new PersistenciaArquivo(cortado.toString()).salvar(criarCasoDeReferencia());
        } catch (PersistenciaException e) {
            conferir("Falha inesperada ao preparar o teste", true, false);
        }
        byte[] bytes = Files.readAllBytes(cortado);
        Files.write(cortado, Arrays.copyOf(bytes, bytes.length / 2));
        verificarInvalido("Arquivo cortado no meio", cortado);

        //4) serialização válida, mas de outro tipo de objeto (uma String)
        Path outroTipo = pasta.resolve("outro_tipo.dat");
        try (ObjectOutputStream saida = new ObjectOutputStream(Files.newOutputStream(outroTipo))) {
            saida.writeObject("não sou um Planejamento");
        }
        verificarInvalido("Objeto de outro tipo", outroTipo);
    }

    //Confere que carregar() recusa o arquivo com ArquivoInvalidoException e não mexe no planejamento em uso
    private static void verificarInvalido(String titulo, Path arquivo) {
        Planejamento emUso = criarCasoDeReferencia();
        try {
            emUso = new PersistenciaArquivo(arquivo.toString()).carregar();
            conferir(titulo + ": deveria lançar ArquivoInvalidoException", true, false);
        } catch (ArquivoInvalidoException e) {
            System.out.println(titulo + " -> " + e.getMessage());
            conferir(titulo + ": ArquivoInvalidoException", true, true);
        } catch (PersistenciaException e) {
            conferir(titulo + ": deveria ser ArquivoInvalidoException, mas foi " + e.getClass().getSimpleName(),
                    true, false);
        }
        conferir(titulo + ": planejamento em uso preservado", 21000, emUso.calcularCustoTotal());
    }

    //Entradas inválidas no próprio uso da persistência
    private static void testarCaminhosInvalidos(Path pasta) {
        System.out.println("\n=== Caminhos inválidos ===");

        //caminho vazio ou nulo é recusado já no construtor
        try {
            new PersistenciaArquivo("   ");
            conferir("Caminho vazio deveria ser recusado", true, false);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
            conferir("Caminho vazio recusado", true, true);
        }
        try {
            new PersistenciaArquivo((String) null);
            conferir("Caminho nulo deveria ser recusado", true, false);
        } catch (IllegalArgumentException e) {
            conferir("Caminho nulo recusado", true, true);
        }

        //caminho que é uma pasta: tanto salvar quanto carregar recusam com mensagem
        PersistenciaPlanejamento naPasta = new PersistenciaArquivo(pasta.toString());
        try {
            naPasta.salvar(criarCasoDeReferencia());
            conferir("Salvar em uma pasta deveria falhar", true, false);
        } catch (PersistenciaException e) {
            System.out.println("Erro: " + e.getMessage());
            conferir("Salvar em uma pasta recusado", true, true);
        }
        try {
            naPasta.carregar();
            conferir("Carregar de uma pasta deveria falhar", true, false);
        } catch (ArquivoInvalidoException e) {
            conferir("Carregar de uma pasta recusado", true, true);
        } catch (PersistenciaException e) {
            conferir("Carregar de uma pasta: exceção inesperada", true, false);
        }

        //salvar sem planejamento
        try {
            new PersistenciaArquivo(pasta.resolve("nulo.dat").toString()).salvar(null);
            conferir("Salvar planejamento nulo deveria falhar", true, false);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
            conferir("Salvar planejamento nulo recusado", true, true);
        } catch (PersistenciaException e) {
            conferir("Salvar planejamento nulo: exceção inesperada", true, false);
        }

        //salvar em uma pasta que ainda não existe: a pasta é criada
        Path subpasta = pasta.resolve("nova_pasta").resolve("plan.dat");
        try {
            new PersistenciaArquivo(subpasta.toString()).salvar(criarCasoDeReferencia());
            conferir("Pasta inexistente criada ao salvar", true, Files.exists(subpasta));
        } catch (PersistenciaException e) {
            conferir("Salvar em pasta nova: " + e.getMessage(), true, false);
        }
    }

    //Monta o caso de referência da seção 4 do enunciado (mesmos dados do TesteParteB)
    private static Planejamento criarCasoDeReferencia() {
        Planejamento planejamento = new Planejamento();
        planejamento.cadastrarParticipante(1, "Ana", 7000);
        planejamento.cadastrarParticipante(2, "Bruno", 6000);
        planejamento.cadastrarParticipante(3, "Carla", 7000);

        Participante ana = planejamento.buscarParticipante(1);
        Participante bruno = planejamento.buscarParticipante(2);
        Participante carla = planejamento.buscarParticipante(3);

        planejamento.incluirDespesa(new DespesaFixa("Transporte", 9000, Arrays.asList(ana, bruno, carla)));
        planejamento.incluirDespesa(new DespesaPorPessoa("Almoço", 2000, Arrays.asList(ana, bruno, carla)));
        planejamento.incluirDespesa(new DespesaFixa("Atividade recreativa", 3000, Arrays.asList(ana, carla)));
        planejamento.incluirDespesa(new DespesaPorPessoa("Sobremesa", 1000, Arrays.asList(ana, bruno, carla)));
        return planejamento;
    }

    //Compara o valor esperado com o obtido e imprime OK ou FALHOU (aceita int, boolean e texto)
    private static void conferir(String descricao, Object esperado, Object obtido) {
        verificacoes++;
        boolean ok = esperado.equals(obtido);
        if (!ok) {
            falhas++;
        }
        System.out.println("[" + (ok ? "OK" : "FALHOU") + "] " + descricao
                + " | esperado: " + esperado + " | obtido: " + obtido);
    }

    //Apaga a pasta temporária dos testes com tudo que há dentro
    private static void apagarPasta(Path pasta) throws IOException {
        try (Stream<Path> caminhos = Files.walk(pasta)) {
            caminhos.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        }
    }
}
