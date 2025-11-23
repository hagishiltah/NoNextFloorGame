package zombiegame;

import java.awt.Color;
import java.awt.Graphics;

public class Bullet {
    public int x;
    public int y;
    public double vx;
    public double vy;
    public double angle;

    // 수명(프레임 단위)
    public int life = 0;
    public int maxLife = Integer.MAX_VALUE; // 기본: 무제한

    public Bullet(int startX, int startY, double angle) {
        this.x = startX;
        this.y = startY;
        this.angle = angle;

        double speed = 10;
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;

        this.maxLife = Integer.MAX_VALUE;
        this.life = 0;
    }

    // 수명 지정 생성자 (샷건용 짧은 수명 등)
    public Bullet(int startX, int startY, double angle, int maxLife) {
        this(startX, startY, angle);
        this.maxLife = maxLife;
    }

    // 업데이트: 위치 갱신 및 life 증가
    public void update() {
        x += vx;
        y += vy;
        life++;
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillOval(x - 5, y - 5, 10, 10);
    }

    // 화면 밖으로 나갔거나 수명이 다했는지 체크
    public boolean isExpired(int width, int height) {
        if (x < -50 || x > width + 50 || y < -50 || y > height + 50) return true;
        if (life > maxLife) return true;
        return false;
    }
}
