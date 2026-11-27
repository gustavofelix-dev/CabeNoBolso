//Package serve para informar que essa página pertence a essa pasta de trabalho
package persistencia;

import java.util.Arrays;

import excecoes.PersistenciaException;
import modelo.DespesaFixa;
import modelo.DespesaPorPessoa;
import modelo.Participante;
import modelo.Planejamento;

//Programa pequeno que gera o ARQUIVO DE EXEMPLO do repositório (dados/caso_referencia.dat),
//com o caso de referência da seção 4 do enunciado (Ana, Bruno e Carla; Transporte, Almoço, Atividade e Sobremesa).
//Quem corrigir o trabalho pode abrir esse arquivo na aplicação sem digitar nada.
//Se o modelo mudar, é só executar este programa de novo para gerar um arquivo compatível com a versão final.
//
//Uso (a partir da pasta raiz do projeto):  java -cp out persistencia.GeradorArquivoExemplo
public class GeradorArquivoExemplo {

    public static void main(String[] args) {
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

        PersistenciaArquivo persistencia = new PersistenciaArquivo("dados/caso_referencia.dat");
        try {
            persistencia.salvar(planejamento);
            System.out.println("Arquivo de exemplo gerado em: " + persistencia.getCaminho());
        } catch (PersistenciaException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}
