// EndingPanel.java 파일의 전체 내용

package zombiegame; // 💡 패키지 선언 추가!

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import java.io.File;

public class EndingPanel extends JPanel implements KeyListener { // 💡 독립적인 클래스로 선언
    private Image img1, img2;
    private int state = 0;
    private JFrame mainFrame; // JFrame 자체를 받도록 변경
    
    // 페이드인 효과를 위한 변수
    private float alpha1 = 0.0f;      // ending1.png의 알파 값
    private float alpha2 = 0.0f;      // ending2.png의 알파 값
    private float fadeSpeed = 0.01f;  // 페이드인 속도 (Loading과 동일)
    private Timer fadeTimer;          // 페이드인 애니메이션 타이머

    public EndingPanel(JFrame frame) { // 💡 NoNextFloorGame 대신 JFrame을 인자로 받습니다.
        this.mainFrame = frame;

        try {
            // 이미지 경로 확인: 프로젝트 루트가 아닌 'images/' 폴더를 사용합니다.
            img1 = ImageIO.read(new File("images/ending1.png"));
            img2 = ImageIO.read(new File("images/ending2.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                proceedToNext();
            }
        });
        
        setFocusable(true);
        addKeyListener(this);
        requestFocusInWindow();
        
        // 페이드인 애니메이션 타이머 (약 16ms마다 업데이트, 60fps)
        fadeTimer = new Timer(16, e -> {
            boolean needRepaint = false;
            
            // ending1 페이드인
            if (state == 0 && alpha1 < 1.0f) {
                alpha1 += fadeSpeed;
                if (alpha1 > 1.0f) alpha1 = 1.0f;
                needRepaint = true;
            }
            
            // ending2 페이드인 (state가 1일 때)
            if (state == 1 && alpha2 < 1.0f) {
                alpha2 += fadeSpeed;
                if (alpha2 > 1.0f) alpha2 = 1.0f;
                needRepaint = true;
            }
            
            if (needRepaint) {
                repaint();
            }
        });
        fadeTimer.start();
    }
    
    private void proceedToNext() {
        if (state == 0) {
            // ending2.png로 전환, 페이드인 시작
            state = 1;
            alpha2 = 0.0f; // ending2의 알파를 0으로 리셋
            repaint();
        } else {
            JOptionPane.showMessageDialog(mainFrame, "세상은 일상으로 돌아갔다");
            System.exit(0); // 💡 시스템 종료
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            proceedToNext();
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // 먼저 검은색 배경 그리기
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        Graphics2D g2d = (Graphics2D) g;
        Composite oldComposite = g2d.getComposite();
        
        // state == 0일 때: ending1.png 페이드인
        if (state == 0 && img1 != null && alpha1 > 0) {
            AlphaComposite alphaComposite = AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, alpha1
            );
            g2d.setComposite(alphaComposite);
            g2d.drawImage(img1, 0, 0, getWidth(), getHeight(), null);
        }
        
        // state == 1일 때: ending2.png 페이드인
        if (state == 1 && img2 != null && alpha2 > 0) {
            AlphaComposite alphaComposite = AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, alpha2
            );
            g2d.setComposite(alphaComposite);
            g2d.drawImage(img2, 0, 0, getWidth(), getHeight(), null);
        }
        
        g2d.setComposite(oldComposite);
    }
}