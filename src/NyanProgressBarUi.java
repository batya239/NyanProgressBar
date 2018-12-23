import com.intellij.openapi.ui.GraphicsConfig;
import com.intellij.openapi.util.ScalableIcon;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.GraphicsUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import sun.swing.SwingUtilities2;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.*;

public class NyanProgressBarUi extends BasicProgressBarUI {

    @SuppressWarnings({"MethodOverridesStaticMethodOfSuperclass", "UnusedDeclaration"})
    public static ComponentUI createUI(JComponent c) {
        c.setBorder(JBUI.Borders.empty().asUIResource());
        return new NyanProgressBarUi();
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        return new Dimension(super.getPreferredSize(c).width, JBUI.scale(20));
   }

    @Override
    protected void installListeners() {
        super.installListeners();
        progressBar.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                super.componentHidden(e);
            }
        });
    }

    protected volatile int offset = 0;
    protected volatile int offset2 = 0;
    protected volatile int velocity = 1;
    @Override
    protected void paintIndeterminate(Graphics g2d, JComponent c) {

        if (!(g2d instanceof Graphics2D)) {
            return;
        }
        Graphics2D g = (Graphics2D)g2d;


        Insets b = progressBar.getInsets(); // area for border
        int barRectWidth = progressBar.getWidth() - (b.right + b.left);
        int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);

        if (barRectWidth <= 0 || barRectHeight <= 0) {
            return;
        }
        //boxRect = getBox(boxRect);
        g.setColor(new JBColor(Gray._240.withAlpha(50), Gray._128.withAlpha(50)));
        int w = c.getWidth();
        int h = c.getPreferredSize().height;
        if (!isEven(c.getHeight() - h)) h++;

        LinearGradientPaint baseRainbowPaint = getRainbowPaintFromHeight(h);

        g.setPaint(baseRainbowPaint);

        if (c.isOpaque()) {
            g.fillRect(0, (c.getHeight() - h)/2, w, h);
        }
        g.setColor(new JBColor(Gray._165.withAlpha(50), Gray._88.withAlpha(50)));
        final GraphicsConfig config = GraphicsUtil.setupAAPainting(g);
        g.translate(0, (c.getHeight() - h) / 2);
        int x = -offset;

        Paint old = g.getPaint();
        g.setPaint(baseRainbowPaint);

        final float R = JBUI.scale(8f);
        final float R2 = JBUI.scale(9f);
        final Area containingRoundRect = new Area(new RoundRectangle2D.Float(1f, 1f, w - 2f, h - 2f, R, R));
//        while (x < Math.max(c.getWidth(), c.getHeight())) {
//            Path2D.Double path = new Path2D.Double();
//            float ww = getPeriodLength() / 2f;
//            path.moveTo(x, 0);
//            path.lineTo(x+ww, 0);
//            path.lineTo(x+ww - h / 2, h);
//            path.lineTo(x-h / 2, h);
//            path.lineTo(x, 0);
//            path.closePath();
//
//            final Area area = new Area(path);
//            area.intersect(containingRoundRect);
            g.fill(containingRoundRect);
//            x+= getPeriodLength();
//        }
        g.setPaint(old);
        offset = (offset + 1) % getPeriodLength();
        offset2 += velocity;
        if (offset2 <= 2) {
            offset2 = 2;
            velocity = 1;
        } else if (offset2 >= w - JBUI.scale(15)) {
            offset2 = w - JBUI.scale(15);
            velocity = -1;
        }
//        offset2 = (offset2 + 1) % (w - 3);
        Area area = new Area(new Rectangle2D.Float(0, 0, w, h));
        area.subtract(new Area(new RoundRectangle2D.Float(1f, 1f, w - 2f, h - 2f, R, R)));
        g.setPaint(Gray._128);
//        g.setPaint(baseRainbowPaint);
        if (c.isOpaque()) {
            g.fill(area);
        }

        area.subtract(new Area(new RoundRectangle2D.Float(0, 0, w, h, R2, R2)));

        Container parent = c.getParent();
        Color background = parent != null ? parent.getBackground() : UIUtil.getPanelBackground();
        g.setPaint(background);
//        g.setPaint(baseRainbowPaint);
        if (c.isOpaque()) {
            g.fill(area);
        }

//        g.setPaint(baseRainbowPaint);

        Icon scaledIcon = velocity > 0 ? ((ScalableIcon) NyanIcons.CAT_ICON) : ((ScalableIcon) NyanIcons.RCAT_ICON) ;
//        if (velocity < 0) {
//            scaledIcon = new ReflectedIcon(scaledIcon);
//        }
        scaledIcon.paintIcon(progressBar, g, offset2 - JBUI.scale(10), -JBUI.scale(6));

        g.draw(new RoundRectangle2D.Float(1f, 1f, w - 2f - 1f, h - 2f -1f, R, R));
        g.translate(0, -(c.getHeight() - h) / 2);

        // Deal with possible text painting
        if (progressBar.isStringPainted()) {
            if (progressBar.getOrientation() == SwingConstants.HORIZONTAL) {
                paintString(g, b.left, b.top, barRectWidth, barRectHeight, boxRect.x, boxRect.width);
            }
            else {
                paintString(g, b.left, b.top, barRectWidth, barRectHeight, boxRect.y, boxRect.height);
            }
        }
        config.restore();
    }

    @Override
    protected void paintDeterminate(Graphics g, JComponent c) {
        if (!(g instanceof Graphics2D)) {
            return;
        }

        if (progressBar.getOrientation() != SwingConstants.HORIZONTAL || !c.getComponentOrientation().isLeftToRight()) {
            super.paintDeterminate(g, c);
            return;
        }
        final GraphicsConfig config = GraphicsUtil.setupAAPainting(g);
        Insets b = progressBar.getInsets(); // area for border
        int w = progressBar.getWidth();
        int h = progressBar.getPreferredSize().height;
        if (!isEven(c.getHeight() - h)) h++;

        int barRectWidth = w - (b.right + b.left);
        int barRectHeight = h - (b.top + b.bottom);

        if (barRectWidth <= 0 || barRectHeight <= 0) {
            return;
        }

        int amountFull = getAmountFull(b, barRectWidth, barRectHeight);

        Container parent = c.getParent();
        Color background = parent != null ? parent.getBackground() : UIUtil.getPanelBackground();

        g.setColor(background);
        Graphics2D g2 = (Graphics2D)g;
        if (c.isOpaque()) {
            g.fillRect(0, 0, w, h);
        }

        final float R = JBUI.scale(8f);
        final float R2 = JBUI.scale(9f);
        final float off = JBUI.scale(1f);

        g2.translate(0, (c.getHeight() - h)/2);
        g2.setColor(progressBar.getForeground());
        g2.fill(new RoundRectangle2D.Float(0, 0, w - off, h - off, R2, R2));
        g2.setColor(background);
        g2.fill(new RoundRectangle2D.Float(off, off, w - 2f*off - off, h - 2f*off - off, R, R));
//        g2.setColor(progressBar.getForeground());
        g2.setPaint(getRainbowPaintFromHeight(h));

        NyanIcons.CAT_ICON.paintIcon(progressBar, g2, amountFull - JBUI.scale(10), -JBUI.scale(6));
        g2.fill(new RoundRectangle2D.Float(2f*off,2f*off, amountFull - JBUI.scale(5f), h - JBUI.scale(5f), JBUI.scale(7f), JBUI.scale(7f)));
        g2.translate(0, -(c.getHeight() - h)/2);

        // Deal with possible text painting
        if (progressBar.isStringPainted()) {
            paintString(g, b.left, b.top,
                    barRectWidth, barRectHeight,
                    amountFull, b);
        }
        config.restore();
    }

    private void paintString(Graphics g, int x, int y, int w, int h, int fillStart, int amountFull) {
        if (!(g instanceof Graphics2D)) {
            return;
        }

        Graphics2D g2 = (Graphics2D)g;
        String progressString = progressBar.getString();
        g2.setFont(progressBar.getFont());
        Point renderLocation = getStringPlacement(g2, progressString,
                x, y, w, h);
        Rectangle oldClip = g2.getClipBounds();

        if (progressBar.getOrientation() == SwingConstants.HORIZONTAL) {
            g2.setColor(getSelectionBackground());
            SwingUtilities2.drawString(progressBar, g2, progressString,
                    renderLocation.x, renderLocation.y);
            g2.setColor(getSelectionForeground());
            g2.clipRect(fillStart, y, amountFull, h);
            SwingUtilities2.drawString(progressBar, g2, progressString,
                    renderLocation.x, renderLocation.y);
        } else { // VERTICAL
            g2.setColor(getSelectionBackground());
            AffineTransform rotate =
                    AffineTransform.getRotateInstance(Math.PI/2);
            g2.setFont(progressBar.getFont().deriveFont(rotate));
            renderLocation = getStringPlacement(g2, progressString,
                    x, y, w, h);
            SwingUtilities2.drawString(progressBar, g2, progressString,
                    renderLocation.x, renderLocation.y);
            g2.setColor(getSelectionForeground());
            g2.clipRect(x, fillStart, w, amountFull);
            SwingUtilities2.drawString(progressBar, g2, progressString,
                    renderLocation.x, renderLocation.y);
        }
        g2.setClip(oldClip);
    }

    /** Create a gradient such as [0, 0.99, 1, 1.99, ...], [RED, RED, ORANGE, ORANGE, ..] */
    private LinearGradientPaint getRainbowPaintFromHeight(float scaledHeight) {

        int numRainbowColors = RainbowColors.RAINBOW_ARRAY.length;
        float epsilon = 0.000001f;

        float[] fractionList = new float[numRainbowColors * 2];
        Color[] colorList = new Color[numRainbowColors * 2];

        for (int i = 0; i < numRainbowColors; i++) {
            fractionList[i * 2] = (float) i / numRainbowColors;
            fractionList[i * 2 + 1] = ((i + 1) - epsilon) / numRainbowColors;

            colorList[i * 2] = RainbowColors.RAINBOW_ARRAY[i];
            colorList[i * 2 + 1] = RainbowColors.RAINBOW_ARRAY[i];
        }

        return new LinearGradientPaint(0, JBUI.scale(1),
                0, scaledHeight - JBUI.scale(3),
                fractionList, colorList);
    }


    @Override
    protected int getBoxLength(int availableLength, int otherDimension) {
        return availableLength;
    }

    protected int getPeriodLength() {
        return JBUI.scale(16);
    }

    private static boolean isEven(int value) {
        return value % 2 == 0;
    }

    private class ReflectedIcon implements Icon {
        private final Icon myIcon;

        private ReflectedIcon(Icon myIcon) {
            this.myIcon = myIcon;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D)g.create();
            g2d.setTransform(AffineTransform.getQuadrantRotateInstance(2, (double)this.getIconWidth() / 2.0D, (double)this.getIconHeight() / 2.0D));
            myIcon.paintIcon(c, g2d, x, y);
        }

        @Override
        public int getIconWidth() {
            return myIcon.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return myIcon.getIconHeight();
        }
    }

}

