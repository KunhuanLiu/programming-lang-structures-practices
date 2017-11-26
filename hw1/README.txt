This assignment is to make a compiler that can translate an input program written in a made-up language called Sumatra into a Java program.

For the detailed context free grammar of Sumatra, please see assignment1.pdf.

The uniqueness of my compiler hereby is that I tried to generate a genric compiler which with slight change, can be used for any other language translation (ideally, haha).

Instead of the common top-down parsing technique (we are required to use top-down for this assignment), I generated a generic parsing function and the grammar rules were written in a format that can be read by the program; therefore, I do not have to write function for each grammar rule, but the generic parsing function will take grammar rule input and recognize the pattern.

The best thing here is that I can easily convert this compiler (I mean, translator more accurately) into one that converts C++ program into Java program. Nice!

The testcase.sum is a Sumatra program provided by our professor Chris Healy and can be used for testing the correctness of the program. Although there were 20+ more cases, he refused to give us all... The program passed all cases, in case you want to know that explicitly ^^