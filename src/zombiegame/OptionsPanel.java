package zombiegame;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class OptionsPanel extends JPanel {
    private JButton resumeButton;
    private JButton mainMenuButton;
    private JButton muteButton;
    private boolean isMuted;
    private Image background;

    public OptionsPanel(boolean isMuted, Runnable onResume, Runnable onMainMenu, Runnable onMuteToggle) {
        this.isMuted = isMuted;
        setLayout(null);
        setOpaque(false); // 투명 배경

        // 배경 이미지
        try {
            background = ImageIO.read(new File("images/options.png"));
        } catch (IOException e) {
            System.out.println("options.png 로드 실패: " + e.getMessage());
        }

        // 버튼 간격을 위한 상수 정의
        final int GAP_RESUME_TO_MAIN = 40; 
        final int GAP_MAIN_TO_MUTE = 60; 
        // ↓↓↓ 시작 Y 좌표를 200에서 300으로 변경합니다. ↓↓↓
        final int START_Y = 250; 

     // resumeButton
        ImageIcon resumeIcon = new ImageIcon("images/resume_button.png");
        resumeButton = new JButton(resumeIcon);
        // Y: 300
        resumeButton.setBounds(500, START_Y, resumeIcon.getIconWidth(), resumeIcon.getIconHeight());
        resumeButton.setContentAreaFilled(false);
        resumeButton.setBorderPainted(false);
        resumeButton.setFocusPainted(false);
        resumeButton.addActionListener(e -> onResume.run());
        add(resumeButton);

        // mainMenuButton
        ImageIcon mainMenuIcon = new ImageIcon("images/mainmenu_button.png");
        mainMenuButton = new JButton(mainMenuIcon);
        // Y: 300 + Resume Height + GAP_RESUME_TO_MAIN(40)
        mainMenuButton.setBounds(500, START_Y + resumeIcon.getIconHeight() + GAP_RESUME_TO_MAIN,
                                mainMenuIcon.getIconWidth(), mainMenuIcon.getIconHeight());
        mainMenuButton.setContentAreaFilled(false);
        mainMenuButton.setBorderPainted(false);
        mainMenuButton.setFocusPainted(false);
        mainMenuButton.addActionListener(e -> onMainMenu.run());
        add(mainMenuButton);

        // muteButton
        ImageIcon muteIcon = new ImageIcon("images/mute_button.png"); // 항상 이 이미지 사용
        muteButton = new JButton(muteIcon);
        // Y: 300 + Resume Height + GAP_RESUME_TO_MAIN(40) + MainMenu Height + GAP_MAIN_TO_MUTE(60)
        muteButton.setBounds(500, START_Y + resumeIcon.getIconHeight() + GAP_RESUME_TO_MAIN + mainMenuIcon.getIconHeight() + GAP_MAIN_TO_MUTE,
                             muteIcon.getIconWidth(), muteIcon.getIconHeight());
        muteButton.setContentAreaFilled(false);
        muteButton.setBorderPainted(false);
        muteButton.setFocusPainted(false);
        muteButton.addActionListener(e -> {
            this.isMuted = !this.isMuted;
            // 클릭 시 아이콘은 그대로, 필요 시 색상/효과만 바꾸고 싶다면 여기서 처리
            onMuteToggle.run();
        });
        add(muteButton);

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        }
    }
}