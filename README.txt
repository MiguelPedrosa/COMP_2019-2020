**PROJECT TITLE: Compiler of the Java-- language to Java Bytecodes


**GROUP: 1A

    NAME1: André Filipe de Soveral Torres Lopes dos Santos, NR1: 200505634, GRADE1: 10, CONTRIBUTION: 10%

    NAME2: Diogo Luís Cerqueira Carneiro da Silva, NR2: 201405742, GRADE2: 5, CONTRIBUTION: 5%

    NAME3: Luís Miguel Pedrosa de Moura Oliveira Henriques, NR3: 201604343, GRADE3: 20, CONTRIBUTION: 42%

    NAME4: Pedro Hugo Lima Noevo , NR4: 201604725, GRADE4: 20, CONTRIBUTION: 43%

...

GLOBAL Grade of the project: 19

**SUMMARY: 
    This project is a compiler for the Java-- language. 
    The compiler generates valid JVM (Java Virtual Machine) instructions to jasmin, a tool to generate Java bytecodes given assembly programs with JVM instructions.

**EXECUTE: 

    The produced .j file will be stored in the `jFiles`.
    The `jasmin.jar` must be located in folder `jasminTest` to follow these instructions
    Any aditional libs the `.jmm` file requires must be in the folder `./jasminTest/libs`
    To run our tool, there are 2 alternatives:

        1. Run ./jasmin.sh <name of the j file> (example "./jasmin.sh Life)

        2.
            2.1. Compile with `gradle build`

            2.2 `java -jar <compiler .jar name> <filepath to .jmm> [-o] [-r=NUM]`

            2.3. run `java -jar jasminTest/jasmin.jar jFiles/<name of the j file>.j -d ./jasminTest/libs`

            2.4. run `java -cp jasminTest/libs <name of the j file>`


**DEALING WITH SYNTACTIC ERRORS:
    * Our project with exit early if a unmatched token is found.
    * If a while token is found, then the compiler will do some error recovery if need. This is done by ignoring the preceding wrong tokens, until a '{' is found, in which case compilation is resumed. If within those tokens the '{' character isn't found then compilation will terminate with an error.
    * If the compiler identifies more than 10 errors, it will exit and the programmer will be notified that he should take action to fix the program.


**SEMANTIC ANALYSIS:
    This phase was implemented by analysing the generated AST from the previous stage and using it to firstly generate a Symbol Table.
    * The Symbol Table contains information about each variable declared in the program as well as it's type and a set of helper variables that were later used for optimazations.
    * An error does not immediately terminate this stage. The compiler will try to gather as many errors as possible before exiting. Each error is then printed with the corresponding line where the error was found. 
    * The compiler can detect if there is a type mismatch between expressions. For example, assignments to a variable with a value that has an incorrect type.
    * Variable are detected if they were initialized when using in conditions (using `if` or `while`). In cases where initialisation can't be asserted a warning is given, but compilation doesn't terminate.
    * Arguments in function calls are compared against existing functions prototypes and determined if they are correct.
    * Detects if functions are static and takes appropriate behavior.
    * Detects if variables were declared as well as their scope.
    * Stores all information regarding imports.

**INTERMEDIATE REPRESENTATIONS (IRs):
    No Intermediate representation was done for this project.

**CODE GENERATION:
    The project produces a `.j` file which can then be translated using `jasmin.jar` to create a java classfile. This file can be linked and run will other necessary class files.
    All the given goals for code generation were met and the project can generated jasmin compatible code from valid `J--` file.
    The code generation has correct stack and locals calculation.
    If the `-o` flag is passed, the following optimizations are enabled:
    * Integer Division: via right shifts if divisor is a power of 2.
    * Integer Multiplication: via left shifts if it is a power of 2.
    * Constant Folding: the expression `2 + 4` is compiled to `6`
    * Expression simplification: Several mathmatical properties were applied. Multiplying a number by `0`, return a `0`, etc..
    * Branch elimination: if an `if`'s condition can be determined at compile time, the code from the non-execution branch is removed and the `if` code is completly replaced. A `while`'s code is also skiped if condition is evaluated to `false` at compile time.
    * Constant propagation: If it is possible, variables are replaced by constants.
    * Usage of jasmin otimized instructions where possible: `iinc`, `sipush`, `ifgt`, `iflt`, etc..
    * While template: If the first iteration of the loop can be asserted to run, the first `goto` that jumps to the test is ignored and the program immindiately runs the while's body.
    The flag `-r` is also implemented and has the expected behavior. Since dead code elimination isn't fully implemented, the `-r` can generate problematic code.


**OVERVIEW:
    Other than `javacc` and `jasmin.jar`, there were no external tools or libraries used in this project.
    Each main compiler step (semantic analysis, code generation, ..) has a separete file location and tries to be as isolated as possible, while trying to provide a suitable interface. 
    The main logic is based on recursion where a current node does not need to know everything about it's children or parent. It performs the operations it needs to and calls a commum function on it's children where they are decoded into the matching node type and the appropriate function is called. This prevents overcomplicated logic and insures a steadier development and debuging.


**TASK DISTRIBUTION: 

    * André Lopes dos Santos: Contributions to code generation; Compiler complition and testing; Optimizations
    * Diogo Silva: Translation to a JJTree; minor contributions to code generation
    * Luís Miguel Henriques: Parser development; Error treatment and recovery mechanisms; Translation to a JJTree; Symbol Table; Semantic Analysis; Code generation for function invocations, arithmetic expressions, loops and arrays; Compiler complition and testing; Optimizations; Register allocation
    * Pedro Noevo: Parser development; Error treatment and recovery mechanisms; Translation to a JJTree; Symbol Table; Semantic Analysis; Code generation for function invocations, arithmetic expressions, loops and arrays; Compiler complition and testing; Optimizations; Register allocation

**PROS:
    * It generates and runs correctly all the test files provided.
    * The project has support for overloaded function (as long as they have the same return type, but can have different arguments) as well as polymorphism via the use of the special keyword `extends` and by importing the correct functions. This is done by generating an unique signiture for each given method and using it as key to identifie the function's proprties.
    * Array suport is also present and, although we can only create arrays of type `int`, the project can interact with other types of arrays, for example the argument of the main function. As long as functions are imported that can interact with `String`, this argument can be used. This can be tested using the provided test `MainArg.jmm` which simply prints the given command line arguments.
    * It has the necessary algorithms to implement the `-r` flag.

**CONS:
    * We hope all cons are nullified in our project.