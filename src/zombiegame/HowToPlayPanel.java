package zombiegame; 

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

// 📌 파일명과 클래스명이 대소문자까지 정확히 일치해야 합니다.
public class HowToPlayPanel extends JPanel implements KeyListener { 
    private NoNextFloorGame mainGame;
    private Image howToPlayImage;
    private static final String HOW_TO_PLAY_IMAGE_PATH = "images/howtoplay.png";

    public HowToPlayPanel(NoNextFloorGame mainGame) {
        this.mainGame = mainGame;
        setLayout(null); 
        
        try {
            howToPlayImage = ImageIO.read(new File(HOW_TO_PLAY_IMAGE_PATH));
        } catch (IOException e) {
            System.err.println("조작 화면 이미지 로드 실패: " + HOW_TO_PLAY_IMAGE_PATH);
            howToPlayImage = null;
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
    }
    
    private void proceedToNext() {
        mainGame.remove(HowToPlayPanel.this);
        mainGame.showStageIntro(1);
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
        if (howToPlayImage != null) {
            g.drawImage(howToPlayImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("조작 화면 (이미지 없음)", 50, 50);
            g.drawString("클릭하여 스테이지 1 로딩", 50, 100);
        }
    }
}