package modelo;

public class Teste {
    public static void main(String[] args) {

        Planejamento planejamento = new Planejamento();

        planejamento.cadastrarParticipante(1, "Joao", 7000);
        planejamento.cadastrarParticipante(3, "Laila", 7000);
        planejamento.cadastrarParticipante(4, "Luiz", 7000);
        planejamento.cadastrarParticipante(5, "Grazi", 7000);
        planejamento.cadastrarParticipante(2, "Jenifer", 7000);




        //TESTES
        // código duplicado
        try {
            planejamento.cadastrarParticipante(1, "Outro", 5000);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }

        // orçamento negativo
        try {
            planejamento.cadastrarParticipante(6, "Teste", -100);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }

        // o código 6 não deve ter sido cadastrado
        System.out.println(planejamento.buscarParticipante(6));   // null

        // alterar nome e orçamento
        planejamento.alterarParticipante(1, "Joao Bosco", 8000);
        System.out.println(planejamento.buscarParticipante(1));

        // percorrer em ordem crescente de código
        for (Participante p : planejamento.getTodosParticipantes()) {
            System.out.println(p);
        }
    }
}