package com.pipit.agc.agc.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

public class TextDrawable extends Drawable {

    protected final Paint textPaint;
    protected ColorStateList color;
    protected String text;
    protected int iHeight;
    protected int iWidth;
    protected int measuredWidth, measuredHeight;
    private float ascent;
    /**
     * A flag whether the drawable is stateful - whether to redraw if the state of view has changed
     */
    protected boolean stateful;
    /**
     * Vertical alignment of text
     */
    private VerticalAlignment verticalAlignment;

    public TextDrawable(Context ctx, String text, ColorStateList color, float textSize, VerticalAlignment verticalAlignment) {
        textPaint = new Paint();
        this.text = text;
        initPaint();
        this.textPaint.setTextSize(textSize);
        measureSize();
        setBounds(0, 0, iWidth, iHeight);
        this.color = color;
        textPaint.setColor(color.getDefaultColor());
        this.verticalAlignment = verticalAlignment;
    }

    /**
     * Set bounds of drawable to start on coordinate [0,0] and end on coordinate[measuredWidth,
     * measuredHeight]
     */
    public final void setBoundsByMeasuredSize() {
        setBounds(0, 0, measuredWidth, measuredHeight);
        invalidateSelf();
    }

    @Override
    public boolean isStateful() {
        return stateful;
    }

    public void setStateful(boolean stateful) {
        this.stateful = stateful;
    }

    private void initPaint() {
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    /**
     * Vertical alignment of text within the drawable (Horizontally it is always aligned to center
     */
    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    /**
     * Vertical alignment of text within the drawable (Horizontally it is always aligned to center
     */
    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        if (this.verticalAlignment != verticalAlignment) {
            this.verticalAlignment = verticalAlignment;
            invalidateSelf();
        }
    }

    /**
     * Displayed text
     */
    public String getText() {
        return text;
    }

    /**
     * Displayed text
     */

    public void setText(String text) {
        if (this.text == null || !this.text.equals(text)) {
            this.text = text;
            invalidateSelf();
        }
    }

    /**
     * The color of text
     */
    public ColorStateList getColor() {
        return color;
    }

    /**
     * The color of text
     */
    public void setColor(ColorStateList colorStateList) {
        if (this.color == null || !this.color.equals(colorStateList)) {
            this.color = colorStateList;
            invalidateSelf();
        }
    }

    /**
     * The color of text
     */
    public void setColor(int color) {
        setColor(ColorStateList.valueOf(color));
    }

    /**
     * Text size
     */
    public void setTextSize(float size) {
        if (this.textPaint.getTextSize() != size) {
            this.textPaint.setTextSize(size);
            measureSize();
            invalidateSelf();
        }
    }

    /**
     * Text size
     */
    public void setTextSize(int unit, float size, Context context) {
        setTextSize(TypedValue.applyDimension(unit, size, context.getResources().getDisplayMetrics()));
    }

    /**
     * This method is called by default when any property that may have some influence on the size
     * of drawable This method should use measuredWidth and measuredHeight properties to store the
     * measured walues By default the measuredWIdth and measuredHeight are set to iWidth and iHeight
     * (size of text) by this method.
     */
    protected void measureSize() {
        ascent = -textPaint.ascent();
        iWidth = (int) (0.5f + textPaint.measureText(text));
        iHeight = (int) (0.5f + textPaint.descent() + ascent);
        measuredWidth = iWidth;
        measuredHeight = iHeight;
    }

    public float getTextSize() {
        return textPaint.getTextSize();
    }

    @Override
    protected boolean onStateChange(int[] state) {
        int clr = color != null ? color.getColorForState(state, 0) : 0;
        if (textPaint.getColor() != clr) {
            textPaint.setColor(clr);
            return true;
        } else {
            return false;
        }
    }

    public Typeface getTypeface() {
        return textPaint.getTypeface();
    }

    public void setTypeface(Typeface typeface) {
        if (!textPaint.getTypeface().equals(typeface)) {
            textPaint.setTypeface(typeface);
            invalidateSelf();
        }
    }

    /**
     * The method is called before the text is drawn. This method can be overridden to draw some background (by default this method does nothing).
     *
     * @param canvas The canvas where to draw.
     * @param bounds The bounds of the drawable.
     */
    protected void drawBefore(Canvas canvas, Rect bounds) {

    }

    /**
     * The method is called after the text is drawn. This method can be overriden to draw some more graphics over the text (by default this method does nothing).
     *
     * @param canvas The canvas where to draw.
     * @param bounds The bound of the drawable.
     */
    protected void drawAfter(Canvas canvas, Rect bounds) {

    }

    @Override
    public void draw(Canvas canvas) {
        if (text == null || text.isEmpty()) {
            return;
        }
        final Rect bounds = getBounds();
        int stack = canvas.save();
        canvas.translate(bounds.left, bounds.top);
        drawBefore(canvas, bounds);
        if (text != null && !text.isEmpty()) {
            final float x = bounds.width() >= iWidth ? bounds.centerX() : iWidth * 0.5f;
            float y = 0;
            switch (verticalAlignment) {
                case BASELINE:
                    y = (bounds.height() - iHeight) * 0.5f + ascent;
                    break;
                case TOP:
                    y = bounds.height();
                    break;
                case BOTTOM:
                    y = bounds.height();
                    break;
            }
            canvas.drawText(text, x, y, textPaint);
        }
        drawAfter(canvas, bounds);
        canvas.restoreToCount(stack);

    }

    @Override
    public void setAlpha(int alpha) {
        if (textPaint.getAlpha() != alpha) {
            textPaint.setAlpha(alpha);
            invalidateSelf();
        }
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (textPaint.getColorFilter() == null || !textPaint.getColorFilter().equals(cf)) {
            textPaint.setColorFilter(cf);
            invalidateSelf();
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public enum VerticalAlignment {
        TOP, BOTTOM, BASELINE
    }
}