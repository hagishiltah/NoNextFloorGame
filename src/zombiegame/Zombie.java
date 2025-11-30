package zombiegame;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List; // 🌟 [필수] List 임포트 추가

public class Zombie {
    public int x, y;
    public int hp;
    public int maxHp;
    public int zombieWidth;
    public int zombieHeight;
    public double angle; 

    protected double speed;
    protected double scale = 0.1; 
    
    protected Image zombieImage;
    protected Image damagedImage;
    protected Image fullHeartImage;

    protected int CHASE_RADIUS = 800;
    protected long attackCooldown = 1_000_000_000L;
    protected long lastAttackTime = 0;

    protected boolean isDamaged = false;
    protected long damagedStartTime = 0;
    protected final long damagedDuration = 500;
    
    // 🌟 [수정 1] ArrayList -> List로 변경 (더 넓은 범위 수용)
    protected List<Rectangle> walls; 

    public Zombie(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.speed = 1; 
        this.hp = 3;
        this.maxHp = 3;

        try {
            zombieImage = ImageIO.read(new File("images/zombie.png"));
            damagedImage = ImageIO.read(new File("images/damaged_zombie.png"));
            if (zombieImage != null) {
                int originalWidth = zombieImage.getWidth(null);
                int originalHeight = zombieImage.getHeight(null);
                if (originalWidth > 0 && originalHeight > 0) {
                    zombieWidth = (int) (originalWidth * scale);
                    zombieHeight = (int) (originalHeight * scale);
                } else {
                    zombieWidth = 40; zombieHeight = 40;
                }
            } else {
                zombieWidth = 40; zombieHeight = 40;
            }
        } catch (IOException e) {
            zombieWidth = 40; zombieHeight = 40;
        }

        try {
            fullHeartImage = ImageIO.read(new File("images/heart_full.png"));
        } catch (IOException e) {}
    }

    // 🌟 [수정 2] 파라미터도 List로 변경 (여기가 핵심!)
    // IngamePanel의 wallRects가 ArrayList든 List든 모두 받을 수 있게 됩니다.
    public void setWalls(List<Rectangle> walls) {
        this.walls = walls;
    }

    // 🌟 [수정 3] 충돌 체크 로직 (List도 for문 사용 가능)
    protected boolean canMove(int nextX, int nextY) {
        Rectangle nextPos = new Rectangle(nextX, nextY, zombieWidth, zombieHeight);
        
        // 벽 정보가 없으면 통과
        if (walls == null) return true; 
        
        for (Rectangle wall : walls) {
            if (nextPos.intersects(wall)) return false;
        }
        return true;
    }

    // ... (이하 takeDamage, draw, update 등 기존 코드 그대로 유지) ...

    public void takeDamage(int amount) {
        hp -= amount;
        if (hp < 0) hp = 0;
        isDamaged = true;
        damagedStartTime = System.currentTimeMillis();
    }

    public void draw(Graphics g) {
        if (isDamaged && System.currentTimeMillis() - damagedStartTime > damagedDuration) {
            isDamaged = false;
        }

        Image usingImage = isDamaged && damagedImage != null ? damagedImage : zombieImage;
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform old = g2d.getTransform(); 
        
        int centerX = x + zombieWidth / 2;
        int centerY = y + zombieHeight / 2;

        g2d.rotate(angle, centerX, centerY);

        if (usingImage != null) {
            g2d.drawImage(usingImage, x, y, zombieWidth, zombieHeight, null);
        } else {
            g.setColor(Color.GREEN);
            g.fillRect(x, y, zombieWidth, zombieHeight);
        }
        g2d.setTransform(old);
        drawHearts(g);
    }
    
    protected void drawHearts(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int heartSize = 12;
        int heartX = x + (zombieWidth - (hp * 14)) / 2; 
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

    public void update(Player player) {
        double distance = Math.hypot(player.x - x, player.y - y);
        int centerX = x + zombieWidth / 2;
        int centerY = y + zombieHeight / 2;
        int pCenterX = player.x + player.playerWidth / 2;
        int pCenterY = player.y + player.playerHeight / 2;
        this.angle = Math.atan2(pCenterY - centerY, pCenterX - centerX);

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