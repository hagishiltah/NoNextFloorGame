package zombiegame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class OptionsPanel extends JPanel implements KeyListener {
    private JButton resumeButton;
    private JButton mainMenuButton;
    private JButton muteButton;
    private boolean isMuted;
    private Image background;
    private Image arrowImage; // arrow.png 이미지
    
    // 키보드 네비게이션을 위한 변수
    private int selectedIndex = 0; // 0: RESUME, 1: HOME, 2: SOUND O/X
    private Runnable onResume;
    private Runnable onMainMenu;
    private Runnable onMuteToggle;
    
    // 각 버튼의 Y 좌표 (화살표 위치 계산용)
    private int[] buttonYPositions = new int[3];
    
    // 화살표 위치 계산을 위한 상수
    private final int BUTTON_X = 500; // 버튼 X 좌표
    private final int ARROW_OFFSET = -50; // 버튼에서 화살표까지의 간격 (조정 가능)

    public OptionsPanel(boolean isMuted, Runnable onResume, Runnable onMainMenu, Runnable onMuteToggle) {
        this.isMuted = isMuted;
        this.onResume = onResume;
        this.onMainMenu = onMainMenu;
        this.onMuteToggle = onMuteToggle;
        
        setLayout(null);
        setOpaque(false);
        setFocusable(true); // 키 입력을 받기 위해 필수
        addKeyListener(this);

        // 배경 이미지
        try {
            background = ImageIO.read(new File("images/options.png"));
        } catch (IOException e) {
            System.out.println("options.png 로드 실패: " + e.getMessage());
        }
        
        // arrow.png 이미지 로드
        try {
            arrowImage = ImageIO.read(new File("images/arrow.png"));
        } catch (IOException e) {
            System.out.println("arrow.png 로드 실패: " + e.getMessage());
            arrowImage = null;
        }

        // 버튼 간격을 위한 상수 정의
        final int GAP_RESUME_TO_MAIN = 20; // 화살표 내려가는 간격 조정
        final int GAP_MAIN_TO_MUTE = 27;   // 화살표 내려가는 간격 조정
        final int START_Y = 270; 
        final int BUTTON_WIDTH = 200; // 버튼 클릭 영역 너비
        final int BUTTON_HEIGHT = 50; // 버튼 클릭 영역 높이

     // resumeButton (아이콘 없이 투명 버튼)
        resumeButton = new JButton();
        resumeButton.setBounds(BUTTON_X, START_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        resumeButton.setContentAreaFilled(false);
        resumeButton.setBorderPainted(false);
        resumeButton.setFocusPainted(false);
        resumeButton.addActionListener(e -> onResume.run());
        add(resumeButton);
        // 버튼 중앙 Y 좌표 저장
        buttonYPositions[0] = START_Y + BUTTON_HEIGHT / 2;

        // mainMenuButton (아이콘 없이 투명 버튼)
        mainMenuButton = new JButton();
        mainMenuButton.setBounds(BUTTON_X, START_Y + BUTTON_HEIGHT + GAP_RESUME_TO_MAIN, BUTTON_WIDTH, BUTTON_HEIGHT);
        mainMenuButton.setContentAreaFilled(false);
        mainMenuButton.setBorderPainted(false);
        mainMenuButton.setFocusPainted(false);
        mainMenuButton.addActionListener(e -> onMainMenu.run());
        add(mainMenuButton);
        buttonYPositions[1] = START_Y + BUTTON_HEIGHT + GAP_RESUME_TO_MAIN + BUTTON_HEIGHT / 2;

        // muteButton (아이콘 없이 투명 버튼)
        muteButton = new JButton();
        muteButton.setBounds(BUTTON_X, START_Y + BUTTON_HEIGHT + GAP_RESUME_TO_MAIN + BUTTON_HEIGHT + GAP_MAIN_TO_MUTE, BUTTON_WIDTH, BUTTON_HEIGHT);
        muteButton.setContentAreaFilled(false);
        muteButton.setBorderPainted(false);
        muteButton.setFocusPainted(false);
        muteButton.addActionListener(e -> {
            this.isMuted = !this.isMuted;
            onMuteToggle.run();
        });
        add(muteButton);
        buttonYPositions[2] = START_Y + BUTTON_HEIGHT + GAP_RESUME_TO_MAIN + BUTTON_HEIGHT + GAP_MAIN_TO_MUTE + BUTTON_HEIGHT / 2;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        }
        
        // arrow.png 이미지를 선택된 버튼 왼쪽에 그리기
        if (arrowImage != null) {
            int arrowY = buttonYPositions[selectedIndex] - arrowImage.getHeight(null) / 2 ;
            int arrowX = BUTTON_X - arrowImage.getWidth(null) - ARROW_OFFSET; // 버튼 왼쪽에 배치
            g.drawImage(arrowImage, arrowX, arrowY, null);
        }
    }
    
    // 키보드 입력 처리
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        
        // W 키 또는 위쪽 화살표: 위로 이동
        if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
            selectedIndex--;
            if (selectedIndex < 0) selectedIndex = 2; // 순환 (맨 위에서 맨 아래로)
            repaint();
        }
        // S 키 또는 아래쪽 화살표: 아래로 이동
        else if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) {
            selectedIndex++;
            if (selectedIndex > 2) selectedIndex = 0; // 순환 (맨 아래에서 맨 위로)
            repaint();
        }
        // 스페이스바: 선택된 버튼 실행
        else if (keyCode == KeyEvent.VK_SPACE) {
            executeSelectedButton();
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {}
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    // 선택된 버튼 실행
    private void executeSelectedButton() {
        switch(selectedIndex) {
            case 0: // RESUME
                onResume.run();
                break;
            case 1: // HOME
                onMainMenu.run();
                break;
            case 2: // SOUND O/X
                isMuted = !isMuted;
                onMuteToggle.run();
                break;
        }
    }
}