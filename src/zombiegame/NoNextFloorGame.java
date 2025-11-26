package zombiegame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.RadialGradientPaint;
import java.awt.BasicStroke;


public class NoNextFloorGame extends JFrame {
    private Image backgroundImage;
    private boolean isMuted = false;
    private JButton soundButton;
    private JButton startButton;
    private GamePanel gamePanel;

    // 현재 보여지는 Loading 혹은 Ingame 패널 참조
    private Loading loadingPanel;
    private IngamePanel currentIngamePanel;
    private HowToPlayPanel howToPlayPanel;

    // 색상 정의 (기존 유지)
    // private static final Color RED_PRIMARY = new Color(255, 0, 0);
    // private static final Color RED_DARK = new Color(139, 0, 0);
    // private static final Color RED_LIGHT = new Color(255, 102, 102);
    // private static final Color BLACK_TRANSPARENT = new Color(0, 0, 0, 180);

    // 스테이지 관리
    private int stage = 1;
    private final int MAX_STAGE = 3;
 // 전역 탄약 (스테이지 전환 시 유지됨)
    private int pistolAmmo = 75;
    private int shotgunAmmo = 25;


    // 2번 방식: 스테이지별 고정 좀비 좌표 (원하는 대로 수정 가능)
    // 각 stage index는 1-based이므로 배열은 stage-1 로 접근
    private final int[][][] stageZombies = new int[][][] {
        // stage 1: 3 마리 (예시 좌표)
        { {350, 130}, {880, 450}, {700, 500} },
        // stage 2: 5 마리
        { {400, 150}, {700, 180}, {400, 400}, {900, 420}, {800, 220} },
        // stage 3: 2 마리
        { {300, 450},{500, 420} }
    };

    // 생성자 (기본 스켈레톤 로직 유지)
    public NoNextFloorGame() {
        setTitle("No Next Floor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setResizable(false);

        // 메인 메뉴 배경 이미지 로드 (images/elevator_bg.png)
        try {
            File bgFile = new File("images/elevator_bg.png");
            if (bgFile.exists()) {
                backgroundImage = ImageIO.read(bgFile);
            } else {
                throw new IOException("파일을 찾을 수 없습니다: images/elevator_bg.png");
            }
        } catch (IOException e) {
            System.out.println("배경 이미지를 찾을 수 없습니다: " + e.getMessage());
            backgroundImage = null;
        }

        // 메인 패널 설정 (스켈레톤 유지)
        SoundManager.loadSounds();
        gamePanel = new GamePanel();
        add(gamePanel);

        setVisible(true);
    }
    

    /* ===================== GamePanel (메뉴 화면) ===================== */
    class GamePanel extends JPanel implements KeyListener {
        private Image backgroundImage;
        private Image startButtonImage; // start_selected.png
        private Image exitButtonImage;   // exit_button.png
        private JButton soundButton; // 사운드 토글 버튼
        
        // 키보드 네비게이션을 위한 변수
        private int selectedIndex = 0; // 0: START, 1: EXIT
        
        // 버튼 위치 상수
        private final int BUTTON_X = 942; // START 버튼 X 좌표
        private final int EXIT_BUTTON_X = 932; // EXIT 버튼 X 좌표 (왼쪽으로 이동하려면 값 감소)
        private final int START_Y = 410;  // START 버튼 Y 좌표 (아래로 가려면 값 증가가)
        private final int GAP = -5;        // START와 EXIT 사이 간격
        
        // 각 버튼의 Y 좌표 (이미지 위치 계산용)
        private int[] buttonYPositions = new int[2];

        public GamePanel() {
            setLayout(null);
            setBackground(Color.BLACK);
            setFocusable(true); // 키 입력을 받기 위해 필수
            addKeyListener(this);
            
            if (!isMuted) SoundManager.playLoop("start", 0.3f);
            
            // 사운드 토글 버튼 (작게)
            soundButton = createSoundButton();
            soundButton.setBounds(1200, 20, 30, 30); // 50x50에서 30x30으로 작게
            add(soundButton);
            
            // 배경 이미지 로드
            try {
                backgroundImage = ImageIO.read(new File("images/elevator_bg.png"));
            } catch (IOException e) {
                System.out.println("elevator_bg.png 로드 실패: " + e.getMessage());
                backgroundImage = null;
            }
            
            // START 버튼 이미지 로드
            try {
                startButtonImage = ImageIO.read(new File("images/start_selected.png"));
            } catch (IOException e) {
                System.out.println("start_selected.png 로드 실패: " + e.getMessage());
                startButtonImage = null;
            }
            
            // EXIT 버튼 이미지 로드
            try {
                exitButtonImage = ImageIO.read(new File("images/exit_button.png"));
            } catch (IOException e) {
                System.out.println("exit_button.png 로드 실패: " + e.getMessage());
                exitButtonImage = null;
            }
            
            // 버튼 Y 좌표 저장
            if (startButtonImage != null) {
                buttonYPositions[0] = START_Y + startButtonImage.getHeight(null) / 2;
            }
            if (exitButtonImage != null) {
                buttonYPositions[1] = START_Y + (startButtonImage != null ? startButtonImage.getHeight(null) : 0) + GAP + exitButtonImage.getHeight(null) / 2;
            }
        }
        
        private JButton createSoundButton() {
            JButton button = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // 배경
                    g2d.setColor(new Color(0, 0, 0, 150));
                    g2d.fillRect(0, 0, getWidth(), getHeight());

                    // 테두리
                    g2d.setColor(new Color(255, 0, 0, 100));
                    g2d.setStroke(new BasicStroke(1));
                    g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

                    // 아이콘 (작게)
                    g2d.setColor(new Color(255, 100, 100));
                    if (isMuted) {
                        // X 표시 (작게)
                        g2d.drawLine(8, 8, 22, 22);
                        g2d.drawLine(22, 8, 8, 22);
                    } else {
                        // 스피커 아이콘 (작게)
                        int[] xPoints = {8, 13, 13, 8};
                        int[] yPoints = {12, 10, 20, 18};
                        g2d.fillPolygon(xPoints, yPoints, 4);
                        g2d.drawArc(13, 11, 4, 7, -30, 60);
                        g2d.drawArc(14, 9, 6, 10, -30, 60);
                    }
                }
            };

            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBorder(BorderFactory.createLineBorder(new Color(255, 0, 0, 200), 1));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBorder(null);
                }
            });

            button.addActionListener(e -> {
                isMuted = !isMuted;
                if (isMuted) {
                    SoundManager.stop("start");
                } else {
                    SoundManager.playLoop("start", 0.3f);
                }
                button.repaint();
            });

            return button;
        }

        public void stopAnimation() {
            // 애니메이션 타이머가 없으므로 빈 메서드
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // 배경 이미지
            if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                // 어두운 오버레이
                g2d.setColor(new Color(0, 0, 0, 80));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            } else {
                // 배경 이미지가 없을 경우 검은 배경
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
            
            // 선택된 버튼 이미지 그리기
            if (selectedIndex == 0 && startButtonImage != null) {
                // START 버튼이 선택된 경우
                int startY = START_Y;
                int w = (int)(startButtonImage.getWidth(null) * 0.65);
                int h = (int)(startButtonImage.getHeight(null) * 0.65);
                g2d.drawImage(startButtonImage, BUTTON_X, startY, w, h, null);
            } else if (selectedIndex == 1 && exitButtonImage != null) {
                // EXIT 버튼이 선택된 경우
                int startScaledH = startButtonImage != null ? (int)(startButtonImage.getHeight(null) * 0.7) : 0;
                int exitY = START_Y + startScaledH + GAP;
                int w = (int)(exitButtonImage.getWidth(null) * 0.65);
                int h = (int)(exitButtonImage.getHeight(null) * 0.65);
                g2d.drawImage(exitButtonImage, EXIT_BUTTON_X, exitY, w, h, null);
            }
        }
        
        // 키보드 입력 처리
        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();
            
            // W 키 또는 위쪽 화살표: 위로 이동
            if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
                selectedIndex--;
                if (selectedIndex < 0) selectedIndex = 1; // 순환 (맨 위에서 맨 아래로)
                repaint();
            }
            // S 키 또는 아래쪽 화살표: 아래로 이동
            else if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) {
                selectedIndex++;
                if (selectedIndex > 1) selectedIndex = 0; // 순환 (맨 아래에서 맨 위로)
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
            if (selectedIndex == 0) {
                // START 버튼: 게임 시작
                SoundManager.stop("start");
                showHowToPlay();
            } else if (selectedIndex == 1) {
                // EXIT 버튼: 게임 종료
                System.exit(0);
            }
        }
    }
    

    /* ===================== IngamePanel ===================== */
    class IngamePanel extends JPanel implements KeyListener, Runnable {
    	private NoNextFloorGame mainGame;
    	private boolean isGameOver = false;
    	private Image gameOverImage;
    	private Image arrowImage;
    	private int gameOverSelection = 0; // 0: YES, 1: NO
    	private long gameOverStartTime = 0; // 게임오버 시작 시간 (페이드인용)
    	private final long GAMEOVER_FADEIN_DURATION = 800; // 페이드인 지속 시간 (밀리초, 0.8초)
    	private javax.swing.Timer gameOverFadeTimer; // 페이드인 애니메이션용 타이머
    	
    	// 게임오버 버튼 위치 (배경 이미지의 YES/NO 버튼 위치 기준)
    	private final int GAMEOVER_YES_X = 450;
    	private final int GAMEOVER_NO_X = 930;
    	private final int GAMEOVER_BUTTON_Y = 500;
    	private final int GAMEOVER_ARROW_OFFSET_X = -130; // 버튼 왼쪽으로부터의 거리
    	


    	// 무기 시스템
    	private static final int WEAPON_PISTOL = 0;
    	private static final int WEAPON_SHOTGUN = 1;
    	private int currentWeapon = WEAPON_PISTOL;
    	private Image pistolIcon;
    	private Image shotgunIcon;
    	private Rectangle pistolSlotRect;
    	private Rectangle shotgunSlotRect;
    	private List<Rectangle> wallRects = new ArrayList<>();
    	private boolean isFlashingImage = false;
    	private long flashStartTime = 0;
    	private Image televisionImage;
    	private final long FLASH_INTERVAL = 500; // 0.2초마다 켜고 끔
    	private final long FLASH_DURATION = 3000; // 총 3초간 깜빡임 (원하는 시간으로 설정 가능)
    	private boolean flashSequenceCompleted = false;


    	// 무기별 쿨다운 (원하면 조정)
    	private final long PISTOL_COOLDOWN = 200_000_000L; // 0.2초
    	private final long SHOTGUN_COOLDOWN = 600_000_000L; // 0.6초

    	private int stageForThisPanel;
        private Image gameBackground;
        private Player player;
        private ArrayList<Zombie> zombies;
        private ArrayList<Bullet> bullets;
        private Thread gameThread;
        private boolean isRunning;
        private long lastShootTime = 0;
        private Image fullHeartImage;
        private Image uiPlayerIcon;
        private Image uiZombieIcon;
        private Image uiHeartFull;
        private Image uiHeartEmpty;
        private JButton homeButton;
        private OptionsPanel optionsPanel;  // options 패널 참조
        private BossZombie bossZombie;
        private int visionRadius = 150; // 주인공 주변 밝게 보이는 범위

        // 키 입력 상태 추적 변수 (매끄러운 이동을 위해 추가)
        private boolean leftPressed = false;
        private boolean rightPressed = false;
        private boolean upPressed = false;
        private boolean downPressed = false;

     // 2F Key 시스템
        private boolean keySpawned = false;
        private boolean keyCollected = false;
        private Image keyImage;
        private long keySpawnTime = 0;              
        private final long KEY_SHOW_DURATION = 2_000; // 2초

        // 키 등장 좌표
        private final int keyX = 850;
        private final int keyY = 200;
        
     // 3F Antidote 시스템
        private Image antidoteImage;
        private boolean antidoteSpawned = false;
        private boolean antidoteCollected = false;
        private long antidoteSpawnTime = 0;
        private final long ANTIDOTE_SHOW_DURATION = 30_000; // 2초

        // 해독제 등장 좌표
        private final int antidoteX = 700;
        private final int antidoteY = 450;

        public IngamePanel(NoNextFloorGame mainGame, int stage) {
            this.mainGame = mainGame;
            this.stageForThisPanel = stage;
            loadTelevisionImage();
            loadUIResources();

            SoundManager.stop("start"); // 메뉴 BGM 정지

            // 👇 [수정] BGM 재생 로직 전체를 if 블록 { } 안에 묶습니다.
            if (!mainGame.isMuted()) {
                
                // 1. 베이스 BGM (기존 코드에서는 삭제됨)
                // SoundManager.playLoop("bgm", 0.2f);
                
                // 2. 스테이지별 BGM 및 베이스 BGM(bgm)을 통합하여 재생
                switch(stage) {
                    case 1:
                        //SoundManager.playLoop("bgm", 0.03f); // 👈 bgm.wav 추가
                        SoundManager.playLoop("game_bg", 0.03f);
                        break;
                    case 2:
                       // SoundManager.playLoop("bgm", 0.03f); // 👈 bgm.wav 추가
                        SoundManager.playLoop("game_bg2", 0.15f);
                        break;
                    case 3:
                       // SoundManager.playLoop("bgm", 0.03f); // 👈 bgm.wav 추가
                        SoundManager.playLoop("game_bg3", 0.15f);
                        break;
                }
                
                // 3. 스테이지별 좀비 BGM
                switch(stage) {
                    case 1:
                        SoundManager.playLoop("zombie1", 0.2f);
                        break;
                    case 2:
                    case 3:
                        SoundManager.playLoop("zombie23", 0.2f);
                        break;
                }
            } 
            // 👆 if 블록 닫힘. (생성자는 아직 닫히지 않았습니다.)
            
            // 이제 setFocusable(true);가 생성자 내부에 위치하게 됩니다.
            setFocusable(true);
            addKeyListener(this);
            

            // 마우스 클릭 이벤트 추가 - 발사
            addMouseListener(new MouseAdapter() {
            	@Override
            	public void mouseClicked(MouseEvent e) {
            	    int mx = e.getX(), my = e.getY();
            	    if (!mainGame.isMuted()) SoundManager.play("button", 1.0f);

            	    if (isGameOver) {
            	        // 게임오버 상태에서는 클릭 이벤트 무시
            	        return;
            	    }

            	    // 게임 중 발사 처리 (기존 코드 유지)
            	   // shootBullet(mouseX, mouseY);
            	//사운드 중복 방지할려고 주석처리했어요



                    // 슬롯 클릭 우선 처리
                    if (pistolSlotRect != null && pistolSlotRect.contains(mx, my)) {
                        currentWeapon = WEAPON_PISTOL;
                        System.out.println("PISTOL selected");
                        return;
                    }
                    if (shotgunSlotRect != null && shotgunSlotRect.contains(mx, my)) {
                        currentWeapon = WEAPON_SHOTGUN;
                        System.out.println("SHOTGUN selected");
                        return;
                    }
                    

                    // 슬롯이 아닌 곳 클릭 -> 발사
                    shootBullet(mx, my);
                }
            });

            keySpawned = false;
            keyCollected = false;
            antidoteSpawned = false;
            antidoteCollected = false;
         // key 이미지 로드
            try {
                keyImage = ImageIO.read(new File("images/key.png"));
            } catch (Exception e) {
                System.out.println("key.png 로드 실패: " + e.getMessage());
            }
         // 3F antidote 이미지 로드
            try {
                antidoteImage = ImageIO.read(new File("images/antidote.png"));
            } catch (Exception e) {
                System.out.println("antidote.png 로드 실패: " + e.getMessage());
            }
            try {
                pistolIcon = ImageIO.read(new File("images/pistol.png"));
            } catch (Exception e) {
                System.out.println("pistol.png 로드 실패: " + e.getMessage());
                pistolIcon = null;
            }
            try {
                shotgunIcon = ImageIO.read(new File("images/shotgun.png"));
            } catch (Exception e) {
                System.out.println("shotgun.png 로드 실패: " + e.getMessage());
                shotgunIcon = null;
            }
            try {
                gameOverImage = ImageIO.read(new File("images/gameover.png"));
            } catch (Exception e) {
                System.out.println("gameover.png 로드 실패: " + e.getMessage());
            }
            try {
                arrowImage = ImageIO.read(new File("images/arrow.png"));
            } catch (Exception e) {
                System.out.println("arrow.png 로드 실패: " + e.getMessage());
                arrowImage = null;
            }




            

            // 해당 스테이지의 배경 로드: images/game_bg.png, game_bg2.png, game_bg3.png ...
            try {
                File bgFile = new File("images/game_bg" + (stage == 1 ? "" : stage) + ".png");
                if (bgFile.exists()) {
                    gameBackground = ImageIO.read(bgFile);
                } else {
                    throw new IOException("파일을 찾을 수 없습니다: " + bgFile.getPath());
                }
            } catch (IOException e) {
                System.out.println("게임 배경 이미지를 찾을 수 없습니다: " + e.getMessage());
                setBackground(Color.BLACK);
                gameBackground = null;
            }
            

            // 하트 이미지 로드 (공통)
            try {
                File heartFile = new File("images/heart_full.png");
                if (heartFile.exists()) {
                    fullHeartImage = ImageIO.read(heartFile);
                } else {
                    throw new IOException("파일을 찾을 수 없습니다: images/heart_full.png");
                }
            } catch (IOException e) {
                System.out.println("하트 이미지를 찾을 수 없습니다. 기본 하트가 그려집니다. 에러: " + e.getMessage());
                fullHeartImage = null;
            }
            

            // player 초기화 (Player 클래스 내부에서 images/player.png 로드)
            int startX = 100, startY = 100;
            switch(stageForThisPanel) {
                case 1: startX = 490; startY = 520; break;
                case 2: startX = 190; startY = 180; break;
                case 3: startX = 315; startY = 70; break;
            }
            player = new Player(startX, startY);

            // 좀비 생성 - stageZombies 배열의 고정 좌표 사용 (2번 방식)
            zombies = new ArrayList<>();
            int idx = stage - 1;
            if (idx >= 0 && idx < stageZombies.length) {
                for (int[] pos : stageZombies[idx]) {
                    zombies.add(new Zombie(pos[0], pos[1]));
                }
            } else {
                // 안전 장치: 기본 3마리 랜덤 위치
                zombies.add(new Zombie(600, 200));
                zombies.add(new Zombie(900, 400));
                zombies.add(new Zombie(700, 500));
            }
         // 보스 좀비 스폰 (3스테이지일 때만)
            if(stage == 3) {
                bossZombie = new BossZombie(950, 200);  // 원하는 위치로 세팅
            }


            bullets = new ArrayList<>();
            isRunning = true;
            gameThread = new Thread(this);
            gameThread.start();
            addHomeButton();
            loadStageWalls(stage);
        }
        private void onPlayerDied() {
            isRunning = false;
            isGameOver = true;
            gameOverStartTime = System.currentTimeMillis(); // 페이드인 시작 시간 기록
            gameOverSelection = 0; // 기본 선택은 YES
            SoundManager.stop("bgm"); SoundManager.stop("game_bg"); SoundManager.stop("game_bg2"); SoundManager.stop("game_bg3"); SoundManager.stop("zombie1"); SoundManager.stop("zombie23"); if (!mainGame.isMuted()) SoundManager.play("gameover", 1.0f);

            // 페이드인 애니메이션을 위한 타이머 시작 (60 FPS)
            if (gameOverFadeTimer != null) {
                gameOverFadeTimer.stop();
            }
            gameOverFadeTimer = new javax.swing.Timer(16, e -> {
                long elapsed = System.currentTimeMillis() - gameOverStartTime;
                if (elapsed < GAMEOVER_FADEIN_DURATION) {
                    repaint(); // 페이드인 중일 때 계속 repaint 호출
                } else {
                    repaint(); // 마지막 한 번 더 호출
                    if (gameOverFadeTimer != null) {
                        gameOverFadeTimer.stop(); // 페이드인 완료 후 타이머 정지
                    }
                }
            });
            gameOverFadeTimer.start();

            // 키보드 입력을 받기 위해 포커스 가져오기
            requestFocusInWindow();
        }
        
        // 게임오버 선택 실행 메서드
        private void executeGameOverSelection() {
            if (!isGameOver) return;
            
            if (gameOverSelection == 0) {
                // YES → 현재 스테이지 시작 지점에서 다시 시작
                isGameOver = false;
                player.hp = 3;  // 초기 체력으로 복원
                bullets.clear();
                zombies.clear();

                // 플레이어 위치를 스테이지 시작 지점으로 리셋
                switch(stageForThisPanel) {
                    case 1: 
                        player.x = 490; 
                        player.y = 520; 
                        break;
                    case 2: 
                        player.x = 190; 
                        player.y = 180; 
                        break;
                    case 3: 
                        player.x = 315; 
                        player.y = 70; 
                        break;
                }
                player.angle = 0; // 각도도 초기화

                // 좀비 다시 스폰 (스테이지 초기화)
                int idx = stageForThisPanel - 1;
                if (idx >= 0 && idx < stageZombies.length) {
                    for (int[] pos : stageZombies[idx]) {
                        zombies.add(new Zombie(pos[0], pos[1]));
                    }
                }
                if(stageForThisPanel == 3) {
                    bossZombie = new BossZombie(950, 200);  // 원래 시작 위치로
                }

                lastShootTime = 0;
                isRunning = true;
                gameThread = new Thread(IngamePanel.this);
                gameThread.start();
                SoundManager.stop("gameover"); 
                if (!mainGame.isMuted()) { 
                    switch(stageForThisPanel) { 
                        case 1:
                            SoundManager.playLoop("game_bg", 0.15f);
                            break;
                        case 2:
                            SoundManager.playLoop("game_bg2", 0.15f);
                            break;
                        case 3:
                            SoundManager.playLoop("game_bg3", 0.15f);
                            break;
                    } 
                    switch(stageForThisPanel) { 
                        case 1:
                            SoundManager.playLoop("zombie1", 0.2f);
                            break;
                        case 2:
                        case 3:
                            SoundManager.playLoop("zombie23", 0.2f);
                            break;
                    } 
                }
                repaint();
            } else {
                // NO → 메인 메뉴로 돌아가기
                SoundManager.stop("gameover"); 
                SoundManager.stop("bgm"); 
                SoundManager.stop("game_bg"); 
                SoundManager.stop("game_bg2"); 
                SoundManager.stop("game_bg3"); 
                SoundManager.stop("zombie1"); 
                SoundManager.stop("zombie23");
                NoNextFloorGame.this.remove(IngamePanel.this);
                currentIngamePanel = null;

                NoNextFloorGame.this.gamePanel = new GamePanel();
                NoNextFloorGame.this.add(gamePanel);
                gamePanel.requestFocusInWindow();
                NoNextFloorGame.this.revalidate();
                NoNextFloorGame.this.repaint();
            }
        }
        private void loadTelevisionImage() { 
            try {
                televisionImage = ImageIO.read(new File("images/television.png")); 
            } catch (IOException e) {
                System.err.println("television.png 이미지 로드 실패: " + e.getMessage());
            }
        }
        


        private void showOptions() {
            isRunning = false;

            SwingUtilities.invokeLater(() -> {
                // 홈 버튼 숨기기
                if (homeButton != null) {
                    homeButton.setVisible(false);
                }
                
                optionsPanel = new OptionsPanel(
                    isMuted,
                    // 게임 재개
                    () -> {
                        remove(optionsPanel);
                        optionsPanel = null;
                        // 홈 버튼 다시 보이기
                        if (homeButton != null) {
                            homeButton.setVisible(true);
                        }
                        requestFocusInWindow();
                        isRunning = true;
                        gameThread = new Thread(this);
                        gameThread.start();
                    },
                    // 메인 메뉴
                    () -> {
                        isRunning = false;
                        SoundManager.stop("game_bg");
                        SoundManager.stop("game_bg2");
                        SoundManager.stop("game_bg3");
                        SoundManager.stop("zombie1");
                        SoundManager.stop("zombie23");
                        SoundManager.stop("bgm");      // 혹시 남아있을 수 있는 bgm 정지
                        SoundManager.stop("elevator");
                        remove(optionsPanel);
                        optionsPanel = null;

                        NoNextFloorGame.this.remove(IngamePanel.this);
                        currentIngamePanel = null;

                        NoNextFloorGame.this.gamePanel = new GamePanel();
                        NoNextFloorGame.this.add(gamePanel);
                        gamePanel.requestFocusInWindow();
                        NoNextFloorGame.this.revalidate();
                        NoNextFloorGame.this.repaint();
                    },
                    // 음소거 토글
                    () -> isMuted = !isMuted
                );

                setLayout(null);
                optionsPanel.setBounds(0, 0, getWidth(), getHeight());
                add(optionsPanel);
                optionsPanel.setVisible(true);
                optionsPanel.requestFocusInWindow();
                repaint();
            });
        }
     // showOptions() 메서드와 addHomeButton() 메서드 사이에 위치하거나,
     // IngamePanel의 모든 필드, 생성자, 그리고 기본 메서드(run, paintComponent 등)가 끝난 후 추가합니다.

        /* ===================== shootBullet (마우스 클릭 발사 로직) ===================== */
        private void shootBullet(int mx, int my) {
            long now = System.nanoTime();
            
            // 쿨다운 체크
            long cooldown = (currentWeapon == WEAPON_PISTOL) ? PISTOL_COOLDOWN : SHOTGUN_COOLDOWN;
            if (now - lastShootTime < cooldown) return;
            
            // 플레이어 중심 좌표
            int centerX = player.x + player.playerWidth / 2;
            int centerY = player.y + player.playerHeight / 2;
            
            // 🌟 무기 SFX 재생
            if (!mainGame.isMuted()) {
                if(currentWeapon == WEAPON_PISTOL) SoundManager.play("pistol", 0.7f);
                else if(currentWeapon == WEAPON_SHOTGUN) SoundManager.play("shotgun", 0.7f);
            }
            
            if (currentWeapon == WEAPON_PISTOL) {
                if (NoNextFloorGame.this.pistolAmmo <= 0) return;

                NoNextFloorGame.this.pistolAmmo--;

                double angle = Math.atan2(my - centerY, mx - centerX);
                int pistolLife = 120; // 권총 사거리 제한 (이동 로직에 따라 조정)
                bullets.add(new Bullet(centerX, centerY, angle, pistolLife));

            } else if (currentWeapon == WEAPON_SHOTGUN) {
                if (NoNextFloorGame.this.shotgunAmmo <= 0) return;

                NoNextFloorGame.this.shotgunAmmo--;

                double baseAngle = Math.atan2(my - centerY, mx - centerX);
                int shotgunLife = 30; // 샷건 사거리 제한 (짧게)

                // 전방 범위로  퍼지게
                double[] spread = {
                    baseAngle - Math.toRadians(45),
                    baseAngle - Math.toRadians(30),
                    baseAngle - Math.toRadians(15),
                    baseAngle - Math.toRadians(0),
                    baseAngle + Math.toRadians(15),
                    baseAngle + Math.toRadians(30),
                    baseAngle + Math.toRadians(45)
                };

                for (double angle : spread) {
                    bullets.add(new Bullet(centerX, centerY, angle, shotgunLife));
                }
            }
            lastShootTime = now;
        }

        /* ===================== playerHit (피격 SFX) ===================== */
        public void playerHit(int damage) {
            player.hp -= damage;
            if (!mainGame.isMuted()) SoundManager.play("hit", 3.0f); // 🌟 피격 SFX
            
            if (player.hp <= 0) {
                onPlayerDied();
            }
        }

        /* ===================== killZombie (좀비 사망 SFX) ===================== */
        private void killZombie(Zombie z) {
            zombies.remove(z);
            if (!mainGame.isMuted()) SoundManager.play("zombie_die", 0.4f); // 🌟 좀비 사망 SFX
            
            // *** 좀비 사망 시 로직 (점수/아이템) 추가 ***
        }
        

        /* ===================== onStageCleared (엘리베이터 BGM) ===================== */
        private void onStageCleared() {
            isRunning = false;
            
            // 🌟 BGM 모두 정지
            SoundManager.stop("bgm");
            SoundManager.stop("game_bg");
            SoundManager.stop("game_bg2");
            SoundManager.stop("game_bg3");
            SoundManager.stop("zombie1");
            SoundManager.stop("zombie23");
            
            if (!mainGame.isMuted()) SoundManager.playLoop("elevator", 0.5f); // 🌟 엘리베이터 BGM
            // *** 다음 스테이지 전환 로직은 mainGame.onStageCleared(int)에서 처리됩니다. ***
        }

        /* ===================== revivePlayer (부활 로직 - BGM 재시작) ===================== */
        // 게임오버 버튼 제거로 인해 현재 사용되지 않습니다.
        private void revivePlayer() { 
            // *** 부활 게임 로직 (체력, 위치, 쿨다운 등 초기화) ***
            
            // 🌟 BGM 재시작
        	if (!mainGame.isMuted()) {
                SoundManager.stop("gameover");
                
                // 1. 공통 BGM (볼륨 0.2f -> 0.1f로 낮춤)
               // SoundManager.playLoop("bgm", 0.1f); 
                
                // 2. 스테이지별 BGM (볼륨 0.15f -> 0.08f로 낮춤, 파일명은 기존 유지)
                switch(stageForThisPanel) {
                    case 1:
                        SoundManager.playLoop("game_bg", 0.03f);
                        break;
                    case 2:
                        SoundManager.playLoop("game_bg2", 0.08f);
                        break;
                    case 3:
                        SoundManager.playLoop("game_bg3", 0.08f);
                        break;
                }
                
                // 3. 좀비 사운드는 1F, 2F, 3F 스테이지 BGM이 재생 중일 때만 나오도록 조건 변경 
                //    (game_bg1, game_bg2, game_bg3에 해당한다고 가정)
                //    좀비 사운드 자체의 볼륨은 0.4f로 유지합니다.
                //    요청하신 조건이 '이 BGM에서만 나오게' 이므로, 모든 BGM에 조건이 걸리도록 유지합니다.
                switch(stageForThisPanel) {
                    case 1:
                        SoundManager.playLoop("zombie1", 0.2f);
                        break;
                    case 2:
                    case 3:
                        SoundManager.playLoop("zombie23", 0.2f);
                        break;
                }
            }
        }

           private void addHomeButton() {
               homeButton = new JButton();
               try {
                   ImageIcon originalIcon = new ImageIcon("images/home.png");
                   // 이미지 크기 조정 (원하는 크기로 변경 가능)
                   Image img = originalIcon.getImage();
                   Image scaledImg = img.getScaledInstance(30, 30, Image.SCALE_SMOOTH); // 30x30으로 축소
                   ImageIcon icon = new ImageIcon(scaledImg);
                   homeButton.setIcon(icon);
               } catch (Exception e) {
                   System.out.println("home.png 없음");
               }

               // 버튼 기본 설정
               homeButton.setContentAreaFilled(false);
               homeButton.setBorderPainted(false);
               homeButton.setFocusPainted(false);
               homeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
               homeButton.addActionListener(e -> showOptions());
               add(homeButton);

               // 패널 크기가 결정된 후 위치 지정
               SwingUtilities.invokeLater(() -> {
                   homeButton.setBounds(getWidth() - 70, 20, 30, 30); // 50x50에서 30x30으로 변경
                   homeButton.repaint();
                   int slotSize = 48;
                   int margin = 20;
                   int slotX = getWidth() - margin - slotSize - 100;
                   int slotY = getHeight() - margin - slotSize - 50;

                   pistolSlotRect = new Rectangle(slotX, slotY, slotSize, slotSize);
                   shotgunSlotRect = new Rectangle(slotX - slotSize - 8, slotY, slotSize, slotSize);
               });
           }
           private void drawVisionMask(Graphics2D g2d) {

        	    int w = getWidth();
        	    int h = getHeight();

        	    // 어두운 레이어 생성
        	    BufferedImage mask = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        	    Graphics2D g = mask.createGraphics();

        	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        	    // 전체 어둡게
        	    g.setColor(new Color(0, 0, 0, 180)); // 화면 전체 어둡게
        	    g.fillRect(0, 0, w, h);

        	    // 밝기 그라데이션 영역
        	    float centerX = player.x;
        	    float centerY = player.y;
        	    float radius = visionRadius;

        	    // 중심은 완전 투명 → 밖으로 갈수록 검정
        	    RadialGradientPaint light =
        	        new RadialGradientPaint(
        	            centerX, centerY, radius,
        	            new float[]{0f, 1f},
        	            new Color[]{
        	                new Color(0, 0, 0, 0),      // 중심(플레이어) 완전 밝음
        	                new Color(0, 0, 0, 180)     // 가장자리 어둠
        	            }
        	        );

        	    g.setComposite(AlphaComposite.DstOut);   // 어두운 레이어에서 원형 구멍 내기
        	    g.setPaint(light);
        	    g.fillOval((int)(centerX - radius), (int)(centerY - radius),
        	               (int)(radius * 2), (int)(radius * 2));

        	    g.dispose();

        	    g2d.drawImage(mask, 0, 0, null);
        	}

           @Override
           protected void paintComponent(Graphics g) {
               super.paintComponent(g);
               Graphics2D g2d = (Graphics2D) g;
               g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
               g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
               
               // ----------------------------------------------------------------------
               // 🌟 [수정된 배경 그리기 및 깜빡임 로직] 🌟
               // ----------------------------------------------------------------------
               boolean isFlashingOffTime = false; // 깜빡임 '꺼짐' 타이밍 플래그

               if (stageForThisPanel == 3 && isFlashingImage) { 
                   long elapsed = System.currentTimeMillis() - flashStartTime;
                   
                   // 홀수 간격일 때 (즉, '꺼짐' 타이밍일 때)만 true
                   if ((elapsed / FLASH_INTERVAL) % 2 != 0) {
                       isFlashingOffTime = true; 
                   }
               }

               // 배경 그리기 (게임오버일 때도 페이드 효과를 위해 배경 먼저 그림)
               if (gameBackground != null) {
                   if (isFlashingOffTime) {
                       // '꺼짐' 타이밍 (깜빡임 ON/OFF가 OFF인 경우)에는 television.png를 그립니다.
                       if (televisionImage != null) {
                           g.drawImage(televisionImage, 0, 0, getWidth(), getHeight(), this);
                       }
                       // televisionImage가 null이면, 패널의 기본 배경색(검은색)이 노출됩니다.
                   } else {
                       // '켜짐' 타이밍이거나 깜빡임 비활성화 상태일 때 기존 배경을 그립니다.
                       g.drawImage(gameBackground, 0, 0, getWidth(), getHeight(), this);
                   }
               }
               // ----------------------------------------------------------------------
               
               // 💡 [수정] 벽 충돌 영역 숨김: 이 코드를 주석 처리하여 빨간 벽이 보이지 않게 합니다.
               // g2d.setColor(new Color(255, 0, 0, 100));
               // for (Rectangle wall : wallRects) {
               //     g2d.fill(wall);
               // }
               
               // 배경 그리기 // 💡 [수정] 중복 그리기 방지: 이 코드를 주석 처리하여 깜빡임 로직을 깨뜨리지 않게 합니다.
               // if (gameBackground != null) {
               //     g.drawImage(gameBackground, 0, 0, getWidth(), getHeight(), this);
               // }
               
               if (isGameOver) {
                   // 즉시 검은 화면으로 전환
                   g2d.setColor(Color.BLACK);
                   g2d.fillRect(0, 0, getWidth(), getHeight());
                   
                   // 페이드인 효과 계산
                   if (gameOverStartTime == 0) {
                       gameOverStartTime = System.currentTimeMillis(); // 안전 체크: 시작 시간이 없으면 지금으로 설정
                   }
                   long currentTime = System.currentTimeMillis();
                   long elapsed = currentTime - gameOverStartTime;
                   float fadeInProgress = Math.min(1.0f, (float)elapsed / GAMEOVER_FADEIN_DURATION); // 0.0 ~ 1.0
                   
                   // 게임오버 이미지를 알파값에 따라 페이드인으로 그리기
                   if (gameOverImage != null) {
                       // Composite를 사용하여 알파 블렌딩
                       AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeInProgress);
                       g2d.setComposite(ac);
                       g2d.drawImage(gameOverImage, 0, 0, getWidth(), getHeight(), null);
                       g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); // 원래대로 복원
                   }
                   
                   // 페이드인 완료 후에만 화살표 표시
                   if (fadeInProgress >= 1.0f) {
                       // 2️⃣ 화살표 그리기 (YES/NO 버튼 왼쪽에 위치)
                       int arrowX, arrowY;
                       if (gameOverSelection == 0) {
                           // YES 선택 - 화살표를 YES 버튼 왼쪽에
                           arrowX = GAMEOVER_YES_X + GAMEOVER_ARROW_OFFSET_X;
                           arrowY = GAMEOVER_BUTTON_Y;
                       } else {
                           // NO 선택 - 화살표를 NO 버튼 왼쪽에
                           arrowX = GAMEOVER_NO_X + GAMEOVER_ARROW_OFFSET_X;
                           arrowY = GAMEOVER_BUTTON_Y;
                       }
                       
                       if (arrowImage != null) {
                           g2d.drawImage(arrowImage, arrowX, arrowY, null);
                       }
                   }
                   return;
               }
           
               // 우측 텍스트 추가 (주석 처리)
               /*
               int rightX = 900;
               int startY = 200;
               int lineSpacing = 40;

               g2d.setColor(Color.WHITE);
               //switch문으로 스테이지마다 문장 달라지게 만들기
               switch (stageForThisPanel) {
               case 1: // 1F
                 

                   g2d.setColor(Color.GREEN); 
                   g2d.setFont(new Font("Chiller", Font.PLAIN, 25));
                   g2d.drawString("Kill zombies", rightX+100, startY+30 + lineSpacing * 2);
                   break;

               case 2: // 2F
                  

                   g2d.setColor(Color.YELLOW); 
                   g2d.setFont(new Font("Chiller", Font.PLAIN, 25));
                   g2d.drawString("Get key", rightX+110, startY-150 + lineSpacing * 2);
                   break;

               case 3: // 3F
               

                   g2d.setColor(Color.RED); 
                   g2d.setFont(new Font("Chiller", Font.PLAIN, 28));
                   g2d.drawString("From Dr.zombie", rightX-270, startY+20 + lineSpacing * 2);
                   break;

               default:
                   break;
           } 
           */
               // 플레이어 그리기
               if (player != null) {
                   player.draw(g);
                   drawPlayerHearts(g);
               }

               // 좀비 그리기
           
               for (Zombie z : new ArrayList<>(zombies)) {

                   // 2층(맵2)만 시야 제한 적용
                   if (stageForThisPanel == 2) {
                       double dx = z.x - player.x;
                       double dy = z.y - player.y;
                       double dist = Math.sqrt(dx*dx + dy*dy);

                       if (dist > visionRadius) continue;  // 시야 밖 → 안 보임
                   }

                   // 1층, 3층은 항상 보임
                   z.draw(g);
               }


            // IngamePanel.java 내부 (paintComponent 메서드)

            // ... (다른 그리기 로직) ...

            // --- 보스 좀비 그리기 (3층) ---
            if (stageForThisPanel == 3 && bossZombie != null) {
                double dx = bossZombie.x - player.x;
                double dy = bossZombie.y - player.y;
                double dist = Math.sqrt(dx*dx + dy*dy);
                
                // 1. 현재 보스가 시야 범위 내에 있다면 감지 플래그를 활성화합니다.
                if (dist <= visionRadius) {
                    // BossZombie 클래스에 추가된 isDetected 플래그를 true로 설정
                    bossZombie.isDetected = true; 
                }

                // 2. 🌟 [핵심] 보스가 한 번이라도 감지되었다면 (isDetected == true) 그립니다. 🌟
                if (bossZombie.isDetected) {
                    bossZombie.draw(g);
                }
            }



               // 총알 그리기
               for (Bullet b : new ArrayList<>(bullets)) {
                   b.draw(g);
                   
               }
               if (stageForThisPanel == 2) {
               drawVisionMask(g2d);
               }
                // ★ 2F에서만 키 표시
                // 2F Key 표시
                   if (stageForThisPanel == 2 && keySpawned && !keyCollected) {
                       if (System.currentTimeMillis() - keySpawnTime <= KEY_SHOW_DURATION) {
                           g.drawImage(keyImage, keyX, keyY, 20, 20, this);
                       }
                   }

                   // 3F Antidote 표시
                   if (stageForThisPanel == 3 && antidoteSpawned && !antidoteCollected) {
                       if (System.currentTimeMillis() - antidoteSpawnTime <= ANTIDOTE_SHOW_DURATION) {
                           g.drawImage(antidoteImage, antidoteX - 25, antidoteY - 25, 20, 20, null);
                       }
                   }
                // 우측 하단 무기 슬롯 표시
                   // slotSize와 margin은 addHomeButton()에서 이미 계산됨

                   // 피스톨 슬롯 (오른쪽)
                   if (pistolSlotRect != null) {
                       // 배경
                       g.setColor(new Color(0, 0, 0, 120));
                       g.fillRect(pistolSlotRect.x, pistolSlotRect.y, pistolSlotRect.width, pistolSlotRect.height);
                       // 아이콘
                       if (pistolIcon != null) g.drawImage(pistolIcon, pistolSlotRect.x + 4, pistolSlotRect.y + 4, pistolSlotRect.width - 8, pistolSlotRect.height - 8, null);
                       // 선택 테두리
                       if (currentWeapon == WEAPON_PISTOL) {
                           g.setColor(Color.YELLOW);
                           g.drawRect(pistolSlotRect.x, pistolSlotRect.y, pistolSlotRect.width, pistolSlotRect.height);
                       }
                       // 탄알 숫자
                       g.setColor(Color.WHITE);
                       g.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
                       g.drawString("" + NoNextFloorGame.this.pistolAmmo, pistolSlotRect.x - 10, pistolSlotRect.y - 6);
                   }

                   // 샷건 슬롯 (왼쪽)
                   if (shotgunSlotRect != null) {
                       g.setColor(new Color(0, 0, 0, 120));
                       g.fillRect(shotgunSlotRect.x, shotgunSlotRect.y, shotgunSlotRect.width, shotgunSlotRect.height);
                       if (shotgunIcon != null) g.drawImage(shotgunIcon, shotgunSlotRect.x + 4, shotgunSlotRect.y + 4, shotgunSlotRect.width - 8, shotgunSlotRect.height - 8, null);
                       if (currentWeapon == WEAPON_SHOTGUN) {
                           g.setColor(Color.YELLOW);
                           g.drawRect(shotgunSlotRect.x, shotgunSlotRect.y, shotgunSlotRect.width, shotgunSlotRect.height);
                       }
                       g.setColor(Color.WHITE);
                       g.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
                       g.drawString("" + NoNextFloorGame.this.shotgunAmmo, shotgunSlotRect.x - 10, shotgunSlotRect.y - 6);
                   }

               
               drawHUD(g);
              
           }
           private void drawHUD(Graphics g) {
               Graphics2D g2 = (Graphics2D) g;
               g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

               int baseX = 20;
               int baseY = getHeight() - 120;  // 좌측 하단 배치

               // 플레이어 아이콘
               if (uiPlayerIcon != null) {
                   g2.drawImage(uiPlayerIcon, baseX, baseY, 32, 32, null);
               }

               // 플레이어 HP 하트
               int heartX = baseX + 40;
               for (int i = 0; i < 3; i++) {
                   if (i < player.hp) {
                       g2.drawImage(uiHeartFull, heartX + (i * 24), baseY, 20, 20, null);
                   } else {
                       g2.drawImage(uiHeartEmpty, heartX + (i * 24), baseY, 20, 20, null);
                   }
               }

               // 좀비 아이콘 + 남은 수
               if (uiZombieIcon != null) {
                   g2.drawImage(uiZombieIcon, baseX, baseY + 40, 32, 32, null);
               }

               g2.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
               g2.setColor(Color.WHITE);
               g2.drawString("" + zombies.size(), baseX + 40, baseY + 65);
           }

           private void loadUIResources() {
               try {
                   uiPlayerIcon = ImageIO.read(new File("images/player.png"));
                   uiZombieIcon = ImageIO.read(new File("images/zombie.png"));
                   uiHeartFull = ImageIO.read(new File("images/heart_full.png"));
                   uiHeartEmpty = ImageIO.read(new File("images/heart_empty.png"));
               } catch (Exception e) {
                   System.out.println("UI 이미지 로드 실패: " + e.getMessage());
               }
           }


           private void drawPlayerHearts(Graphics g) {
               Graphics2D g2d = (Graphics2D) g;
               int heartSize = 16;
               int heartX = player.x - heartSize;
               int heartY = player.y;

               if (fullHeartImage != null) {
                   for (int i = 0; i < player.hp; i++) {
                       g2d.drawImage(fullHeartImage, heartX, heartY + (i * (heartSize + 6)), heartSize, heartSize, null);
                   }
               } else {
                   for (int i = 0; i < player.hp; i++) {
                       drawHeart(g2d, heartX, heartY + (i * 22), heartSize);
                   
                   }
               }
           }

           private void drawHeart(Graphics2D g2d, int x, int y, int size) {
               g2d.setColor(Color.RED);
               int halfSize = size / 2;
               g2d.fillOval(x, y, halfSize, halfSize);
               g2d.fillOval(x + halfSize, y, halfSize, halfSize);
               int[] xPoints = {x, x + size, x + halfSize};
               int[] yPoints = {y + halfSize, y + halfSize, y + size};
               g2d.fillPolygon(xPoints, yPoints, 3);
           }
           

           // 키 이벤트 처리
           @Override public void keyTyped(KeyEvent e) {}
           @Override
           public void keyPressed(KeyEvent e) {
               int keyCode = e.getKeyCode();
               
               // 게임오버 상태에서 키 입력 처리
               if (isGameOver) {
                   if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
                       gameOverSelection = 0; // YES 선택
                       repaint();
                       return;
                   } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
                       gameOverSelection = 1; // NO 선택
                       repaint();
                       return;
                   } else if (keyCode == KeyEvent.VK_SPACE) {
                       // 스페이스바로 선택 실행
                       executeGameOverSelection();
                       return;
                   }
                   return;
               }
               
               if (player == null) return;
               
               // 키 상태 플래그 설정 (매끄러운 이동을 위해)
               if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
                   leftPressed = true;
               } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
                   rightPressed = true;
               } else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
                   upPressed = true;
               } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
                   downPressed = true;
               }
               
               // 발사 키 처리
               if (keyCode == KeyEvent.VK_SPACE) {
                   shootBulletForward();
               }
           }
           @Override 
           public void keyReleased(KeyEvent e) {
               if (player == null) return;
               int keyCode = e.getKeyCode();
               
               // 키 상태 플래그 해제
               if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
                   leftPressed = false;
               } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
                   rightPressed = false;
               } else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
                   upPressed = false;
               } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
                   downPressed = false;
               }
           }

           // 전방 총알 발사 (스페이스바)
           private void shootBulletForward() {
               long now = System.nanoTime();
               long cooldown = (currentWeapon == WEAPON_PISTOL) ? PISTOL_COOLDOWN : SHOTGUN_COOLDOWN;
               if (now - lastShootTime < cooldown) return;

               // 탄약 체크 
               if (currentWeapon == WEAPON_PISTOL && NoNextFloorGame.this.pistolAmmo <= 0) {
                   System.out.println("피스톨 탄약 떨어짐");
                   return;
               }
               if (currentWeapon == WEAPON_SHOTGUN && NoNextFloorGame.this.shotgunAmmo <= 0) {
                   System.out.println("샷건 탄약 떨어짐");
                   return;
               }
               
               // 🌟 무기 SFX 재생
               if (!mainGame.isMuted()) {
                   if(currentWeapon == WEAPON_PISTOL) SoundManager.play("pistol", 0.7f);
                   else if(currentWeapon == WEAPON_SHOTGUN) SoundManager.play("shotgun", 0.7f);
               }

               lastShootTime = now;
               int bulletX = player.x + player.playerWidth / 2;
               int bulletY = player.y + player.playerHeight / 2;

               if (currentWeapon == WEAPON_PISTOL) {
                   NoNextFloorGame.this.pistolAmmo--;
                   int pistolLife = 120;
                   bullets.add(new Bullet(bulletX, bulletY, player.angle, pistolLife));
               } else {
                   NoNextFloorGame.this.shotgunAmmo--;
                   int shotgunLife = 25;
                   double up = -Math.PI / 2.0;
                   double right = 0.0;
                   double down = Math.PI / 2.0;
                   double left = Math.PI;
                   // 샷건은 플레이어의 방향과 상관없이 상하좌우로 발사됩니다.
                   bullets.add(new Bullet(bulletX, bulletY, up, shotgunLife));
                   bullets.add(new Bullet(bulletX, bulletY, right, shotgunLife));
                   bullets.add(new Bullet(bulletX, bulletY, down, shotgunLife));
                   bullets.add(new Bullet(bulletX, bulletY, left, shotgunLife));
               }
           }
        // IngamePanel 클래스 내부

        // 📌 2) 스테이지 로딩 시 벽 좌표 불러오기
        private void loadStageWalls(int stage) {
            wallRects.clear();

            switch(stage) {
                case 1:
                    loadStage1Walls();
                    break;
                case 2:
                    loadStage2Walls();
                    break;
                case 3:
                    loadStage3Walls();
                    break;
            }
        }
        private void loadStage1Walls() {
            wallRects.clear();
            
            int wallThick = 24; // 벽 두께
            wallRects.add(new Rectangle(240, 40, 1000, wallThick)); 
            wallRects.add(new Rectangle(240, 40, wallThick, 340)); 
            wallRects.add(new Rectangle(690, 40, wallThick, 340));
            wallRects.add(new Rectangle(250, 356, 370, wallThick)); 
            wallRects.add(new Rectangle(680, 356, 580, wallThick));
            wallRects.add(new Rectangle(970, 356, wallThick, 354));
            wallRects.add(new Rectangle(580, 616, 700, wallThick));
            wallRects.add(new Rectangle(550, 366, wallThick, 114)); 
            wallRects.add(new Rectangle(440, 480, 120, wallThick));
            wallRects.add(new Rectangle(440, 480, wallThick, 130));
            wallRects.add(new Rectangle(440, 556, 120, wallThick));
            wallRects.add(new Rectangle(380, 150, 200, 70));
            
            wallRects.add(new Rectangle(1100, 64, 120, 60));
            wallRects.add(new Rectangle(1080, 450, 120, 120));
            wallRects.add(new Rectangle(550, 580, 60, 60));
        }

        private void loadStage2Walls() {

       	    wallRects.clear();
       	    int w = 24; // 벽 두께
       	    wallRects.add(new Rectangle(140, 150, 120, w));        // 상단
       	    wallRects.add(new Rectangle(140, 150, w, 170));        // 좌측
       	    wallRects.add(new Rectangle(140, 215, 120, w));        // 하단
       	    wallRects.add(new Rectangle(240, 50, 400, w));
       	    wallRects.add(new Rectangle(755, 150, w, 100));
       	    wallRects.add(new Rectangle(520, 130, 700, w));
       	    wallRects.add(new Rectangle(240, 50, w, 110));
       	    wallRects.add(new Rectangle(520, 50, w, 100));
       	    wallRects.add(new Rectangle(520, 335, w, 40));
       	    wallRects.add(new Rectangle(650, 275, w, 175));
       	    wallRects.add(new Rectangle(240, 215, 120, w));
       	    wallRects.add(new Rectangle(350, 335, 170, w));
       	    wallRects.add(new Rectangle(520, 250, w, 80));
       	    wallRects.add(new Rectangle(535, 250, 30, w));
       	    wallRects.add(new Rectangle(620, 250, 135, w));
       	    wallRects.add(new Rectangle(1020, 140, w, 400));
       	    wallRects.add(new Rectangle(520, 420, w, 90));
       	    wallRects.add(new Rectangle(680, 520, 330, w));
       	    wallRects.add(new Rectangle(1020, 520, w, 240));
       	    wallRects.add(new Rectangle(660, 520, w, 160));
       	    wallRects.add(new Rectangle(520, 630, 260, w));
       	    wallRects.add(new Rectangle(520, 500, w, 200));
       	    wallRects.add(new Rectangle(780, 330, 130, 70));
       	    wallRects.add(new Rectangle(240, 520, 300, w));
       	    wallRects.add(new Rectangle(240, 215, w, 280));
       	    wallRects.add(new Rectangle(240, 760, 300, w));
       	}
        private void loadStage3Walls() {

       	    wallRects.clear();
       	    int w = 13; // 벽 두께(필요하면 조절)

       	    wallRects.add(new Rectangle(270, 30, w, 60));        
       	    wallRects.add(new Rectangle(270, 30, 70, w));        
       	    wallRects.add(new Rectangle(350, 30, w, 60)); 
       	    wallRects.add(new Rectangle(320, 210, 120, 120)); 
       	    wallRects.add(new Rectangle(930, 420, 100, 100)); 
       	    wallRects.add(new Rectangle(200, 100, 80, w)); 
       	    wallRects.add(new Rectangle(350, 100, 220, w)); 
       	    wallRects.add(new Rectangle(200, 100, w, 450)); 
       	    wallRects.add(new Rectangle(550, 100, w, 300)); 
       	    wallRects.add(new Rectangle(550, 410, 100, w)); 
       	    wallRects.add(new Rectangle(520, 490, 100, w)); 
       	    wallRects.add(new Rectangle(200, 500, 360, w)); 
       	    wallRects.add(new Rectangle(610, 610, 450, w)); 
       	    wallRects.add(new Rectangle(610, 330, 220, w)); 
       	    wallRects.add(new Rectangle(610, 330, w, 70)); 
       	    wallRects.add(new Rectangle(610, 490, w, 130)); 
       	    wallRects.add(new Rectangle(830, 130, 220, w)); 
       	    wallRects.add(new Rectangle(830, 130, w, 220)); 
       	    wallRects.add(new Rectangle(1050, 120, w, 5000)); 
       	    
       	}

        // 📌 6) 플레이어/좀비 충돌 체크 함수 (통합)
        private boolean checkCollision(Rectangle nextPos) {
            // lastCollidedWall = null; 삭제
            
            for (Rectangle wall : wallRects) {
                if (nextPos.intersects(wall)) {
                    // lastCollidedWall = wall; 삭제
                    return true;
                }
            }
            return false;
        }





        // 좀비 시야 체크 (벽에 가려졌는지 확인)
           private boolean hasLineOfSight(Zombie z, Player p) {
               Line2D sightLine = new Line2D.Double(
                   z.x + z.zombieWidth / 2.0, z.y + z.zombieHeight / 2.0,
                   p.x + p.playerWidth / 2.0, p.y + p.playerHeight / 2.0
               );
               for (Rectangle wall : wallRects) {
                   if (wall.intersectsLine(sightLine)) return false; // 벽에 막힘
               }
               return true; // 보임
           }


           public void updateGame() {
               if (player == null || zombies == null) return;
               
               // =========================================================
               // 0. 플레이어 이동 처리 (키 입력 상태 기반 - 매끄러운 이동)
               // =========================================================
               if (!isGameOver) {
                   int newX = player.x;
                   int newY = player.y;
                   int speed = player.speed;
                   boolean moved = false;
                   
                   // 방향별 이동 처리
                   if (leftPressed) {
                       newX -= speed;
                       moved = true;
                   }
                   if (rightPressed) {
                       newX += speed;
                       moved = true;
                   }
                   if (upPressed) {
                       newY -= speed;
                       moved = true;
                   }
                   if (downPressed) {
                       newY += speed;
                       moved = true;
                   }
                   
                   // 각도 즉시 변경
                   if (moved) {
                       if (leftPressed && !rightPressed) {
                           if (upPressed && !downPressed) {
                               player.angle = -Math.PI * 3.0 / 4.0; // 좌상
                           } else if (downPressed && !upPressed) {
                               player.angle = Math.PI * 3.0 / 4.0; // 좌하
                           } else {
                               player.angle = Math.PI; // 좌
                           }
                       } else if (rightPressed && !leftPressed) {
                           if (upPressed && !downPressed) {
                               player.angle = -Math.PI / 4.0; // 우상
                           } else if (downPressed && !upPressed) {
                               player.angle = Math.PI / 4.0; // 우하
                           } else {
                               player.angle = 0; // 우
                           }
                       } else if (upPressed && !downPressed) {
                           player.angle = -Math.PI / 2.0; // 상
                       } else if (downPressed && !upPressed) {
                           player.angle = Math.PI / 2.0; // 하
                       }
                   }
                   
                   // 충돌 체크 후 위치 업데이트
                   if (moved) {
                       Rectangle nextPos = new Rectangle(newX, newY, player.playerWidth, player.playerHeight);
                       if (!checkCollision(nextPos)) {
                           player.x = newX;
                           player.y = newY;
                       } else {
                           // 충돌 시 X, Y 각각 체크하여 부분 이동 허용 (대각선 이동 시)
                           Rectangle nextXPos = new Rectangle(newX, player.y, player.playerWidth, player.playerHeight);
                           Rectangle nextYPos = new Rectangle(player.x, newY, player.playerWidth, player.playerHeight);
                           if (!checkCollision(nextXPos)) {
                               player.x = newX;
                           }
                           if (!checkCollision(nextYPos)) {
                               player.y = newY;
                           }
                       }
                   }
                   
                   // 체력 체크
                   if (player.hp <= 0 && !isGameOver) {
                       onPlayerDied();
                   }
               }
               
        	   if (stageForThisPanel == 3 && bossZombie != null) {
        	        
        		// 1. 일반 좀비 전멸 조건 확인
        	        boolean allRegularZombiesDefeated = zombies.isEmpty(); 

        	        // 2. 깜빡임 시작 조건:
        	        //    전멸했고, 보스가 살아있고, 아직 깜빡임이 시작되지 않았으며, 
        	        //    🌟 [추가]: 시퀀스가 이전에 완료되지 않았을 때만 시작합니다. 🌟
        	        if (allRegularZombiesDefeated && !isFlashingImage && !flashSequenceCompleted) {
        	            isFlashingImage = true;
        	            flashStartTime = System.currentTimeMillis();
        	        }
        	    }
        	    
        	    // 3. 깜빡임 시간 종료 체크
        	    if (isFlashingImage) {
        	        long now = System.currentTimeMillis();
        	        
        	        if (now - flashStartTime > FLASH_DURATION) {
        	            isFlashingImage = false;
        	            // 🌟 [추가]: 깜빡임이 끝나면 시퀀스 완료 플래그를 true로 설정합니다. 🌟
        	            flashSequenceCompleted = true; 
        	            // 깜빡임 종료 후, 보스가 살아있다면 게임은 계속 진행됩니다.
        	        }
        	    }
        	   if (zombies.isEmpty()) {
        		    // Stage 1: zombie1 사운드 정지
        		    if (stageForThisPanel == 1) {
        		        SoundManager.stop("zombie1");
        		    } 
        		    // Stage 2 & 3: zombie23 사운드 정지 (3스테이지는 보스가 남더라도 일반 좀비 소리는 멈춥니다)
        		    else if (stageForThisPanel == 2 || stageForThisPanel == 3) {
        		        SoundManager.stop("zombie23");
        		    }
        		}

               // =========================================================
               // 1. 총알 업데이트 및 제거 (+ 벽 충돌 기능 추가)
               // =========================================================
               for (int i = bullets.size() - 1; i >= 0; i--) {
                   Bullet b = bullets.get(i);
                   b.update();
                   
                   // (1) 화면 밖으로 나감
                   if (b.isExpired(getWidth(), getHeight())) {
                       bullets.remove(i);
                       continue;
                   }
                   
                   // ★ (2) 총알이 벽에 맞음
                   Rectangle bulletRect = new Rectangle((int)b.x, (int)b.y, 4, 4);
                   if (checkCollision(bulletRect)) {
                       bullets.remove(i);
                       continue;
                   }
               }

               // =========================================================
               // 2. 좀비 업데이트 및 충돌 검사
               // =========================================================
               
               // 추격 거리 설정 (이 거리 밖이면 안 쫓아옴)
               final int DETECTION_RANGE = 400; 

               for (int i = zombies.size() - 1; i >= 0; i--) {
                   Zombie z = zombies.get(i);
                   
                   // ★ 추격 여부 판단 (거리 + 시야)
                   double distToPlayer = Math.hypot(player.x - z.x, player.y - z.y);
                   boolean canChase = (distToPlayer < DETECTION_RANGE) && hasLineOfSight(z, player);

                   // [기존 이동 로직]을 if (canChase)로 감쌌습니다.
                   if (canChase) {
                       // 1. 현재 위치 저장
                       int prevX = z.x;
                       int prevY = z.y;

                       // 2. 좀비가 가고 싶은 목표 위치 계산
                       z.update(player); 
                       int targetX = z.x;
                       int targetY = z.y;

                       // 3. 일단 위치를 원래대로 되돌림
                       z.x = prevX;
                       z.y = prevY;

                       // 4. [X축 시도] 좌우로만 움직여본다.
                       Rectangle nextXPos = new Rectangle(targetX, prevY, z.zombieWidth, z.zombieHeight);
                       if (!checkCollision(nextXPos)) {
                           z.x = targetX; 
                       }

                       // 5. [Y축 시도] 상하로만 움직여본다.
                       Rectangle nextYPos = new Rectangle(z.x, targetY, z.zombieWidth, z.zombieHeight);
                       if (!checkCollision(nextYPos)) {
                           z.y = targetY; 
                       }
                   } 
                   // canChase가 false면 좀비는 제자리에 멈춤

                   // =========================================================
                   // [기존 공격 및 피격 로직 유지] 
                   // =========================================================

                   // 플레이어와 충돌 (공격)
                   double dist = Math.hypot(player.x - z.x, player.y - z.y);
                   if (dist < 50 && z.canAttack()) {
                       player.takeDamage(1);
                       if (!mainGame.isMuted()) {
                           SoundManager.play("hit", 1.0f); 
                       }
                       z.recordAttack();
                       System.out.println("좀비에게 공격당함! Player HP: " + player.hp);
                   }

                   // 총알과 충돌 검사
                   for (int j = bullets.size() - 1; j >= 0; j--) {
                       Bullet b = bullets.get(j);
                       double zCenterX = z.x + z.zombieWidth / 2.0;
                       double zCenterY = z.y + z.zombieHeight / 2.0;
                       
                       double bulletDist = Math.hypot(b.x - zCenterX, b.y - zCenterY);
                       
                       if (bulletDist < (z.zombieWidth / 2.0)) {
                           z.takeDamage(1);
                           bullets.remove(j);
                           System.out.println("좀비 HP: " + z.hp);
                           if (z.hp <= 0) {
                               killZombie(z); // 좀비 사망 로직 분리
                           }
                           break;
                       }
                   }
               }
               

               // 🔥 보스 좀비 업데이트 (기존 코드 유지)
               if (stageForThisPanel == 3 && bossZombie != null) {
                   bossZombie.update(player);

                   // 보스 → 플레이어 충돌
                   double bossDist = Math.hypot(player.x - bossZombie.x, player.y - bossZombie.y);
                   if (bossDist < 70 && bossZombie.canAttack()) {
                       player.takeDamage(1);
                       if (!mainGame.isMuted()) {
                           SoundManager.play("hit", 1.0f); 
                       }
                       bossZombie.recordAttack();
                       System.out.println("보스에게 공격당함!");
                   }

                   // 보스 ← 총알 충돌
                   for (int j = bullets.size() - 1; j >= 0; j--) {
                       Bullet b = bullets.get(j);
                       double bulletDist = Math.hypot(
                           b.x - (bossZombie.x + bossZombie.zombieWidth / 2.0),
                           b.y - (bossZombie.y + bossZombie.zombieHeight / 2.0)
                       );
                       if (bulletDist < (bossZombie.zombieWidth / 2.0)) {
                           bossZombie.takeDamage(1);
                           bullets.remove(j);
                           System.out.println("보스 HP: " + bossZombie.hp);
                           if (bossZombie.hp <= 0) {
                               bossZombie = null;
                               System.out.println("보스 처치!");
                           }
                           break;
                       }
                   }
               }
         

               // 모두 처치되면 다음 스테이지로 이동
            // ================== 공통: 좀비 0마리 체크 ==================
               if (zombies.isEmpty() && (stageForThisPanel != 3 || bossZombie == null)) {

                   // ======== ★ 2F 스테이지: 열쇠 등장/획득 ========
                   if (stageForThisPanel == 2) {

                       if (!keySpawned) {
                           keySpawned = true;
                           keySpawnTime = System.currentTimeMillis(); 
                           System.out.println("열쇠가 등장했습니다!");
                       }

                       if (keySpawned && !keyCollected) {
                           double dist = Math.hypot(player.x - keyX, player.y - keyY);
                           if (dist < 60) {
                               keyCollected = true;

                               JOptionPane.showMessageDialog(
                                   this,
                                   "🔑옥상열쇠를 획득했습니다!",
                                   "KEY FOUND",
                                   JOptionPane.INFORMATION_MESSAGE
                               );

                               isRunning = false;
                               SwingUtilities.invokeLater(() -> mainGame.onStageCleared(stageForThisPanel)); // 🌟 수정됨
                               return;
                           }
                       }

                       return; // 키 먹기 전에는 이동 불가
                   }

                   // ======== ★ 3F 스테이지: 해독제 등장/획득 (보스 처치 후) ========
                   if (stageForThisPanel == 3 && bossZombie == null) {

                       if (!antidoteSpawned) {
                           antidoteSpawned = true;
                           antidoteSpawnTime = System.currentTimeMillis();
                           System.out.println("어라 보스좀비가 무언가를 떨어뜨린 모양입니다..");
                       }

                       if (antidoteSpawned && !antidoteCollected) {
                           double dist = Math.hypot(player.x - antidoteX, player.y - antidoteY);
                           if (dist < 60) {
                               antidoteCollected = true;

                               JOptionPane.showMessageDialog(
                                   this,
                                   "좀비가된 약사의 주머니에서 무언가가 떨어졌습니다.. 해독제를 찾았습니다 축하합니다!",
                                   "ANTIDOTE FOUND",
                                   JOptionPane.INFORMATION_MESSAGE
                               );

                               isRunning = false;
                               SwingUtilities.invokeLater(() -> mainGame.onStageCleared(stageForThisPanel)); // 🌟 수정됨
                               return;
                           }
                       }

                       // 해독제 먹기 전에는 종료 불가
                       return;
                   }

                   // ======== ★ 1F 스테이지는 기존처럼 즉시 이동 ========
                   isRunning = false;
                   SwingUtilities.invokeLater(() -> mainGame.onStageCleared(stageForThisPanel)); // 🌟 수정됨
                   return;
               }
           }
               public void stopGameThread() {
                   isRunning = false; // 게임 루프 플래그를 false로 설정하여 루프를 중지
                   if (gameThread != null) {
                       try {
                           gameThread.join(); // 스레드가 완전히 종료될 때까지 대기
                       } catch (InterruptedException e) {
                           Thread.currentThread().interrupt();
                           System.out.println("게임 스레드 종료 중단됨");
                       }
                   }
               }


            // IngamePanel.java 내부 (run() 메서드)

               @Override
                       public void run() {
                           double drawInterval = 1000000000.0 / 60.0;
                        double delta = 0;
                        long lastTime = System.nanoTime();
                        
                        while (isRunning) {
                            long currentTime = System.nanoTime();
                            delta += (currentTime - lastTime) / drawInterval;
                            lastTime = currentTime;
                            if (delta >= 1) {
                                updateGame();
                                // repaint()를 두 번 호출할 필요는 없습니다. 아래쪽의 repaint()만 유지합니다.
                                
                                if (player.hp <= 0 && !isGameOver) {
                                    onPlayerDied(); // 페이드인 타이머를 포함한 모든 게임오버 처리를 여기서 수행
                                }

                                repaint(); // 게임 상태 갱신 후 화면을 다시 그립니다.
                                delta--;

                                try { Thread.sleep(16); } catch (Exception e) {}
                            
                            }
                        }
                    }
                }

    /* ===================== 스테이지 전환/시작 관련 메소드 ===================== */

    // 메뉴 -> 특정 스테이지 로딩 화면 표시
 // NoNextFloorGame.java 내부의 showStageIntro 메서드

    public void showStageIntro(int stageToShow) {
        // 1) 메뉴 제거 (있다면)
        if (gamePanel != null) {
            gamePanel.stopAnimation();
            remove(gamePanel);
            gamePanel = null;
        }
        
        // 2) 기존 인게임 패널 제거(안정성)
        if (currentIngamePanel != null) {
            
            // 🌟 [핵심 수정]: 이전 스테이지의 BGM 및 스레드 정지 🌟
            currentIngamePanel.stopGameThread(); // 게임 루프 스레드 정지 (안정성)
            
            // 모든 인게임 BGM 트랙 정지 (스테이지 소리 및 좀비 소리)
            SoundManager.stop("game_bg");
            SoundManager.stop("game_bg2");
            SoundManager.stop("game_bg3");
            SoundManager.stop("zombie1");
            SoundManager.stop("zombie23");
            // ----------------------------------------------------

            remove(currentIngamePanel);
            currentIngamePanel = null;
        }
        
        // 3) 새 Loading 패널 추가
        this.stage = stageToShow;
        loadingPanel = new Loading(this, stageToShow);
        add(loadingPanel);
        revalidate();
        repaint();
        loadingPanel.requestFocusInWindow();
    }

    // Loading 클릭 시 실제 스테이지 시작 (Loading -> Ingame)
    public void startStage(int stageToStart) {
        // remove loading
        if (loadingPanel != null) {
            remove(loadingPanel);
            loadingPanel = null;
        }
        // create ingame panel for this stage
        currentIngamePanel = new IngamePanel(this, stageToStart);
        add(currentIngamePanel);
        revalidate();
        repaint();
        currentIngamePanel.requestFocusInWindow();
    }

    // 스테이지 클리어 후 처리: 다음 스테이지 로딩 혹은 최종 승리
    private void onStageCleared(int clearedStage) {
        if (clearedStage < MAX_STAGE) {
            int nextStage = clearedStage + 1;
            showStageIntro(nextStage);
        } else {
            // 마지막 스테이지 → 엔딩 패널로 전환
            showEnding(); 
            // 💡 showEnding() 호출
        }
    }
    public boolean isMuted() { 
        return isMuted;
    }
    public void showHowToPlay() {
        // 1) 기존 메인 화면 컴포넌트 제거 (startButton, soundButton 등)
        if (startButton != null) remove(startButton);
        if (soundButton != null) remove(soundButton);
        if (gamePanel != null) { // GamePanel이 메인 화면을 담당했다면
            // GamePanel 내부의 애니메이션 타이머를 멈춥니다.
            gamePanel.stopAnimation(); 
            remove(gamePanel);
            gamePanel = null;
        }
        
        // 2) HowToPlayPanel 생성 및 추가
        howToPlayPanel = new HowToPlayPanel(this);
        
        // 프레임의 모든 컴포넌트를 지우고 (안전한 제거)
        getContentPane().removeAll(); 
        // 새로운 HowToPlayPanel을 프레임에 추가
        add(howToPlayPanel, BorderLayout.CENTER); 
        
        // 3) 화면 갱신
        revalidate();
        repaint();
        
        // 4) 포커스 요청 (키 입력을 받기 위해)
        howToPlayPanel.requestFocusInWindow();
    }

    // ===================== 엔딩 화면 표시 =====================
    /**
     * 최종 엔딩 화면을 표시하고 게임을 종료합니다.
     * EndingPanel이 독립된 클래스 파일(EndingPanel.java)이라고 가정하고,
     * 해당 클래스의 생성자에 JFrame 인스턴스(this)를 전달합니다.
     */
    public void showEnding() {
        // 1) 기존 인게임 패널 제거
        if (currentIngamePanel != null) {
            currentIngamePanel.stopGameThread(); // 스레드 중지
            remove(currentIngamePanel);
            currentIngamePanel = null;
        }
        SoundManager.stop("game_bg"); 
        SoundManager.stop("game_bg2");
        SoundManager.stop("game_bg3");
        SoundManager.stop("zombie1");
        SoundManager.stop("zombie23");
        SoundManager.stop("bgm"); 
        SoundManager.stop("elevator"); // 혹시 로딩 화면에서 넘어온 잔여 사운드 정지
        // --------------------------------------------------------

        // 🌟 [추가]: 엔딩 패널에서 elevator.wav 재생 🌟
        if (!isMuted()) { // isMuted 상태가 아닐 때만 재생
            SoundManager.playLoop("elevator", 0.5f); 
        }

        // 2) 엔딩 패널 생성 및 추가
        // EndingPanel.java가 독립적인 파일이라고 가정하고 호출합니다.
        // EndingPanel 생성자에 'NoNextFloorGame' 인스턴스(this)를 넘겨줍니다.
        EndingPanel endingPanel = new EndingPanel(this);
        add(endingPanel);
        
        revalidate();
        repaint();
        endingPanel.requestFocusInWindow();
    }
    // ========================================================

    // ★★★ 여기 바로 아래에 추가하면 됨! ★★★
    /* ===================== 기존 main ===================== */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new NoNextFloorGame();
        });
    }
}
