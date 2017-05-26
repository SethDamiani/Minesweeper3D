package Minesweeper3D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import java.util.HashSet;

/**
 * Created by Seth on 2017-03-18.
 * Object type definition for individual cells in Main.
 */
class Cell {
    private Color unClicked = Color.GRAY;
    HashSet<Cell> surroundingCells;
    HashSet<Cell> surroundingBombs;
    int surroundingBombsCount;
    boolean isClicked;
    boolean isFlagged;
    boolean isBomb;
    /*
     * The variable names for x, y, and z are slightly unintuitive here.
     * The x variable represents the face of the cube that it belongs to;
     * The y variable represents the x co-ordinate of the cell;
     * And the z variable represents the y co-ordinate of the cell.
     */
    double x;
    private double y;
    private double z;
    boolean isEdge;
    int edge;
    Rectangle cell;
    Paint clickedFill;
    final private Image flag = new Image("/images/minesweeper-flag.png");

    Cell(Group cube, double cellSize, double x, double y, double side){
        this.surroundingCells = new HashSet<>();
        this.surroundingBombs = new HashSet<>();
        this.isFlagged = false;
        cell = new Rectangle(cellSize, cellSize, unClicked);
        cell.setX(x + 40);
        cell.setY(y + 35);
        this.z = y / 40;
        this.x = side;
        this.y = x / 40;
        this.isClicked = false;
        this.isBomb = false;
        cell.setStroke(Color.BLACK);
        cell.setStrokeWidth(3);
        if (side == 1 || side == 3 || side == 5){ // Make un-mirror these faces
            cell.setRotationAxis(new Point3D(0,1,0));
            cell.setRotate(180);
        }
        cell.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (isBomb && !isFlagged) Main.gameLost();
                else if (Main.getFlaggedBombs()== Main.getTotalBombs()) Main.gameWon();
                else click();
            }
            else if (event.getButton() == MouseButton.SECONDARY) flag();
            if (Main.getFlaggedBombs()== Main.getTotalBombs()) Main.gameWon();
        });
        cell.setOnMouseEntered(event -> {
            if (!this.isClicked && !this.isFlagged) cell.setFill(Color.GRAY.brighter());
        });
        cell.setOnMouseExited(event -> {
            if (!this.isClicked && !this.isFlagged) cell.setFill(Color.GRAY);
        });
        cube.getChildren().add(cell);
        if (this.y == 0 || this.y == 9 || this.z == 0 || this.z == 9) this.isEdge = true;

        if      (this.y == 0 && this.z == 0) this.edge = 14;
        else if (this.y == 9 && this.z == 9) this.edge = 23;
        else if (this.y == 9 && this.z == 0) this.edge = 12;
        else if (this.y == 0 && this.z == 9) this.edge = 34;
        else if (this.y == 0               ) this.edge = 4;
        else if (this.y == 9               ) this.edge = 2;
        else if (               this.z == 0) this.edge = 1;
        else if (               this.z == 9) this.edge = 3;
    }
    private void click(){
        if (isClicked || isFlagged){ return; }
        if (isBomb){ Main.gameLost(); }
        cell.setFill(clickedFill);
        isClicked = true;

        if (surroundingBombsCount == 0 && !isBomb){
            for (Cell c:surroundingCells){
                c.click();
            }
        }
    }
    private void flag(){
        if (isClicked) { return; }
        if (isFlagged) {
            int temp = Main.getFlaggedCells();
            int tempBombs = Main.getFlaggedBombs();
            temp--;
            Main.setFlaggedCells(temp);
            if (isBomb){
                tempBombs--;
                Main.setFlaggedBombs(tempBombs);
            }
            isFlagged = false;
            cell.setFill(unClicked);
        }
        else {
            int temp = Main.getFlaggedCells();
            temp++;
            Main.setFlaggedCells(temp);
            int tempBombs = Main.getFlaggedBombs();
            if (isBomb){
                tempBombs++;
                Main.setFlaggedBombs(tempBombs);
            }
            isFlagged = true;
            cell.setFill(new ImagePattern(flag));
        }
    }
}
