package zombiegame;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;

public class Player {
    public int x, y;
    public int hp;
    int speed;

    private Image playerImage;
    private Image damagedImage;

    public int playerWidth;
    public int playerHeight;
    public double scale = 0.1; // 원본 1/10 크기
    public double angle;
    private int lastDx = 1;
    private int lastDy = 0;

    // 벽 충돌용
    private ArrayList<Rectangle> walls;

    // 데미지 상태
    private boolean isDamaged = false;
    private long damagedStartTime = 0;
    private final long damagedDuration = 1000; // 1초

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.speed = 2;
        this.hp = 3;
        this.angle = 0;

        try {
            File normalFile = new File("images/player.png");
            File damagedFile = new File("images/damaged_player.png");

            if (normalFile.exists()) playerImage = ImageIO.read(normalFile);
            if (damagedFile.exists()) damagedImage = ImageIO.read(damagedFile);

            // Java 21 호환: null 안전성 강화 및 이미지 크기 가져오기
            if (playerImage != null) {
                int originalWidth = playerImage.getWidth(null);
                int originalHeight = playerImage.getHeight(null);
                if (originalWidth > 0 && originalHeight > 0) {
                    this.playerWidth = (int) (originalWidth * scale);
                    this.playerHeight = (int) (originalHeight * scale);
                } else {
                    // 이미지가 아직 로드되지 않은 경우 기본값 사용
                    this.playerWidth = (int) (30 * scale);
                    this.playerHeight = (int) (40 * scale);
                }
            } else {
                // 이미지 로드 실패 시 기본값 사용
                this.playerWidth = (int) (30 * scale);
                this.playerHeight = (int) (40 * scale);
            }

        } catch (IOException e) {
            System.out.println("플레이어 이미지 로드 실패: " + e.getMessage());
            playerImage = null;
            damagedImage = null;
            this.playerWidth = (int) (30 * scale);
            this.playerHeight = (int) (40 * scale);
        }
    }

    // 벽 정보 세팅
    public void setWalls(ArrayList<Rectangle> walls) {
        this.walls = walls;
    }

    // 이동 가능 여부 체크
    private boolean canMove(int nextX, int nextY) {
        Rectangle nextPos = new Rectangle(nextX, nextY, playerWidth, playerHeight);
        if (walls == null) return true; // 아직 세팅 안됨
        for (Rectangle wall : walls) {
            if (nextPos.intersects(wall)) return false;
        }
        return true;
    }

    public void takeDamage(int amount) {
        hp -= amount;
        if (hp < 0) hp = 0;

        isDamaged = true;
        damagedStartTime = System.currentTimeMillis();
    }

    public void draw(Graphics g) {
        // 데미지 시간 체크
        if (isDamaged) {
            long now = System.currentTimeMillis();
            if (now - damagedStartTime > damagedDuration) {
                isDamaged = false;
            }
        }

        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();
        g2d.rotate(angle, x + playerWidth / 2.0, y + playerHeight / 2.0);

        Image usingImage = isDamaged && damagedImage != null ? damagedImage : playerImage;

        if (usingImage != null) {
            g2d.drawImage(usingImage, x, y, playerWidth, playerHeight, null);
        } else {
            g2d.setColor(Color.RED);
            g2d.fillRect(x, y, playerWidth, playerHeight);
        }

        g2d.setTransform(oldTransform);
    }

    // ------------------ 이동 메서드 (벽 충돌 적용) ------------------
    public void moveLeft() {
        int nextX = x - speed;
        if (canMove(nextX, y)) { x = nextX; lastDx = -1; lastDy = 0; updateAngle(); }
    }

    public void moveRight() {
        int nextX = x + speed;
        if (canMove(nextX, y)) { x = nextX; lastDx = 1; lastDy = 0; updateAngle(); }
    }

    public void moveUp() {
        int nextY = y - speed;
        if (canMove(x, nextY)) { y = nextY; lastDx = 0; lastDy = -1; updateAngle(); }
    }

    public void moveDown() {
        int nextY = y + speed;
        if (canMove(x, nextY)) { y = nextY; lastDx = 0; lastDy = 1; updateAngle(); }
    }

    private void updateAngle() {
        angle = Math.atan2(lastDy, lastDx);
    }

    public void aimAt(int targetX, int targetY) {
        int dx = targetX - (x + playerWidth / 2);
        int dy = targetY - (y + playerHeight / 2);
        angle = Math.atan2(dy, dx);
    }

    // 플레이어 중심 좌표 반환
    public int getCenterX() { return x + playerWidth / 2; }
    public int getCenterY() { return y + playerHeight / 2; }
}
