package zombiegame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * 스테이지 시작 전 로딩 화면 (애니메이션 . . . 포함)
 */
public class Loading extends JPanel implements MouseListener {

    private Image introImage;
    private NoNextFloorGame mainGame;
    private int stage;

    private int dotCount = 1;        // 1~3 반복
    private Timer dotTimer;          // 점 애니메이션 타이머

    public Loading(NoNextFloorGame mainGame, int stage) {
        this.mainGame = mainGame;
        this.stage = stage;

        // 로딩 이미지 읽기 (파일 기반)
        try {
            File f = new File("images/loading" + stage + ".png");
            if (f.exists()) {
                introImage = ImageIO.read(f);
            } else {
                System.out.println("⚠ 로딩 이미지 없음: " + f.getPath());
                introImage = null;
            }
        } catch (IOException e) {
            introImage = null;
        }

        // 빨간 점 애니메이션 (0.5초마다 변경)
        dotTimer = new Timer(500, e -> {
            dotCount++;
            if (dotCount > 3) dotCount = 1;
            repaint();
        });
        dotTimer.start();

        // 🌟 [추가된 부분] 로딩 화면 진입 시 elevator.wav 반복 재생 🌟
        if (!mainGame.isMuted()) {
            // 볼륨 0.5f로 엘리베이터 사운드 반복 재생 시작
            SoundManager.playLoop("elevator", 0.5f); 
        }

        addMouseListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 배경 이미지
        if (introImage != null) {
            g.drawImage(introImage, 0, 0, getWidth(), getHeight(), this);
        }

        // 로딩 점 표시
        g.setColor(Color.RED);
        g.setFont(new Font("Malgun Gothic", Font.BOLD, 30));

        String dots = ".".repeat(dotCount);   // ".", "..", "..."
        String msg = dots;

        FontMetrics fm = g.getFontMetrics();
        int tx = (getWidth() - fm.stringWidth(msg)) / 2;
        int ty = getHeight() - 100;           // 이미지 아래 100px 위치

        g.drawString(msg, tx, ty);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        dotTimer.stop();       // 타이머 종료
        
        // 🌟 [추가된 부분] 클릭 시 elevator.wav 정지 🌟
        SoundManager.stop("elevator");
        
        mainGame.startStage(stage);
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}