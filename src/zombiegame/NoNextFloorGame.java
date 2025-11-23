package zombiegame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.RadialGradientPaint;


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
    private static final Color RED_PRIMARY = new Color(255, 0, 0);
    private static final Color RED_DARK = new Color(139, 0, 0);
    private static final Color RED_LIGHT = new Color(255, 102, 102);
    private static final Color RED_TRANSPARENT = new Color(255, 0, 0, 180);
    private static final Color BLACK_TRANSPARENT = new Color(0, 0, 0, 180);
    private static final Color TEXT_RED = new Color(255, 100, 100, 180);

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
        // stage 3: 3 마리
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
    class GamePanel extends JPanel {
        private Timer animationTimer;
        private float titleOpacity = 0f;
        private float buttonOpacity = 0f;
        private float textOpacity = 0f;

        public GamePanel() {
            setLayout(null);
            setBackground(Color.BLACK);

            // 애니메이션 타이머 (기존 로직 유지)
            animationTimer = new Timer(30, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (titleOpacity < 1.0f) {
                        titleOpacity = Math.min(1.0f, titleOpacity + 0.02f);
                    }
                    if (titleOpacity > 0.5f && buttonOpacity < 1.0f) {
                        buttonOpacity = Math.min(1.0f, buttonOpacity + 0.03f);
                    }
                    if (buttonOpacity > 0.5f && textOpacity < 1.0f) {
                        textOpacity = Math.min(1.0f, textOpacity + 0.02f);
                    }

                    repaint();

                    if (titleOpacity >= 1.0f && buttonOpacity >= 1.0f && textOpacity >= 1.0f) {
                        animationTimer.stop();
                    }
                }
            });
            animationTimer.start();
            if (!isMuted) SoundManager.playLoop("start", 0.3f); // 사운드 토글 버튼 soundButton = createSoundButton();

            // 사운드 토글 버튼
            soundButton = createSoundButton();
            soundButton.setBounds(1200, 20, 50, 50);
            add(soundButton);

            // 게임 시작 버튼
            startButton = createStartButton();
            startButton.setBounds(850, 380, 200, 50);
            add(startButton);
        }

        public void stopAnimation() {
            if (animationTimer != null && animationTimer.isRunning()) {
                animationTimer.stop();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 배경 이미지 (메뉴)
            if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                // 어두운 오버레이
                g2d.setColor(new Color(0, 0, 0, 80));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            } else {
                // 배경 이미지가 없을 경우 그라데이션 배경
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(20, 0, 0),
                        0, getHeight(), new Color(0, 0, 0)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }

            // 제목/부제/하단/스캔라인 (원본 로직 유지)
            drawTitle(g2d);
            drawSubtitle(g2d);
            drawFooterText(g2d);
            drawScanlines(g2d);
        }
        

        private void drawTitle(Graphics2D g2d) {
            int x = 850;
            int y = 220;
            Font titleFont = new Font("Impact", Font.BOLD, 48);
            g2d.setFont(titleFont);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, titleOpacity * 0.4f));
            g2d.setColor(RED_DARK);
            g2d.drawString("NO NEXT", x - 2, y - 2);
            g2d.drawString("FLOOR", x - 2, y + 48 - 2);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, titleOpacity * 0.3f));
            g2d.setColor(RED_LIGHT);
            g2d.drawString("NO NEXT", x + 2, y + 2);
            g2d.drawString("FLOOR", x + 2, y + 48 + 2);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, titleOpacity));
            g2d.setStroke(new BasicStroke(3));
            g2d.setColor(Color.BLACK);
            FontMetrics fm = g2d.getFontMetrics();
            drawOutlineText(g2d, "NO NEXT", x, y, fm);
            drawOutlineText(g2d, "FLOOR", x, y + 48, fm);

            g2d.setColor(RED_PRIMARY);
            g2d.drawString("NO NEXT", x, y);
            g2d.drawString("FLOOR", x, y + 48);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, titleOpacity));
            GradientPaint lineGradient = new GradientPaint(
                x, y + 60, RED_PRIMARY,
                x + 280, y + 60, new Color(255, 0, 0, 0)
            );
            g2d.setPaint(lineGradient);
            g2d.fillRect(x, y + 60, 280, 2);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        private void drawOutlineText(Graphics2D g2d, String text, int x, int y, FontMetrics fm) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx != 0 || dy != 0) {
                        g2d.drawString(text, x + dx, y + dy);
                    }
                }
            }
        }

        private void drawSubtitle(Graphics2D g2d) {
            int x = 850;
            int y = 320;
            Font subtitleFont = new Font("Malgun Gothic", Font.PLAIN, 18);
            g2d.setFont(subtitleFont);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, titleOpacity));
            g2d.setColor(new Color(255, 200, 200));
            g2d.drawString("내리실 층은 없습니다.", x, y);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        private void drawFooterText(Graphics2D g2d) {
            int x = 850;
            int y = 480;
            Font footerFont = new Font("Chiller", Font.PLAIN, 20);
            g2d.setFont(footerFont);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textOpacity * 1.0f));
            g2d.setColor(new Color(255, 200, 200));
            g2d.drawString("THERE IS NO WAY UP", x, y);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        private void drawScanlines(Graphics2D g2d) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f));
            g2d.setColor(RED_PRIMARY);
            for (int i = 0; i < getHeight(); i += 4) {
                g2d.drawLine(0, i, getWidth(), i);
            }
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        

        private JButton createSoundButton() {
            JButton button = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // 배경
                    g2d.setColor(BLACK_TRANSPARENT);
                    g2d.fillRect(0, 0, getWidth(), getHeight());

                    // 테두리
                    g2d.setColor(new Color(255, 0, 0, 100));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRect(1, 1, getWidth() - 2, getHeight() - 2);

                    // 아이콘 (간단한 스피커)
                    g2d.setColor(new Color(255, 100, 100));
                    if (isMuted) {
                        // X 표시
                        g2d.drawLine(15, 15, 35, 35);
                        g2d.drawLine(35, 15, 15, 35);
                    } else {
                        // 스피커 아이콘
                        int[] xPoints = {15, 25, 25, 15};
                        int[] yPoints = {20, 15, 35, 30};
                        g2d.fillPolygon(xPoints, yPoints, 4);
                        g2d.drawArc(25, 18, 8, 14, -30, 60);
                        g2d.drawArc(28, 15, 12, 20, -30, 60);
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
                    button.setBorder(BorderFactory.createLineBorder(new Color(255, 0, 0, 200), 2));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBorder(null);
                }
            });

            button.addActionListener(e -> {
                isMuted = !isMuted;
                if (isMuted){ SoundManager.stop("start"); } else { SoundManager.playLoop("start", 0.3f); }
                button.repaint();
            });

            return button;
        }

        private JButton createStartButton() {
            JButton button = new JButton() {
                private boolean isHovered = false;

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // 배경
                    if (isHovered) {
                        g2d.setColor(new Color(200, 0, 0, 220));
                    } else {
                        g2d.setColor(new Color(180, 0, 0, 200));
                    }
                    g2d.fillRect(0, 0, getWidth(), getHeight());

                    // 테두리
                    g2d.setColor(new Color(255, 100, 100));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRect(1, 1, getWidth() - 2, getHeight() - 2);

                    // Glow 효과 (hover 시)
                    if (isHovered) {
                        g2d.setColor(new Color(255, 0, 0, 50));
                        g2d.setStroke(new BasicStroke(8));
                        g2d.drawRect(-3, -3, getWidth() + 6, getHeight() + 6);
                    }

                    // 텍스트
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
                    FontMetrics fm = g2d.getFontMetrics();
                    String text = "▶ 게임 시작";
                    int textWidth = fm.stringWidth(text);
                    int textX = (getWidth() - textWidth) / 2;
                    int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

                    // 텍스트 그림자
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(text, textX + 2, textY + 2);

                    g2d.setColor(Color.WHITE);
                    g2d.drawString(text, textX, textY);
                }
            };

            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setOpaque(false);

            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    ((JButton) e.getSource()).putClientProperty("isHovered", true);
                    button.repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    ((JButton) e.getSource()).putClientProperty("isHovered", false);
                    button.repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    Rectangle bounds = button.getBounds();
                    button.setBounds(bounds.x + 2, bounds.y + 2, bounds.width - 4, bounds.height - 4);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    Rectangle bounds = button.getBounds();
                    button.setBounds(bounds.x - 2, bounds.y - 2, bounds.width + 4, bounds.height + 4);
                }
            });
            

         // 메뉴의 "게임 시작" 버튼 -> 조작 화면(HowToPlay)으로 진입
            button.addActionListener(e -> {
                // 1. 메인 화면 BGM 정지
                SoundManager.stop("start");
                
                
                // 2. 💡 showStageIntro(1) 대신 showHowToPlay() 호출로 변경
                showHowToPlay(); // 👈 조작 화면으로 먼저 진입합니다.
            });

            return button;
        }
    }
    

    /* ===================== IngamePanel ===================== */
    class IngamePanel extends JPanel implements KeyListener, Runnable {
    	private NoNextFloorGame mainGame;
    	private boolean isGameOver = false;
    	private Image gameOverImage;
    	private Rectangle btnYesRect = new Rectangle(500, 400, 120, 50);
    	private Rectangle btnNoRect = new Rectangle(650, 400, 120, 50);
    	


    	// 무기 시스템
    	private Rectangle lastCollidedWall = null;
    	private enum Weapon { PISTOL, SHOTGUN }
    	private Weapon currentWeapon = Weapon.PISTOL;
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
        private final long shootCooldown = 200_000_000L; // 0.2초
        private Image fullHeartImage;
        private Image uiPlayerIcon;
        private Image uiZombieIcon;
        private Image uiHeartFull;
        private Image uiHeartEmpty;
        private JButton homeButton;
        private OptionsPanel optionsPanel;  // options 패널 참조
        private BossZombie bossZombie;
        private int visionRadius = 150; // 주인공 주변 밝게 보이는 범위

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
         // IngamePanel 생성자 내부 (수정된 부분)

            SoundManager.stop("start"); // 메뉴 BGM 정지

            // 👇 [수정] BGM 재생 로직 전체를 if 블록 { } 안에 묶습니다.
            if (!mainGame.isMuted()) {
                
                // 1. 베이스 BGM (기존 코드에서는 삭제됨)
                // SoundManager.playLoop("bgm", 0.2f);
                
                // 2. 스테이지별 BGM 및 베이스 BGM(bgm)을 통합하여 재생
                switch(stage) {
                    case 1 -> {
                        //SoundManager.playLoop("bgm", 0.03f); // 👈 bgm.wav 추가
                        SoundManager.playLoop("game_bg", 0.03f);
                    }
                    case 2 -> {
                       // SoundManager.playLoop("bgm", 0.03f); // 👈 bgm.wav 추가
                        SoundManager.playLoop("game_bg2", 0.15f);
                    }
                    case 3 -> {
                       // SoundManager.playLoop("bgm", 0.03f); // 👈 bgm.wav 추가
                        SoundManager.playLoop("game_bg3", 0.15f);
                    }
                }
                
                // 3. 스테이지별 좀비 BGM
                switch(stage) {
                    case 1 -> SoundManager.playLoop("zombie1", 0.2f);
                    case 2, 3 -> SoundManager.playLoop("zombie23", 0.2f);
                }
            } 
            // 👆 if 블록 닫힘. (생성자는 아직 닫히지 않았습니다.)
// ...
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
            	        if (btnYesRect.contains(mx, my)) {
            	            // YES → 현재 스테이지에서 부활
            	            isGameOver = false;
            	            player.hp = 3;  // 초기 체력으로 복원
            	            bullets.clear();
            	            zombies.clear();

            	            // 좀비 다시 스폰 (스테이지 초기화)
            	            int idx = stageForThisPanel - 1;
            	            if (idx >= 0 && idx < stageZombies.length) {
            	                for (int[] pos : stageZombies[idx]) {
            	                    zombies.add(new Zombie(pos[0], pos[1]));
            	                }
            	            }
            	            if(stageForThisPanel == 3 && bossZombie != null) {
            	                bossZombie = new BossZombie(800, 400);
            	            }

            	            lastShootTime = 0;
            	            isRunning = true;
            	            gameThread = new Thread(IngamePanel.this); // IngamePanel의 run() 사용
            	            gameThread.start();
            	            SoundManager.stop("gameover"); 
            	            if (!mainGame.isMuted())
            	            { 
                                // 🌟 [수정된 부분]: SoundManager.playLoop("bgm", 0.2f); 라인만 삭제 🌟
            	                switch(stageForThisPanel) 
            	                { 
            	                    case 1 -> SoundManager.playLoop("game_bg", 0.15f); 
            	                    case 2 -> SoundManager.playLoop("game_bg2", 0.15f); 
            	                    case 3 -> SoundManager.playLoop("game_bg3", 0.15f); 
            	                } 
            	                switch(stageForThisPanel) { 
            	                    case 1 -> SoundManager.playLoop("zombie1", 0.2f); 
            	                    case 2, 3 -> SoundManager.playLoop("zombie23", 0.2f); 
            	                } 
            	            }
            	            
            	            repaint();
            	            return;
            	        }

            	        if (btnNoRect.contains(mx, my)) {
            	        
            	            // NO → 메인 메뉴로 돌아가기
            	        	SoundManager.stop("gameover"); SoundManager.stop("bgm"); SoundManager.stop("game_bg"); SoundManager.stop("game_bg2"); SoundManager.stop("game_bg3"); SoundManager.stop("zombie1"); SoundManager.stop("zombie23");
            	            NoNextFloorGame.this.remove(IngamePanel.this);
            	            currentIngamePanel = null;

            	            NoNextFloorGame.this.gamePanel = new GamePanel();
            	            NoNextFloorGame.this.add(gamePanel);
            	            gamePanel.requestFocusInWindow();
            	            NoNextFloorGame.this.revalidate();
            	            NoNextFloorGame.this.repaint();
            	            return;
            	        }
            	    }

            	    // 게임 중 발사 처리 (기존 코드 유지)
            	    int mouseX = e.getX();
            	    int mouseY = e.getY();
            	   // shootBullet(mouseX, mouseY);
            	//사운드 중복 방지할려고 주석처리했어요



                    // 슬롯 클릭 우선 처리
                    if (pistolSlotRect != null && pistolSlotRect.contains(mx, my)) {
                        currentWeapon = Weapon.PISTOL;
                        System.out.println("PISTOL selected");
                        return;
                    }
                    if (shotgunSlotRect != null && shotgunSlotRect.contains(mx, my)) {
                        currentWeapon = Weapon.SHOTGUN;
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
            SoundManager.stop("bgm"); SoundManager.stop("game_bg"); SoundManager.stop("game_bg2"); SoundManager.stop("game_bg3"); SoundManager.stop("zombie1"); SoundManager.stop("zombie23"); if (!mainGame.isMuted()) SoundManager.play("gameover", 1.0f);

            // 버튼 클릭을 받기 위해 포커스 가져오기
            requestFocusInWindow();
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
                optionsPanel = new OptionsPanel(
                    isMuted,
                    // 게임 재개
                    () -> {
                        remove(optionsPanel);
                        optionsPanel = null;
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

     // showOptions() 메서드와 addHomeButton() 메서드 사이에 위치하거나,
        // IngamePanel의 모든 필드, 생성자, 그리고 기본 메서드(run, paintComponent 등)가 끝난 후 추가합니다.

        /* ===================== shootBullet (마우스 클릭 발사 로직) ===================== */
        private void shootBullet(int mx, int my) {
            long now = System.nanoTime();
            
            // 쿨다운 체크
            long cooldown = (currentWeapon == Weapon.PISTOL) ? PISTOL_COOLDOWN : SHOTGUN_COOLDOWN;
            if (now - lastShootTime < cooldown) return;
            
            // 플레이어 중심 좌표
            int centerX = player.x + player.playerWidth / 2;
            int centerY = player.y + player.playerHeight / 2;
            
            // 🌟 무기 SFX 재생
            if (!mainGame.isMuted()) {
                if(currentWeapon == Weapon.PISTOL) SoundManager.play("pistol", 0.7f);
                else if(currentWeapon == Weapon.SHOTGUN) SoundManager.play("shotgun", 0.7f);
            }
            
            if (currentWeapon == Weapon.PISTOL) {
                if (NoNextFloorGame.this.pistolAmmo <= 0) return;

                NoNextFloorGame.this.pistolAmmo--;

                double angle = Math.atan2(my - centerY, mx - centerX);
                int pistolLife = 120; // 권총 사거리 제한 (이동 로직에 따라 조정)
                bullets.add(new Bullet(centerX, centerY, angle, pistolLife));

            } else if (currentWeapon == Weapon.SHOTGUN) {
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
        // 이 메서드는 btnYesRect 클릭 시 이미 로직이 구현되었지만, 필요 시 정의해 둡니다.
     // IngamePanel.java 내 revivePlayer() 메서드 (기존)
     // IngamePanel.java 내 revivePlayer() 메서드 (수정됨)
        private void revivePlayer() { 
            // *** 부활 게임 로직 (체력, 위치, 쿨다운 등 초기화) ***
            
            // 🌟 BGM 재시작
        	if (!mainGame.isMuted()) {
                SoundManager.stop("gameover");
                
                // 1. 공통 BGM (볼륨 0.2f -> 0.1f로 낮춤)
               // SoundManager.playLoop("bgm", 0.1f); 
                
                // 2. 스테이지별 BGM (볼륨 0.15f -> 0.08f로 낮춤, 파일명은 기존 유지)
                switch(stageForThisPanel) {
                    case 1 -> SoundManager.playLoop("game_bg", 0.03f); 
                    case 2 -> SoundManager.playLoop("game_bg2", 0.08f);
                    case 3 -> SoundManager.playLoop("game_bg3", 0.08f);
                }
                
                // 3. 좀비 사운드는 1F, 2F, 3F 스테이지 BGM이 재생 중일 때만 나오도록 조건 변경 
                //    (game_bg1, game_bg2, game_bg3에 해당한다고 가정)
                //    좀비 사운드 자체의 볼륨은 0.4f로 유지합니다.
                //    요청하신 조건이 '이 BGM에서만 나오게' 이므로, 모든 BGM에 조건이 걸리도록 유지합니다.
                switch(stageForThisPanel) {
                    case 1 -> SoundManager.playLoop("zombie1", 0.2f);
                    case 2, 3 -> SoundManager.playLoop("zombie23", 0.2f);
                }
            }
        }

           private void addHomeButton() {
               homeButton = new JButton();
               try {
                   ImageIcon icon = new ImageIcon("images/home.png");
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
                   homeButton.setBounds(getWidth() - 70, 20, 50, 50);
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

               // 배경 그리기
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
                   // 1️⃣ gameOverImage를 화면 전체로 확대
                   if (gameOverImage != null) {
                       g.drawImage(gameOverImage, 0, 0, getWidth(), getHeight(), null);
                   }
           // ... (나머지 로직 유지) ...
           

                   // 2️⃣ YES 버튼 (배경 흰색)
                   g.setColor(Color.WHITE);
                   g.fillRect(btnYesRect.x, btnYesRect.y, btnYesRect.width, btnYesRect.height);
                   g.setColor(Color.BLACK);
                   g.setFont(new Font("Arial", Font.BOLD, 30));
                   g.drawString("YES", btnYesRect.x + 30, btnYesRect.y + 35);

                   // 3️⃣ NO 버튼 (배경 흰색)
                   g.setColor(Color.WHITE);
                   g.fillRect(btnNoRect.x, btnNoRect.y, btnNoRect.width, btnNoRect.height);
                   g.setColor(Color.BLACK);
                   g.drawString("NO", btnNoRect.x + 40, btnNoRect.y + 35);
                   return;
               }
           
               // 우측 텍스트 추가
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
                   int slotSize = 48;
                   int margin = 20;
                   int slotX = getWidth() - margin - slotSize;
                   int slotY = getHeight() - margin - slotSize;

                   // 피스톨 슬롯 (오른쪽)
                   if (pistolSlotRect != null) {
                       // 배경
                       g.setColor(new Color(0, 0, 0, 120));
                       g.fillRect(pistolSlotRect.x, pistolSlotRect.y, pistolSlotRect.width, pistolSlotRect.height);
                       // 아이콘
                       if (pistolIcon != null) g.drawImage(pistolIcon, pistolSlotRect.x + 4, pistolSlotRect.y + 4, pistolSlotRect.width - 8, pistolSlotRect.height - 8, null);
                       // 선택 테두리
                       if (currentWeapon == Weapon.PISTOL) {
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
                       if (currentWeapon == Weapon.SHOTGUN) {
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
               if (player == null) return;
               int keyCode = e.getKeyCode();
               
               int newX = player.x;
               int newY = player.y;
               // Player 클래스에 public int speed; 변수가 있다고 가정
               int speed = player.speed; 

               // 1. 다음 이동 위치 계산
               if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
                   newX -= speed;
                   player.angle = Math.PI; // 방향 설정
               } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
                   newX += speed;
                   player.angle = 0; // 방향 설정
               } else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
                   newY -= speed;
                   player.angle = -Math.PI / 2.0; // 방향 설정
               } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
                   newY += speed;
                   player.angle = Math.PI / 2.0; // 방향 설정
               }
               

               // 2. 다음 위치의 충돌 박스 생성 (Player 클래스의 크기 사용)
               // *주의: Player 클래스에 playerWidth, playerHeight가 정의되어 있어야 합니다.*
               Rectangle nextPos = new Rectangle(newX, newY, player.playerWidth, player.playerHeight);

               // 3. 📌 충돌 체크: 충돌하지 않을 때만 위치 업데이트
               if (!checkCollision(nextPos)) {
                   player.x = newX;
                   player.y = newY;
               }
               if (player.hp <= 0) {
                   onPlayerDied();
                   return;
               }
               
               // 4. 발사 키 처리
               if (keyCode == KeyEvent.VK_SPACE) shootBulletForward();
           }
           @Override public void keyReleased(KeyEvent e) {}

           // 전방 총알 발사 (스페이스바)
           private void shootBulletForward() {
               long now = System.nanoTime();
               long cooldown = (currentWeapon == Weapon.PISTOL) ? PISTOL_COOLDOWN : SHOTGUN_COOLDOWN;
               if (now - lastShootTime < cooldown) return;

               // 탄약 체크 
               if (currentWeapon == Weapon.PISTOL && NoNextFloorGame.this.pistolAmmo <= 0) {
                   System.out.println("피스톨 탄약 떨어짐");
                   return;
               }
               if (currentWeapon == Weapon.SHOTGUN && NoNextFloorGame.this.shotgunAmmo <= 0) {
                   System.out.println("샷건 탄약 떨어짐");
                   return;
               }
               
               // 🌟 무기 SFX 재생
               if (!mainGame.isMuted()) {
                   if(currentWeapon == Weapon.PISTOL) SoundManager.play("pistol", 0.7f);
                   else if(currentWeapon == Weapon.SHOTGUN) SoundManager.play("shotgun", 0.7f);
               }

               lastShootTime = now;
               int bulletX = player.x + player.playerWidth / 2;
               int bulletY = player.y + player.playerHeight / 2;

               if (currentWeapon == Weapon.PISTOL) {
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
       	    lastCollidedWall = null;  // 매 프레임 초기화

       	    for (Rectangle wall : wallRects) {
       	        if (nextPos.intersects(wall)) {
       	            lastCollidedWall = wall; // 어떤 벽에 닿았는지 기록
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
               if (player == null || zombies == null) return;

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
                                    isGameOver = true;
                                    isRunning = false;   // 게임 스레드 정지

                                    // 🌟 [핵심 수정]: 게임 오버 시 모든 BGM 정지 및 gameover.wav 재생 🌟
                                    if (!mainGame.isMuted()) {
                                        // 모든 인게임 BGM 정지 (배경음, 좀비 소리)
                                        SoundManager.stop("game_bg");
                                        SoundManager.stop("game_bg2");
                                        SoundManager.stop("game_bg3");
                                        SoundManager.stop("zombie1");
                                        SoundManager.stop("zombie23");
                                        SoundManager.stop("bgm"); 
                                        
                                        // gameover.wav 재생
                                        SoundManager.stop("gameover"); // 혹시 재생 중이라면 멈추고
                                        SoundManager.play("gameover", 1.0f); // 다시 재생
                                    }
                                    // ----------------------------------------------------------------------
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
