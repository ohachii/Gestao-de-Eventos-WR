package org.example.view;

import java.sql.SQLException;
import java.util.Scanner;
import org.example.dao.*;
import org.example.models.*;
import org.example.utils.ConnectionFactory;

public class Main {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            iniciarSistema();
            mostrarMenuPrincipal();
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static void iniciarSistema() {
        ConnectionFactory.criarTabelas();
    }

    private static void mostrarMenuPrincipal() {
        System.out.println("\n--- Sistema de Eventos Acadêmicos ---");

        while (true) {
            System.out.println("\n1. Login");
            System.out.println("2. Cadastrar");
            System.out.println("3. Sair");
            System.out.print("Escolha uma opção: ");

            int opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1:
                    fazerLogin();
                    break;
                case 2:
                    cadastrarUsuario();
                    break;
                case 3:
                    System.out.println("Saindo do sistema...");
                    return;
                default:
                    System.out.println("Opção inválida!");
            }
        }
    }

    private static void fazerLogin() {
        System.out.println("\n--- Login ---");
        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        try {
            Usuario usuario = new UsuarioDAO().login(email, senha);

            if (usuario == null) {
                System.out.println("Credenciais inválidas!");
                return;
            }

            System.out.println("\nBem-vindo, " + usuario.getNome() + "!");

            if (usuario instanceof Administrador) {
                mostrarMenuAdmin();
            } else if (usuario instanceof Participante) {
                mostrarMenuParticipante((Participante) usuario);
            }

        } catch (SQLException e) {
            System.err.println("Erro no login: " + e.getMessage());
        }
    }

    private static void cadastrarUsuario() {
        System.out.println("\n--- Cadastro ---");
        System.out.print("Nome completo: ");
        String nome = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Senha (mínimo 6 caracteres): ");
        String senha = scanner.nextLine();

        System.out.print("Tipo (ALUNO/PROFESSOR/PROFISSIONAL): ");
        String tipo = scanner.nextLine().toUpperCase();

        try {
            Participante novoUsuario = new Participante(nome, email, senha, tipo);
            new UsuarioDAO().createUser(novoUsuario);
            System.out.println("Cadastro realizado com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro no cadastro: " + e.getMessage());
        }
    }

    private static void mostrarMenuAdmin() {
        System.out.println("\n--- Menu Administrador ---");

        while (true) {
            System.out.println("\n1. Gerenciar Eventos");
            System.out.println("2. Gerenciar Atividades");
            System.out.println("3. Configurações");
            System.out.println("4. Sair");
            System.out.print("Escolha uma opção: ");

            int opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1:
                    gerenciarEventos();
                    break;
                case 2:
                    gerenciarAtividades();
                    break;
                case 3:
                    gerenciarConfiguracoes();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Opção inválida!");
            }
        }
    }

    private static void gerenciarConfiguracoes() {

        System.out.println("Em construção ( Libera ponto por favor )");
    }

    private static void mostrarMenuParticipante(Participante participante) {
        System.out.println("\n--- Menu Participante ---");

        while (true) {
            System.out.println("\n1. Ver Eventos");
            System.out.println("2. Minhas Inscrições");
            System.out.println("3. Sair");
            System.out.print("Escolha uma opção: ");

            int opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1:
                    listarEventosDisponiveis();
                    break;
                case 2:
                    verMinhasInscricoes(participante.getId());
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Opção inválida!");
            }
        }
    }


    private static void gerenciarEventos() {
        System.out.println("\n--- Gerenciamento de Eventos ---");
    }

    private static void gerenciarAtividades() {
        System.out.println("\n--- Gerenciamento de Atividades ---");
    }

    private static void listarEventosDisponiveis() {
        try {
            System.out.println("\n--- Eventos Disponíveis ---");
            System.out.println(new EventoDAO().listarEventosFormatados());
        } catch (SQLException e) {
            System.err.println("Erro ao listar eventos: " + e.getMessage());
        }
    }

    private static void verMinhasInscricoes(int idUsuario) {
        try {
            System.out.println("\n--- Minhas Inscrições ---");
            System.out.println(new InscricaoEventoDAO().listarEventosConfirmadosDoUsuario(idUsuario));
        } catch (SQLException e) {
            System.err.println("Erro ao listar inscrições: " + e.getMessage());
        }
    }
}