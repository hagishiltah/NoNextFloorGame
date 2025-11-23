// HowToPlayPanel.java 파일 내용 (전체)

package zombiegame; // 👈 📌 이 줄이 반드시 있어야 합니다.

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

// 📌 파일명과 클래스명이 대소문자까지 정확히 일치해야 합니다.
public class HowToPlayPanel extends JPanel { 
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
                mainGame.remove(HowToPlayPanel.this);
                mainGame.showStageIntro(1); 
            }
        });
    }

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