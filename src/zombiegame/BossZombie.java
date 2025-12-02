package zombiegame;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

// 상속
public class BossZombie extends Zombie {

    public boolean isDetected = false; // 보스 전용 변수

    // 1️⃣ [추가] 소수점 단위 이동을 기억할 정밀 좌표 변수
    private double preciseX;
    private double preciseY;
    
    // 속도 변화 관련 변수
    private double baseSpeed = 1.1;  // 기본 속도
    private double slowSpeed = 0.7;  // 느린 속도
    private double fastSpeed = 1.8;  // 빠른 속도
    private long speedChangeInterval = 2000;  // 속도 변경 주기 (밀리초)
    private long lastSpeedChangeTime = 0;
    private boolean isFastMode = false;  // 현재 빠른 모드인지

    public BossZombie(int startX, int startY) {
        
        super(startX, startY);

        // 2️⃣ [추가] 시작할 때 정밀 좌표도 초기화
        this.preciseX = startX;
        this.preciseY = startY;
    
        this.hp = 7;
        this.maxHp = 7;
        this.speed = baseSpeed;  // 기본 속도로 시작
        this.lastSpeedChangeTime = System.currentTimeMillis(); 
        this.scale = 0.11; 
        this.CHASE_RADIUS = 550; 

        try {
            this.zombieImage = ImageIO.read(new File("images/boss_zombie.png"));
            this.damagedImage = ImageIO.read(new File("images/damaged_boss_zombie.png"));

            if (zombieImage != null) {
                this.zombieWidth = (int) (zombieImage.getWidth(null) * scale);
                this.zombieHeight = (int) (zombieImage.getHeight(null) * scale);
            }
        } catch (IOException e) {
            System.out.println("보스 이미지 로드 실패");
        }
    }

    public void setDetected(boolean detected) {
        if (detected) this.isDetected = true;
    }

  
    @Override
    public void update(Player player) {
        if (hp <= 0) return; 

        // 속도 변화 로직: 주기적으로 느려졌다가 빨라졌다가 반복
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpeedChangeTime >= speedChangeInterval) {
            isFastMode = !isFastMode;  // 모드 전환
            this.speed = isFastMode ? fastSpeed : slowSpeed;
            lastSpeedChangeTime = currentTime;
        }

        double distance = Math.hypot(player.x - x, player.y - y);

        int centerX = x + zombieWidth / 2;
        int centerY = y + zombieHeight / 2;
        int pCenterX = player.x + player.playerWidth / 2;
        int pCenterY = player.y + player.playerHeight / 2;
        this.angle = Math.atan2(pCenterY - centerY, pCenterX - centerX);

        // 3️⃣ [수정] 이동 로직: preciseX/Y를 사용하여 소수점 이동 반영
        if (distance < CHASE_RADIUS) {
            double dx = speed * Math.cos(angle);
            double dy = speed * Math.sin(angle);
   
            // X축 이동 (정밀 좌표 사용)
            double nextPreciseX = preciseX + dx;
            if (super.canMove((int)nextPreciseX, y)) {
                preciseX = nextPreciseX; // 소수점 위치 저장
                x = (int) preciseX;      // 실제 좌표 반영
            }

            // Y축 이동 (정밀 좌표 사용)
            double nextPreciseY = preciseY + dy;
            if (super.canMove(x, (int)nextPreciseY)) {
                preciseY = nextPreciseY; // 소수점 위치 저장
                y = (int) preciseY;      // 실제 좌표 반영
            }
        }

        // 돌진 패턴
        if (distance < 200 && Math.random() < 0.01) {
            int dashSpeed = 5;
            int dashX = x;
            int dashY = y;

            if (player.x > x) dashX += dashSpeed; else dashX -= dashSpeed;
            if (player.y > y) dashY += dashSpeed; else dashY -= dashSpeed;

            // 돌진 시 정밀 좌표 동기화 (중요)
            if (super.canMove(dashX, y)) {
                x = dashX;
                preciseX = x; // 돌진 후 위치로 정밀 좌표 갱신
            }
            if (super.canMove(x, dashY)) {
                y = dashY;
                preciseY = y; // 돌진 후 위치로 정밀 좌표 갱신
            }
        }
    }
}