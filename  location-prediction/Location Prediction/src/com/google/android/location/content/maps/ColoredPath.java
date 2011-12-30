package com.google.android.location.content.maps;

import android.graphics.Paint;
import android.graphics.Path;

/**
 * Represents a colored {@code Path} to save its relative color for drawing.
 * needs to be revised
 * @author Andrei
 */
public class ColoredPath {
  private final Path path;
  private final Paint pathPaint;

  /**
   * Constructor for a ColoredPath by color.
   * needs to be revised
   */
  public ColoredPath(int color) {
      path = new Path();
      pathPaint = new Paint();
      pathPaint.setColor(color);
      pathPaint.setStrokeWidth(3);
      pathPaint.setStyle(Paint.Style.STROKE);
      pathPaint.setAntiAlias(true);
  }
  
  /**
   * Constructor for a ColoredPath by Paint.
   */
  public ColoredPath(Paint paint) {
      path = new Path();
      pathPaint = paint;
  }
  
  /**
   * @return the path
   */
  public Path getPath() {
    return path;
  }
  
  /**
   * @return the pathPaint
   */
  public Paint getPathPaint() {
    return pathPaint;
  }
}