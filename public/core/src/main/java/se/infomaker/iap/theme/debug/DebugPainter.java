package se.infomaker.iap.theme.debug;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import se.infomaker.iap.theme.util.UI;

public class DebugPainter {
    public static final int RED = Color.parseColor("#c80d35");
    public static final int GREEN = Color.parseColor("#23552D");
    public static final int BLUE = Color.parseColor("#065588");
    private final Position position;
    private final Paint debugPaint = new Paint();
    private final Paint debugBackground = new Paint();
    private String debugMessage = "";
    private final Rect debugBounds = new Rect();

    public enum Position {
        ON_CONTENT, BOTTOM_LEFT, TOP_RIGHT
    }

    public DebugPainter(int color, Position position) {
        this.position = position;
        debugPaint.setColor(Color.WHITE);
        debugPaint.setAntiAlias(true);
        debugPaint.setStyle(Paint.Style.FILL);
        debugPaint.setTextSize(UI.dp2px(10));
        debugBackground.setColor(color);
        debugBackground.setStyle(Paint.Style.FILL);
    }

    public void setDebugMessage(String debugMessage) {
        if (!debugMessage.isEmpty()) {
            this.debugMessage = debugMessage + " ";
        }
    }

    public void paint(Canvas canvas, View view) {
        debugPaint.getTextBounds(debugMessage, 0, debugMessage.length(), debugBounds);
        int width = debugBounds.width() + 5;
        int height = debugBounds.height();
        switch (position) {
            case ON_CONTENT: {
                canvas.drawRect(view.getPaddingLeft(), view.getPaddingTop() + 5, width + view.getPaddingLeft(), height + view.getPaddingTop() + 10, debugBackground);
                canvas.drawText(debugMessage, view.getPaddingLeft() , height + view.getPaddingTop() + 5, debugPaint);
                break;
            }
            case BOTTOM_LEFT: {
                canvas.drawRect(0, view.getHeight() - height -5, width, view.getHeight(), debugBackground);
                canvas.drawText(debugMessage, 0 , view.getHeight() - 5, debugPaint);
                break;
            }
            case TOP_RIGHT: {
                canvas.drawRect(view.getWidth()- width, 0, view.getWidth(), height + 10, debugBackground);
                canvas.drawText(debugMessage, view.getWidth()- width, height + 5, debugPaint);
                break;
            }
        }

    }
}
