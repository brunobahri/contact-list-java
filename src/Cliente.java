package src;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Cliente {
    private String nome;
    private LocalDateTime dataCadastro;
    private List<Contato> contatos;

    public Cliente(String nome) {
        this.nome = nome;
        this.dataCadastro = LocalDateTime.now();
        this.contatos = new ArrayList<>();
    }

    public void adicionarContato(Contato contato) {
        contatos.add(contato);
    }

    public List<Contato> getContatos() {
        return contatos;
    }

    public String getNome() {
        return nome;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    @Override
    public String toString() {
        return nome;
    }
}
