import java.util.*;

import java.util.*;

public class DominoGame {
    private static final int MAX_PLAYERS = 4;
    private List<Player> players = new ArrayList<>();
    private List<GameRecord> gameHistory = new ArrayList<>();
    private List<DominoTile> tableTiles = new ArrayList<>();
    private boolean isGameActive = false;
    private int end1 = -1; // Extremidade esquerda da mesa
    private int end2 = -1; // Extremidade direita da mesa

    public static void main(String[] args) {
        new DominoGame().start();
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            displayMainMenu();
            int choice = getValidInput(scanner, 1, 4);
            switch (choice) {
                case 1:
                    startNewGame(scanner);
                    break;
                case 2:
                    displayGameHistory();
                    break;
                case 3:
                    resetGame();
                    break;
                case 4:
                    System.out.println("Encerrando o sistema...");
                    return;
            }
        }
    }

    private void displayMainMenu() {
        System.out.println("\n--- 🎲 Menu Principal 🎲 ---");
        System.out.println("1. 🚀 Iniciar nova partida");
        System.out.println("2. 📋 Ver histórico de partidas");
        System.out.println("3. 🔁 Reiniciar o histórico");
        System.out.println("4. ❌ Encerrar execução");
        System.out.print("Escolha uma opção: ");
    }

    private void startNewGame(Scanner scanner) {
        resetGameVariables();
        registerPlayers(scanner);
        int principalPlayerIndex = selectPrincipalPlayer(scanner);
        definePlayerDominoes(scanner, players.get(principalPlayerIndex));
        int startingPlayerIndex = selectStartingPlayer(scanner);
        playGame(scanner, startingPlayerIndex, principalPlayerIndex);
    }

    private void registerPlayers(Scanner scanner) {
        System.out.println("\n--- 👥 Cadastro de Jogadores ---");
        players.clear();
        for (int i = 0; i < MAX_PLAYERS; i++) {
            System.out.print("Digite o nome do jogador " + (i + 1) + " (ou pressione Enter para usar o nome padrão): ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                name = "Jogador " + (i + 1);
            }
            players.add(new Player(name));
        }
        System.out.println("\n--- ✅ Jogadores cadastrados ---");
        for (Player player : players) {
            System.out.println("- " + player.getName());
        }
    }

    private int selectPrincipalPlayer(Scanner scanner) {
        System.out.println("\n--- 👤 Quem é você? ---");
        return selectPlayer(scanner);
    }

    private void definePlayerDominoes(Scanner scanner, Player player) {
        System.out.println("\n--- 🪨 Definindo as pedras de " + player.getName() + " ---");
        List<DominoTile> playerDominoes = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            while (true) {
                try {
                    System.out.print("Digite a pedra " + (i + 1) + " no formato 'x/y': ");
                    String dominoInput = scanner.nextLine().trim();
                    if (!dominoInput.matches("\\d+/\\d+")) {
                        throw new IllegalArgumentException("Formato inválido. Use o formato 'x/y' com números inteiros.");
                    }
                    String[] sides = dominoInput.split("/");
                    int side1 = Integer.parseInt(sides[0]);
                    int side2 = Integer.parseInt(sides[1]);
                    if (side1 > 6 || side2 > 6) {
                        System.out.println("Pedra inválida! O valor das pedras deve ser entre 0 e 6.");
                        continue;
                    }
                    playerDominoes.add(new DominoTile(side1, side2));
                    break;
                } catch (Exception e) {
                    System.out.println("Entrada inválida. Tente novamente.");
                }
            }
        }
        player.setDominoes(playerDominoes);
        System.out.println("✅ Pedras definidas para " + player.getName() + ": " + playerDominoes);
    }

    private int selectStartingPlayer(Scanner scanner) {
        System.out.println("\n--- 🎯 Selecione quem vai começar a partida ---");
        return selectPlayer(scanner);
    }

    private int selectPlayer(Scanner scanner) {
        for (int i = 0; i < players.size(); i++) {
            System.out.println((i + 1) + ". " + players.get(i).getName());
        }
        int selectedPlayerIndex = 0;
        while (true) {
            try {
                int selection = Integer.parseInt(scanner.nextLine()) - 1;
                if (selection >= 0 && selection < players.size()) {
                    selectedPlayerIndex = selection;
                    break;
                } else {
                    System.out.println("Seleção inválida. Escolha um jogador da lista.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um número correspondente ao jogador.");
            }
        }
        return selectedPlayerIndex;
    }

    private void playGame(Scanner scanner, int startingPlayerIndex, int principalPlayerIndex) {
        System.out.println("\n--- 🎮 Início do Jogo ---");
        isGameActive = true;
        tableTiles.clear();
        Set<DominoTile> allTiles = generateAllTiles();

        // Remove as pedras dos jogadores do conjunto total
        for (Player player : players) {
            allTiles.removeAll(player.getDominoes());
        }

        int currentPlayerIndex = startingPlayerIndex;

        while (isGameActive) {
            Player currentPlayer = players.get(currentPlayerIndex);

            // Calcula as possíveis pedras para todos os jogadores, exceto o principal
            calculatePossibleTilesForPlayers(allTiles, principalPlayerIndex);

            System.out.println("\n--- 🪞 Mesa atual: " + formatTableTiles() + " ---");
            System.out.println("➡️ Vez de " + currentPlayer.getName() + ":");
            if (currentPlayerIndex == principalPlayerIndex) {
                System.out.println("🪨 Minhas pedras: " + currentPlayer.getDominoes());
            } else {
                System.out.println("📚 Histórico do jogador: " + currentPlayer.getFormattedHistory());
            }

            boolean validMove = false;
            while (!validMove) {
                System.out.print("Escolha uma pedra para jogar (formato x/y) ou digite 'p' para passar: ");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("p")) {
                    System.out.println(currentPlayer.getName() + " passou a vez.");

                    // Registra o passe no histórico com os valores das pontas da mesa como negativos
                    DominoTile passTile = new DominoTile(-Math.abs(end1), -Math.abs(end2));
                    currentPlayer.addToHistory(passTile);

                    // Remove todas as pedras que contenham os números das extremidades (6 ou 2)
                    if (end1 != -1) {
                        removeTilesWithNumber(currentPlayer, Math.abs(end1));
                    }
                    if (end2 != -1) {
                        removeTilesWithNumber(currentPlayer, Math.abs(end2));
                    }

                    break;
                }
                try {
                    String[] split = input.split("/");
                    if (split.length != 2) throw new IllegalArgumentException("Formato inválido.");
                    int side1 = Integer.parseInt(split[0]);
                    int side2 = Integer.parseInt(split[1]);
                    DominoTile tileToPlay = new DominoTile(side1, side2);

                    if (currentPlayerIndex != principalPlayerIndex && !allTiles.contains(tileToPlay)) {
                        throw new IllegalArgumentException("A pedra não está entre as possíveis pedras do jogador.");
                    }

                    if (playTileOnTable(tileToPlay)) {
                        currentPlayer.playTile(tileToPlay);
                        allTiles.remove(tileToPlay);

                        // Remover a pedra jogada das possíveis pedras de todos os jogadores
                        for (Player player : players) {
                            if (player != currentPlayer) { // Não altera o conjunto do jogador atual
                                player.addToNotPossibleTiles(tileToPlay);
                            }
                        }

                        validMove = true;
                        if (currentPlayer.getDominoes().isEmpty() && currentPlayer.getPlayCount() == 7) {
                            System.out.println("🎉 Parabéns, " + currentPlayer.getName() + "! Você venceu por jogar todas as suas peças após 7 jogadas.");
                            endGame(currentPlayer);
                            return;
                        } else if (currentPlayer.getDominoes().isEmpty()) {
                            System.out.println(currentPlayer.getName() + " jogou todas as suas peças, mas ainda não completou 7 jogadas. Continuando...");
                        }
                        System.out.println(currentPlayer.getName() + " jogou " + tileToPlay);
                    } else {
                        System.out.println("❌ Jogada inválida. A pedra não combina com nenhuma extremidade da mesa.");
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ " + e.getMessage());
                }
            }
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }
    }

    private Set<DominoTile> generateAllTiles() {
        Set<DominoTile> allTiles = new HashSet<>();
        for (int i = 0; i <= 6; i++) {
            for (int j = i; j <= 6; j++) {
                allTiles.add(new DominoTile(i, j));
            }
        }
        return allTiles;
    }

    private void calculatePossibleTilesForPlayers(Set<DominoTile> allTiles, int principalPlayerIndex) {
        for (int i = 0; i < players.size(); i++) {
            if (i == principalPlayerIndex) continue;
            Player player = players.get(i);
            Set<DominoTile> possibleTiles = new HashSet<>(allTiles);
            possibleTiles.removeAll(player.getNotPossibleTiles());
            System.out.println("🎲 Possíveis pedras para " + player.getName() + ": " + possibleTiles);
        }
    }

    private boolean playTileOnTable(DominoTile tile) {
        if (tableTiles.isEmpty()) {
            tableTiles.add(tile);
            end1 = tile.getSide1();
            end2 = tile.getSide2();
            return true;
        }
        if (tile.getSide1() == end1) {
            tableTiles.add(0, new DominoTile(tile.getSide2(), tile.getSide1()));
            end1 = tile.getSide2();
            return true;
        } else if (tile.getSide2() == end1) {
            tableTiles.add(0, tile);
            end1 = tile.getSide1();
            return true;
        } else if (tile.getSide1() == end2) {
            tableTiles.add(tile);
            end2 = tile.getSide2();
            return true;
        } else if (tile.getSide2() == end2) {
            tableTiles.add(new DominoTile(tile.getSide2(), tile.getSide1()));
            end2 = tile.getSide1();
            return true;
        }
        return false;
    }

    private String formatTableTiles() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tableTiles.size(); i++) {
            DominoTile tile = tableTiles.get(i);
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(tile);
        }
        return sb.toString().trim();
    }

    private void endGame(Player winner) {
        System.out.println("\n--- 🏆 Fim da partida! ---");
        System.out.println("O vencedor é: " + winner.getName());
        isGameActive = false;
        gameHistory.add(new GameRecord(gameHistory.size() + 1, players));
    }

    private void displayGameHistory() {
        if (gameHistory.isEmpty()) {
            System.out.println("\n--- 📋 Nenhuma partida registrada no histórico ---");
        } else {
            System.out.println("\n--- 📋 Histórico de Partidas ---");
            for (GameRecord record : gameHistory) {
                System.out.println(record);
            }
        }
    }

    private void resetGame() {
        players.clear();
        gameHistory.clear();
        tableTiles.clear();
        isGameActive = false;
        System.out.println("\n--- 🔄 Jogo reiniciado com sucesso! ---");
    }

    private void resetGameVariables() {
        tableTiles.clear();
        isGameActive = false;
    }

    private int getValidInput(Scanner scanner, int min, int max) {
        while (true) {
            try {
                int input = Integer.parseInt(scanner.nextLine());
                if (input >= min && input <= max) {
                    return input;
                } else {
                    System.out.println("Opção inválida. Tente novamente.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um número.");
            }
        }
    }

    private void removeTilesWithNumber(Player player, int number) {
        Set<DominoTile> possibleTiles = generateAllTiles(); // Gera todas as pedras possíveis
        possibleTiles.removeAll(player.getNotPossibleTiles()); // Remove as já impossíveis

        for (DominoTile tile : possibleTiles) {
            if (tile.getSide1() == number || tile.getSide2() == number) {
                player.addToNotPossibleTiles(tile); // Adiciona ao conjunto de pedras impossíveis
            }
        }
    }
}
// Classes auxiliares
class DominoTile {
    private final int side1;
    private final int side2;

    public DominoTile(int side1, int side2) {
        this.side1 = Math.min(side1, side2);
        this.side2 = Math.max(side1, side2);
    }

    public int getSide1() {
        return side1;
    }

    public int getSide2() {
        return side2;
    }

    @Override
    public String toString() {
        return side1 + "/" + side2;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DominoTile tile = (DominoTile) obj;
        return side1 == tile.side1 && side2 == tile.side2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(side1, side2);
    }
}

class GameRecord {
    private final int gameId;
    private final List<Player> players;

    public GameRecord(int gameId, List<Player> players) {
        this.gameId = gameId;
        this.players = players;
    }

    @Override
    public String toString() {
        return "Partida #" + gameId + " - Jogadores: " +
                players.stream()
                        .map(Player::getName)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
    }
}

class Player {
    private String name;
    private List<DominoTile> dominoes = new ArrayList<>();
    private List<DominoTile> history = new ArrayList<>();
    private Set<DominoTile> notPossibleTiles = new HashSet<>();
    private int playCount = 0;

    public Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<DominoTile> getDominoes() {
        return dominoes;
    }

    public void setDominoes(List<DominoTile> dominoes) {
        this.dominoes = dominoes;
    }

    public List<DominoTile> getHistory() {
        return history;
    }

    public void playTile(DominoTile tile) {
        dominoes.remove(tile);
        history.add(tile);
        playCount++;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void addToHistory(DominoTile tile) {
        history.add(tile);
    }

    public void addToNotPossibleTiles(DominoTile tile) {
        notPossibleTiles.add(tile);
    }

    public Set<DominoTile> getNotPossibleTiles() {
        return notPossibleTiles;
    }

    public String getFormattedHistory() {
        if (history.isEmpty()) {
            return "Nenhuma jogada realizada ainda";
        }
        StringBuilder sb = new StringBuilder();
        for (DominoTile tile : history) {
            sb.append(tile).append(" ");
        }
        return sb.toString().trim();
    }
}