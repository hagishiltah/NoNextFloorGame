package zombiegame;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;

public class BossZombie {

    public int x, y;
    public int hp;
    public int maxHp;
    public int zombieWidth;
    public int zombieHeight;
    public boolean isDetected = false;

    private double speed = 1.1;
    private double scale = 0.15; // 보스는 크게

    private Image zombieImage;
    private Image damagedImage;
    private Image fullHeartImage;

    private int CHASE_RADIUS = 350; // 보스는 더 멀리서 추적
    private long attackCooldown = 1_500_000_000L;
    private long lastAttackTime = 0;

    private boolean isDamaged = false;
    private long damagedStartTime = 0;
    private final long damagedDuration = 200;

    public boolean isDead = false;
    public double angle = 0;
 // BossZombie.java 내부

    public void setDetected(boolean detected) {
        // 한 번 true가 되면 다시 false로 바뀌지 않도록 처리
        if (detected) {
            this.isDetected = true;
        }
    }

    // ------------------- 벽 충돌용 -------------------
    private ArrayList<Rectangle> walls;

    public BossZombie(int startX, int startY) {
        this.x = startX;
        this.y = startY;

        this.hp = 9;
        this.maxHp = 9;

        try {
            zombieImage = ImageIO.read(new File("images/boss_zombie.png"));
            damagedImage = ImageIO.read(new File("images/damaged_boss_zombie.png"));

            // Java 21 호환: null 안전성 강화 및 이미지 크기 가져오기
            if (zombieImage != null) {
                int originalWidth = zombieImage.getWidth(null);
                int originalHeight = zombieImage.getHeight(null);
                if (originalWidth > 0 && originalHeight > 0) {
                    zombieWidth = (int) (originalWidth * scale);
                    zombieHeight = (int) (originalHeight * scale);
                } else {
                    // 이미지가 아직 로드되지 않은 경우 기본값 사용
                    zombieWidth = 30;
                    zombieHeight = 40;
                }
            } else {
                // 이미지 로드 실패 시 기본값 사용
                zombieWidth = 30;
                zombieHeight = 40;
            }

        } catch (IOException e) {
            System.out.println("보스 이미지 로드 실패: " + e.getMessage());
            zombieWidth = 30;
            zombieHeight = 40;
        }

        try {
            fullHeartImage = ImageIO.read(new File("images/heart_full.png"));
        } catch (IOException e) {
            System.out.println("하트 이미지 로드 실패: " + e.getMessage());
        }
    }

    // ------------------- 벽 정보 세팅 -------------------
    public void setWalls(ArrayList<Rectangle> walls) {
        this.walls = walls;
    }

    // ------------------- 이동 가능 여부 체크 -------------------
    private boolean canMove(int nextX, int nextY) {
        Rectangle nextPos = new Rectangle(nextX, nextY, zombieWidth, zombieHeight);
        if (walls == null) return true; // 벽 정보 없으면 이동 가능
        for (Rectangle wall : walls) {
            if (nextPos.intersects(wall)) return false;
        }
        return true;
    }

    public void takeDamage(int amount) {
        if (isDead) return;

        hp -= amount;
        if (hp <= 0) {
            hp = 0;
            isDead = true;
        }

        isDamaged = true;
        damagedStartTime = System.currentTimeMillis();
    }

 // BossZombie.java 내부 (update(Player player) 메서드)

    public void update(Player player) {
        if (isDead) return;

        double distance = Math.hypot(player.x - x, player.y - y);

        // 1. 플레이어를 바라보는 각도 계산 (이전 단계에서 추가되었다고 가정)
        int centerX = x + zombieWidth / 2;
        int centerY = y + zombieHeight / 2;
        int pCenterX = player.x + player.playerWidth / 2;
        int pCenterY = player.y + player.playerHeight / 2;
        this.angle = Math.atan2(pCenterY - centerY, pCenterX - centerX);


        if (distance < CHASE_RADIUS) {
            
            // 🌟 [수정]: 각도를 사용하여 X, Y 이동 거리(dx, dy)를 계산 🌟
            // 보스의 speed와 angle을 이용하여 X, Y 방향으로 가야 할 거리를 구합니다.
            double dx = speed * Math.cos(angle);
            double dy = speed * Math.sin(angle);

            int targetX = x;
            int targetY = y;
            
            // 🌟 [수정]: X축 이동과 Y축 이동을 독립적으로 검사 (Sliding Collision) 🌟
            
            // X축 이동 시도
            int nextX_only = (int) (x + dx);
            if (canMove(nextX_only, y)) {
                targetX = nextX_only; // X축으로 이동 가능하면 이동
            }

            // Y축 이동 시도
            int nextY_only = (int) (y + dy);
            if (canMove(x, nextY_only)) {
                targetY = nextY_only; // Y축으로 이동 가능하면 이동
            }
            
            // 최종 위치 업데이트
            x = targetX;
            y = targetY;
        }

        // 2. 순간 돌진 로직 (기존 로직 유지하며 충돌 체크를 적용)
        if (distance < 200 && Math.random() < 0.01) {
            int dashX = x;
            int dashY = y;

            int dashSpeed = 5; 
            
            // 플레이어를 향해 돌진할 방향 결정
            if (player.x > x) dashX = (int) (x + dashSpeed);
            else dashX = (int) (x - dashSpeed);

            if (player.y > y) dashY = (int) (y + dashSpeed);
            else dashY = (int) (y - dashSpeed);
            
            // 🌟 [수정]: 돌진 시에도 벽 충돌을 정확히 검사 🌟
            if (canMove(dashX, y)) x = dashX;
            if (canMove(x, dashY)) y = dashY;
        }
    }

    public boolean canAttack() {
        return (System.nanoTime() > lastAttackTime + attackCooldown);
    }

    public void recordAttack() {
        lastAttackTime = System.nanoTime();
    }

    public void draw(Graphics g) {
        if (isDead) return;

        if (isDamaged) {
            long now = System.currentTimeMillis();
            if (now - damagedStartTime > damagedDuration) {
                isDamaged = false;
            }
        }

Image img = isDamaged ? damagedImage : zombieImage;
        
        // 🌟 [핵심 수정]: Graphics2D를 사용하여 회전 적용 시작 🌟
        Graphics2D g2d = (Graphics2D) g;
        
        // 1. 현재 Graphics2D의 상태(변환 정보)를 저장합니다.
        // (체력바를 회전시키지 않기 위해 필요)
        java.awt.geom.AffineTransform old = g2d.getTransform(); 
        
        // 2. 보스의 중심점을 계산합니다.
        int centerX = x + zombieWidth / 2;
        int centerY = y + zombieHeight / 2;

        // 3. 중심점을 기준으로 angle만큼 이미지를 회전시킵니다.
        // angle은 update() 메서드에서 플레이어를 향하도록 계산했습니다.
        g2d.rotate(angle, centerX, centerY);

        // 4. 회전된 이미지를 그립니다.
        g2d.drawImage(img, x, y, zombieWidth, zombieHeight, null);
        
        // 5. 회전된 상태를 해제하고 이전 상태로 복구합니다.
        g2d.setTransform(old);
        // ----------------------------------------------------
        
        drawHearts(g);
    }
    private void drawHearts(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        int heartSize = 14;
        int heartX = x + zombieWidth + 5;
        int heartY = y;

        for (int i = 0; i < hp; i++) {
            g2d.drawImage(fullHeartImage, heartX, heartY + (i * (heartSize + 4)),
                    heartSize, heartSize, null);
        }
    }
}
