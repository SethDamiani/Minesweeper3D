package Minesweeper3D;
import javafx.beans.property.SimpleStringProperty;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * Created by Seth Damiani on 2017-03-10.
 * Object type definition for high scores in Main.
 */
public class HighScore {
    private SimpleStringProperty ID;
    private SimpleStringProperty difficulty;
    private SimpleStringProperty firstName;
    private SimpleStringProperty lastName;
    private SimpleStringProperty time;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm aa"); // Parse unix times as readable dates

    HighScore(long ID, String difficulty, String firstName, String lastName, int time){
        this.ID = new SimpleStringProperty(Long.toString(ID));
        this.difficulty = new SimpleStringProperty(difficulty);
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.time = new SimpleStringProperty(Integer.toString(time));
    }
    public String getID() { return dateFormat.format(new Date(Long.parseLong(ID.get())*1000)); } // Get readable date
    public String getRawID() { return ID.get(); } // Get raw unix date
    public String getDifficulty() { return difficulty.get(); }
    public String getFirstName() { return firstName.get(); }
    public String getLastName() { return lastName.get(); }
    public int getTime() { return Integer.parseInt(time.get()); }
    public void setDifficulty(String difficulty) { this.difficulty.set(difficulty); }
    public void setFirstName(String firstName) { this.firstName.set(firstName); }
    public void setLastName(String lastName) { this.lastName.set(lastName); }
    @Override
    public String toString() { return (ID.get()+","+difficulty.get()+","+firstName.get()+","+lastName.get()+","+time.get()); }
}
