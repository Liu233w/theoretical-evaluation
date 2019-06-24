# Average Performance Based Fault Localization and Test Suit Optimization System

This repo contains the project of my deploma paper, which based on the thesis of 
`A theoretical evaluation framework for test suite quality assessment to improve spectral fault localization techniques`
by Chunyan Ma (School of Software and Microelectronics, Northwestern Polytechnical University).

[My Paper (in Chinese)](./paper.pdf)

## Package Structure
- runner: to run the program in database ([Software-artifact Infrastructure Repository](http://sir.csc.ncsu.edu/portal/index.php)
and [Defects4j](https://github.com/rjust/defects4j)) and generate coverage information.
- analyze: test suit quality assessment and optimization
- chart: generate chart
- utils: utility functions
- application: a bunch of class that contains main method, to run the project.

SIR database is located in [resources](./src/main/resources), while Defects4j has to be executed by docker.

## Usage
1. run the main class directly in IDE
2. or use `mvn exec:java -DmainClass="...."`
3. or install docker and use `build-and-run-image.sh`

Output files are located in `target/outputs/`, cache file in `target/outputs/.cache`.
