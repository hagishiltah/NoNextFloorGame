package zombiegame;

import java.awt.*;
import java.awt.geom.AffineTransform; // ★ 추가
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Zombie {
    public int x, y;
    public int hp;
    public int maxHp;
    public int zombieWidth;
    public int zombieHeight;

    private int speed;
    private double scale = 0.1; // 크기 조절

    private Image zombieImage;
    private Image damagedImage;
    private Image fullHeartImage;

    private int CHASE_RADIUS = 800; // 추격 범위 (좀 넓게 잡음)
    private long attackCooldown = 1_000_000_000L; // 1초
    private long lastAttackTime = 0;

    private boolean isDamaged = false;
    private long damagedStartTime = 0;
    private final long damagedDuration = 500;
    
    // ★ 추가: 좀비가 바라보는 각도
    public double angle; 

    public Zombie(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.speed = 1; // 속도 조절
        this.hp = 3;
        this.maxHp = 3;

        try {
            zombieImage = ImageIO.read(new File("images/zombie.png"));
            damagedImage = ImageIO.read(new File("images/damaged_zombie.png"));
            
            // 이미지 크기 설정
            int originalWidth = zombieImage.getWidth(null);
            int originalHeight = zombieImage.getHeight(null);
            zombieWidth = (int) (originalWidth * scale);
            zombieHeight = (int) (originalHeight * scale);
            
        } catch (IOException e) {
            System.out.println("좀비 이미지 로드 실패: " + e.getMessage());
            zombieWidth = 40;
            zombieHeight = 40;
        }

        try {
            fullHeartImage = ImageIO.read(new File("images/heart_full.png"));
        } catch (IOException e) {}
    }

    public void takeDamage(int amount) {
        hp -= amount;
        if (hp < 0) hp = 0;
        isDamaged = true;
        damagedStartTime = System.currentTimeMillis();
    }

    // ★ 수정됨: 회전하여 그리기
    public void draw(Graphics g) {
        if (isDamaged && System.currentTimeMillis() - damagedStartTime > damagedDuration) {
            isDamaged = false;
        }

        Image usingImage = isDamaged && damagedImage != null ? damagedImage : zombieImage;
        
        Graphics2D g2d = (Graphics2D) g;
        
        // 현재 상태 저장
        AffineTransform old = g2d.getTransform(); 
        
        // 좀비의 중심점
        int centerX = x + zombieWidth / 2;
        int centerY = y + zombieHeight / 2;

        // 회전 적용
        g2d.rotate(angle, centerX, centerY);

        if (usingImage != null) {
            g2d.drawImage(usingImage, x, y, zombieWidth, zombieHeight, null);
        } else {
            g.setColor(Color.GREEN);
            g.fillRect(x, y, zombieWidth, zombieHeight);
        }
        
        // 회전 복구 (체력바는 회전하면 안 되니까)
        g2d.setTransform(old);

        drawHearts(g);
    }

    private void drawHearts(Graphics g) {
        // 체력바 그리기 (기존 로직 유지)
        Graphics2D g2d = (Graphics2D) g;
        int heartSize = 12;
        int heartX = x + (zombieWidth - (hp * 14)) / 2; // 중앙 정렬
        int heartY = y - 15;

        for (int i = 0; i < hp; i++) {
            if (fullHeartImage != null) {
                g2d.drawImage(fullHeartImage, heartX + (i * 14), heartY, heartSize, heartSize, null);
            } else {
                g2d.setColor(Color.RED);
                g2d.fillOval(heartX + (i * 14), heartY, heartSize, heartSize);
            }
        }
    }

    // ★ 수정됨: 플레이어를 바라보는 각도 계산 및 이동
    public void update(Player player) {
        double distance = Math.hypot(player.x - x, player.y - y);
        
        // 플레이어를 바라보는 각도 계산 (항상 바라봄)
        int centerX = x + zombieWidth / 2;
        int centerY = y + zombieHeight / 2;
        int pCenterX = player.x + player.playerWidth / 2;
        int pCenterY = player.y + player.playerHeight / 2;
        
        this.angle = Math.atan2(pCenterY - centerY, pCenterX - centerX);

        // 추격 범위 내라면 이동
        if (distance < CHASE_RADIUS) {
            if (player.x > x) x += speed;
            else if (player.x < x) x -= speed;

            if (player.y > y) y += speed;
            else if (player.y < y) y -= speed;
        }
    }

    public boolean canAttack() {
        return (System.nanoTime() > lastAttackTime + attackCooldown);
    }

    public void recordAttack() {
        lastAttackTime = System.nanoTime();
    }
}