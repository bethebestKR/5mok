import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class OmokRenju extends JFrame {
    private static final int SIZE = 19;
    private static final int CELL_SIZE = 45;
    private static final int BOARD_SIZE = SIZE * CELL_SIZE;
    private static final int EMPTY = 0, BLACK = 1, WHITE = 2;
    private int[][] board = new int[SIZE][SIZE];
    private boolean blackTurn = true;
    private boolean gameOver = false;
    private boolean crackMode = false; // 크랙 버전 여부
    private String player1Name = "플레이어 1";
    private String player2Name = "플레이어 2";
    private boolean showRealStones = false; // 진짜 돌 보기 토글 상태

    public OmokRenju() {
        // 플레이어 이름 입력
        player1Name = JOptionPane.showInputDialog(null, "플레이어 1(흑)의 이름을 입력하세요.", "플레이어 이름 입력", JOptionPane.QUESTION_MESSAGE);
        if (player1Name == null || player1Name.trim().isEmpty()) player1Name = "플레이어 1";
        player2Name = JOptionPane.showInputDialog(null, "플레이어 2(백)의 이름을 입력하세요.", "플레이어 이름 입력", JOptionPane.QUESTION_MESSAGE);
        if (player2Name == null || player2Name.trim().isEmpty()) player2Name = "플레이어 2";
        // 버전 선택 다이얼로그
        String[] options = {"기본 버전", "크랙 버전"};
        int selected = JOptionPane.showOptionDialog(
            null,
            "플레이할 버전을 선택하세요.",
            "마블스 오목에 오신거 환영합니다",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        crackMode = (selected == 1);
        setTitle("마블스 오목" + (crackMode ? " - 크랙 버전" : ""));
        // 플레이어 이름 및 선 안내
        JOptionPane.showMessageDialog(
            null,
            "플레이어 1(흑): " + player1Name + "\n플레이어 2(백): " + player2Name + "\n\n선공: " + player1Name,
            "게임 시작 안내",
            JOptionPane.INFORMATION_MESSAGE
        );
        int frameWidth = BOARD_SIZE + 940;
        int frameHeight = BOARD_SIZE + 200;
        setSize(frameWidth, frameHeight);  //창 크기 조절
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        BoardPanel panel = new BoardPanel();
        getContentPane().setLayout(new BorderLayout());
        int sideMargin = (frameWidth - BOARD_SIZE) / 2;
        int topBottomMargin = (frameHeight - BOARD_SIZE) / 2;
        JPanel left = new JPanel();
        left.setPreferredSize(new Dimension(sideMargin, BOARD_SIZE));
        JPanel right = new JPanel();
        right.setPreferredSize(new Dimension(sideMargin, BOARD_SIZE));
        JPanel top = new JPanel();
        top.setPreferredSize(new Dimension(frameWidth, topBottomMargin));
        JPanel bottom = new JPanel();
        bottom.setPreferredSize(new Dimension(frameWidth, topBottomMargin));
        add(left, BorderLayout.WEST);
        add(right, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);
        add(bottom, BorderLayout.SOUTH);
        if (crackMode) {
            JButton toggleBtn = new JButton("돌 색 보이기");
            toggleBtn.addActionListener(e -> {
                showRealStones = !showRealStones;
                toggleBtn.setText(showRealStones ? "돌 색 숨기기" : "돌 색 보이기");
                panel.repaint();
            });
            JPanel btnPanel = new JPanel();
            btnPanel.add(toggleBtn);
            add(panel, BorderLayout.CENTER);
            add(btnPanel, BorderLayout.PAGE_END);
        } else {
            add(panel, BorderLayout.CENTER);
        }
        setVisible(true);
    }

    private class BoardPanel extends JPanel {
        public BoardPanel() {
            setPreferredSize(new Dimension(BOARD_SIZE, BOARD_SIZE));
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (gameOver) return;
                    int x = e.getX() / CELL_SIZE;
                    int y = e.getY() / CELL_SIZE;
                    if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) return;
                    if (board[y][x] != EMPTY) return;
                    int color = blackTurn ? BLACK : WHITE;
                    // 렌주룰 금수 체크
                    if (color == BLACK && isForbidden(y, x)) {
                        JOptionPane.showMessageDialog(null, "둘수 없습니다 다른 곳에 두세요.");
                        return;
                    }
                    board[y][x] = color;
                    repaint();
                    if (checkWin(y, x, color)) {
                        gameOver = true;
                        String winner = (color == BLACK ? player1Name : player2Name);
                        String loser = (color == BLACK ? player2Name : player1Name);
                        JOptionPane.showMessageDialog(null, winner + " 승리!\n" + loser + " 병신 ㅋㅋ!");
                    } else {
                        blackTurn = !blackTurn;
                    }
                }
            });
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            // 바둑판 그리기
            g.setColor(new Color(222, 184, 135));
            g.fillRect(0, 0, BOARD_SIZE, BOARD_SIZE);
            g.setColor(Color.BLACK);
            for (int i = 0; i < SIZE; i++) {
                g.drawLine(CELL_SIZE / 2, CELL_SIZE / 2 + i * CELL_SIZE, BOARD_SIZE - CELL_SIZE / 2, CELL_SIZE / 2 + i * CELL_SIZE);
                g.drawLine(CELL_SIZE / 2 + i * CELL_SIZE, CELL_SIZE / 2, CELL_SIZE / 2 + i * CELL_SIZE, BOARD_SIZE - CELL_SIZE / 2);
            }
            // 돌 그리기
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    if (board[y][x] == BLACK) {
                        if (crackMode && !showRealStones) {
                            g.setColor(Color.GRAY);
                            g.fillOval(x * CELL_SIZE + 5, y * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
                        } else {
                            g.setColor(Color.BLACK);
                            g.fillOval(x * CELL_SIZE + 5, y * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
                        }
                    } else if (board[y][x] == WHITE) {
                        if (crackMode && !showRealStones) {
                            g.setColor(Color.GRAY);
                            g.fillOval(x * CELL_SIZE + 5, y * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
                        } else {
                            g.setColor(Color.WHITE);
                            g.fillOval(x * CELL_SIZE + 5, y * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
                            g.setColor(Color.BLACK);
                            g.drawOval(x * CELL_SIZE + 5, y * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
                        }
                    }
                }
            }
        }
    }

    // 렌주룰 금수 체크 (6목 이상, 3-3, 4-4)
    private boolean isForbidden(int y, int x) {
        // 6목 이상 체크
        if (countStones(y, x, BLACK) > 5) return true;
        // 3-3, 4-4 체크
        int threes = countOpenThrees(y, x, BLACK);
        int fours = countOpenFours(y, x, BLACK);
        if (threes >= 2 || fours >= 2) return true;
        return false;
    }

    // 5목 이상 체크
    private boolean checkWin(int y, int x, int color) {
        return countStones(y, x, color) == 5;
    }

    // 8방향 5목 이상 체크
    private int countStones(int y, int x, int color) {
        int max = 1;
        int[][] dirs = {{1,0},{0,1},{1,1},{1,-1}};
        for (int[] d : dirs) {
            int cnt = 1;
            for (int i = 1; i < 15; i++) {
                int ny = y + d[0]*i, nx = x + d[1]*i;
                if (ny < 0 || ny >= SIZE || nx < 0 || nx >= SIZE || board[ny][nx] != color) break;
                cnt++;
            }
            for (int i = 1; i < 15; i++) {
                int ny = y - d[0]*i, nx = x - d[1]*i;
                if (ny < 0 || ny >= SIZE || nx < 0 || nx >= SIZE || board[ny][nx] != color) break;
                cnt++;
            }
            max = Math.max(max, cnt);
        }
        return max;
    }

    // 열린 3 체크
    private int countOpenThrees(int y, int x, int color) {
        int count = 0;
        int[][] dirs = {{1,0},{0,1},{1,1},{1,-1}};
        for (int[] d : dirs) {
            if (isOpenThree(y, x, d[0], d[1], color)) count++;
        }
        return count;
    }

    // 열린 4 체크
    private int countOpenFours(int y, int x, int color) {
        int count = 0;
        int[][] dirs = {{1,0},{0,1},{1,1},{1,-1}};
        for (int[] d : dirs) {
            if (isOpenFour(y, x, d[0], d[1], color)) count++;
        }
        return count;
    }

    // 열린 3 판정
    private boolean isOpenThree(int y, int x, int dy, int dx, int color) {
        // 임시로 돌을 놓고 3목이면서 양쪽이 비어있는지 확인
        board[y][x] = color;
        int cnt = 1;
        int by = y, bx = x;
        for (int i = 1; i < 5; i++) {
            int ny = y + dy*i, nx = x + dx*i;
            if (ny < 0 || ny >= SIZE || nx < 0 || nx >= SIZE || board[ny][nx] != color) break;
            cnt++;
        }
        for (int i = 1; i < 5; i++) {
            int ny = y - dy*i, nx = x - dx*i;
            if (ny < 0 || ny >= SIZE || nx < 0 || nx >= SIZE || board[ny][nx] != color) break;
            cnt++;
        }
        boolean open1 = isOpenEnd(y, x, dy, dx, color, 3);
        boolean open2 = isOpenEnd(y, x, -dy, -dx, color, 3);
        board[y][x] = EMPTY;
        return cnt == 3 && open1 && open2;
    }

    // 열린 4 판정
    private boolean isOpenFour(int y, int x, int dy, int dx, int color) {
        board[y][x] = color;
        int cnt = 1;
        for (int i = 1; i < 5; i++) {
            int ny = y + dy*i, nx = x + dx*i;
            if (ny < 0 || ny >= SIZE || nx < 0 || nx >= SIZE || board[ny][nx] != color) break;
            cnt++;
        }
        for (int i = 1; i < 5; i++) {
            int ny = y - dy*i, nx = x - dx*i;
            if (ny < 0 || ny >= SIZE || nx < 0 || nx >= SIZE || board[ny][nx] != color) break;
            cnt++;
        }
        boolean open1 = isOpenEnd(y, x, dy, dx, color, 4);
        boolean open2 = isOpenEnd(y, x, -dy, -dx, color, 4);
        board[y][x] = EMPTY;
        return cnt == 4 && (open1 || open2);
    }

    // 양쪽 끝이 비어있는지 확인
    private boolean isOpenEnd(int y, int x, int dy, int dx, int color, int length) {
        int ny = y, nx = x;
        for (int i = 0; i < length; i++) {
            ny += dy;
            nx += dx;
            if (ny < 0 || ny >= SIZE || nx < 0 || nx >= SIZE) return false;
            if (board[ny][nx] != color) break;
        }
        ny += dy;
        nx += dx;
        if (ny < 0 || ny >= SIZE || nx < 0 || nx >= SIZE) return false;
        return board[ny][nx] == EMPTY;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(OmokRenju::new);
    }
} 