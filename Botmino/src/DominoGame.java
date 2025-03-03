import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class DominoGame {
    private static final int MAX_PLAYERS = 4;
    private List<Player> players = new ArrayList<>();
    private List<GameRecord> gameHistory = new ArrayList<>();
    private List<DominoTile> tableTiles = new ArrayList<>();
    private boolean isGameActive = false;
    private int end1 = -1; // Extremidade esquerda da mesa
    private int end2 = -1; // Extremidade direita da mesa
    // Variáveis para controle de coelhos e gatos
    private Map<String, Integer> playerScores = new HashMap<>(); // Nome do jogador -> Vitórias
    private int rabbitPoints = 0; // Coelhos
    private int catPoints = 0;    // Gatos

    public static void main(String[] args) {
        new DominoGame().start();
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            displayMainMenu();
            int choice = getValidInput(scanner, 1, 5); // Atualizado para incluir a nova opção
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
                    simulatePredefinedGame(); // Nova funcionalidade
                    break;
                case 5:
                    System.out.println("Encerrando o sistema...");
                    return;
            }
        }
    }

    private void registerPlayers(Scanner scanner) {
        System.out.println("\n--- 👥 Cadastro de Jogadores ---");
        players.clear();
        playerScores.clear(); // Limpa as pontuações ao reiniciar
        for (int i = 0; i < MAX_PLAYERS; i++) {
            System.out.print("Digite o nome do jogador " + (i + 1) + " (ou pressione Enter para usar o nome padrão): ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                name = "Jogador " + (i + 1);
            }
            players.add(new Player(name));
            playerScores.put(name, 0); // Inicializa a pontuação do jogador
        }
        System.out.println("\n--- ✅ Jogadores cadastrados ---");
        for (Player player : players) {
            System.out.println("- " + player.getName());
        }
    }

    private void saveResultsToFile(Player winner) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("game_results.txt", true))) {
            writer.write("Partida #" + gameHistory.size() + " - Vencedor: " + winner.getName());
            writer.newLine();
            writer.write("Pontuação Atual:");
            for (Map.Entry<String, Integer> entry : playerScores.entrySet()) {
                writer.write(" " + entry.getKey() + ": " + entry.getValue());
            }
            writer.newLine();
            writer.write("Coelhos: " + rabbitPoints + ", Gatos: " + catPoints);
            writer.newLine();
            writer.write("	");
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Erro ao salvar os resultados no arquivo.");
        }
    }

    private void endGame(Player winner) {
        System.out.println("\n--- 🏆 Fim da partida! ---");
        System.out.println("O vencedor é: " + winner.getName());
        // Incrementa a pontuação do vencedor
        playerScores.put(winner.getName(), playerScores.get(winner.getName()) + 1);
        // Verifica se o jogador atingiu 4 vitórias (coelho)
        if (playerScores.get(winner.getName()) >= 4) {
            rabbitPoints++;
            System.out.println("🎉 " + winner.getName() + " fez um COELHO!");
            playerScores.put(winner.getName(), 0); // Reseta a pontuação do jogador
        }
        // Verifica se nenhum jogador marcou pontos (gato)
        boolean noPoints = true;
        for (Player player : players) {
            if (player.getPlayCount() > 0) {
                noPoints = false;
                break;
            }
        }
        if (noPoints) {
            catPoints++;
            System.out.println("🐱 Nenhum jogador marcou pontos. Um GATO foi adicionado!");
        }
        // Salva os resultados no arquivo
        saveResultsToFile(winner);
        // Exibe os resultados no console
        displayScores();
        isGameActive = false;
        gameHistory.add(new GameRecord(gameHistory.size() + 1, players));
    }

    private void displayScores() {
        System.out.println("\n--- 📊 Pontuação Atual ---");
        for (Map.Entry<String, Integer> entry : playerScores.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue() + " vitórias");
        }
        System.out.println("Coelhos: " + rabbitPoints);
        System.out.println("Gatos: " + catPoints);
    }

    private void displayMainMenu() {
        System.out.println("\n--- 🎲 Menu Principal 🎲 ---");
        System.out.println("1. 🚀 Iniciar nova partida");
        System.out.println("2. 📋 Ver histórico de partidas");
        System.out.println("3. 🔁 Reiniciar o histórico");
        System.out.println("4. 🧪 Simular um jogo predefinido"); // Nova opção
        System.out.println("5. ❌ Encerrar execução");
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
                    if (playTileOnTable(tileToPlay, scanner)) {
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
                            // Remove a mensagem indesejada
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

    private boolean playTileOnTable(DominoTile tile, Scanner scanner) {
        if (tableTiles.isEmpty()) {
            tableTiles.add(tile);
            end1 = tile.getSide1();
            end2 = tile.getSide2();
            return true;
        }
        boolean canPlaceLeft = tile.getSide1() == end1 || tile.getSide2() == end1;
        boolean canPlaceRight = tile.getSide1() == end2 || tile.getSide2() == end2;
        if (canPlaceLeft && canPlaceRight) {
            System.out.print("A pedra pode ser colocada nos dois lados. Escolha o lado (E para esquerdo, D para direito): ");
            String sideChoice = scanner.nextLine().trim().toUpperCase();
            if (sideChoice.equals("E")) {
                canPlaceRight = false; // Força o uso do lado esquerdo
            } else if (sideChoice.equals("D")) {
                canPlaceLeft = false; // Força o uso do lado direito
            } else {
                System.out.println("Escolha inválida. Usando o lado esquerdo por padrão.");
                canPlaceRight = false;
            }
        }
        if (canPlaceLeft) {
            if (tile.getSide1() == end1) {
                tableTiles.add(0, new DominoTile(tile.getSide2(), tile.getSide1()));
                end1 = tile.getSide2();
            } else {
                tableTiles.add(0, tile);
                end1 = tile.getSide1();
            }
            return true;
        }
        if (canPlaceRight) {
            if (tile.getSide1() == end2) {
                tableTiles.add(tile);
                end2 = tile.getSide2();
            } else {
                tableTiles.add(new DominoTile(tile.getSide2(), tile.getSide1()));
                end2 = tile.getSide1();
            }
            return true;
        }
        return false;
    }

    private String formatTableTiles() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tableTiles.size(); i++) {
            DominoTile tile = tableTiles.get(i);
            if (i > 0) {
                sb.append(" - "); // Conector visual entre as pedras
            }
            sb.append(tile);
        }
        return sb.toString().trim();
    }

    private void displayGameHistory() {
        if (gameHistory.isEmpty()) {
            System.out.println("\n--- 📋 Nenhuma partida registrada no histórico ---");
        } else {
            System.out.println("\n--- 📋 Histórico de Partidas ---");
            for (GameRecord record : gameHistory) {
                System.out.print("Partida #" + record.gameId + " - Jogadores: ");
                for (Player player : record.players) {
                    int rabbitCount = playerScores.get(player.getName()) / 4; // Coelhos
                    int catCount = playerScores.get(player.getName()) % 4; // Gatos
                    System.out.print(player.getName() + " (" + rabbitCount + " coelhos, " + catCount + " gatos) ");
                }
                System.out.println();
            }
        }
    }

    private void resetGame() {
        players.clear();
        gameHistory.clear();
        tableTiles.clear();
        isGameActive = false;
        playerScores.clear();
        rabbitPoints = 0;
        catPoints = 0;
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

    private void simulatePredefinedGame() {
        System.out.println("\n--- 🧪 Simulando um Jogo Predefinido ---");

        // Criar jogadores fictícios
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < MAX_PLAYERS; i++) {
            players.add(new Player("Jogador " + (i + 1)));
        }

        // Definir manualmente as pedras dos jogadores
        List<DominoTile> player1Tiles = Arrays.asList(
                new DominoTile(0, 0), new DominoTile(1, 1), new DominoTile(2, 2),
                new DominoTile(3, 3), new DominoTile(4, 4), new DominoTile(5, 5),
                new DominoTile(6, 6)
        );
        List<DominoTile> player2Tiles = Arrays.asList(
                new DominoTile(0, 1), new DominoTile(1, 2), new DominoTile(2, 3),
                new DominoTile(3, 4), new DominoTile(4, 5), new DominoTile(5, 6),
                new DominoTile(0, 6)
        );
        List<DominoTile> player3Tiles = Arrays.asList(
                new DominoTile(0, 2), new DominoTile(1, 3), new DominoTile(2, 4),
                new DominoTile(3, 5), new DominoTile(4, 6), new DominoTile(0, 5),
                new DominoTile(1, 6)
        );
        List<DominoTile> player4Tiles = Arrays.asList(
                new DominoTile(0, 3), new DominoTile(1, 4), new DominoTile(2, 5),
                new DominoTile(3, 6), new DominoTile(0, 4), new DominoTile(1, 5),
                new DominoTile(2, 6)
        );

        // Atribuir as pedras aos jogadores
        players.get(0).setDominoes(player1Tiles);
        players.get(1).setDominoes(player2Tiles);
        players.get(2).setDominoes(player3Tiles);
        players.get(3).setDominoes(player4Tiles);

        // Simular o jogo
        simulateGame(players);
    }

    private void simulateGame(List<Player> players) {
        List<DominoTile> tableTiles = new ArrayList<>();
        int end1 = -1, end2 = -1;

        while (!isGameOver(players)) {
            for (Player player : players) {
                if (player.getDominoes().isEmpty()) continue;

                Optional<DominoTile> playableTile = findPlayableTile(player.getDominoes(), end1, end2);
                if (playableTile.isPresent()) {
                    DominoTile tile = playableTile.get();
                    playTileOnTable(tile, tableTiles, player);
                    end1 = tableTiles.get(0).getSide1();
                    end2 = tableTiles.get(tableTiles.size() - 1).getSide2();
                    System.out.println(player.getName() + " jogou: " + tile);
                } else {
                    System.out.println(player.getName() + " passou a vez.");
                }
            }
        }

        System.out.println("--- 🏁 Fim da Simulação ---");
    }

    private boolean isGameOver(List<Player> players) {
        return players.stream().allMatch(player -> player.getDominoes().isEmpty());
    }

    private Optional<DominoTile> findPlayableTile(List<DominoTile> dominoes, int end1, int end2) {
        return dominoes.stream()
                .filter(tile -> end1 == -1 || tile.getSide1() == end1 || tile.getSide2() == end1 ||
                        tile.getSide1() == end2 || tile.getSide2() == end2)
                .findFirst();
    }

    private void playTileOnTable(DominoTile tile, List<DominoTile> tableTiles, Player player) {
        if (tableTiles.isEmpty()) {
            tableTiles.add(tile);
        } else {
            int end1 = tableTiles.get(0).getSide1();
            int end2 = tableTiles.get(tableTiles.size() - 1).getSide2();

            if (tile.getSide1() == end1) {
                tableTiles.add(0, new DominoTile(tile.getSide2(), tile.getSide1()));
            } else if (tile.getSide2() == end1) {
                tableTiles.add(0, tile);
            } else if (tile.getSide1() == end2) {
                tableTiles.add(tile);
            } else if (tile.getSide2() == end2) {
                tableTiles.add(new DominoTile(tile.getSide2(), tile.getSide1()));
            }
        }
        player.playTile(tile);
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

// Classe auxiliar GameRecord
class GameRecord {
    final int gameId;
    final List<Player> players;

    public GameRecord(int gameId, List<Player> players) {
        this.gameId = gameId;
        this.players = players;
    }

    public List<Player> getPlayers() {
        return players;
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