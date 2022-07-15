# MesoGen-1.04

Mesoscopic model generation application - code
MesoGen is an android application to generate mesoscopic model of aggregate with circular particles in concrete using Monte Carlo's method and grading curve . 
It After launching it, you will see the main window and you can select to use 50 mm  or 150 mm sized concrete cube. If you want to use some other dimension, 
use the "Other" button. Then press the "RUN" button at the centre of the screen and the app will ask for sieve data details and mix ratio. Enter the data and 
save and come back to main window. Now you can press the "RUN" button again to start the generation process. The first step will be to create the necessary 
number of particles in various segments, and it will be over in a few seconds and the number of particles generated will be shown at the bottom of the wilndow. 
Then the app will automatically start the placing of the generated particles with number of iteration of each particle  and after finishing it will show the 
number of placed particle. Then you can email the data of the model for Ansys Mechanical APDL as a command file or as AutoCAD  command file or as raw data for 
further manipulation to suit to any other software. The data contains, the radius and coordinates of each particle  inside the given concrete cube. To use the data, 
in Ansys APDL, just use the option " Import data using" from file menu and select the shared apdl file.

To use it in AutoCAD, just open the text file in google documents directly from mail, copy the whole content and directly paste it at the command prompt.
The aggregates will be subtracted from the cube and a copy of aggregates will be placed exactly at the required positions. Then this model can be exported to Ansys 
Workbench
![image](https://user-images.githubusercontent.com/108704507/179165649-d4b0fae2-29b2-4edc-a246-0ab471ea072a.png)
