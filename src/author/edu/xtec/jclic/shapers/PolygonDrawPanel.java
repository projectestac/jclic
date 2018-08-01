/*
 * File    : PolygonDrawPanel.java
 * Created : 17-may-2002 10:20
 * By      : allastar
 *
 * JClic - Authoring and playing system for educational activities
 *
 * Copyright (C) 2000 - 2005 Francesc Busquets & Departament
 * d'Educacio de la Generalitat de Catalunya
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details (see the LICENSE file).
 */

package edu.xtec.jclic.shapers;

import edu.xtec.util.StrUtils;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

/**
 * @author allastar
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.17
 */
public class PolygonDrawPanel implements java.awt.event.MouseMotionListener, java.awt.event.MouseListener {

  // List containing the polygon currently being modified
  private List<EditableShape> vShapes;
  private static List<EditableShape> vCopied;
  private List<EditableShape> vRedrawingLines, vRedrawingLinesBeforeModify, vShapeBeforeModify;
  private List<PointListener> vPointListeners;
  private double iniX, iniY, finX, finY, lastFinX, lastFinY;
  private Point2D iniPoint = null, lastPoint = null;
  private boolean bSelectedPoint = false;
  private double zoomX = 0, zoomY = 0, zoomH = -1, zoomW = -1;
  private boolean creatingRect = false, creatingEllipse = false, creatingPolygon = false, bRedrawingLines = false,
      bSelectingArea = false;
  private boolean bResizing = false;
  private int resizingDirection = NO_RESIZING;
  private boolean bSpecialLine = false;
  private boolean bMoving = false;
  private EditableShape specialLine = null;
  private int drawingMode = SELECTING;
  static double defaultSensibility = 1.5;
  private AffineTransform at;
  private List<Rectangle> vDrawnBorders = null;
  private Shape current = null;

  private JComponent container;

  private short INITIAL = 0;
  private short END = 1;

  public static final int SELECTING = 1;
  public static final int MOVING = 2;
  public static final int NEW_POINT = 4;
  public static final int DRAWING_RECT = 5;
  public static final int DRAWING_ELLIPSE = 6;
  public static final int DRAWING_POLYGON = 7;
  public static final int ZOOM = 12;

  public static final int NO_RESIZING = -1;
  public static final int EAST = 0;
  public static final int SOUTH = 1;
  public static final int SOUTH_EAST = 2;

  private int backgroundComposite = 0;

  protected HolesEditorPanel hep;
  protected boolean canResize;
  protected Rectangle lastPreviewArea;

  static Cursor[] cursors = null;
  public static final int PEN_CURSOR = 0;
  public static final int CIRCLE_CURSOR = 1;

  protected Shape esborram = null;

  /** Creates new PolygonDrawPanel */
  public PolygonDrawPanel(int width, int height, HolesEditorPanel hep, boolean canResize) {
    this.hep = hep;
    this.canResize = canResize;
    vShapes = new ArrayList<EditableShape>();
    if (vCopied == null)
      vCopied = new ArrayList<EditableShape>();

    vRedrawingLines = new ArrayList<EditableShape>();
    vRedrawingLinesBeforeModify = new ArrayList<EditableShape>();
    vPointListeners = new ArrayList<PointListener>();
    at = new AffineTransform();
    initDrawnBorders();

    if (cursors == null) {
      cursors = new Cursor[2];
      Toolkit tk = Toolkit.getDefaultToolkit();
      cursors[PEN_CURSOR] = tk.createCustomCursor(
          edu.xtec.util.ResourceManager.getImageIcon("cursors/llapis.gif").getImage(), new Point(12, 24), "pen");
      cursors[CIRCLE_CURSOR] = tk.createCustomCursor(
          edu.xtec.util.ResourceManager.getImageIcon("cursors/cercle.gif").getImage(), new Point(16, 16), "circle");
    }
    hep.addKeyListener(new PolygonDrawPanel.KeyHandler());
  }

  public void setDrawingMode(int drawingMode) {
    if (this.drawingMode != drawingMode) {
      this.drawingMode = drawingMode;
      if (creatingPolygon)
        joinPolygon();
      if (drawingMode != NEW_POINT && drawingMode != SELECTING) {
        endPolygon();
      }
      hep.repaint(0);
    }
  }

  public int getVisibleWidth() {
    return hep.getPreviewPanel().getWidth();
  }

  public int getVisibleHeight() {
    return hep.getPreviewPanel().getHeight();
  }

  public void initDrawnBorders() {
    if (vDrawnBorders != null)
      vDrawnBorders.clear();
    else
      vDrawnBorders = new ArrayList<Rectangle>();
    for (int i = 0; i < hep.getNumShapes(); i++) {
      if (i != hep.currentShape) {
        Shape s = hep.getHoles().getShape(i, hep.previewArea);
        if (s != null)
          vDrawnBorders.addAll(getBorders(s));
      }
    }
  }

  private List<Rectangle> getBorders(Shape s) {
    // Utility function that returns the points that define the "segments" of the
    // polygon 's'.
    int xIni = 0;
    int yIni = 0;

    if (s == null)
      return null;
    List<Rectangle> vPoints = new ArrayList<Rectangle>();
    double x, y;
    if (s instanceof GeneralPath) {
      GeneralPath gp = (GeneralPath) s;
      PathIterator it = gp.getPathIterator(new AffineTransform());
      double[] coords = new double[6];
      while (!it.isDone()) {
        int type = it.currentSegment(coords);
        switch (type) {
        case PathIterator.SEG_MOVETO:
          x = coords[0];
          y = coords[1];
          vPoints.add(new Rectangle((int) (x + xIni) - (EditableShapeConstants.selectLength / 2),
              (int) (y + yIni) - (EditableShapeConstants.selectLength / 2), EditableShapeConstants.selectLength,
              EditableShapeConstants.selectLength));
          break;
        case PathIterator.SEG_LINETO:
          x = coords[0];
          y = coords[1];
          vPoints.add(new Rectangle((int) (x + xIni) - (EditableShapeConstants.selectLength / 2),
              (int) (y + yIni) - (EditableShapeConstants.selectLength / 2), EditableShapeConstants.selectLength,
              EditableShapeConstants.selectLength));
          break;
        case PathIterator.SEG_CUBICTO:
          x = coords[4];
          y = coords[5];
          vPoints.add(new Rectangle((int) (x + xIni) - (EditableShapeConstants.selectLength / 2),
              (int) (y + yIni) - (EditableShapeConstants.selectLength / 2), EditableShapeConstants.selectLength,
              EditableShapeConstants.selectLength));
          break;
        case PathIterator.SEG_QUADTO:
          x = coords[2];
          y = coords[3];
          vPoints.add(new Rectangle((int) (x + xIni) - (EditableShapeConstants.selectLength / 2),
              (int) (y + yIni) - (EditableShapeConstants.selectLength / 2), EditableShapeConstants.selectLength,
              EditableShapeConstants.selectLength));
          break;
        case PathIterator.SEG_CLOSE:
          break;
        default:
        }
        it.next();
      }
    }
    return vPoints;
  }

  public void paint(java.awt.Graphics2D g) {

    Graphics2D g2d = (Graphics2D) g;

    if (EditableShapeConstants.showDrawnPoints)
      paintDrawnBorders(g);

    for (EditableShape esh : vShapes) {
      if (bSpecialLine && esh == specialLine)
        esh.paintWithColor(g, drawingMode, EditableShapeConstants.CUT_COLOR);
      else
        esh.paintWithColor(g, drawingMode, EditableShapeConstants.ACTIVE_COLOR);
    }
    if (bMoving)
      paintMoved(g);
    if (creatingRect) {
      g.setColor(EditableShapeConstants.selectedColor);
      EditableRectangle rect = new EditableRectangle((int) iniX, (int) iniY, (int) (finX - iniX), (int) (finY - iniY));
      rect.paintWithColor(g, drawingMode, EditableShapeConstants.selectedColor);
    }
    if (creatingEllipse) {
      g.setColor(EditableShapeConstants.selectedColor);
      EditableEllipse2D ellipse = new EditableEllipse2D((int) iniX, (int) iniY, (int) (finX - iniX),
          (int) (finY - iniY));
      ellipse.paintWithColor(g, drawingMode, EditableShapeConstants.selectedColor);
    }
    if (creatingPolygon) {
      if (lastPoint != null) {
        EditableLine2D el = new EditableLine2D(lastPoint.getX(), lastPoint.getY(), finX, finY);
        el.paintWithColor(g, drawingMode, EditableShapeConstants.selectedColor);
      }
    }
  }

  public void drawGrid(java.awt.Graphics g, int gridWidth) {
    if (gridWidth <= 1)
      return;
    int width = (int) (hep.previewArea.getWidth());
    int height = (int) (hep.previewArea.getHeight());
    g.setColor(EditableShapeConstants.gridColor);
    for (double i = hep.previewArea.x; i <= hep.previewArea.x + width; i += (gridWidth * hep.xFactor)) {
      // from 0 in order to avoid changes in the location of the grid when zoomed
      // vertical
      g.drawLine((int) i, hep.previewArea.y, (int) i, (int) (hep.previewArea.y + height));
    }
    for (double i = hep.previewArea.y; i <= hep.previewArea.y + height; i += (gridWidth * hep.yFactor)) {
      // horitzontal
      g.drawLine(hep.previewArea.x, (int) i, (int) (hep.previewArea.x + width), (int) i);
    }
  }

  protected void paintDrawnBorders(java.awt.Graphics2D g) {
    for (Rectangle r : vDrawnBorders) {
      double x = r.getX();
      double y = r.getY();
      double w = r.getWidth();
      double h = r.getHeight();
      x = x + (w / 4);
      y = y + (h / 4);
      w = w / 2;
      h = h / 2;
      g.setColor(EditableShapeConstants.DRAWN_BORDER_COLOR);
      g.fillRect((int) x, (int) y, (int) w, (int) h);
    }
  }

  private void paintMoved(java.awt.Graphics2D g) {
    for (EditableShape esh : vCopied) {
      EditableShape copied = (EditableShape) esh.clone();
      copied.transform(AffineTransform.getTranslateInstance(finX - iniX, finY - iniY));
      copied.paintWithColor(g, drawingMode, EditableShapeConstants.movingColor);
    }
  }

  public void updateView() {
    List v = getGeneralPath();
    if (lastPreviewArea == null)
      lastPreviewArea = hep.previewArea;
    if (v.size() > 0) {
      move(hep.previewArea.x - lastPreviewArea.x, hep.previewArea.y - lastPreviewArea.y, false, false);
    }
    try {
      lastPreviewArea = (Rectangle) (hep.previewArea.clone());
    } catch (Exception e) {
      System.err.println("Error updating view:\n" + e);
    }
  }

  public void setShapeData(ShapeData sd, double x, double y, double scaleX, double scaleY) {
    // x,y indicate the current position
    clean();
    current = (sd != null) ? sd.getShape(hep.previewArea) : null;
    double firstX = -1, firstY = -1;
    if (sd != null && sd.primitiveType >= 0 && sd.primitivePoints != null && sd.primitivePoints.length > 3) {
      EditableShape es;
      double xTr = (sd.primitivePoints[0] * hep.previewArea.getWidth()) + hep.previewArea.getX();
      double yTr = (sd.primitivePoints[1] * hep.previewArea.getHeight()) + hep.previewArea.getY();
      double wSc = sd.primitivePoints[2] * hep.previewArea.getWidth();
      double hSc = sd.primitivePoints[3] * hep.previewArea.getHeight();
      switch (sd.primitiveType) {
      case ShapeData.RECTANGLE:
        es = new EditableRectangle((int) xTr, (int) yTr, (int) wSc, (int) hSc);
        vShapes.add(es);
        break;
      case ShapeData.ELLIPSE:
        es = new EditableEllipse2D((int) xTr, (int) yTr, (int) wSc, (int) hSc);
        vShapes.add(es);
        break;
      }
    } else if (sd != null) {
      Shape s = sd.getShape(hep.previewArea);
      if (s instanceof GeneralPath) {
        GeneralPath gp = (GeneralPath) s;
        PathIterator it = gp.getPathIterator(new AffineTransform());
        double[] coords = new double[6];
        while (!it.isDone()) {
          int type = it.currentSegment(coords);
          switch (type) {
          case PathIterator.SEG_MOVETO:
            x = coords[0];
            y = coords[1];
            if (firstX == -1) {
              // Too close
              firstX = x;
              firstY = y;
            }
            break;
          case PathIterator.SEG_LINETO:
            vShapes.add(new EditableLine2D(x, y, coords[0], coords[1]));
            x = coords[0];
            y = coords[1];
            break;
          case PathIterator.SEG_CUBICTO:
            vShapes
                .add(new EditableCubicCurve2D(x, y, coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]));
            x = coords[4];
            y = coords[5];
            break;
          case PathIterator.SEG_QUADTO:
            vShapes.add(new EditableQuadCurve2D(x, y, coords[0], coords[1], coords[2], coords[3]));
            x = coords[2];
            y = coords[3];
            break;
          case PathIterator.SEG_CLOSE:
            if (firstX != -1 && (x != firstX || y != firstY)) {
              vShapes.add(new EditableLine2D(x, y, firstX, firstY));
              x = firstX;
              y = firstY;
            }
            break;
          default:
            break;
          }
          it.next();
        }
        if (firstX != -1 && (x != firstX || y != firstY)) {
          // That's to be sure that the shape is always closed
          vShapes.add(new EditableLine2D(x, y, firstX, firstY));
        }
      }
    }
    removeDrawnBorders(sd);
  }

  public void clean() {
    vShapes = new ArrayList<EditableShape>();
    vRedrawingLines = new ArrayList<EditableShape>();
    vRedrawingLinesBeforeModify = new ArrayList<EditableShape>();
  }

  public boolean selectDrawnShape(Point2D p) {
    endPolygon();
    for (int i = 0; i < hep.getNumShapes(); i++) {
      Shape s = hep.getHoles().getShape(i, hep.previewArea);
      if (s.contains(p) && hep.currentShape != i) {
        hep.setCurrentShape(i);
        setShapeData(hep.getHoles().getShapeData(i), 0, 0, 1, 1);
        return true;
      }
    }
    hep.setCurrentShape(hep.getHoles().getNumCells() + 1);
    clean();
    return false;
  }

  public void selectShape(int iIndex) {
    if (iIndex < 0)
      return;
    ShapeData sd = hep.getHoles().getShapeData(iIndex);
    if (sd != null) {
      setShapeData(sd, 0, 0, 1, 1);
    }
  }

  private EditableShape aproximationToLine(double x, double y) {
    return aproximationToLine(x, y, null);
  }

  private EditableShape aproximationToLine(double x, double y, List<EditableShape> vRedrawingLines) {
    // returns an EditableShape when there is a corner at (x,y) that not belongs to
    // the lines in the Rectangle List vRedrawingLines. Otherwise returns null
    if (vRedrawingLines != null) {
      for (EditableShape esh : vRedrawingLines) {
        if (!vRedrawingLines.contains(esh)) {
          if (esh.hasClickedBorder((int) x, (int) y, false)) {
            return esh;
          }
        }
      }
    }
    return null;
  }

  private Point2D getTransformedPoint(Point2D p, boolean mustBeOnGrid) {
    // mustBeOnGrid is used to discard the approach
    Point2D mousePoint = new Point2D.Double(p.getX(), p.getY());
    if (EditableShapeConstants.gridWidth != -1 && EditableShapeConstants.pointsOnGrid && mustBeOnGrid)
      moveToGrid(mousePoint);
    return mousePoint;
  }

  private void moveToGrid(Point2D p) {
    // Moves the supplied point to the closest grid point
    int x = (int) p.getX();
    int y = (int) p.getY();

    x -= hep.previewArea.getX();
    y -= hep.previewArea.getY();

    double wd = EditableShapeConstants.gridWidth * hep.xFactor;
    int w = (int) wd;
    if (w == -1)
      return;

    int xLeft = (int) (((int) (x / wd)) * wd);
    if ((x - xLeft) < (w / 2))
      x = xLeft;
    else
      x = (int) (((int) ((x + w - 1) / wd)) * wd);

    int yUp = (int) (((int) (y / wd)) * wd);
    if ((y - yUp) < (w / 2))
      y = yUp;
    else
      y = (int) (((int) ((y + w - 1) / wd)) * wd);
    x += hep.previewArea.getX();
    y += hep.previewArea.getY();
    p.setLocation(x, y);
  }

  public Point2D aproximationToDrawnBorder(double x, double y) {
    // Returns true if a rectangle in vDrawnBorders contains the point (x,y)
    for (Rectangle2D r : vDrawnBorders) {
      if (r.contains(x, y))
        return new Point2D.Double(r.getX() + (r.getWidth() / 2), r.getY() + (r.getHeight() / 2));
    }
    return null;
  }

  protected void redrawingLines(double x, double y) {
    // moves all the selected shapes containing (x,y) in one of its corners
    for (EditableShape esh : vRedrawingLines)
      esh.changeBorder(x, y);
  }

  private void cleanZoom() {
    zoomX = 0;
    zoomY = 0;
    zoomW = -1;
    zoomH = -1;
    at = new AffineTransform();
    cancelCurrentOperations();
    bSelectingArea = false;
    hep.repaint(0);
  }

  public void cancelCurrentOperations() {
    creatingRect = false;
    creatingEllipse = false;
  }

  public void cut(double x, double y) {
    copy(false);
    clean();
    bMoving = true;
    iniX = x;
    iniY = y;
    finX = iniX;
    finY = iniY;
  }

  public void cut() {
    cut(-1, -1);
  }

  public void copy(boolean needSelected) {
    vCopied.clear();
    for (EditableShape esh : vShapes) {
      esh = (EditableShape) esh.clone();
      if (!needSelected || esh.isSelected()) {
        vCopied.add(esh);
      }
    }
  }

  public void paste() {
    bMoving = true;
    iniX = -1;
    iniY = -1;
    finX = -1;
    finY = -1;
    paste(5, 5);
  }

  public void paste(double x, double y) {
    List<EditableShape> newCopied = new ArrayList<EditableShape>();
    deSelectAll();
    for (EditableShape esh : vCopied) {
      EditableShape copied = (EditableShape) esh.clone();
      copied.transform(AffineTransform.getTranslateInstance(x, y));
      copied.setSelected(true);
      vShapes.add(copied);
      newCopied.add(copied);
      // to avoid overlap of shapes when the user makes "paste" two times
    }
    vCopied = newCopied;
  }

  public void deSelectAll() {
    for (EditableShape esh : vShapes) {
      esh.setSelected(false);
    }
    bSelectedPoint = false;
    hep.repaint(0);
  }

  private EditableShape nearestLine(double x, double y) {
    EditableShape nearest = null;
    double distance = 0;
    double currentDistance;
    for (EditableShape esh : vShapes) {
      currentDistance = esh.distanceTo(x, y);
      if (nearest == null || (currentDistance < distance)) {
        distance = currentDistance;
        nearest = esh;
      }
    }
    return nearest;
  }

  private void clicatISeleccionada(int x, int y, boolean needSelected) {
    // With needSelected=false it's not necessary to have selected a shape in order
    // to drag it leaves in vRedrawingLines the selected lines with one point near
    // the supplied co-ordinates (x,y)
    Point2D redrawingPoint = null;
    vRedrawingLines.clear();
    for (EditableShape esh : vShapes) {
      if ((!needSelected || esh.isSelected()) && esh.hasClickedBorder(x, y, needSelected)) {
        Point2D p = esh.getNearestBorder(x, y);
        if (redrawingPoint == null || redrawingPoint.equals(p)) {
          redrawingPoint = p;
          vRedrawingLines.add(esh);
        }
      }
    }
    vRedrawingLinesBeforeModify = cloneVector(vRedrawingLines);
  }

  private List<EditableShape> cloneVector(List<EditableShape> v) {
    List<EditableShape> vClone = new ArrayList<EditableShape>();
    if (v != null) {
      for (EditableShape es : v) {
        vClone.add((EditableShape) es.clone());
      }
    }
    return vClone;
  }

  private void divideShape(EditableShape specialLine, double x, double y) {
    if (specialLine != null) {
      EditableShape[] shapes = specialLine.divide(x, y);
      if (shapes != null) {
        // -> The two following lines are necessary in order to grant the connection of
        // the resulting shape
        // Dividing two times in the same point can create an independent line out of
        // the shape
        List<EditableShape> vCheckPoint = new ArrayList<EditableShape>();
        vCheckPoint.addAll(vShapes);
        vShapes.remove(specialLine);
        for (int i = 0; i < shapes.length; i++) {
          if (shapes[i] != null)
            vShapes.add(shapes[i]);
        }
        boolean bValidate = validateShape();
        if (!bValidate)
          vShapes = vCheckPoint;
      }
      hep.updateView();
    }
    hep.repaint(0);
  }

  private boolean validateShape() {
    return (getGeneralPath().size() == 1);
  }

  public List<GeneralPath> getGeneralPath() {
    List<GeneralPath> vGpaths = new ArrayList<GeneralPath>();
    GeneralPath currentPolygon = new GeneralPath();
    List<EditableShape> shapes = new ArrayList<EditableShape>();
    shapes.addAll(vShapes);
    if (!(shapes.size() > 0))
      return vGpaths;
    EditableShape esh = shapes.get(0);
    shapes.remove(esh);
    currentPolygon.append(esh, true);
    short notUsedPoint = END;
    // indicates the side of the last shape non-adjacent to anyone
    while (shapes.size() > 0) {
      EditableShape shape = getAdjacent(shapes, esh, notUsedPoint);
      if (shape != null) {
        currentPolygon.append(shape, true);
        notUsedPoint = getNotUsed(esh, shape);
        // returns the point of the shape non-adjacent to "current"
        shapes.remove(shape);
        esh = shape;
      } else {
        vGpaths.add(currentPolygon);
        currentPolygon = new GeneralPath();
        notUsedPoint = END;
        esh = (EditableShape) shapes.get(0);
        shapes.remove(esh);
        currentPolygon.append(esh, true);
      }
    }
    vGpaths.add(currentPolygon);
    return vGpaths;
  }

  private short getNotUsed(EditableShape current, EditableShape shape) {
    // returns the point of the shape non-adjacent to "current"
    if (shape.getInitialPoint().equals(current.getInitialPoint())
        || shape.getInitialPoint().equals(current.getEndPoint()))
      return END;
    else
      return INITIAL;
  }

  private EditableShape getAdjacent(List<EditableShape> shapes, EditableShape sh, short notUsedPoint) {
    Point2D p;
    if (notUsedPoint == INITIAL)
      p = sh.getInitialPoint();
    else
      p = sh.getEndPoint();
    for (EditableShape shape : shapes) {
      if (shape.isAdjacentTo(p))
        return shape;
    }
    return null;
  }

  public boolean hasSelectedDrawnShape(Point2D p) {
    for (int i = 0; i < hep.getNumShapes(); i++) {
      Shape s = hep.getHoles().getShape(i, hep.previewArea);
      if (s.contains(p)) {
        return true;
      }
    }
    return false;
  }

  private double distanceToNearest(double x, double y) {
    EditableShape nearest = nearestLine(x, y);
    if (nearest != null)
      return nearest.distanceTo(x, y);
    else
      return -1;
  }

  public void deleteSelected(boolean isCut) {
    if (hasSelectedPoint()) {
      joinAdjacentsToSelectedPoint();
      bSelectedPoint = false;
    } else {
      List<EditableShape> vShapesCopy = new ArrayList<EditableShape>();
      boolean allSelected = true, noneSelected = true;
      vShapesCopy.addAll(vShapes);
      for (EditableShape esh : vShapesCopy) {
        if (!esh.isSelected())
          allSelected = false;
        else {
          noneSelected = false;
          if (isCut || vShapes.size() >= 4) {
            // Avoid to delete objects when there are only 3 or less elements, unless
            // is a "cut"
            vShapes.remove(esh);
            if (!isCut)
              joinAdjacentsTo(esh, vShapes);
          }
        }
      }
      // allSelected indicates if all the object was selected
      if (allSelected || noneSelected) {
        vShapes.clear();
        this.current = null;
        hep.getHoles().removeShape(hep.currentShape);
        hep.setCurrentShape(hep.getHoles().getNumCells());
      }
    }
  }

  private void joinAdjacentsTo(EditableShape current, List<EditableShape> vShapes) {
    // All the shapes in vShapes will converge in one of the "current" points.
    EditableShape s1 = getAdjacent(vShapes, current, INITIAL);
    if (s1 != null) {
      // Always
      s1.hasClickedBorder(current.getInitialPoint().getX(), current.getInitialPoint().getY(), false);
      // hasClickedBorder marks the shape corner nearest to the supplied point.
      // Calling changeBorder this corner will be modified to the new point.
      s1.changeBorder(current.getEndPoint().getX(), current.getEndPoint().getY());
    }
  }

  private void joinAdjacentsToSelectedPoint() {

    if (vShapes.size() != 1 && vShapes.size() <= 3)
      return;

    EditableShape other = null;
    int count = 0;
    for (EditableShape esh : vShapes) {
      if (esh.hasSelectedBorder()) {
        if (esh instanceof EditableRectangle) {
          Point2D p = esh.getNotSelectedBorder();
          convertToSimpleShapes();
          selectBorder(p.getX(), p.getY());
          joinAdjacentsToSelectedPoint();
          break;
        } else {
          count++;
          if (count == 1)
            other = esh;
          else if (other != null) {
            Point2D p1 = esh.getNotSelectedBorder();
            Point2D p2 = other.getNotSelectedBorder();
            vShapes.add(new EditableLine2D(p1, p2));
            vShapes.remove(esh);
            vShapes.remove(other);
          }
        }
      }
    }
    hep.repaint(0);
  }

  private void setEndToVector(double finX, double finY, List<EditableShape> vRedrawingLines) {
    // approach of all the lines of vRedrawingLines to the point finX, finY
    for (EditableShape esh : vRedrawingLines)
      esh.aproximateNearestBorder(finX, finY);
  }

  public boolean hasSelectedPoint() {
    return bSelectedPoint;
  }

  public List<EditableShape> getSelectedShapes() {
    List<EditableShape> v = new ArrayList<EditableShape>();
    for (EditableShape esh : vShapes) {
      if (esh.isSelected())
        v.add(esh);
    }
    return v;
  }

  public int getNumShapes() {
    return vShapes.size();
  }

  public void deleteCurrent() {
    clean();
    current = null;
  }

  public ShapeData getShapeData() {
    ShapeData sd = null;
    AffineTransform aft = AffineTransform.getScaleInstance((1 / hep.previewArea.getWidth()),
        (1 / hep.previewArea.getHeight()));
    aft.concatenate(AffineTransform.getTranslateInstance(-hep.previewArea.x, -hep.previewArea.y));
    if (getNumShapes() == 1) {
      // Is a rectangle or a ellipse
      EditableShape es = (EditableShape) vShapes.get(0).clone();
      es.transform(aft);
      Shape s;
      if (es instanceof EditableEllipse2D)
        s = ((EditableEllipse2D) es).getEllipse();
      else
        s = es;
      sd = ShapeData.getShapeData(s, null, false);
    } else {
      List<GeneralPath> v = getGeneralPath();
      // Get only the first polygon found (should be unique)
      if (v.size() > 0) {
        GeneralPath gp = (v.get(0));
        Shape s = gp.createTransformedShape(aft);
        sd = ShapeData.getShapeData(s, null);
      }
    }
    return sd;
  }

  public void endPolygon() {
    endPolygon(false, true);
  }

  public void endPolygon(boolean changeShape, boolean updateList) {
    endPolygon(changeShape, updateList, -1);
  }

  public void endPolygon(boolean changeShape, boolean updateList, int iNextShape) {
    ShapeData sd = getShapeData();
    addCurrentDrawnBorders(sd);
    endPolygon(sd, changeShape, updateList, iNextShape);
    if (sd != null)
      clean();
    bSelectedPoint = false;
  }

  private void addCurrentDrawnBorders(ShapeData sd) {
    if (sd != null && hep != null) {
      Shape s = sd.getShape(hep.previewArea);
      vDrawnBorders.addAll(getBorders(s));
    }
  }

  private void removeDrawnBorders(ShapeData sd) {
    Shape s = sd.getShape(hep.previewArea);
    vDrawnBorders.removeAll(getBorders(s));
    // removeAll removes all the instances of the elements passed over the
    // shapedata (only one instance is needed)
  }

  public void endPolygon(ShapeData sd, boolean changeShape, boolean updateList, int iNextShape) {
    // Save the created/modified polygon. changeShape indicates if we "come" from a
    // tab key.
    if (sd != null) {

      addCurrentDrawnBorders(sd);

      if (hep.currentShape < hep.getHoles().getNumCells()) {
        // hep.currentShape has been modified
        hep.getHoles().modifyShape(hep.currentShape, sd);
        hep.updateView();
      } else {
        // A comment has been created
        sd.comment = StrUtils.secureString(sd.comment, "" + hep.currentShape);
        hep.getHoles().addShape(sd);
        hep.updateList();
        hep.updateView();
      }
    }
    int iCurrentShape = hep.currentShape + 1;
    if (changeShape) {
      if (iNextShape >= 0)
        iCurrentShape = iNextShape;
      else
        iCurrentShape = iCurrentShape % hep.getHoles().getNumCells();
    } else
      iCurrentShape = hep.getHoles().getNumCells();
    // Next one will be new
    if (hep.currentShape != iCurrentShape)
      hep.setCurrentShape(iCurrentShape);
  }

  private void aplicateTransformation(AffineTransform aTransf, boolean needSelected) {
    for (EditableShape esh : vShapes) {
      if (!needSelected || esh.isSelected()) {
        esh.transform(aTransf);
      }
    }
  }

  public void move(int xInc, int yInc, boolean needSelected, boolean moveAll) {
    // moveAll indicates if we want to move also the inactive objects
    AffineTransform aTransf = AffineTransform.getTranslateInstance(xInc, yInc);
    aplicateTransformation(aTransf, needSelected);
    hep.repaint(0);
  }

  public void scale(double xInc, double yInc, boolean needSelected, boolean scaleAll) {
    Point2D center = getCenter(scaleAll);
    AffineTransform aTransf = AffineTransform.getTranslateInstance(center.getX(), center.getY());
    aTransf.concatenate(AffineTransform.getScaleInstance(xInc, yInc));
    aTransf.concatenate(AffineTransform.getTranslateInstance(-center.getX(), -center.getY()));
    aplicateTransformation(aTransf, needSelected);
    hep.repaint(0);
  }

  public void rotate(double theta, boolean needSelected, boolean rotateAll) {
    convertToSimpleShapes();
    // If it is a triangle, it will be necessary to convert it to lines in
    // order to rotate the shape
    Point2D center = getCenter(rotateAll);
    AffineTransform aTransf = AffineTransform.getRotateInstance(theta, center.getX(), center.getY());
    aplicateTransformation(aTransf, needSelected);
    hep.repaint(0);
  }

  private Point2D getCenter(boolean cellCenter) {
    // Returns the central point of the edited shape when cellCenter==false,
    // otherwise, returns the center of the cell
    if (!cellCenter) {
      GeneralPath gp = new GeneralPath();
      for (EditableShape esh : vShapes)
        gp.append(esh, false);
      Rectangle2D r = gp.getBounds();
      // to calculate the central point
      return new Point2D.Double(r.getCenterX(), r.getCenterY());
    } else
      return new Point2D.Double(hep.getPreviewPanel().getX(), hep.getPreviewPanel().getY());
  }

  private void convertToSimpleShapes() {
    // If the edited shape is a rectangle or a ellipse, transform it to a set of
    // segments or cubic lines
    for (EditableShape esh : vShapes) {
      if (esh instanceof EditableRectangle) {
        // Rectangular shapes must be converted to simple shapes in order to
        // rotate it
        vShapes.remove(esh);
        EditableShape[] lines = ((EditableRectangle) esh).divide(-1, -1, false);
        // Do no add any point
        for (int i = 0; i < lines.length; i++)
          if (lines[i] != null)
            vShapes.add(lines[i]);
      }
    }
  }

  private EditableShape getSelectedShape(boolean hasToBeALine) {
    // Returns the selected line when there is only one
    EditableShape selected = null;
    int i = 0;
    for (EditableShape esh : vShapes) {
      if (esh.isSelected()) {
        if (!hasToBeALine || esh instanceof EditableLine2D) {
          selected = esh;
          i++;
        } else {
          i = 2;
          // Do nothing
        }
      }
      if (i >= 2)
        break;
    }
    if (i == 1)
      return selected;
    else
      return null;
  }

  public void convertToBezier() {
    EditableShape selected = getSelectedShape(false);
    if (selected != null) {
      double x1 = selected.getInitialPoint().getX();
      double y1 = selected.getInitialPoint().getY();
      double x2 = selected.getEndPoint().getX();
      double y2 = selected.getEndPoint().getY();
      double ctrl1x = x1 + ((x2 - x1) / 3);
      double ctrl2x = x1 + (2 * ((x2 - x1) / 3));
      double ctrl1y = y1 + ((y2 - y1) / 3);
      double ctrl2y = y1 + (2 * ((y2 - y1) / 3));
      EditableCubicCurve2D bez = new EditableCubicCurve2D(x1, y1, ctrl1x, ctrl1y, ctrl2x, ctrl2y, x2, y2);
      bez.setSelected(true);
      vShapes.remove(selected);
      vShapes.add(bez);
    }
  }

  public void convertToQuad() {
    EditableShape selected = getSelectedShape(false);
    if (selected != null) {
      double x1 = selected.getInitialPoint().getX();
      double y1 = selected.getInitialPoint().getY();
      double x2 = selected.getEndPoint().getX();
      double y2 = selected.getEndPoint().getY();
      double ctrlx = x1 + ((x2 - x1) / 2);
      double ctrly = y1 + ((y2 - y1) / 2);
      EditableQuadCurve2D quad = new EditableQuadCurve2D(x1, y1, ctrlx, ctrly, x2, y2);
      quad.setSelected(true);
      vShapes.remove(selected);
      vShapes.add(quad);
    }
  }

  public void convertToLine() {
    EditableShape selected = getSelectedShape(false);
    if (selected != null && !(selected instanceof EditableRectangle)) {
      EditableLine2D line = new EditableLine2D(selected.getInitialPoint(), selected.getEndPoint());
      line.setSelected(true);
      vShapes.remove(selected);
      vShapes.add(line);
    } else if (selected != null && selected instanceof EditableRectangle) {
      // Convert a rectangle into four lines
      vShapes.remove(selected);
      EditableShape[] lines = ((EditableRectangle) selected).divide(-1, -1, false);
      // Do not add any point
      for (int i = 0; i < lines.length; i++)
        if (lines[i] != null)
          vShapes.add(lines[i]);
    }
  }

  public void notifyShapeChanged() {
    for (PointListener pl : vPointListeners) {
      pl.shapeChanged();
    }
  }

  public void addPointListener(PointListener listener) {
    vPointListeners.add(listener);
  }

  public void undoLastMove(List<EditableShape> vRedrawingLines, List<EditableShape> vRedrawingLinesBeforeModify) {
    vShapes.removeAll(vRedrawingLines);
    vShapes.addAll(vRedrawingLinesBeforeModify);
    vRedrawingLines.clear();
  }

  private boolean isIntoArea(List<EditableShape> vShapes, boolean move) {
    boolean isInto = true;
    Rectangle2D r = new Rectangle2D.Double(hep.previewArea.getX() - 1, hep.previewArea.getY() - 1,
        hep.previewArea.getWidth() + 2, hep.previewArea.getHeight() + 2);
    Point2D[] borders;
    for (EditableShape esh : vShapes) {
      if (!isInto)
        break;
      EditableShape es;
      if (!move)
        es = esh;
      else {
        es = (EditableShape) esh.clone();
        es.transform(AffineTransform.getTranslateInstance(finX - iniX, finY - iniY));
      }

      borders = es.getBorders();
      if (borders == null)
        continue;
      for (int j = 0; j < borders.length && isInto; j++)
        isInto = r.contains(borders[j]);
    }
    return isInto;
  }

  private void joinPolygon() {
    if (vShapes.size() >= 2) {
      vShapes.add(new EditableLine2D(lastPoint.getX(), lastPoint.getY(), iniPoint.getX(), iniPoint.getY()));
    } else
      vShapes.clear();
    creatingPolygon = false;
    lastPoint = null;
    iniPoint = null;
    if (bSelectedPoint)
      deselectBorder();
    bSelectedPoint = false;
    hep.setDrawingMode(SELECTING);
  }

  public void mouseDragged(java.awt.event.MouseEvent mouseEvent) {
    if ((mouseEvent.getModifiers() & java.awt.event.MouseEvent.BUTTON1_MASK) == 0)
      return;
    // Button 1 was not pressed
    Point2D mousePoint = getTransformedPoint(mouseEvent.getPoint(), true);

    if (bMoving)
      hep.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

    if (mousePoint.getX() < hep.previewArea.x || mousePoint.getY() < hep.previewArea.y
        || mousePoint.getX() > hep.previewArea.x + hep.previewArea.getWidth()
        || mousePoint.getY() > hep.previewArea.y + hep.previewArea.getHeight()) {
      return;
    }

    vShapeBeforeModify = (drawingMode == SELECTING && !bMoving) ? cloneVector(vRedrawingLines) : cloneVector(vCopied);
    EditableShape near = null;

    Point2D nearDrawn = aproximationToDrawnBorder(mouseEvent.getX(), mouseEvent.getY());
    if (nearDrawn != null && EditableShapeConstants.pointsOnGrid) {
      finX = nearDrawn.getX();
      finY = nearDrawn.getY();
    } else {
      finX = mousePoint.getX();
      finY = mousePoint.getY();
    }

    if (creatingRect || creatingEllipse) {
      near = aproximationToLine(finX, finY);
      hep.getPreviewPanel().repaint(0);
    } else if (bRedrawingLines) {
      redrawingLines(finX, finY);
      near = aproximationToLine(finX, finY, vRedrawingLines);
      // we are over a shape corner not selected near (at less)...
      hep.repaint(0);
    } else if (bMoving || esInterior(finX, finY)) {
      near = nearestLine(finX, finY);
      if (near != null) {
        double d = near.distanceTo(finX, finY);
        if (!bMoving && d > (EditableShapeConstants.selectLength / 2))
          cut(finX, finY);
      }
      hep.repaint(0);
    }
    if (creatingRect || creatingEllipse || bRedrawingLines) {
      if ((near != null || nearDrawn != null) && EditableShapeConstants.pointsOnGrid)
        hep.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      else
        hep.setCursor(cursors[PEN_CURSOR]);
    }
    boolean b = isIntoArea((drawingMode == SELECTING && !bMoving) ? vRedrawingLines : vCopied, (bMoving));
    if (!b) {
      if (drawingMode == SELECTING && !bMoving) {
        undoLastMove(vRedrawingLines, vShapeBeforeModify);
        finX = lastFinX;
        finY = lastFinY;
      }
      if (bMoving) {
        finX = lastFinX;
        finY = lastFinY;
      }
    } else {
      lastFinX = finX;
      lastFinY = finY;
    }

    if (bResizing)
      setResizingCursor(resizingDirection);
  }

  protected boolean esCantonada(double x, double y) {
    EditableShape near = aproximationToLine(x, y);
    return (near != null || aproximationToDrawnBorder(x, y) != null);
  }

  protected boolean esSobreFigura(double x, double y) {
    int minimumDistance = Math.max(Math.max(2, EditableShapeConstants.selectLength / 2),
        EditableShapeConstants.gridWidth);
    double dist = distanceToNearest(x, y);
    return (dist >= 0 && dist < minimumDistance);
  }

  protected boolean esInterior(double x, double y) {
    return (current != null && current.contains(x, y));
  }

  public void mouseMoved(java.awt.event.MouseEvent mouseEvent) {

    double x, y;
    Point2D mousePoint = mouseEvent.getPoint();

    boolean esCantonada = esCantonada(mouseEvent.getPoint().getX(), mouseEvent.getPoint().getY());
    if (drawingMode == NEW_POINT || (esCantonada && EditableShapeConstants.pointsOnGrid)) {
      x = mousePoint.getX();
      y = mousePoint.getY();
    } else {
      x = mousePoint.getX();
      y = mousePoint.getY();
    }

    if (x < hep.previewArea.x || y < hep.previewArea.y || x > hep.previewArea.x + hep.previewArea.getWidth()
        || y > hep.previewArea.y + hep.previewArea.getHeight())
      return;
    if (drawingMode != NEW_POINT) {
      if (esCantonada && (!creatingPolygon || EditableShapeConstants.pointsOnGrid))
        hep.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      else if (creatingPolygon)
        hep.setCursor(cursors[PEN_CURSOR]);
      else if (!bMoving && esSobreFigura(x, y))
        hep.setCursor(cursors[CIRCLE_CURSOR]);
      else if (esInterior(x, y))
        hep.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
      else
        hep.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    if (drawingMode == NEW_POINT) {
      if (esSobreFigura(x, y)) {
        bSpecialLine = true;
        // temporally paint it in another color
        specialLine = nearestLine(x, y);
        hep.setCursor(cursors[PEN_CURSOR]);
        hep.repaint(0);
      } else {
        boolean willRepaint = bSpecialLine;
        bSpecialLine = false;
        hep.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        if (willRepaint)
          hep.repaint(0);
      }
    }
    if (bMoving) {
      if (iniX == -1) {
        deleteSelected(true);
        iniX = x;
        iniY = y;
      }
      finX = x;
      finY = y;
      hep.repaint(0);
    }
    if (creatingPolygon) {
      finX = x;
      finY = y;
      hep.repaint(0);
    }
    if (canResize) {
      if (!bResizing) {
        int resizing = getResizing(mousePoint);
        if (resizing != NO_RESIZING)
          setResizingCursor(resizing);
      } else
        setResizingCursor(resizingDirection);
    }
  }

  protected int getResizing(Point2D mousePoint) {
    if (!canResize)
      return NO_RESIZING;
    ShapeData sd = hep.getHoles().getEnclosingShapeData();
    Rectangle r = hep.getPreviewArea();
    double width = r.getWidth();
    double height = r.getHeight();
    AffineTransform aft = AffineTransform.getTranslateInstance(-hep.previewArea.x, -hep.previewArea.y);
    aft.transform(mousePoint, mousePoint);
    if (mousePoint.getX() == (width - 1) && mousePoint.getY() == (height - 1))
      return SOUTH_EAST;
    else if (mousePoint.getX() == (width - 1))
      return EAST;
    else if (mousePoint.getY() == (height - 1))
      return SOUTH;
    else
      return NO_RESIZING;
  }

  protected void setResizingCursor(int resizing) {
    if (resizing == EAST)
      hep.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR), false);
    else if (resizing == SOUTH)
      hep.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR), false);
    else if (resizing == SOUTH_EAST)
      hep.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR), false);
  }

  protected void selectBorder(double x, double y) {
    for (EditableShape s : vShapes)
      s.selectBorder(x, y);
  }

  protected void deselectBorder() {
    for (EditableShape s : vShapes)
      s.deselectBorder();
  }

  protected boolean removeNullLines(List<EditableShape> vRedrawingLines) {
    // Removes from vRedrawingLines all the lines having the two points in
    // (approximately) the same co-ordinates
    boolean canRemove = false;
    for (EditableShape s : vRedrawingLines) {
      if (canRemove)
        break;
      if (s instanceof EditableLine2D) {
        Point2D[] p = s.getBorders();
        if (p.length > 1) {
          Rectangle r = new Rectangle((int) (p[0].getX()) - (EditableShapeConstants.selectLength / 2),
              (int) (p[0].getY()) - (EditableShapeConstants.selectLength / 2), EditableShapeConstants.selectLength,
              EditableShapeConstants.selectLength);
          if (r.contains(p[1].getX(), p[1].getY())) {
            // This line is prescindible
            if (vShapes.size() >= 4) {
              // Do not delete any element when there are only 3 or les (except if it's a cut)
              canRemove = true;
              vShapes.remove(s);
              joinAdjacentsTo(s, vShapes);
            }
          }
        }
      }
    }
    return canRemove;
  }

  public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
    Point2D mousePoint = mouseEvent.getPoint();
    boolean bSobreFigura = esSobreFigura(mousePoint.getX(), mousePoint.getY());
    if (drawingMode != NEW_POINT && drawingMode != DRAWING_POLYGON && !bSobreFigura && selectDrawnShape(mousePoint)
        && !creatingPolygon) {
      notifyShapeChanged();
      hep.repaint(0);
    } else if (drawingMode != NEW_POINT && bSobreFigura && !creatingPolygon) {
      EditableShape line = nearestLine(mousePoint.getX(), mousePoint.getY());
      if (line != null) {
        // The caller wants to select a fragment of the polygon
        if (esCantonada(mousePoint.getX(), mousePoint.getY())) {
          Point2D p = line.getNearestBorder(mousePoint.getX(), mousePoint.getY());
          deSelectAll();
          bSelectedPoint = true;
          selectBorder(p.getX(), p.getY());
          hep.repaint(0);
        } else {
          if (bSelectedPoint)
            deselectBorder();
          bSelectedPoint = false;
          if (line.isSelected())
            line.setSelected(false);
          else {
            if ((mouseEvent.getModifiers() & java.awt.event.MouseEvent.SHIFT_MASK) == 0)
              deSelectAll();
            line.setSelected(true);
          }
          notifyShapeChanged();
          hep.repaint(0);
        }
      }
    }
    if (creatingPolygon) {
      if (mouseEvent.getClickCount() == 2)
        joinPolygon();
      else {
        mousePoint = mouseEvent.getPoint();
        EditableShape near = aproximationToLine(mousePoint.getX(), mousePoint.getY(), vRedrawingLines);
        Point2D nearDrawn = null;
        Point2D nearDrawnOther = aproximationToDrawnBorder(mousePoint.getX(), mousePoint.getY());
        if (near != null) {
          // if the ending point is near the mouse, approximate to it
          nearDrawn = near.getNearestBorder(mousePoint.getX(), mousePoint.getY());
        }

        if (nearDrawnOther != null && EditableShapeConstants.pointsOnGrid) {
          finX = nearDrawnOther.getX();
          finY = nearDrawnOther.getY();
        } else {
          mousePoint = getTransformedPoint(mouseEvent.getPoint(), true);
          finX = mousePoint.getX();
          finY = mousePoint.getY();
        }

        if (lastPoint != null) {
          // isn't the first point
          if (nearDrawn != null && iniPoint.getX() == nearDrawn.getX() && iniPoint.getY() == nearDrawn.getY()) {
            // Has clicked over the starting point of the polygon
            if (vShapes.size() >= 2)
              joinPolygon();
          } else {
            if (nearDrawn == null) {
              // Points cannot be repeated
              vShapes.add(new EditableLine2D(lastPoint.getX(), lastPoint.getY(), finX, finY));
              lastPoint = new Point2D.Double(finX, finY);
            }
          }
        } else {
          // it's the first point
          iniPoint = new Point2D.Double(finX, finY);
          lastPoint = iniPoint;
        }
      }
    }
  }

  public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
  }

  public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
  }

  public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
    Point2D mousePoint = getTransformedPoint(mouseEvent.getPoint(), drawingMode != SELECTING);
    // when selecting, the point doesn't must be in the grid
    int x = (int) mousePoint.getX();
    int y = (int) mousePoint.getY();

    if (canResize) {
      int resizing = getResizing(mousePoint);
      if (resizing != NO_RESIZING) {
        if (drawingMode != SELECTING)
          hep.setDrawingMode(SELECTING);
        bResizing = true;
        resizingDirection = resizing;
      }
    }

    if (x < hep.previewArea.x || y < hep.previewArea.y || x > hep.previewArea.x + hep.previewArea.getWidth()
        || y > hep.previewArea.y + hep.previewArea.getHeight())
      return;
    if (bMoving) {
      // CTRL+X has been pressed while moving a shape
      paste(finX - iniX, finY - iniY);
      bMoving = false;
    }
    iniX = x;
    iniY = y;

    if (drawingMode == SELECTING && !bMoving) {
      clicatISeleccionada(x, y, false);
      // false: drag is possible despite of having the shape selected or unselected
      if (vRedrawingLines.size() > 0)
        bRedrawingLines = true;
      // Redraw lines when clicking on a corner
    } else if ((drawingMode == DRAWING_RECT || drawingMode == DRAWING_ELLIPSE || drawingMode == DRAWING_POLYGON)
        && !hasSelectedDrawnShape(mouseEvent.getPoint())) {
      if (drawingMode == DRAWING_RECT)
        creatingRect = true;
      else if (drawingMode == DRAWING_ELLIPSE)
        creatingEllipse = true;
      else
        creatingPolygon = true;
      EditableShape near = aproximationToLine(x, y);
      Point2D pNear = aproximationToDrawnBorder(x, y);
      if (near != null) {
        pNear = near.getNearestBorder(x, y);
      }
      if (pNear != null) {
        iniX = pNear.getX();
        iniY = pNear.getY();
      } else {
        iniX = x;
        iniY = y;
      }
      finX = iniX;
      finY = iniY;
      hep.repaint(0);
    } else if (drawingMode == NEW_POINT) {
      EditableShape lineToDivide = specialLine;
      EditableShape near = aproximationToLine(x, y, null);
      Point2D nearDrawn = null;
      boolean isSelect = false;
      if (near != null)
        nearDrawn = near.getNearestBorder(x, y);
      if (drawingMode == NEW_POINT
          && (lineToDivide != null && bSpecialLine && (nearDrawn == null || lineToDivide instanceof EditableEllipse2D
              || lineToDivide instanceof EditableCubicCurve2D || lineToDivide instanceof EditableQuadCurve2D))) {
        // A point is added only when the current point is not over another one
        divideShape(lineToDivide, x, y);
      } else {
        isSelect = selectDrawnShape(mouseEvent.getPoint());
      }
      if (!isSelect) {
        // A point has been added, or a click has been done out of any shape
        hep.setDrawingMode(SELECTING);
      }
    }
  }

  public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
    if ((mouseEvent.getModifiers() & java.awt.event.MouseEvent.BUTTON1_MASK) == 0) {
      // The button 1 was not pressed
      return;
    }
    if (bMoving) {
      paste(finX - iniX, finY - iniY);
      bMoving = false;
      deSelectAll();
    }

    Point2D mousePoint = mouseEvent.getPoint();

    EditableShape near = aproximationToLine(mousePoint.getX(), mousePoint.getY(), vRedrawingLines);
    Point2D nearDrawnPropi = null;
    Point2D nearDrawn = aproximationToDrawnBorder(mousePoint.getX(), mousePoint.getY());
    // Corner of a non-active polygon
    if (near != null || nearDrawn != null)
      hep.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    else
      hep.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    if (near != null) {
      // if there is any nearest point, approximate the ending point to it
      nearDrawnPropi = near.getNearestBorder(mousePoint.getX(), mousePoint.getY());
      // Corner of the active polygon
    }

    mousePoint = getTransformedPoint(mouseEvent.getPoint(), true);
    if (!(mousePoint.getX() < hep.previewArea.x || mousePoint.getY() < hep.previewArea.y
        || mousePoint.getX() > hep.previewArea.x + hep.previewArea.getWidth()
        || mousePoint.getY() > hep.previewArea.y + hep.previewArea.getHeight())) {
      if (nearDrawn != null && EditableShapeConstants.pointsOnGrid) {
        // Only when approaching
        finX = nearDrawn.getX();
        finY = nearDrawn.getY();
      } else {
        finX = mousePoint.getX();
        // This point is maintained as long as the pointer remains into the drawing area
        finY = mousePoint.getY();
      }
    }

    if ((drawingMode == SELECTING && !bMoving && nearDrawnPropi != null)
        || (!creatingPolygon && vShapes.size() > 1 && !validateShape())) {
      boolean canRemove = removeNullLines(vRedrawingLines);
      if (!canRemove) {
        undoLastMove(vRedrawingLines, vRedrawingLinesBeforeModify);
        finX = iniX;
        finY = iniY;
      }
    } else if (bRedrawingLines) {
      setEndToVector(finX, finY, vRedrawingLines);
      bRedrawingLines = false;
      vRedrawingLines.clear();
      vRedrawingLinesBeforeModify.clear();
    }
    if (creatingRect) {
      creatingRect = false;
      vShapes.add(new EditableRectangle((int) iniX, (int) iniY, (int) (finX - iniX), (int) (finY - iniY)));
      if (hep.currentShape >= hep.getHoles().getNumCells()) {
        // Reserve space for the new rectangle when confirmed(in order to give it a
        // name)
        ShapeData sd = new ShapeData();
        sd.comment = "" + hep.currentShape;
        hep.getHoles().addShape(sd);
        hep.updateList();
      }
    }
    if (creatingEllipse) {
      creatingEllipse = false;
      vShapes.add(new EditableEllipse2D((int) iniX, (int) iniY, (int) (finX - iniX), (int) (finY - iniY)));
      if (hep.currentShape >= hep.getHoles().getNumCells()) {
        // Reserve space for the new rectangle when confirmed(in order to give it a
        // name)
        ShapeData sd = new ShapeData();
        sd.comment = "" + hep.currentShape;
        hep.getHoles().addShape(sd);
        hep.updateList();
      }
    }

    if (bResizing) {
      if (resizingDirection != NO_RESIZING) {
        double x = mousePoint.getX();
        double y = mousePoint.getY();
        double xInc = x - iniX;
        double yInc = y - iniY;
        if (resizingDirection == EAST)
          yInc = 0;
        else if (resizingDirection == SOUTH)
          xInc = 0;
        hep.incDrawingArea(xInc, yInc);
      }
      bResizing = false;
    }

    ShapeData sd = getShapeData();
    current = (sd != null) ? sd.getShape(hep.previewArea) : null;
    // Update the modifications to the shape
    bSelectingArea = false;
    hep.repaint(0);
    if (!creatingPolygon)
      notifyShapeChanged();
  }

  public class KeyHandler extends KeyAdapter {
    @Override
    public void keyPressed(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_DELETE) {
        deleteSelected(false);
        hep.shapeChanged();
      }
    }
  }

  private double viewIniX = -1;
  private double viewIniY = -1;
}
