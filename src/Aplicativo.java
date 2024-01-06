package src;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Aplicativo {
    private static List<Cliente> clientes = new ArrayList<>();
    private static JList<Cliente> listaClientes;
    private static JTable tabelaContatos;
    private static DefaultTableModel modeloTabela;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                criarEExibirGUI();
            }
        });
    }

    private static void criarEExibirGUI() {
        JFrame frame = new JFrame("Cadastro de clientes Qualitermo");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLayout(new BorderLayout());
    
        JButton btnCadastrarCliente = new JButton("Cadastrar Cliente");
        JButton btnAdicionarContato = new JButton("Adicionar Contato");
    
        listaClientes = new JList<>();
        listaClientes.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    Cliente clienteSelecionado = listaClientes.getSelectedValue();
                    if (clienteSelecionado != null) {
                        atualizarTabelaContatos(clienteSelecionado);
                    }
                }
            }
        });
        inicializarTabelaContatos();
        carregarClientes();
    
        // Adicionando o menu de contexto à lista de clientes
        JPopupMenu menuContexto = new JPopupMenu();
        JMenuItem menuItemVerDetalhes = new JMenuItem("Ver Detalhes");
        JMenuItem menuItemExcluirCliente = new JMenuItem("Excluir Cliente");
    
        menuContexto.add(menuItemVerDetalhes);
        menuContexto.add(menuItemExcluirCliente);
        listaClientes.setComponentPopupMenu(menuContexto);
    
        menuItemVerDetalhes.addActionListener(e -> mostrarDetalhesCliente());
        menuItemExcluirCliente.addActionListener(e -> excluirCliente());
    
        JPanel painelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelSuperior.add(btnCadastrarCliente);
        painelSuperior.add(btnAdicionarContato);
    
        // Cria um JLabel para o rodapé
        JLabel lblRodape = new JLabel("app desenvolvido por Bruno Bahri", SwingConstants.CENTER);
        lblRodape.setFont(new Font("Dialog", Font.ITALIC, 12)); // Define a fonte e o tamanho do texto do rodapé
        lblRodape.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Adiciona um pouco de espaço ao redor do rótulo
    
        // Cria um painel para o rodapé que também contém a tabela de contatos
        JPanel painelRodape = new JPanel(new BorderLayout());
        painelRodape.add(lblRodape, BorderLayout.SOUTH); // Adiciona o rótulo do rodapé ao painel
        painelRodape.add(new JScrollPane(tabelaContatos), BorderLayout.CENTER);
    
        frame.add(painelSuperior, BorderLayout.NORTH);
        frame.add(new JScrollPane(listaClientes), BorderLayout.CENTER);
        frame.add(painelRodape, BorderLayout.SOUTH);
    
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salvarClientes();
                frame.dispose();
            }
        });
    
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private static void mostrarDetalhesCliente() {
        Cliente clienteSelecionado = listaClientes.getSelectedValue();
        if (clienteSelecionado != null) {
            JOptionPane.showMessageDialog(null, "Data de Inclusão: " + clienteSelecionado.getDataCadastro());
        }
    }
    
    private static void excluirCliente() {
        Cliente clienteSelecionado = listaClientes.getSelectedValue();
        if (clienteSelecionado != null) {
            String senha = JOptionPane.showInputDialog("Digite a senha para excluir o cliente:");
            if ("1234".equals(senha)) {
                clientes.remove(clienteSelecionado);
                atualizarListaClientes();
                salvarClientes();
                JOptionPane.showMessageDialog(null, "Cliente excluído com sucesso.");
            } else {
                JOptionPane.showMessageDialog(null, "Senha incorreta.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void inicializarTabelaContatos() {
        modeloTabela = new DefaultTableModel();
        modeloTabela.addColumn("Nome");
        modeloTabela.addColumn("Email");
        modeloTabela.addColumn("Telefone");
        modeloTabela.addColumn("Ações");

        tabelaContatos = new JTable(modeloTabela);
        tabelaContatos.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        tabelaContatos.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox()));
    }

    private static void carregarClientes() {
        try (BufferedReader br = new BufferedReader(new FileReader("clientes.txt"))) {
            String linha;
            Cliente clienteAtual = null;
            while ((linha = br.readLine()) != null) {
                if (linha.startsWith("Cliente:")) {
                    String nomeCliente = linha.substring(8);
                    clienteAtual = new Cliente(nomeCliente);
                    clientes.add(clienteAtual);
                } else if (linha.startsWith("Contato:") && clienteAtual != null) {
                    String[] partes = linha.substring(8).split(", ");
                    Contato contato = new Contato(partes[0], partes[1], partes[2]);
                    clienteAtual.adicionarContato(contato);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        atualizarListaClientes(); // Atualizar a lista de clientes na interface
    }

    private static void atualizarListaClientes() {
        DefaultListModel<Cliente> modelo = new DefaultListModel<>();
        for (Cliente cliente : clientes) {
            modelo.addElement(cliente);
        }
        listaClientes.setModel(modelo);
    }

    private static void atualizarTabelaContatos(Cliente cliente) {
        modeloTabela.setRowCount(0); // Limpa a tabela
        for (Contato contato : cliente.getContatos()) {
            modeloTabela.addRow(new Object[]{contato.getNome(), contato.getEmail(), contato.getTelefone(), "Editar/Excluir"});
        }
    }

    private static void salvarClientes() {
    try (PrintWriter out = new PrintWriter(new FileWriter("clientes.txt"))) {
        for (Cliente cliente : clientes) {
            out.println("Cliente:" + cliente.getNome());
            for (Contato contato : cliente.getContatos()) {
                out.println("Contato:" + contato.getNome() + ", " + contato.getEmail() + ", " + contato.getTelefone());
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}

static class ButtonRenderer extends JPanel implements TableCellRenderer {
    private final JButton editButton;
    private final JButton deleteButton;

    public ButtonRenderer() {
        editButton = new JButton("Editar");
        deleteButton = new JButton("-");
        configureButton(editButton, "Editar");
        configureButton(deleteButton, "-");

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        add(Box.createHorizontalGlue()); // Espaço antes do primeiro botão
        add(editButton);
        add(Box.createRigidArea(new Dimension(10, 0))); // Espaço entre os botões
        add(deleteButton);
        add(Box.createHorizontalGlue()); // Espaço após o último botão
    }

    private void configureButton(JButton button, String text) {
        button.setText(text);
        button.setFont(new Font("Dialog", Font.PLAIN, 12));
        button.setMargin(new Insets(0, 5, 0, 5));
        if ("-".equals(text)) {
            button.setBackground(new Color(255, 102, 102)); // Vermelho claro
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
        }
        button.setOpaque(true);
        button.setBorderPainted(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return this;
    }
}

static class ButtonEditor extends DefaultCellEditor {
    protected JPanel panel;
    protected JButton editButton;
    protected JButton deleteButton;
    private int currentRow; // Adicionar um campo para armazenar a linha atual

    public ButtonEditor(JCheckBox checkBox) {
        super(checkBox);
        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0)); // Use FlowLayout para centralizar
        editButton = new JButton("Editar");
        deleteButton = new JButton("-");

        configureButton(editButton, "Editar");
        configureButton(deleteButton, "-");

        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int editingRow = tabelaContatos.getEditingRow();
                if (editingRow >= 0) {
                    editarContato(editingRow);
                    fireEditingStopped();
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int editingRow = tabelaContatos.getEditingRow();
                if (editingRow >= 0) {
                    excluirContato(editingRow);
                    fireEditingStopped();
                }
            }
        });

        panel.add(editButton);
        panel.add(Box.createRigidArea(new Dimension(10, 0))); // Espaço entre os botões
        panel.add(deleteButton);
    }

    private void configureButton(JButton button, String text) {
        button.setText(text);
        button.setFont(new Font("Dialog", Font.PLAIN, 12));
        button.setMargin(new Insets(0, 5, 0, 5));
        if ("-".equals(text)) {
            button.setBackground(new Color(255, 102, 102)); // Vermelho claro
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
        }
        button.setOpaque(true);
        button.setBorderPainted(false);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        currentRow = row;
        editButton = createButton("Editar");
        deleteButton = createButton("-");
        
        // Remova os listeners antigos se eles estiverem presentes para evitar múltiplas chamadas
    for (ActionListener al : editButton.getActionListeners()) {
        editButton.removeActionListener(al);
    }
    for (ActionListener al : deleteButton.getActionListeners()) {
        deleteButton.removeActionListener(al);
    }

    // Adiciona os listeners aos botões com a linha correta
    editButton.addActionListener(e -> {
        editarContato(row); 
        fireEditingStopped(); // Finaliza a edição
    });

    deleteButton.addActionListener(e -> {
        excluirContato(row);
        fireEditingStopped(); // Finaliza a edição
    });
    
    panel.removeAll();
    panel.add(editButton);
    panel.add(Box.createRigidArea(new Dimension(10, 0))); // Espaço entre os botões
    panel.add(deleteButton);

    return panel;
}
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        configureButton(button, text);
        if (text.equals("-")) {
            button.addActionListener(e -> excluirContato(currentRow));
        } else {
            button.addActionListener(e -> editarContato(currentRow));
        }
        return button;
    }
    
    

    private void editarContato(int viewRowIndex) {
        Cliente clienteSelecionado = listaClientes.getSelectedValue();
        if (clienteSelecionado != null) {
            int modelRowIndex = tabelaContatos.convertRowIndexToModel(viewRowIndex);
            if (modelRowIndex >= 0 && modelRowIndex < clienteSelecionado.getContatos().size()) {
                Contato contato = clienteSelecionado.getContatos().get(modelRowIndex);
                String novoNome = JOptionPane.showInputDialog(panel, "Editar Nome do Contato:", contato.getNome());
                String novoEmail = JOptionPane.showInputDialog(panel, "Editar Email do Contato:", contato.getEmail());
                String novoTelefone = JOptionPane.showInputDialog(panel, "Editar Telefone do Contato:", contato.getTelefone());
    
                if (novoNome != null && novoEmail != null && novoTelefone != null) {
                    contato.setNome(novoNome);
                    contato.setEmail(novoEmail);
                    contato.setTelefone(novoTelefone);
                    atualizarTabelaContatos(clienteSelecionado);
                    salvarClientes();
                }
            }
        }
    }
    
    private static void recarregarDados() {
        clientes.clear(); // Limpa a lista de clientes existente
        carregarClientes(); // Recarrega os clientes
        atualizarListaClientes(); // Atualiza a lista de clientes na interface gráfica
    
        // Se um cliente estiver selecionado, atualiza a tabela de contatos para esse cliente
        Cliente clienteSelecionado = listaClientes.getSelectedValue();
        if (clienteSelecionado != null) {
            atualizarTabelaContatos(clienteSelecionado);
        }
    }
    

    private void excluirContato(int viewRowIndex) {
        Cliente clienteSelecionado = listaClientes.getSelectedValue();
        if (clienteSelecionado != null) {
            int modelRowIndex = tabelaContatos.convertRowIndexToModel(viewRowIndex);
            if (modelRowIndex >= 0 && modelRowIndex < clienteSelecionado.getContatos().size()) {
                int confirm = JOptionPane.showConfirmDialog(panel, "Tem certeza que deseja excluir este contato?", "Excluir Contato", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    clienteSelecionado.getContatos().remove(modelRowIndex);
                    salvarClientes();
                    recarregarDados();
                }
            }
        }
    }      
}
}
