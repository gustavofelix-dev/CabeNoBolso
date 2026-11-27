package modelo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//Testes da parte B (despesas e rateio), no mesmo estilo do Teste.java da parte A.
//Para compilar, o trecho Planejamento_trecho_parteB.java já precisa estar colado no Planejamento.java (usa editarDespesa).
public class TesteParteB {
    public static void main(String[] args) {

        //CASO DE REFERÊNCIA DA SEÇÃO 4 DO ENUNCIADO
        Planejamento planejamento = new Planejamento();
        planejamento.cadastrarParticipante(1, "Ana", 7000);
        planejamento.cadastrarParticipante(2, "Bruno", 6000);
        planejamento.cadastrarParticipante(3, "Carla", 7000);

        Participante ana = planejamento.buscarParticipante(1);
        Participante bruno = planejamento.buscarParticipante(2);
        Participante carla = planejamento.buscarParticipante(3);

        //Polimorfismo: as variáveis são do tipo Despesa (superclasse), mas os objetos são das subclasses
        Despesa transporte = new DespesaFixa("Transporte", 9000, Arrays.asList(ana, bruno, carla));
        Despesa almoco = new DespesaPorPessoa("Almoço", 2000, Arrays.asList(ana, bruno, carla));
        Despesa atividade = new DespesaFixa("Atividade recreativa", 3000, Arrays.asList(ana, carla));
        Despesa sobremesa = new DespesaPorPessoa("Sobremesa", 1000, Arrays.asList(ana, bruno, carla));

        planejamento.incluirDespesa(transporte);
        planejamento.incluirDespesa(almoco);
        planejamento.incluirDespesa(atividade);
        planejamento.incluirDespesa(sobremesa);

        System.out.println("=== Caso de referência ===");
        conferir("Total Ana", 7500, planejamento.calcularTotalDoParticipante(1));
        conferir("Total Bruno", 6000, planejamento.calcularTotalDoParticipante(2));
        conferir("Total Carla", 7500, planejamento.calcularTotalDoParticipante(3));
        conferir("Custo total", 21000, planejamento.calcularCustoTotal());
        System.out.println("Bruno aparece no rateio da atividade? " + atividade.calcularRateio().containsKey(2)
                + " (quem não participa contribui com zero)");
        conferir("Excedente Ana", 500, planejamento.calcularExcedente(1));
        conferir("Excedente Carla", 500, planejamento.calcularExcedente(3));
        System.out.println("Bruno dentro do orçamento (total = limite)? " + planejamento.estaDentroDoOrcamento(2));

        //Mostra a tabela "quem paga quanto em cada despesa" (igual à tabela da seção 4)
        for (Despesa d : planejamento.getDespesas()) {
            System.out.println(d + " -> rateio " + formatarRateio(d.calcularRateio())
                    + " | custo da despesa: " + reais(d.calcularCustoTotal()));
        }

        //CENTAVOS NO RATEIO: R$ 100,00 fixo para os códigos 1, 2 e 3
        System.out.println("\n=== Centavos no rateio ===");
        //a lista é passada fora de ordem de propósito: o rateio precisa seguir a ordem dos códigos mesmo assim
        Despesa cem = new DespesaFixa("Teste centavos", 10000, Arrays.asList(carla, ana, bruno));
        Map<Integer, Integer> rateioCem = cem.calcularRateio();
        conferir("Código 1", 3334, rateioCem.get(1));
        conferir("Código 2", 3333, rateioCem.get(2));
        conferir("Código 3", 3333, rateioCem.get(3));
        conferir("Soma", 10000, cem.calcularCustoTotal());

        //VALIDAÇÕES: cada uma deve dar erro e NÃO alterar o planejamento
        System.out.println("\n=== Validações ===");
        int quantidadeAntes = planejamento.getDespesas().size();

        // valor zero
        try {
            planejamento.incluirDespesa(new DespesaFixa("Pedágio", 0, Arrays.asList(ana)));
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
        // valor negativo
        try {
            planejamento.incluirDespesa(new DespesaFixa("Pedágio", -1000, Arrays.asList(ana)));
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
        // sem participantes
        try {
            List<Participante> vazia = Arrays.asList();
            planejamento.incluirDespesa(new DespesaFixa("Pedágio", 1000, vazia));
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
        // participante repetido
        try {
            planejamento.incluirDespesa(new DespesaPorPessoa("Pedágio", 1000, Arrays.asList(ana, ana)));
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
        // descrição vazia
        try {
            planejamento.incluirDespesa(new DespesaFixa("  ", 1000, Arrays.asList(ana)));
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
        // participante que não está cadastrado neste planejamento
        try {
            Participante davi = new Participante(9, "Davi", 5000); // criado, mas não cadastrado no planejamento
            planejamento.incluirDespesa(new DespesaFixa("Pedágio", 1000, Arrays.asList(ana, davi)));
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
        System.out.println("[" + (quantidadeAntes == planejamento.getDespesas().size() ? "OK" : "FALHOU")
                + "] Quantidade de despesas não mudou | esperado: " + quantidadeAntes
                + " | obtido: " + planejamento.getDespesas().size());
        conferir("Custo total não mudou", 21000, planejamento.calcularCustoTotal());

        //EDITAR DESPESA: a atividade recreativa passa a incluir o Bruno (R$ 30,00 / 3 = R$ 10,00 cada)
        System.out.println("\n=== Editar despesa ===");
        Despesa atividadeNova = new DespesaFixa("Atividade recreativa", 3000, Arrays.asList(ana, bruno, carla));
        planejamento.editarDespesa(atividade, atividadeNova);
        conferir("Total Ana depois de editar", 7000, planejamento.calcularTotalDoParticipante(1));
        conferir("Total Bruno depois de editar", 7000, planejamento.calcularTotalDoParticipante(2));
        conferir("Custo total continua", 21000, planejamento.calcularCustoTotal());
        System.out.println("Posição da atividade na lista (0 = primeira): "
                + planejamento.getDespesas().indexOf(atividadeNova) + " (esperado: 2, a mesma de antes)");

        //Trocar o tipo: almoço passa de "por pessoa R$ 20,00" para "fixa R$ 60,00" (dá os mesmos R$ 20,00 cada)
        Despesa almocoFixo = new DespesaFixa("Almoço", 6000, Arrays.asList(ana, bruno, carla));
        planejamento.editarDespesa(almoco, almocoFixo);
        conferir("Custo total com almoço fixo", 21000, planejamento.calcularCustoTotal());
        System.out.println("Tipo do almoço agora: " + planejamento.getDespesas().get(1).getTipo());

        //Desfaz a edição da atividade para voltar ao caso original
        planejamento.editarDespesa(atividadeNova, atividade);

        //EXCLUIR DESPESA: sem a atividade recreativa, cada um paga R$ 60,00 e o total vai para R$ 180,00
        System.out.println("\n=== Excluir despesa ===");
        planejamento.excluirDespesa(atividade);
        conferir("Total Ana", 6000, planejamento.calcularTotalDoParticipante(1));
        conferir("Total Bruno", 6000, planejamento.calcularTotalDoParticipante(2));
        conferir("Total Carla", 6000, planejamento.calcularTotalDoParticipante(3));
        conferir("Custo total", 18000, planejamento.calcularCustoTotal());

        //Participante com despesa vinculada não pode ser excluído (regra da parte A usando getParticipantes da parte B)
        try {
            planejamento.excluirParticipante(2);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    //Compara o valor esperado com o obtido e imprime OK ou FALHOU
    private static void conferir(String descricao, int esperado, int obtido) {
        String resultado = (esperado == obtido) ? "OK" : "FALHOU";
        System.out.println("[" + resultado + "] " + descricao + " | esperado: " + reais(esperado)
                + " | obtido: " + reais(obtido));
    }

    //Converte centavos para texto em reais (ex.: 3334 -> "33,34")
    private static String reais(int centavos) {
        return String.format("%.2f", centavos / 100.0).replace('.', ',');
    }

    //Mostra o mapa de rateio como "1=30,00 2=30,00 3=30,00"
    private static String formatarRateio(Map<Integer, Integer> rateio) {
        String texto = "";
        for (Map.Entry<Integer, Integer> item : rateio.entrySet()) {
            texto = texto + item.getKey() + "=" + reais(item.getValue()) + " ";
        }
        return texto.trim();
    }
}
