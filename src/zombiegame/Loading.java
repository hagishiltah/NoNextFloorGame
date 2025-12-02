package zombiegame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * 스테이지 시작 전 로딩 화면
 */
public class Loading extends JPanel implements MouseListener, KeyListener {

    private Image introImage;
    private NoNextFloorGame mainGame;
    private int stage;
    
    // 페이드인 효과를 위한 변수
    private float alpha = 0.0f;      // 현재 알파 값 (0.0 = 투명, 1.0 = 불투명)
    private float fadeSpeed = 0.01f; // 페이드인 속도 (값이 클수록 빠름)
    private Timer fadeTimer;         // 페이드인 애니메이션 타이머

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

        // 페이드인 애니메이션 (약 16ms마다 업데이트, 60fps)
        fadeTimer = new Timer(16, e -> {
            if (alpha < 1.0f) {
                alpha += fadeSpeed;
                if (alpha > 1.0f) alpha = 1.0f; // 최대값 제한
                repaint();
            } else {
                fadeTimer.stop(); // 페이드인이 완료되면 타이머 정지
            }
        });
        fadeTimer.start();

        // 🌟 [추가된 부분] 로딩 화면 진입 시 elevator.wav 반복 재생 🌟
        if (!mainGame.isMuted()) {
            // 볼륨 0.4f로 엘리베이터 사운드 반복 재생 시작 (게임 배경음 수준)
            SoundManager.playLoop("elevator", 0.6f); 
        }

        addMouseListener(this);
        setFocusable(true); // 키 입력을 받기 위해 필요
        addKeyListener(this);
        requestFocusInWindow(); // 포커스 요청
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // 먼저 검은색 배경 그리기
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // 배경 이미지를 알파 값으로 페이드인하여 그리기
        if (introImage != null && alpha > 0) {
            Graphics2D g2d = (Graphics2D) g;
            Composite oldComposite = g2d.getComposite();
            
            // 알파 값을 사용하여 투명도 설정
            AlphaComposite alphaComposite = AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, alpha
            );
            g2d.setComposite(alphaComposite);
            
            g2d.drawImage(introImage, 0, 0, getWidth(), getHeight(), this);
            
            // 원래 Composite 복원
            g2d.setComposite(oldComposite);
        }
    }

    private void proceedToNext() {
        // 🌟 [추가된 부분] 클릭 시 elevator.wav 정지 🌟
        SoundManager.stop("elevator");
        mainGame.startStage(stage);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        proceedToNext();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            proceedToNext();
        }
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}