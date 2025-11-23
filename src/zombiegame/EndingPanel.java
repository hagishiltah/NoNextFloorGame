// EndingPanel.java 파일의 전체 내용

package zombiegame; // 💡 패키지 선언 추가!

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import java.io.File;

public class EndingPanel extends JPanel { // 💡 독립적인 클래스로 선언
    private Image img1, img2;
    private int state = 0;
    private JFrame mainFrame; // JFrame 자체를 받도록 변경

    public EndingPanel(JFrame frame) { // 💡 NoNextFloorGame 대신 JFrame을 인자로 받습니다.
        this.mainFrame = frame;

        try {
            // 이미지 경로 확인: 프로젝트 루트가 아닌 'images/' 폴더를 사용합니다.
            img1 = ImageIO.read(new File("images/ending1.png"));
            img2 = ImageIO.read(new File("images/ending2.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (state == 0) {
                    state = 1;
                    repaint();
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "세상은 일상으로 돌아갔다");
                    System.exit(0); // 💡 시스템 종료
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Image now = (state == 0) ? img1 : img2;
        g.drawImage(now, 0, 0, getWidth(), getHeight(), null);
    }
}