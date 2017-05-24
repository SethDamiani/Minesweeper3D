# Minesweeper3D
#### By Seth Damiani
This is my final project for ICS3U (Grade 11 Computer Science). 

A 3D version of Minesweeper, implemented in JavaFX. Each face of a cube is a 2D minesweeper board. Each face interacts at it's edges.

Controls:
- Clear a cell: left click
- Flag a cell: right click
- Rotate the cube: WASD or arrow keys
- Pause: space
- Exit game without confirmation: Escape (this wil be fixed in a later version)

It maintains a list of all scores in a .csv file. The score is the time in seconds it took to flag all bombs on the cube. The high scores table supports editing to fulfill a requirement of the assignment.

Three difficulty modes:
- Easy: 50 bombs
- Medium: 100 bombs
- Hard: 150 bombs

TODOs:

- [x] Add high scores table
- [ ] Improve the ridiculously inefficient method for determining adjacancy between faces of the cube
- [ ] Add confirmation to exit
- [ ] Add quit button within game
- [ ] Enable first-click bomb protection
- [ ] Add advanced mode where the cube rotates randomly
- [ ] Add instructions screen
- [ ] Add more intiutive controls to rotate cube
  - [ ] Mouse dragging
  - [ ] Touch dragging
- [ ] Add more shapes (this will take a while)
