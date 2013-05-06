humanTrafficking
================

A simulation of global human trafficking phenomenon, in particular forced
prostitution.


To compile, javac all source files. Make sure your CLASSPATH includes the
src directory (in which the edu/umw/cpsc/humantrafficking directory 
hierarchy resides).

You will also need to include the following in your CLASSPATH:
  - mason.16.jar, available inside the .zip file here:
   http://cs.gmu.edu/~eclab/projects/mason/mason16.zip
  - geomason1.5.jar, available here:
   http://cs.gmu.edu/~eclab/projects/mason/extensions/geomason/geomason.1.5.jar
  - jts-1.13.jar, available inside the .zip file here:
     http://sourceforge.net/projects/jts-topo-suite/files/latest/download

To run, cd to the src/edu/umw/cpsc/humantrafficking directory. Then either:
  java edu.umw.cpsc.humantrafficking.HumanTrafficking       (for text output)
or
  java edu.umw.cpsc.humantrafficking.HumanTraffickingWithUI (for GUI)
